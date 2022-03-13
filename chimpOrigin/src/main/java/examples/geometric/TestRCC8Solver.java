package examples.geometric;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.spatial.RCC.RCCConstraint;
import org.metacsp.spatial.RCC.RCCConstraintSolver;
import org.metacsp.spatial.RCC.Region;

public class TestRCC8Solver {

	public static void main(String[] args) {
		
//      RCC-8
//		disconnected (DC)
//		externally connected (EC)
//		equal (EQ)
//		partially overlapping (PO)
//		tangential proper part (TPP)
//		tangential proper part inverse (TPPi)
//		non-tangential proper part (NTPP)
//		non-tangential proper part inverse (NTPPi)
		
		RCCConstraintSolver solver = new RCCConstraintSolver(); 
		Variable[] vars = solver.createVariables(4);
		
		
//      try to put 3 objects on table and all this 3 objects are disconnected from each other
		Region tab = (Region)vars[0];
		Region re1 = (Region)vars[1];
		Region re2 = (Region)vars[2];
		Region re3 = (Region)vars[3];
		
		
		// make the 3 region on the table(tab)
		RCCConstraint con0 = new RCCConstraint(RCCConstraint.Type.NTPP,RCCConstraint.Type.TPP);
		con0.setFrom(re1);
		con0.setTo(tab);
		System.out.println("Adding constraint " + con0 + ": " + solver.addConstraint(con0));
		
		RCCConstraint con1 = new RCCConstraint(RCCConstraint.Type.NTPP,RCCConstraint.Type.TPP);
		con1.setFrom(re2);
		con1.setTo(tab);
		System.out.println("Adding constraint " + con1 + ": " + solver.addConstraint(con1));
		
		RCCConstraint con2 = new RCCConstraint(RCCConstraint.Type.NTPP,RCCConstraint.Type.TPP);
		con2.setFrom(re3);
		con2.setTo(tab);
		System.out.println("Adding constraint " + con2 + ": " + solver.addConstraint(con2));
		
		
		// the 3 regions should be disconnected from each other
		
		RCCConstraint con3 = new RCCConstraint(RCCConstraint.Type.DC);
		con3.setFrom(re1);
		con3.setTo(re3);
		System.out.println("Adding constraint " + con3 + ": " + solver.addConstraint(con3));
		
		RCCConstraint con4 = new RCCConstraint(RCCConstraint.Type.DC);
		con4.setFrom(re1);
		con4.setTo(re2);
		System.out.println("Adding constraint " + con3 + ": " + solver.addConstraint(con4));
		
		RCCConstraint con5 = new RCCConstraint(RCCConstraint.Type.DC);
		con5.setFrom(re2);
		con5.setTo(re3);
		System.out.println("Adding constraint " + con3 + ": " + solver.addConstraint(con5));

//		
		
//		RCCConstraint[] allConstraints = {con0, con1, con2};
//		if (!solver.addConstraints(allConstraints)) { 
//			System.out.println("Failed to add constraints!");
//			System.exit(0);
//		}
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());
	}

}
