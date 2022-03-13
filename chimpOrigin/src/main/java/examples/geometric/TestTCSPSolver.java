package examples.geometric;

import java.awt.Color;
import java.util.logging.Level;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.meta.TCSP.MostConstrainedFirstVarOH;
import org.metacsp.meta.TCSP.TCSPLabeling;
import org.metacsp.meta.TCSP.TCSPSolver;
import org.metacsp.meta.TCSP.WidestIntervalFirstValOH;
import org.metacsp.multi.TCSP.DistanceConstraint;
import org.metacsp.multi.TCSP.DistanceConstraintSolver;
import org.metacsp.multi.TCSP.MultiTimePoint;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;


public class TestTCSPSolver {
	
	public static void main(String args[]) {
		
		TCSPSolver metaSolver = new TCSPSolver(0, 100, 0);
		DistanceConstraintSolver groundSolver = (DistanceConstraintSolver)metaSolver.getConstraintSolvers()[0];		
		APSPSolver groundGroundSolver = (APSPSolver)groundSolver.getConstraintSolvers()[0];
		
		MetaCSPLogging.setLevel(metaSolver.getClass(), Level.FINEST);
		
		/*
		 * 3 objects near each other 
		 *    obj1          obj2            obj3
		 *  x1----x2      x1----x2        x1----x2
		 *  
		 */
		
		MultiTimePoint Obj1x1 = (MultiTimePoint)groundSolver.createVariable();
		MultiTimePoint Obj1x2 = (MultiTimePoint)groundSolver.createVariable();
		MultiTimePoint Obj2x1 = (MultiTimePoint)groundSolver.createVariable();
		MultiTimePoint Obj2x2 = (MultiTimePoint)groundSolver.createVariable();
		MultiTimePoint Obj3x1 = (MultiTimePoint)groundSolver.createVariable();
		MultiTimePoint Obj3x2 = (MultiTimePoint)groundSolver.createVariable();
		
		ConstraintNetwork.draw(groundSolver.getConstraintNetwork(), "TCSP");
		ConstraintNetwork.draw(groundGroundSolver.getConstraintNetwork(), "STP");
		
		DistanceConstraint obj1Starts = new DistanceConstraint(new Bounds(0, 5));
		obj1Starts.setFrom(groundSolver.getSource());
		obj1Starts.setTo(Obj1x1);
		
		DistanceConstraint obj1Ends = new DistanceConstraint(new Bounds(5,5));
		obj1Ends.setColor(new Color(0.6f,0.6f,0.6f));

		obj1Ends.setFrom(Obj1x1);
		obj1Ends.setTo(Obj1x2);
		
		DistanceConstraint obj2Starts = new DistanceConstraint(new Bounds(10, 10));
		obj2Starts.setFrom(Obj1x2);
		obj2Starts.setTo(Obj2x1);
		
		DistanceConstraint obj2Ends = new DistanceConstraint(new Bounds(5,5));
		obj2Ends.setFrom(Obj2x1);
		obj2Ends.setTo(Obj2x2);
		
		DistanceConstraint obj3Starts = new DistanceConstraint(new Bounds(10, 10));
		obj3Starts.setFrom(Obj2x2);
		obj3Starts.setTo(Obj3x1);
		
		DistanceConstraint obj3Ends = new DistanceConstraint(new Bounds(5,5));
		obj3Ends.setFrom(Obj3x1);
		obj3Ends.setTo(Obj3x2);
		
		
		groundSolver.addConstraints(new DistanceConstraint[] {obj1Starts,obj1Ends,obj2Starts,obj2Ends,obj3Starts,obj3Ends});
		
		VariableOrderingH varOH = new MostConstrainedFirstVarOH();
		
		ValueOrderingH valOH = new WidestIntervalFirstValOH();

		TCSPLabeling metaCons = new TCSPLabeling(varOH, valOH);
		metaSolver.addMetaConstraint(metaCons);
		
		long startTime = System.nanoTime();
		System.out.println("Solved? " + metaSolver.backtrack());
		long endTime = System.nanoTime();
		System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 

		
		
		//TU Delft
		//Leon Planken - see his website for temporal reasoning survey (2007)
		//See his JAIR 2012 for ICAPS work
		
	}

}
