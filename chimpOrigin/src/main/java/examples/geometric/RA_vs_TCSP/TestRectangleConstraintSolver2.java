package examples.geometric.RA_vs_TCSP;

import java.util.Vector;
import java.util.logging.Level;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.meta.TCSP.MostConstrainedFirstVarOH;
import org.metacsp.meta.TCSP.TCSPLabeling;
import org.metacsp.meta.TCSP.WidestIntervalFirstValOH;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangleConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangleConstraintSolver;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangularRegion;
import org.metacsp.multi.spatial.rectangleAlgebra.UnaryRectangleConstraint;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

public class TestRectangleConstraintSolver2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// RectangleConstraint :
		// Each constraint represents two dimension Allen relations between spatial
		// entities.
		// a rectangle whose sides are parallel to the coordinate axes.
		RectangleConstraintSolver solver = new RectangleConstraintSolver(0, 40);
		Vector<Constraint> allConstraints = new Vector<Constraint>();

		// ..........................................................
		RectangularRegion A = (RectangularRegion) solver.createVariable();
		A.setName("A");

		RectangularRegion B = (RectangularRegion) solver.createVariable();
		B.setName("B");

		RectangularRegion C = (RectangularRegion) solver.createVariable();
		C.setName("C");

		RectangularRegion D = (RectangularRegion) solver.createVariable();
		D.setName("D");

		// ..........................................................

		Bounds A_size_x = new Bounds(20,20);
		Bounds A_size_y = new Bounds(10,10);
		Bounds B_size_x = new Bounds(10,10);
		Bounds B_size_y = new Bounds(10,10);
		Bounds C_size_x = new Bounds(20,20);
		Bounds C_size_y = new Bounds(20,20);
		Bounds D_size_x = new Bounds(10,10);
		Bounds D_size_y = new Bounds(15,15);
		
		UnaryRectangleConstraint sizeA = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size,
				A_size_x,A_size_y);
		sizeA.setFrom(A);
		sizeA.setTo(A);
		allConstraints.add(sizeA);
		
		UnaryRectangleConstraint sizeB = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size,
				B_size_x,B_size_y);
		sizeB.setFrom(B);
		sizeB.setTo(B);
		allConstraints.add(sizeB);

		UnaryRectangleConstraint sizeC = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size,
				C_size_x,C_size_y);
		sizeC.setFrom(C);
		sizeC.setTo(C);
		allConstraints.add(sizeC);
		
		UnaryRectangleConstraint sizeD = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size,
				D_size_x,D_size_y);
		sizeD.setFrom(D);
		sizeD.setTo(D);
		allConstraints.add(sizeD);
		


		
		// ------- constraint between boxes
		
	
		RectangleConstraint AtoB = new RectangleConstraint(
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, new Bounds(10, 10)),
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals));
		AtoB.setFrom(A);
		AtoB.setTo(B);
		allConstraints.add(AtoB);

		RectangleConstraint AtoC = new RectangleConstraint(
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals),
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.MetBy));
		AtoC.setFrom(A);
		AtoC.setTo(C);
		allConstraints.add(AtoC);
		
		RectangleConstraint CtoD = new RectangleConstraint(
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets),
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.StartedBy));
		CtoD.setFrom(C);
		CtoD.setTo(D);
		allConstraints.add(CtoD);


		Constraint[] allConstraintsArray = allConstraints.toArray(new Constraint[allConstraints.size()]);
		MetaCSPLogging.setLevel(Level.ALL);

		System.out.println(solver.drawAlmostCentreRectangle(100, A));
		System.out.println(solver.drawAlmostCentreRectangle(100, B));
		System.out.println(solver.drawAlmostCentreRectangle(100, C));
		System.out.println(solver.drawAlmostCentreRectangle(100, D));

		
		long startTime = System.nanoTime();
		System.out.println("Solved? " + solver.addConstraints(allConstraintsArray));

		long endTime = System.nanoTime();
		System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 
		ConstraintNetwork.draw(solver.getConstraintNetwork());


	}

}
