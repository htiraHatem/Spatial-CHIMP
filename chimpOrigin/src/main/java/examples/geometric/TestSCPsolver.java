package examples.geometric;

import scpsolver.constraints.Constraint;
import scpsolver.constraints.LinearBiggerThanEqualsConstraint;
import scpsolver.constraints.LinearConstraint;
import scpsolver.constraints.LinearEqualsConstraint;
import scpsolver.constraints.LinearSmallerThanEqualsConstraint;
import scpsolver.problems.LPSolution;
import scpsolver.problems.LPWizard;
import scpsolver.problems.LinearProgram;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;

public class TestSCPsolver {

	public static void main(String[] args) {
//        Min a1 + c2 
//        a1 > 5
//	    	        a2 = a1 + 5   c1  : a2-a1 =5
//	    			b1 = a2 + 10  c2  : b1-a2 = 10
//					b2 = b1 + 5   c3  : b2-b1 = 5
//					c1 = b2 + 10  c4  : c1-b2 = 10
//					c2 = c1 + 5   c5
		
//		LPWizard lpw = new LPWizard();
//		lpw.plus("a1",1.0).plus("b1",1.0);
//		lpw.addConstraint("c0",0,">").plus("a1",1.0);
//		lpw.addConstraint("c1",5,"<=").plus("a1",1.0);
//		lpw.addConstraint("c2",5,"=").plus("a2",1.0).plus("a1",-1.0);
//		lpw.addConstraint("c3",10,"=").plus("b1",1.0).plus("a2",-1.0);
//		lpw.addConstraint("c4",5,"=").plus("b2",1.0).plus("b1",-1.0);
//		lpw.addConstraint("c5",10,"=").plus("c1",1.0).plus("b2",-1.0);
//		lpw.addConstraint("c6",5,"=").plus("c2",1.0).plus("c1",-1.0);
//
//		lpw.setMinProblem(true);
//		LPSolution sol =lpw.solve();

		LinearProgram lp = new LinearProgram(new double[] { 1,0,0,0,0,1 });

		lp.addConstraint(new LinearBiggerThanEqualsConstraint(new double[] { 1.0, 0, 0, 0, 0, 0 }, 5, "c1"));
		lp.addConstraint(new LinearBiggerThanEqualsConstraint(new double[] { -1, 1, 0, 0, 0, 0 }, 5, "c2"));
		lp.addConstraint(new LinearBiggerThanEqualsConstraint(new double[] { 0, -1, 1, 0, 0, 0 }, 10, "c3"));
		lp.addConstraint(new LinearBiggerThanEqualsConstraint(new double[] { 0, 0, -1, 1, 0, 0 }, 5, "c4"));
		lp.addConstraint(new LinearBiggerThanEqualsConstraint(new double[] { 0, 0, 0, -1, 1, 0 }, 10, "c5"));
		lp.addConstraint(new LinearBiggerThanEqualsConstraint(new double[] { 0, 0, 0, 0, -1, 1 }, 5, "c6"));
		lp.addConstraint(new LinearSmallerThanEqualsConstraint(new double[] { 0, 0, 0, 0, 0, 1 }, 50, "c7"));

		lp.setMinProblem(true);

		LinearProgramSolver solver = SolverFactory.newDefault();
		long startTime = System.nanoTime();
		double[] sol = solver.solve(lp);
		long endTime = System.nanoTime();
		System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 

	}
}
