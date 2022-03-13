package htn;

import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint.Type;
import org.metacsp.multi.spatial.rectangleAlgebra.UnaryRectangleConstraint;

public class SpatialConstraintTemplate {

    public AllenIntervalConstraint.Type type1=null;
    public AllenIntervalConstraint.Type type2=null;
    public UnaryRectangleConstraint.Type type3 = null;
    public String op1=null;
    public String op2=null;
    public int cste = 0;
    public String[] varKeys = null;


	public SpatialConstraintTemplate(String[] varKeys,Type type1, Type type2) {
		this.type1 = type1;
		this.type2 = type2;
		this.varKeys = varKeys;
	}
	
	public SpatialConstraintTemplate( String[] varKeys, int cste, String op1, String op2) {
		this.varKeys=varKeys;
		this.op1 = op1;
		this.op2 = op2;
		this.cste=cste;
	}

	public SpatialConstraintTemplate(String[] varKeys, UnaryRectangleConstraint.Type constraintType) {
		this.type3 = constraintType;
		this.varKeys = varKeys;
	}



}
