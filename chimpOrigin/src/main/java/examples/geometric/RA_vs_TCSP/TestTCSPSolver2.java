package examples.geometric.RA_vs_TCSP;

import java.awt.Color;
import java.util.logging.Level;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.meta.TCSP.TCSPSolver;
import org.metacsp.multi.TCSP.DistanceConstraint;
import org.metacsp.multi.TCSP.DistanceConstraintSolver;
import org.metacsp.multi.TCSP.MultiTimePoint;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;


public class TestTCSPSolver2 {
	
	public static void main(String args[]) {
		
		TCSPSolver metaSolverX = new TCSPSolver(0, 100, 0);
		TCSPSolver metaSolverY = new TCSPSolver(0, 100, 0);
		DistanceConstraintSolver groundSolverX = (DistanceConstraintSolver)metaSolverX.getConstraintSolvers()[0];		
		DistanceConstraintSolver groundSolverY = (DistanceConstraintSolver)metaSolverY.getConstraintSolvers()[0];		
		
		MetaCSPLogging.setLevel(metaSolverX.getClass(), Level.FINEST);
		MetaCSPLogging.setLevel(metaSolverY.getClass(), Level.FINEST);
		
		/*
		 * 3 objects near each other 
		 *    obj1          obj2            obj3
		 *  x1----x2      x1----x2        x1----x2
		 *  
		 */
		
		Bounds A_size_x = new Bounds(20,20);
		Bounds A_size_y = new Bounds(10,10);
		Bounds B_size_x = new Bounds(10,10);
		Bounds B_size_y = new Bounds(10,10);
		Bounds C_size_x = new Bounds(20,20);
		Bounds C_size_y = new Bounds(20,20);
		Bounds D_size_x = new Bounds(10,10);
		Bounds D_size_y = new Bounds(15,15);
		
		
		MultiTimePoint x1 = groundSolverX.getSource();
		MultiTimePoint x2 = (MultiTimePoint)groundSolverX.createVariable();
		MultiTimePoint x3 = (MultiTimePoint)groundSolverX.createVariable();
		MultiTimePoint x4 = (MultiTimePoint)groundSolverX.createVariable();
//		MultiTimePoint x5 = (MultiTimePoint)groundSolverX.createVariable();
//		MultiTimePoint x6 = (MultiTimePoint)groundSolverX.createVariable();
		
		ConstraintNetwork.draw(groundSolverX.getConstraintNetwork(), "TCSP_X");
		ConstraintNetwork.draw(groundSolverY.getConstraintNetwork(), "TCSP_Y");
		
		DistanceConstraint Ax = new DistanceConstraint(A_size_x);
		Ax.setFrom(groundSolverX.getSource());
		Ax.setTo(x2);
		
		
		DistanceConstraint Cx = new DistanceConstraint(C_size_x);
		Cx.setFrom(groundSolverX.getSource());
		Cx.setTo(x2);
		
		DistanceConstraint Dx = new DistanceConstraint(D_size_x);
		Dx.setFrom(x2);
		Dx.setTo(x3);
		
		DistanceConstraint Bx = new DistanceConstraint(B_size_x);
		Bx.setFrom(x3);
		Bx.setTo(x4);
		
//		DistanceConstraint Ex1 = new DistanceConstraint(D_size_x);
//		Ex1.setFrom(x4);
//		Ex1.setTo(x5);
//		
//		DistanceConstraint Fx1 = new DistanceConstraint(D_size_x);
//		Fx1.setFrom(x5);
//		Fx1.setTo(x6);

	
		
		MultiTimePoint y1 = (MultiTimePoint)groundSolverY.createVariable();
		MultiTimePoint y2 = (MultiTimePoint)groundSolverY.createVariable();
		MultiTimePoint y3 = (MultiTimePoint)groundSolverY.createVariable();
		MultiTimePoint y4 = (MultiTimePoint)groundSolverY.createVariable();
		
		DistanceConstraint Ay = new DistanceConstraint(A_size_y);
		Ay.setFrom(groundSolverY.getSource());
		Ay.setTo(y2);
		
		DistanceConstraint By = new DistanceConstraint(B_size_y);
		By.setFrom(groundSolverY.getSource());
		By.setTo(y2);
		
		DistanceConstraint Cy = new DistanceConstraint(C_size_y);
		Cy.setFrom(y2);
		Cy.setTo(y3);
		
		DistanceConstraint Dy = new DistanceConstraint(D_size_y);
		Dy.setFrom(y2);
		Dy.setTo(y4);
		


		
		long startTime = System.nanoTime();
		groundSolverX.addConstraints(new DistanceConstraint[] {Ax,Bx,Bx,Cx,Dx});
		groundSolverY.addConstraints(new DistanceConstraint[] {Ay,By,Cy,Dy});
		long endTime = System.nanoTime();
		System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 

		
		
		//TU Delft
		//Leon Planken - see his website for temporal reasoning survey (2007)
		//See his JAIR 2012 for ICAPS work
		
	}

}
