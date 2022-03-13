package hybridDomainParsing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.meta.simplePlanner.SimpleDomain.markings;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangleConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangularRegion;
import org.metacsp.multi.spatial.rectangleAlgebra.UnaryRectangleConstraint;
import org.metacsp.multi.spatial.rectangleAlgebraNew.toRemove.OntologicalSpatialProperty;
import org.metacsp.spatial.utility.SpatialAssertionalRelation;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import fluentSolver.TestFluentNetworkSolver;
import htn.HTNMetaConstraint;
import planner.CHIMPProblem;
import resourceFluent.ResourceUsageTemplate;
import unify.CompoundSymbolicVariable;

public class ProblemParser implements CHIMPProblem {

	public static final String PROBLEM_KEYWORD = "Problem";
	public static final String FLUENT_KEYWORD = "Fluent";
	public static final String TASK_KEYWORD = "Task";
	private static final String ARGUMENT_SYMBOLS_KEYWORD = "ArgumentSymbols";
	private static final String INSTANCES_KEYWORD = "Instances";

	private static final String N = "n";

	private final String fileName;
	private final Map<String, String> fluentElementsMap = new HashMap<String, String>();
	private final Map<String, String> taskElementsMap = new HashMap<String, String>();
	private final Map<String, String> fluentSpacialElementsMap = new HashMap<String, String>();

	private String[] problemStrs;
	private Set<String> argumentSymbols = new HashSet<>();
	private Map<String, String[]> typesInstancesMap;
	Logger logger = MetaCSPLogging.getLogger(ProblemParser.class);

	public ProblemParser(String fileName) {
		this.fileName = fileName;
		parseProblem();
	}

	@Override
	public Boolean createState(FluentNetworkSolver fluentSolver, ClassicHybridDomain domain) {
		Map<String, Variable> varsMap = new HashMap<String, Variable>(
				fluentElementsMap.size() + taskElementsMap.size());
		Map<String, Variable> varSpacialMap = new HashMap<String, Variable>(fluentSpacialElementsMap.size());
		
		Boolean verif=true;
		
		// create spatial variable

		for (Entry<String, String> e : fluentSpacialElementsMap.entrySet()) {
			String inPart = e.getValue();
			int space = e.getValue().indexOf(' ');
			String argName = null;
			if (space < 0) {
				argName = inPart.substring(inPart.indexOf("(") + 1, inPart.indexOf(")")).trim();
			} else {
				String firstArg = inPart.split(" ")[0];
				argName = firstArg.substring(firstArg.indexOf("(") + 1).trim();
				if (varSpacialMap.get(argName) != null) {
					String secondArg = inPart.split(" ")[1];
					argName = secondArg.substring(0, secondArg.indexOf(")")).trim();
				}
			}
			if (varSpacialMap.get(argName) == null) {
				RectangularRegion var = (RectangularRegion) fluentSolver.getConstraintSolvers()[2].createVariable(argName);
				var.setName(argName);		
				var.setMarking(HTNMetaConstraint.markings.JUSTIFIED);

				// setFluentintoNetwork: attach every spatial fluent with duration and release
				
				AllenIntervalConstraint onDuration = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(1000,APSPSolver.INF));
				onDuration.setFrom(var);
				onDuration.setTo(var);				
				AllenIntervalConstraint releaseOn = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(0,0));
				releaseOn.setFrom(var);
				releaseOn.setTo(var);
				fluentSolver.getConstraintSolvers()[1].addConstraints(releaseOn,onDuration);

				varSpacialMap.put(argName, var);
//				logger.info(var.toString());

			}
		}
		
		// create UnaryRectangularConstraint
		for (String stateStr : problemStrs) {
			verif &= fluentSolver.getConstraintSolvers()[2]
					.addConstraints(createUnaryRectangularConstraints(stateStr, varSpacialMap, fluentSolver));
			System.out
					.println("createUnaryRectangularConstraints propagation: " + verif);
		}
		
		// get enoughDistance object name
		String objName=null;
		for (Entry<String, String> e : fluentElementsMap.entrySet()) {
			if(e.getValue().startsWith("enoughDistance"))
			{
				  objName = e.getValue().split(" ")[0];
				  objName= objName.substring(objName.indexOf("(")+1);
			}
		}
		Variable objToPlace = varSpacialMap.get(objName);
		//varSpacialMap.remove(objName);
		
		// create RectangularConstraint
		for (String stateStr : problemStrs) {
			verif &=fluentSolver.getConstraintSolvers()[2]
					.addConstraints(createRectangularConstraints(stateStr, varSpacialMap));
//			verif &=fluentSolver.getConstraintSolvers()[2]
//					.addConstraints(createRectangularConstraintsTest(stateStr, varSpacialMap));
			System.out.println("createRectangularConstraints propagation: " + verif);
		}



		

		
		// create fluents
		for (Entry<String, String> e : fluentElementsMap.entrySet()) {
			Fluent var = (Fluent) fluentSolver.createVariable();
			int firstClosing = e.getValue().indexOf(')') + 1;
			String predicate = e.getValue();
			String symbolicPart = predicate;
			if (firstClosing != e.getValue().length()) {
				String intPart = predicate.substring(predicate.lastIndexOf('(') + 1, predicate.length() - 1);
				String[] intElements = intPart.split(" ");
				for (int i = 0; i < intElements.length; i++) {
					var.getIntegerVariables()[i].setConstantValue(Integer.valueOf(intElements[i]));
				}
				symbolicPart = symbolicPart.substring(0, firstClosing);

			}

			// test enough distance === conflict
			if (symbolicPart.startsWith("enoughDistance")) {
				symbolicPart = symbolicPart.replace("undef",
						testEnoughDistance(objToPlace.getComponent(), fluentSolver, varSpacialMap));
			} else // test collision === duringx2 or Equalsx2
			if (symbolicPart.startsWith("isCollision")) {

				String from = symbolicPart.substring(symbolicPart.indexOf("(") + 1, symbolicPart.indexOf(" ")).trim();
				String to = symbolicPart.split(" ")[1].trim();
				
				Variable obj1 = varSpacialMap.get(from.substring(0, from.length() - 1));
				Variable obj2 = varSpacialMap.get(to.substring(0, to.length() - 1));


				boolean verifColli = false;



				Constraint[] cs = fluentSolver.getConstraintSolvers()[2].getConstraints(obj1, obj2);
				if (cs.length > 0) {
					String a = cs[0].getEdgeLabel();
					if (cs[0].getEdgeLabel().equals("([During], [During])"))
						verifColli = true;
					else if (cs[0].getEdgeLabel().equals("([Equals], [Equals])"))
						verifColli = true;
					symbolicPart = symbolicPart.replace("undef", Boolean.toString(verifColli));
				}
//				else {
//					symbolicPart = symbolicPart.replace("undef", "false");
//
//				}

			}

			var.setName(symbolicPart);
			var.setMarking(HTNMetaConstraint.markings.OPEN);
			varsMap.put(e.getKey(), var);
		}


		Set<String> operatorNames = domain.getOperatorNames();
		// create tasks
		for (Entry<String, String> e : taskElementsMap.entrySet()) {
			String fullName = e.getValue();
			String predicateName = fullName.split("\\(")[0];
			String component;
			if (operatorNames.contains(predicateName)) {
				component = Fluent.ACTIVITY_TYPE_STR;
			} else {
				component = Fluent.TASK_TYPE_STR;
			}
			Fluent var = (Fluent) fluentSolver.createVariable(component);
			var.setName(fullName);
			var.setMarking(HTNMetaConstraint.markings.UNPLANNED);
			varsMap.put(e.getKey(), var);
		}


		// create AllenIntervals
		for (String stateStr : problemStrs) {
			fluentSolver.addConstraints(createAllenIntervalConstraints(stateStr, varsMap));
		}

		// create Ordering Constraints
		for (String stateStr : problemStrs) {
			fluentSolver.addConstraints(createOrderingConstraints(stateStr, varsMap));
		}

		// create FluentResourceUsage constraints
		fluentSolver.addConstraints(createResourceUsageConstraints(domain.getFluentResourceUsages(), varsMap.values()));

		return true;
	}

	private String testEnoughDistance(String objectName, FluentNetworkSolver fluentSolver, Map<String, Variable> varSpacialMap) {
		
		Boolean verifDistance = true;
		RectangleConstraint con = null;
		AllenIntervalConstraint allen1 = null, allen2 = null;

		allen2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.valueOf("Equals"));
		allen1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.valueOf("Equals"));
		con = new RectangleConstraint(allen1, allen2);
		//Variable fluentFrom = varSpacialMap.get(objectName);
		//for DISH serve an empty dish
		Variable fluentFrom = varSpacialMap.get("Dish");
		
		Variable fluentTo = varSpacialMap.get(objectName);
		
		//create temp var
		Constraint cs = fluentSolver.getConstraintSolvers()[2].getConstraints(fluentTo, fluentTo)[0];
		Variable a=fluentSolver.getConstraintSolvers()[2].createVariable();
		UnaryRectangleConstraint uc =(UnaryRectangleConstraint) cs.clone();
		uc.setFrom(a);
		uc.setTo(a);
		 fluentSolver.getConstraintSolvers()[2].addConstraints(uc);
		con.setFrom(fluentFrom);
		con.setTo(a);
		verifDistance &= fluentSolver.getConstraintSolvers()[2].addConstraints(con);
		// if constraints is added , we need to delete: just to check if it possibe to
		// add the constraint and to check for propagation problem
		if (fluentSolver.getConstraintSolvers()[2].getConstraints(fluentFrom, a).length > 0)
			{
			fluentSolver.getConstraintSolvers()[2].removeConstraint(con);
			fluentSolver.getConstraintSolvers()[2].removeConstraint(uc);
			fluentSolver.getConstraintSolvers()[2].removeVariable(a);
			}

		return verifDistance.toString();

	}
	private Constraint[] createResourceUsageConstraints(List<ResourceUsageTemplate> fluentResourceUsages,
			Collection<Variable> vars) {
		List<Constraint> ret = new ArrayList<Constraint>();
		Map<String, List<ResourceUsageTemplate>> usageTemplatesMap = HTNMetaConstraint
				.createResourceUsagesMap(fluentResourceUsages);

		for (Variable var : vars) {
			CompoundSymbolicVariable csv = ((Fluent) var).getCompoundSymbolicVariable();
			String symbol = csv.getPredicateName();

			List<ResourceUsageTemplate> rtList = usageTemplatesMap.get(symbol);
			if (rtList != null) {
				for (ResourceUsageTemplate rt : rtList) {
					FluentConstraint resourceCon = new FluentConstraint(FluentConstraint.Type.RESOURCEUSAGE, rt);
					resourceCon.setFrom(var);
					resourceCon.setTo(var);
					ret.add(resourceCon);
				}
			}
		}

		return ret.toArray(new Constraint[ret.size()]);
	}

	private void reset() {
		fluentElementsMap.clear();
		taskElementsMap.clear();
	}

	private void parseProblem() {
		reset();

		String everything = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			try {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();
				while (line != null) {
					if (!line.trim().startsWith("#")) {
						sb.append(line);
						sb.append('\n');
					}
					line = br.readLine();
				}
				everything = sb.toString();
				// name = parseKeyword(DOMAIN_KEYWORD, everything)[0];

				problemStrs = HybridDomain.parseKeyword(PROBLEM_KEYWORD, everything);
				for (String problemStr : problemStrs) {
					parseProblem(problemStr);
				}

			} finally {
				br.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void parseProblem(String problem) {
		argumentSymbols.addAll(parseArgumentSymbols(problem));
		typesInstancesMap = parseTypesInstancesMap(problem);

		String[] fluentStrs = HybridDomain.parseKeyword(FLUENT_KEYWORD, problem);
		for (String flStr : fluentStrs) {
			String key = flStr.substring(0, flStr.indexOf(" ")).trim();
			String fluent = flStr.substring(flStr.indexOf(" ")).trim();
			fluentElementsMap.put(key, fluent);
		}

		String[] fluentUnarySpatialStrs = HybridDomain.parseKeyword(HybridDomain.UNARY_SPATIAL_KEYWORD, problem);
		for (String flStr : fluentUnarySpatialStrs) {
			String key = flStr.substring(0, flStr.indexOf(" ")).trim();
			String fluent = flStr.split(" ")[1].trim() + ")";
			fluentSpacialElementsMap.put(key, fluent);
		}

		String[] fluentRecSpatialStrs = HybridDomain.parseKeyword(HybridDomain.RECTANGULAR_SPATIAL_KEYWORD, problem);
		for (String flStr : fluentRecSpatialStrs) {
			String key = flStr.substring(0, flStr.indexOf(" ")).trim();
			String fluent = flStr.substring(flStr.indexOf(" ")).trim();
			fluentSpacialElementsMap.put(key, fluent);
		}

		String[] taskStrs = HybridDomain.parseKeyword(TASK_KEYWORD, problem);
		for (String taskStr : taskStrs) {
			String key = taskStr.substring(0, taskStr.indexOf(" ")).trim();
			String fluent = taskStr.substring(taskStr.indexOf(" ")).trim();
			taskElementsMap.put(key, fluent);
		}
	}

	private AllenIntervalConstraint[] createAllenIntervalConstraints(String stateStr,
			Map<String, Variable> fluentsVarsMap) {
		// Parse AllenIntervalConstraints:
		String[] constraintElements = HybridDomain.parseKeyword(HybridDomain.CONSTRAINT_KEYWORD, stateStr);
		AllenIntervalConstraint[] ret = new AllenIntervalConstraint[constraintElements.length];
		for (int i = 0; i < constraintElements.length; i++) {
			String conElement = constraintElements[i];
			String constraintName = null;
			Vector<Bounds> bounds = null;
			if (conElement.contains("[")) {
				constraintName = conElement.substring(0, conElement.indexOf("[")).trim();
				String boundsString = conElement.substring(conElement.indexOf("["), conElement.lastIndexOf("]") + 1);
				String[] splitBounds = boundsString.split("\\[");
				bounds = new Vector<Bounds>();
				for (String oneBound : splitBounds) {
					if (!oneBound.trim().equals("")) {
						String lbString = oneBound.substring(oneBound.indexOf("[") + 1, oneBound.indexOf(",")).trim();
						String ubString = oneBound.substring(oneBound.indexOf(",") + 1, oneBound.indexOf("]")).trim();
						long lb, ub;
						if (lbString.equals("INF"))
							lb = org.metacsp.time.APSPSolver.INF;
						else
							lb = Long.parseLong(lbString);
						if (ubString.equals("INF"))
							ub = org.metacsp.time.APSPSolver.INF;
						else
							ub = Long.parseLong(ubString);
						bounds.add(new Bounds(lb, ub));
					}
				}
			} else {
				constraintName = conElement.substring(0, conElement.indexOf("(")).trim();
			}
			String from = null;
			String to = null;
			String fromSeg = null;
			if (constraintName.equals("Duration") || constraintName.equals("Release")
					|| constraintName.equals("Deadline")) {
				from = conElement.substring(conElement.indexOf("(") + 1, conElement.indexOf(")")).trim();
				to = from;
			} else {
				fromSeg = conElement.substring(conElement.indexOf("("));
				from = fromSeg.substring(fromSeg.indexOf("(") + 1, fromSeg.indexOf(",")).trim();
				to = fromSeg.substring(fromSeg.indexOf(",") + 1, fromSeg.indexOf(")")).trim();
			}

			AllenIntervalConstraint con = null;
			if (bounds != null) {
				con = new AllenIntervalConstraint(AllenIntervalConstraint.Type.valueOf(constraintName),
						bounds.toArray(new Bounds[bounds.size()]));
			} else
				con = new AllenIntervalConstraint(AllenIntervalConstraint.Type.valueOf(constraintName));

			con.setFrom(fluentsVarsMap.get(from));
			con.setTo(fluentsVarsMap.get(to));
			ret[i] = con;
		}
		return ret;
	}

	private Constraint[] createOrderingConstraints(String stateStr, Map<String, Variable> fluentsVarsMap) {
		// Parse AllenIntervalConstraints:
		String[] constraintElements = HybridDomain.parseKeyword(HybridDomain.ORDERING_CONSTRAINT_KEYWORD, stateStr);
		Constraint[] ret = new Constraint[constraintElements.length];
		for (int i = 0; i < constraintElements.length; i++) {
			String orderingElement = constraintElements[i];
			String fromKey = orderingElement.substring(0, orderingElement.indexOf(" ")).trim();
			String toKey = orderingElement.substring(orderingElement.indexOf(" ")).trim();
			FluentConstraint con = new FluentConstraint(FluentConstraint.Type.BEFORE);

			con.setFrom(fluentsVarsMap.get(fromKey));
			con.setTo(fluentsVarsMap.get(toKey));
			ret[i] = con;
		}
		return ret;
	}

	private RectangleConstraint[] createRectangularConstraints(String stateStr, Map<String, Variable> fluentsVarsMap) {
		// Parse RectangleConstraint:
		String[] constraintElements = HybridDomain.parseKeyword(HybridDomain.RECTANGULAR_SPATIAL_KEYWORD, stateStr);
		RectangleConstraint[] ret = new RectangleConstraint[constraintElements.length];
		for (int i = 0; i < constraintElements.length - 1; i++) {

			String YElem = constraintElements[i];
			String XElem = constraintElements[i + 1];
			String constraintName1 = null, constraintName2 = null;
			String prefixX, prefixY = null;
			prefixX = XElem.split(" ")[0];
			prefixY = YElem.split(" ")[0];
			if (!prefixX.substring(0, prefixX.indexOf("x")).trim()
					.equalsIgnoreCase(prefixY.substring(0, prefixY.indexOf("y"))))
				continue;
			else
				i += 1;
			String sdSp = null;
			Vector<Bounds> bounds1 = null, bounds2 = null;
			if (XElem.contains("[")) {
				sdSp = XElem.split(" ")[1];
				constraintName1 = sdSp.substring(0, sdSp.indexOf("(")).trim();
				String boundsString = XElem.substring(XElem.indexOf("["), XElem.lastIndexOf("]") + 1);
				String[] splitBounds = boundsString.split("\\[");
				bounds1 = new Vector<Bounds>();
				for (String oneBound : splitBounds) {
					if (!oneBound.trim().equals("")) {
						String lbString = oneBound.substring(oneBound.indexOf("[") + 1, oneBound.indexOf(",")).trim();
						String ubString = oneBound.substring(oneBound.indexOf(",") + 1, oneBound.indexOf("]")).trim();
						long lb, ub;
						if (lbString.equals("INF"))
							lb = org.metacsp.time.APSPSolver.INF;
						else
							lb = Long.parseLong(lbString);
						if (ubString.equals("INF"))
							ub = org.metacsp.time.APSPSolver.INF;
						else
							ub = Long.parseLong(ubString);
						bounds1.add(new Bounds(lb, ub));
					}
				}
			} else {
				sdSp = XElem.split(" ")[1];
				constraintName1 = sdSp.substring(0, sdSp.indexOf("(")).trim();
			}
			if (YElem.contains("[")) {
				sdSp = YElem.split(" ")[1];
				constraintName2 = sdSp.substring(0, sdSp.indexOf("(")).trim();
				String boundsString = YElem.substring(YElem.indexOf("["), YElem.lastIndexOf("]") + 1);
				String[] splitBounds = boundsString.split("\\[");
				bounds2 = new Vector<Bounds>();
				for (String oneBound : splitBounds) {
					if (!oneBound.trim().equals("")) {
						String lbString = oneBound.substring(oneBound.indexOf("[") + 1, oneBound.indexOf(",")).trim();
						String ubString = oneBound.substring(oneBound.indexOf(",") + 1, oneBound.indexOf("]")).trim();
						long lb, ub;
						if (lbString.equals("INF"))
							lb = org.metacsp.time.APSPSolver.INF;
						else
							lb = Long.parseLong(lbString);
						if (ubString.equals("INF"))
							ub = org.metacsp.time.APSPSolver.INF;
						else
							ub = Long.parseLong(ubString);
						bounds2.add(new Bounds(lb, ub));
					}
				}
			} else {
				sdSp = YElem.split(" ")[1];
				constraintName2 = sdSp.substring(0, sdSp.indexOf("(")).trim();
			}

			String from = null;
			String to = null;
			from = sdSp.substring(sdSp.indexOf("(") + 1).trim();
			String toSeg = YElem.split(" ")[2];

			// from = fromSeg.substring(fromSeg.indexOf("(") + 1).trim();
			to = toSeg.substring(0, toSeg.indexOf(")")).trim();
			RectangleConstraint con = null;

			AllenIntervalConstraint allen1 = null, allen2 = null;
			if (bounds1 != null)
				allen1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.valueOf(constraintName1),
						bounds1.toArray(new Bounds[bounds1.size()]));
			else
				allen1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.valueOf(constraintName1));
			if (bounds2 != null)
				allen2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.valueOf(constraintName2),
						bounds2.toArray(new Bounds[bounds2.size()]));
			else
				allen2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.valueOf(constraintName2));

			con = new RectangleConstraint(allen1, allen2);
			Variable fluentFrom = fluentsVarsMap.get(from);
			Variable fluentTo = fluentsVarsMap.get(to);
			con.setFrom(fluentFrom);
			con.setTo(fluentTo);
			ret[i - 1] = con;
		}
		return Arrays.stream(ret).filter(Objects::nonNull).toArray(RectangleConstraint[]::new);
	}
	
	private RectangleConstraint[] createRectangularConstraintsTest(Map<String, Variable> fluentsVarsMap) {
		// Parse RectangleConstraint:
		RectangleConstraint[] ret = new RectangleConstraint[1];
		

			RectangleConstraint con = null;

			AllenIntervalConstraint allen1 = null, allen2 = null;


			allen2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.valueOf("Equals"));
			allen1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.valueOf("Equals"));
			con = new RectangleConstraint(allen1, allen2);
			Variable fluentFrom = fluentsVarsMap.get("dish");
			Variable fluentTo = fluentsVarsMap.get("dish1");
			con.setFrom(fluentFrom);
			con.setTo(fluentTo);
			ret[0] = con;
		
		return Arrays.stream(ret).filter(Objects::nonNull).toArray(RectangleConstraint[]::new);
	}

	private Constraint[] createUnaryRectangularConstraints(String stateStr, Map<String, Variable> fluentsVarsMap, FluentNetworkSolver fluentSolver) {
		// Parse UnaryRectangleConstraint:
		String[] constraintElements = HybridDomain.parseKeyword(HybridDomain.UNARY_SPATIAL_KEYWORD, stateStr);
		UnaryRectangleConstraint[] ret = new UnaryRectangleConstraint[constraintElements.length];
		for (int i = 0; i < constraintElements.length; i++) {
			String conElement = constraintElements[i];
			String constraintName = null, argName = null;
			String sdSp = null;
			Vector<Bounds> bounds = null;
			if (conElement.contains("[")) {
				sdSp = conElement.split(" ")[1];
				constraintName = sdSp.substring(0, sdSp.indexOf("(")).trim();
				argName = sdSp.substring(sdSp.indexOf("(") + 1).trim();
				String boundsString = conElement.substring(conElement.indexOf("["), conElement.lastIndexOf("]") + 1);
				String[] splitBounds = boundsString.split("\\[");
				bounds = new Vector<Bounds>();
				for (String oneBound : splitBounds) {
					if (!oneBound.trim().equals("")) {
						String lbString = oneBound.substring(oneBound.indexOf("[") + 1, oneBound.indexOf(",")).trim();
						String ubString = oneBound.substring(oneBound.indexOf(",") + 1, oneBound.indexOf("]")).trim();
						long lb, ub;
						if (lbString.equals("INF"))
							lb = org.metacsp.time.APSPSolver.INF;
						else
							lb = Long.parseLong(lbString);
						if (ubString.equals("INF"))
							ub = org.metacsp.time.APSPSolver.INF;
						else
							ub = Long.parseLong(ubString);
						bounds.add(new Bounds(lb, ub));
					}
				}
			} else {
				constraintName = conElement.substring(0, conElement.indexOf("(")).trim();
			}
			String PrefixFrom = null;
			String PrefixTo = null;
			String fromSeg = null;
			if (constraintName.equals("Size") || constraintName.equals("At")) {
				PrefixFrom = PrefixTo = conElement.split(" ")[0].trim();
			} else
				continue;

			UnaryRectangleConstraint con;
			if (bounds.size() < 3) {
				con = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.valueOf(constraintName), bounds.get(0),
						bounds.get(1));
			} else
				con = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.valueOf(constraintName), bounds.get(0),
						bounds.get(1), bounds.get(2), bounds.get(3));

			if(fluentsVarsMap.get(argName)!=null) {
			Variable fluentFrom = fluentsVarsMap.get(argName);
			Variable fluentTo = fluentsVarsMap.get(argName);
			con.setFrom(fluentFrom);
			con.setTo(fluentTo);
			} else continue;
			
			//

//			SpatialAssertionalRelation table_assertion = new SpatialAssertionalRelation(argName+"1", argName);
//			table_assertion.setUnaryAtRectangleConstraint(con);
//			OntologicalSpatialProperty tableOnto = new OntologicalSpatialProperty();
//			table_assertion.setOntologicalProp(tableOnto);
//			tableOnto.setMovable(true);

			ret[i] = con;
		}
		return Arrays.stream(ret).filter(Objects::nonNull).toArray(UnaryRectangleConstraint[]::new);
	}
	
	private static List<String> parseArgumentSymbols(String everyting) {
		String[] parsed = HybridDomain.parseKeyword(ARGUMENT_SYMBOLS_KEYWORD, everyting);
		List<String> retL = new ArrayList<>();
		if (parsed.length > 0) {
			String[] parsedSymbols = parsed[0].split("\\s+");
			retL.addAll(Arrays.asList(parsedSymbols));
		} else {
			MetaCSPLogging.getLogger(ProblemParser.class).warning("Warning: No argument symbols specified in problem!");
		}
		retL.add(N);
		return retL;
	}

	private Map<String, String[]> parseTypesInstancesMap(String everything) {
		Map<String, String[]> ret = new HashMap<String, String[]>();
		for (String typeDef : HybridDomain.parseKeyword(INSTANCES_KEYWORD, everything)) {
			String[] t = typeDef.split("\\s+");
			ret.put(t[0], Arrays.copyOfRange(t, 1, t.length));
		}
		return ret;
	}

	@Override
	public String[] getArgumentSymbols() {
		return argumentSymbols.toArray(new String[argumentSymbols.size()]);
	}

	@Override
	public void addArgumentSymbols(Collection<String> symbols) {
		argumentSymbols.addAll(symbols);
	}

	@Override
	public Map<String, String[]> getTypesInstancesMap() {
		return typesInstancesMap;
	}

}
