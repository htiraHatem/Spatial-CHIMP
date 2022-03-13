package fluentSolver;

import java.util.logging.Logger;

import integers.IntegerConstraint;
import integers.IntegerVariable;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangleConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangularRegion;
import org.metacsp.multi.spatial.rectangleAlgebra.UnaryRectangleConstraint;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

import unify.CompoundSymbolicValueConstraint;
import unify.CompoundSymbolicVariableConstraintSolver;

public class TestFluentNetworkSolver {

	public static void main(String[] args) {
		Logger logger = MetaCSPLogging.getLogger(TestFluentNetworkSolver.class);
		String[] symbolsPredicates = {"on", "robotat", "get_mug"};
		String[] symbolsMugs = {"mug1", "mug2", "mug3", "mug4", "mug5", "mug6", "mug7", "mug8", "mug9", "mug10", "none"};
		String[] symbolsPlAreas = {"pl1", "pl2", "pl3", "pl4", "pl5", "pl6", "pl7", "pl8", "pl9", "pl10", "none"};
		String[] symbolsManAreas = {"ma1", "ma2", "ma3", "ma4", "ma5", "ma6", "ma7", "ma8", "ma9", "ma10", "none"};
		String[] symbolsPreAreas = {"pma1", "pma2", "pma3", "pma4", "pma5", "pma6", "pma7", "pma8", "pma9", "pma10", "none"};
		String[][] symbols = new String[5][];
		symbols[0] = symbolsPredicates;
		symbols[1] = symbolsMugs;
		symbols[2] = symbolsPlAreas;
		symbols[3] = symbolsManAreas;
		symbols[4] = symbolsPreAreas;
		logger.info("Test1");
		FluentNetworkSolver solver = new FluentNetworkSolver(0, 500, symbols, new int[] {1,1,1,1,1});//, 0, 1000, 2);
		logger.info("Test2");
		Fluent[] fluents = (Fluent[]) solver.createVariables(4);
		logger.info("Test3");
		ConstraintNetwork.draw(solver.getConstraintNetwork());
		//ConstraintNetwork.draw(solver.getConstraintSolvers()[1].getConstraintNetwork());
		ConstraintNetwork.draw(solver.getConstraintSolvers()[2].getConstraintNetwork());
		//ConstraintNetwork.draw(solver.getConstraintSolvers()[3].getConstraintNetwork());	
		
		fluents[0].setName("get_mug(mug1 pl1 none none)");
		fluents[1].getRectangularRegioInterval().setName("get_mug(mug1 ?pl1 none none)");
		fluents[2].getRectangularRegioInterval().setName("mug");
		fluents[3].getRectangularRegioInterval().setName("knife");

		logger.info(fluents[0].toString());
		logger.info(fluents[1].toString());
		
		// add symbolic value
		CompoundSymbolicValueConstraint ncon0 =
				new CompoundSymbolicValueConstraint(CompoundSymbolicValueConstraint.Type.MATCHES);
		ncon0.setFrom(fluents[0].getCompoundSymbolicVariable());
		ncon0.setTo(fluents[1].getCompoundSymbolicVariable());
		CompoundSymbolicVariableConstraintSolver nsolver =
				(CompoundSymbolicVariableConstraintSolver) solver.getConstraintSolvers()[0];
		logger.info("Added ncon0? " + nsolver.addConstraint(ncon0));

		logger.info(fluents[0].toString());
		logger.info(fluents[1].toString());
		
		FluentConstraint fcon01 = new FluentConstraint(FluentConstraint.Type.MATCHES);
		fcon01.setFrom(fluents[0]);
		fcon01.setTo(fluents[1]);
		logger.info("Added fcon01? " + solver.addConstraint(fcon01));

		logger.info(fluents[0].toString());
		logger.info(fluents[1].toString());
		
		FluentConstraint fcon21 = new FluentConstraint(FluentConstraint.Type.PRE, new int[] {1,1, 2, 2});
		fcon21.setFrom(fluents[2]);
		fcon21.setTo(fluents[1]);
		logger.info("Added fcon21? " + solver.addConstraint(fcon21));
		AllenIntervalConstraint a = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, new Bounds(4, 10));
				a.setFrom(fluents[2]);
		a.setTo(fluents[1]);
		logger.info("Added fcon21? " + solver.addConstraint(a));
		
//        ------- integer constraints ---		
//		fluents[1].getIntegerVariables()[0].setConstantValue(17);
//		fluents[1].getIntegerVariables()[1].setConstantValue(18);
//		IntegerConstraint ic01 = new IntegerConstraint(IntegerConstraint.Type.ARITHM,
//				new IntegerVariable[]{fluents[0].getIntegerVariables()[0], fluents[1].getIntegerVariables()[0]},
//				"=", "+", 23);
//		logger.info("Added ic01? " + solver.getConstraintSolvers()[3].addConstraint(ic01));
//		logger.info(fluents[0].toString());
//		logger.info(fluents[1].toString());

		// add UnaryRectangleConstraint (size,at)
		UnaryRectangleConstraint fcon11 = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size,
				new Bounds(0, 10), new Bounds(0, 30));
		fcon11.setFrom(fluents[2].getRectangularRegioInterval());
		fcon11.setTo(fluents[2].getRectangularRegioInterval());
		logger.info("Added size? " + solver.getConstraintSolvers()[2].addConstraint(fcon11));
		

		UnaryRectangleConstraint atPos = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.At,
				new Bounds(190, 190), new Bounds(220, 220), new Bounds(110, 110), new Bounds(130, 130));
		atPos.setFrom(fluents[3].getRectangularRegioInterval());
		atPos.setTo(fluents[3].getRectangularRegioInterval());
		logger.info("Added atPos? " + solver.getConstraintSolvers()[2].addConstraint(atPos));		
		
		RectangleConstraint SpatialCon12 = new RectangleConstraint(
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before),
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.During));
		SpatialCon12.setFrom(fluents[3].getRectangularRegioInterval());
		SpatialCon12.setTo(fluents[2].getRectangularRegioInterval());
		logger.info("Added SpatialCon12? " + solver.getConstraintSolvers()[2].addConstraint(SpatialCon12));
		logger.info(fluents[3].toString());
		logger.info(fluents[2].toString());
		
		RectangleConstraint SpatialCon01 = new RectangleConstraint(
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.After),
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.During));
		SpatialCon01.setFrom(fluents[2].getRectangularRegioInterval());
		SpatialCon01.setTo(fluents[3].getRectangularRegioInterval());
		logger.info("Added SpatialCon01? " + solver.getConstraintSolvers()[2].addConstraint(SpatialCon01));
		logger.info(fluents[2].toString());
		logger.info(fluents[3].toString());
		
		// test conflict error=logic
//		RectangleConstraint SpatialCon10 = new RectangleConstraint(
//				new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before),
//				new AllenIntervalConstraint(AllenIntervalConstraint.Type.During));
//		SpatialCon01.setFrom(fluents[0].getRectangularRegioInterval());
//		SpatialCon01.setTo(fluents[1].getRectangularRegioInterval());
//		logger.info("Added SpatialCon10? " + solver.getConstraintSolvers()[2].addConstraint(SpatialCon10));
//		logger.info(fluents[0].toString());
//		logger.info(fluents[1].toString());

		
	}

}
