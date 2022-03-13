package examples.geometric;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangleConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangleConstraintSolver;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangularRegion;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

import dwr.DWRNavigationMetaConstraint;
import fluentSolver.Fluent;
import fluentSolver.FluentNetworkSolver;
import hybridDomainParsing.DomainParsingException;
import hybridDomainParsing.HybridDomain;
import hybridDomainParsing.ProblemParser;
import htn.*;
import htn.HTNMetaConstraint.markings;
import htn.guessOrdering.GuessOrderingMetaConstraint;
import htn.guessOrdering.GuessOrderingValOH;
import htn.valOrderingHeuristics.DeepestFewestsubsNewestbindingsValOH;
import htn.valOrderingHeuristics.UnifyFewestsubsEarliesttasksNewestbindingsValOH;
import resourceFluent.FluentResourceUsageScheduler;
import resourceFluent.FluentScheduler;
import resourceFluent.ResourceUsageTemplate;
import unify.CompoundSymbolicVariableConstraintSolver;

public class TestSpatialHybridDomainParser{
	
	private static HTNPlanner planner;
	private static FluentNetworkSolver fluentSolver;

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
		HTNPlanner planner = new HTNPlanner(0,  1000,  0, symbols, ingredients);
		FluentNetworkSolver fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		pp.createState(fluentSolver, domain);
		
		// display only RectangleConstraintSolver
		ConstraintSolver fs =  fluentSolver.getConstraintSolvers()[2];

		//display possible position (X,Y) of every variable
		for (Variable var : fs.getVariables()) {
			RectangularRegion a = (RectangularRegion) var;
			if (!a.getName().isEmpty())
				System.out.println(((RectangleConstraintSolver) fluentSolver.getConstraintSolvers()[2])
						.drawAlmostCentreRectangle(100, a));

		}
		
		
		ConstraintNetwork.draw(fluentSolver.getConstraintSolvers()[0].getConstraintNetwork(),"symbolic");
		ConstraintNetwork.draw(fluentSolver.getConstraintSolvers()[1].getConstraintNetwork(),"allen");
		ConstraintNetwork.draw(fluentSolver.getConstraintSolvers()[2].getConstraintNetwork(),"RA");
				

	}

}