package examples.geometric;

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

public class TestRectangleConstraintSolver {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// RectangleConstraint :
		// Each constraint represents two dimension Allen relations between spatial
		// entities.
		// a rectangle whose sides are parallel to the coordinate axes.
		RectangleConstraintSolver solver = new RectangleConstraintSolver(0, 250);
		Vector<Constraint> allConstraints = new Vector<Constraint>();

		// ..........................................................
		RectangularRegion knife = (RectangularRegion) solver.createVariable();
		knife.setName("knife");

		RectangularRegion fork = (RectangularRegion) solver.createVariable();
		fork.setName("fork");

		RectangularRegion dish = (RectangularRegion) solver.createVariable();
		dish.setName("dish");

		RectangularRegion serviette = (RectangularRegion) solver.createVariable();
		serviette.setName("serviette");

		// ..........................................................

		Bounds knife_size_x = new Bounds(4, 8);
		Bounds knife_size_y = new Bounds(18, 24);
		Bounds dish_size_x = new Bounds(10, 15);
		Bounds dish_size_y = new Bounds(20, 25);
		Bounds fork_size_x = new Bounds(4, 8);
		Bounds fork_size_y = new Bounds(18, 24);
		
		UnaryRectangleConstraint sizeDish = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size,
				dish_size_x,dish_size_y);
		sizeDish.setFrom(dish);
		sizeDish.setTo(dish);
		allConstraints.add(sizeDish);
		
		UnaryRectangleConstraint sizeKnife = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size,
				knife_size_x,knife_size_y);
		sizeKnife.setFrom(knife);
		sizeKnife.setTo(knife);
		allConstraints.add(sizeKnife);
		
		UnaryRectangleConstraint sizeFork = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size,
				fork_size_x,fork_size_y);
		sizeFork.setFrom(fork);
		sizeFork.setTo(fork);
		allConstraints.add(sizeFork);
		
		//  x lowerbound  , X upperbound ,Y lowerbound,  Y upperbound  
		UnaryRectangleConstraint atServiette = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.At,
				 new Bounds(20,20),new Bounds(130,130), new Bounds(20,20), new Bounds(130, 130));
		atServiette.setFrom(serviette);
		atServiette.setTo(serviette);
		allConstraints.add(atServiette);


		// ...........all at the serviette...............................................

		RectangleConstraint dishAtServiette = new RectangleConstraint(
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.During), //X achse
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.During,  //Y achse
						AllenIntervalConstraint.Type.During.getDefaultBounds()));
		dishAtServiette.setFrom(dish);
		dishAtServiette.setTo(serviette);
		allConstraints.add(dishAtServiette);
		
		RectangleConstraint forkAtServiette = new RectangleConstraint(
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.During),
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.During,
						AllenIntervalConstraint.Type.During.getDefaultBounds()));
		forkAtServiette.setFrom(fork);
		forkAtServiette.setTo(serviette);
		allConstraints.add(forkAtServiette);
		
		RectangleConstraint knifeAtServiette = new RectangleConstraint(
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.During),
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.During,
						AllenIntervalConstraint.Type.During.getDefaultBounds()));
		knifeAtServiette.setFrom(knife);
		knifeAtServiette.setTo(serviette);
		allConstraints.add(knifeAtServiette);
//		
		
		// ------- constraint between knife-fork-dish
		
	
		RectangleConstraint knifetoDish = new RectangleConstraint(
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, new Bounds(4, 10)),
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.During,
						AllenIntervalConstraint.Type.During.getDefaultBounds()));
		knifetoDish.setFrom(knife);
		knifetoDish.setTo(dish);
		allConstraints.add(knifetoDish);

		RectangleConstraint forktoDish = new RectangleConstraint(
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.After),
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.During));
		forktoDish.setFrom(fork);
		forktoDish.setTo(dish);
		allConstraints.add(forktoDish);
		
		//test contradiction
		
//		RectangleConstraint test = new RectangleConstraint(
//				new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before),
//				new AllenIntervalConstraint(AllenIntervalConstraint.Type.During));
//		test.setFrom(fork);
//		test.setTo(knife);
//		allConstraints.add(test);


		Constraint[] allConstraintsArray = allConstraints.toArray(new Constraint[allConstraints.size()]);
		MetaCSPLogging.setLevel(Level.ALL);

		
		VariableOrderingH varOH = new MostConstrainedFirstVarOH();
		
		ValueOrderingH valOH = new WidestIntervalFirstValOH();

		TCSPLabeling metaCons = new TCSPLabeling(varOH, valOH);
		solver.addConstraint(metaCons);
		
		long startTime = System.nanoTime();
		System.out.println("Solved? " + solver.addConstraints(allConstraintsArray));

		long endTime = System.nanoTime();
		System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 
		ConstraintNetwork.draw(solver.getConstraintNetwork());


	}

}
