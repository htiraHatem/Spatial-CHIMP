package examples.geometric;

import java.util.Vector;

import org.metacsp.framework.Variable;
import org.metacsp.spatial.geometry.GeometricConstraint;
import org.metacsp.spatial.geometry.GeometricConstraintSolver;
import org.metacsp.spatial.geometry.Polygon;
import org.metacsp.spatial.geometry.Vec2;
import org.metacsp.utility.UI.PolygonFrame;

public class TestRCC2Solver {

	public static void main(String[] args) {
		
		// test (Region Connection Calculus 2)RCC2 inside | disconnected (DC)
		
		GeometricConstraintSolver solver = new GeometricConstraintSolver();
		Variable[] vars = solver.createVariables(4, "P");
		
		Polygon table = (Polygon)vars[0];		
		Vector<Vec2> vecs1 = new Vector<Vec2>();
		vecs1.add(new Vec2(50,50));
		vecs1.add(new Vec2(250,50));
		vecs1.add(new Vec2(250,250));
		vecs1.add(new Vec2(50,250));
		table.setDomain(vecs1.toArray(new Vec2[vecs1.size()]));
		table.setMovable(false);
		
		Polygon p1 = (Polygon)vars[1];		
		Vector<Vec2> vecs = new Vector<Vec2>();
		vecs.add(new Vec2(110,110));
		vecs.add(new Vec2(130,110));
		vecs.add(new Vec2(130,130));
		vecs.add(new Vec2(110,130));
		p1.setDomain(vecs.toArray(new Vec2[vecs.size()]));
		p1.setMovable(false);
		
		Polygon p2 = (Polygon)vars[2];		
		Vector<Vec2> vec2 = new Vector<Vec2>();
		vec2.add(new Vec2(150,110));
		vec2.add(new Vec2(170,110));
		vec2.add(new Vec2(170,130));
		vec2.add(new Vec2(150,130));
//		vec2.add(new Vec2(137,54));
//		vec2.add(new Vec2(161,54));
//		vec2.add(new Vec2(161,190));
//		vec2.add(new Vec2(137,190));
		p2.setDomain(vec2.toArray(new Vec2[vec2.size()]));
		p2.setMovable(false);
		
		Polygon p3 = (Polygon)vars[3];		
		Vector<Vec2> vec3 = new Vector<Vec2>();
		vec3.add(new Vec2(190,110));
		vec3.add(new Vec2(210,110));
		vec3.add(new Vec2(210,130));
		vec3.add(new Vec2(190,130));
		p3.setDomain(vec3.toArray(new Vec2[vec3.size()]));
		p3.setMovable(false);
		
		
		
								
		PolygonFrame pf = new PolygonFrame("Polygon Constraint Network", solver.getConstraintNetwork());
		
		try { Thread.sleep(1000); }
		catch (InterruptedException e) { e.printStackTrace(); }


		
		GeometricConstraint DC = new GeometricConstraint(GeometricConstraint.Type.DC);
		DC.setFrom(p1);
		DC.setTo(p2);
		System.out.println("Added? dc: " + solver.addConstraint(DC));
		
		GeometricConstraint DC1 = new GeometricConstraint(GeometricConstraint.Type.DC);
		DC1.setFrom(p1);
		DC1.setTo(p3);
		System.out.println("Added? dc1: " + solver.addConstraint(DC1));
		
		GeometricConstraint DC2 = new GeometricConstraint(GeometricConstraint.Type.DC);
		DC2.setFrom(p3);
		DC2.setTo(p2);
		System.out.println("Added? dc2: " + solver.addConstraint(DC2));

		
		// conflict
//		GeometricConstraint inside = new GeometricConstraint(GeometricConstraint.Type.INSIDE);
//		inside.setFrom(p3);
//		inside.setTo(p2);
//		System.out.println("Added? p3 inside p4: " + solver.addConstraint(inside));


	}

}
