package examples.geometric;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.logging.Level;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.framework.multi.MultiConstraintSolver;
import org.metacsp.meta.hybridPlanner.FluentBasedSimpleDomain;
import org.metacsp.meta.hybridPlanner.MetaOccupiedConstraint;
import org.metacsp.meta.hybridPlanner.MetaOccupiedTimesBasedConstraint;
import org.metacsp.meta.hybridPlanner.MetaSpatialAdherenceConstraint;
import org.metacsp.meta.hybridPlanner.SimpleHybridPlanner;
import org.metacsp.meta.simplePlanner.SimpleDomain.markings;
import org.metacsp.multi.activity.SymbolicVariableActivity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.allenInterval.AllenIntervalNetworkSolver;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangleConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangleConstraintSolver;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangularRegion;
import org.metacsp.multi.spatial.rectangleAlgebra.UnaryRectangleConstraint;
import org.metacsp.multi.spatial.rectangleAlgebraNew.toRemove.OntologicalSpatialProperty;
import org.metacsp.multi.temporalRectangleAlgebra.SpatialFluent;
import org.metacsp.multi.temporalRectangleAlgebra.SpatialFluentSolver;
import org.metacsp.spatial.utility.SpatialAssertionalRelation;
import org.metacsp.spatial.utility.SpatialRule;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;
import org.metacsp.utility.timelinePlotting.TimelinePublisher;
import org.metacsp.utility.timelinePlotting.TimelineVisualizer;

import fluentSolver.Fluent;
import fluentSolver.FluentNetworkSolver;
import htn.HTNPlanner;
import hybridDomainParsing.DomainParsingException;
import hybridDomainParsing.HybridDomain;
import hybridDomainParsing.ProblemParser;
import unify.CompoundSymbolicVariable;



public class TestSimpleHybridPlanner {

	//oneCulprit example

	static int pad = 0;    
	static long duration = 1000;

	public static void main(String[] args) {


		String problemFile = "problems/test_m_move_object_2_Spatial.pdl"; 
		//String problemFile = "problems/test_m_move_object_3.pdl"; 

		ProblemParser pp = new ProblemParser(problemFile);

		HybridDomain domain;
		try {
			//domain = new HybridDomain("domains/transterra_v1.ddl");
			domain = new HybridDomain("domains/testPrimitiveHybridPlanningDomain.ddl");

		} catch (DomainParsingException e) {
			System.out.println("Error while parsing domain: " + e.getMessage());
			e.printStackTrace();
			return;
		}
		int[] ingredients = new int[] {1, domain.getMaxArgs()};
		String[][] symbols = new String[2][];
		symbols[0] =  domain.getPredicateSymbols();
		symbols[1] = pp.getArgumentSymbols();
		HTNPlanner planner = new HTNPlanner(0,  600000,  0, symbols, ingredients);
		FluentNetworkSolver fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];

		//Most critical conflict is the one with most activities 
		VariableOrderingH varOH = new VariableOrderingH() {
			@Override
			public int compare(ConstraintNetwork arg0, ConstraintNetwork arg1) {
				return arg1.getVariables().length - arg0.getVariables().length;
			}
			@Override
			public void collectData(ConstraintNetwork[] allMetaVariables) { }
		};
		// no value ordering
		ValueOrderingH valOH = new ValueOrderingH() {
			@Override
			public int compare(ConstraintNetwork o1, ConstraintNetwork o2) { return 0; }
		};
		MetaSpatialAdherenceConstraint metaSpatialAdherence = new MetaSpatialAdherenceConstraint(varOH, valOH);
		FluentNetworkSolver groundSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];

		MetaCSPLogging.setLevel(SimpleHybridPlanner.class, Level.FINEST);
		MetaCSPLogging.setLevel(MetaSpatialAdherenceConstraint.class, Level.FINEST);
		//MetaCSPLogging.setLevel(APSPSolver.class, Level.FINEST);
		//#################################################################################################################

		//#################################################################################################################
		//this is spatial general and assertional rule
		Vector<UnaryRectangleConstraint> srules = new Vector<UnaryRectangleConstraint>();
		Vector<SpatialAssertionalRelation> saRelations = new Vector<SpatialAssertionalRelation>();
		//HashMap<String, Rectangle> observation = new HashMap<String, Rectangle>();

		getSpatialKnowledge(srules, groundSolver);
		getAssertionalRule(saRelations);
		
		insertCurrentStateCurrentGoal(groundSolver);
		//#################################################################################################################
		//add spatial general and assertional rule to MetaSpatialFluentConstraint
//		metaSpatialAdherence.setSpatialRules(srules.toArray(new SpatialRule[srules.size()]));
//	    metaSpatialAdherence.setSpatialAssertionalRelations(saRelations);
//		metaSpatialAdherence.setInitialGoal(new String[]{"cup1"});

		groundSolver.getConstraintSolvers()[2].addConstraints(srules.toArray(new UnaryRectangleConstraint[srules.size()]));
		//add meta constraint

		
		planner.addMetaConstraint(metaSpatialAdherence);


		//planner.backtrack();
		

		//#####################################################################################################################
		//visualization
		//ConstraintNetwork.draw(((FluentNetworkSolver)planner.getConstraintSolvers()[0]).getConstraintSolvers()[2].getConstraintNetwork(), "RA Constraint Network");
		ConstraintNetwork.draw(fluentSolver.getConstraintSolvers()[2].getConstraintNetwork(),"RA");

	}

	

	private static void setFluentintoNetwork(Vector<Constraint> cons, FluentNetworkSolver grounSpatialFluentSolver,
			String name, String symbolicDomain, markings mk, long release){

		Fluent sf = (Fluent)grounSpatialFluentSolver.createVariable();
		//sf.setName(name);

		((RectangularRegion)sf.getInternalVariables()[2]).setName(name);
		//((CompoundSymbolicVariable)sf.getInternalVariables()[0]).setFullName(symbolicDomain);
		((CompoundSymbolicVariable)sf.getInternalVariables()[0]).setMarking(mk);

		if(mk.equals(markings.JUSTIFIED)){
			AllenIntervalConstraint onDuration = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(duration,APSPSolver.INF));
			onDuration.setFrom(sf.getCompoundSymbolicVariable());
			onDuration.setTo(sf.getCompoundSymbolicVariable());
			cons.add(onDuration);

			AllenIntervalConstraint releaseOn = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(release, release));
			releaseOn.setFrom(sf.getCompoundSymbolicVariable());
			releaseOn.setTo(sf.getCompoundSymbolicVariable());
			cons.add(releaseOn);                                    
		}

	}

	private static void insertCurrentStateCurrentGoal(FluentNetworkSolver grounSpatialFluentSolver) {

		Vector<Constraint> cons = new Vector<Constraint>();
		
		setFluentintoNetwork(cons, grounSpatialFluentSolver, "table1", "!at_robot1_table1()", markings.JUSTIFIED,  1);
		setFluentintoNetwork(cons, grounSpatialFluentSolver,  "fork1", "!at_fork1_table1()", markings.JUSTIFIED, 8);
		setFluentintoNetwork(cons, grounSpatialFluentSolver,  "knife1", "!at_knife1_table1()", markings.JUSTIFIED,8);
		setFluentintoNetwork(cons, grounSpatialFluentSolver,  "cup1", "!at_cup1_table1()", markings.UNJUSTIFIED, -1);

		//===================================================================================================================
		//initial State
		//===================================================================================================================


		(grounSpatialFluentSolver.getConstraintSolvers()[1]).addConstraints(cons.toArray(new Constraint[cons.size()]));

	}



	private static void getSpatialKnowledge(Vector<UnaryRectangleConstraint> srules, FluentNetworkSolver groundSolver){

		Bounds knife_size_x = new Bounds(4, 8);
		Bounds knife_size_y = new Bounds(18, 24);
		Bounds cup_size_x = new Bounds(4, 7);
		Bounds cup_size_y = new Bounds(4, 7);
		Bounds fork_size_x = new Bounds(4, 8);
		Bounds fork_size_y = new Bounds(18, 24);

		RectangularRegion var = (RectangularRegion) groundSolver.getConstraintSolvers()[2].createVariable();
		var.setName("knife");
		


		UnaryRectangleConstraint r7 = new UnaryRectangleConstraint( UnaryRectangleConstraint.Type.Size, knife_size_x, knife_size_y);
		r7.setFrom(var);
		r7.setTo(var);
		srules.add(r7);

//		SpatialRule r8 = new SpatialRule("cup", "cup", 
//				new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, cup_size_x, cup_size_y));
//		srules.add(r8);
//
//		SpatialRule r9 = new SpatialRule("fork", "fork", 
//				new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, fork_size_x, fork_size_y));
//		srules.add(r9);


		//Every thing should be on the table            
//		addOnTableConstraint(srules, "fork");
//		addOnTableConstraint(srules, "knife");
		addOnTableConstraint(srules, "cup");



//		SpatialRule r2 = new SpatialRule("cup", "knife", 
//				new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, new Bounds(15, 20)),
//						new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, AllenIntervalConstraint.Type.During.getDefaultBounds() ))
//				);
//		srules.add(r2);
//
//
//
//		SpatialRule r3 = new SpatialRule("cup", "fork", 
//				new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.After, new Bounds(15, 20)),
//						new AllenIntervalConstraint(AllenIntervalConstraint.Type.During , AllenIntervalConstraint.Type.During.getDefaultBounds()))
//
//				);
//		srules.add(r3);


	}



	private static void addOnTableConstraint(Vector<UnaryRectangleConstraint> srules, String str){

		Bounds withinReach_y_lower = new Bounds(5, 20);
		Bounds withinReach_y_upper = new Bounds(5, APSPSolver.INF);
		Bounds withinReach_x_lower = new Bounds(5, APSPSolver.INF);
		Bounds withinReach_x_upper = new Bounds(5, APSPSolver.INF);

//		SpatialRule r8 = new SpatialRule(str, "table", 
//				new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, withinReach_x_lower,withinReach_x_upper),
//						new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, withinReach_y_lower, withinReach_y_upper))
//				);
//		srules.add(r8);

	}

	private static void insertAtConstraint(HashMap<String, Rectangle> recs, Vector<SpatialAssertionalRelation> saRelations, 
			String str, long xl, long xu, long yl, long yu, boolean movable){

		if(xl == 0 && xu == 0 && yl == 0 && yu == 0){
			SpatialAssertionalRelation table_assertion = new SpatialAssertionalRelation(str+"1", str);
			table_assertion.setUnaryAtRectangleConstraint(new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.At, 
					new Bounds(0, APSPSolver.INF), new Bounds(0, APSPSolver.INF), new Bounds(0, APSPSolver.INF), new Bounds(0, APSPSolver.INF)));
			OntologicalSpatialProperty tableOnto = new OntologicalSpatialProperty();
			tableOnto.setMovable(movable);
			table_assertion.setOntologicalProp(tableOnto);
			saRelations.add(table_assertion);                       

		}
		else{
			SpatialAssertionalRelation table_assertion = new SpatialAssertionalRelation(str+"1", str);
			table_assertion.setUnaryAtRectangleConstraint(new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.At, 
					new Bounds(xl, xl), new Bounds(xu, xu), new Bounds(yl, yl), new Bounds(yu, yu)));
			OntologicalSpatialProperty tableOnto = new OntologicalSpatialProperty();
			tableOnto.setMovable(movable);
			table_assertion.setOntologicalProp(tableOnto);
			saRelations.add(table_assertion);                       
			recs.put(str+"1", new Rectangle((int)(xl), (int)(yl), (int)(xu - xl), (int)(yu - yl)));
		}


	}

	private static HashMap<String, Rectangle> getAssertionalRule(Vector<SpatialAssertionalRelation> saRelations){

		HashMap<String, Rectangle> recs = new HashMap<String, Rectangle>();
	
		//both fork and knife should be replaced
		insertAtConstraint(recs, saRelations, "table", 0, 60, 0, 99, false);
		insertAtConstraint(recs, saRelations, "fork", 20, 26, 13, 32, true);
		insertAtConstraint(recs, saRelations, "knife", 30, 36, 10, 33, true);
		insertAtConstraint(recs, saRelations, "cup", 0, 0, 0, 0, true);

		return recs;
	}


}
