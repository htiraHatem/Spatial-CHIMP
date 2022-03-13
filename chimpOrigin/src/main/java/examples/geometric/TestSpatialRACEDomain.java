package examples.geometric;

import java.util.logging.Level;


import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import org.metacsp.utility.logging.MetaCSPLogging;

import externalPathPlanning.LookUpTableDurationEstimator;
import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.DomainParsingException;
import planner.CHIMP;

public class TestSpatialRACEDomain {

    static final boolean LOGGING = false;
    static final boolean GUESS_ORDERING = false;
    static final boolean PRINT_PLAN = true;
    static final boolean DRAW = false;


    public static void main(String[] args) {

    	
    	// put dish1 :enoughDistance true, works!
    	//String problemFile = "domains/spatialDomProb/test_m_put_object_2a.pdl";
    	
    	// put dish1 if not enough space, works
    	//String problemFile = "domains/spatialDomProb/test_m_put_object_2b.pdl";

    	
    	// move dish from counter1 to table1: enoughDistance: true, works!
    	//String problemFile = "domains/spatialDomProb/test_m_move_object_1a.pdl";
    	
    	// move dish from counter1 to table1: enoughDistance: false :  works
    	//String problemFile = "domains/spatialDomProb/test_m_move_object_1b.pdl";
    	
    	// pick up object in every hand: test knife inside a mug : works
    	//String problemFile = "domains/spatialDomProb/test_op_pick_up_two_object_1.pdl";
    	
    	// serve an empty dish: move empty dish from counter1 to table1 (mug on dish)
    	// collision + enough distance to place it on the other table: works!
    	//String problemFile = "domains/spatialDomProb/test_m_serve_empty_dish_1.pdl";
    	
    	// serve an empty dish: move empty dish from counter1 to table1 (no collision)
    	// no-collision + enough distance to place it on the other table: works
    	//String problemFile = "domains/spatialDomProb/test_m_serve_empty_dish_2.pdl";
    	
    	// serve an empty dish: move empty dish from counter1 to table1 (mug on the dish)
    	// collision + not enough distance to place it on the other table (shifting)
    	//String problemFile = "domains/spatialDomProb/test_m_serve_empty_dish_3.pdl";
    	
    	// serve an empty dish: move empty dish from counter1 to table1 (empty dish)
    	// collision-free + not enough distance to place it on the other table (shifting)
    	String problemFile = "domains/spatialDomProb/test_m_serve_empty_dish_4.pdl";
    	

        String domainFile = "domains/spatialDomProb/race_domain.ddl";

        ValueOrderingH valOH = new UnifyDeepestWeightNewestbindingsValOH();

        CHIMP.CHIMPBuilder builder;

        try {
            builder = new CHIMP.CHIMPBuilder(domainFile, problemFile)
            		.origin(0)
            		.horizon(500000)
                    .valHeuristic(valOH)
                    .mbEstimator(new LookUpTableDurationEstimator())
                    .htnUnification(true);
            if (GUESS_ORDERING) {
                builder.guessOrdering(true);
            }

        } catch (DomainParsingException e) {
            e.printStackTrace();
            return;
        }
        CHIMP chimp = builder.build();
        
        if (!LOGGING) {
            MetaCSPLogging.setLevel(Level.OFF);
        }

        System.out.println("Found plan? " + chimp.generatePlan());
        chimp.printStats(System.out);

        if (PRINT_PLAN) {
            Variable[] planVector = chimp.extractActions();
            int c = 0;
            for (Variable act : planVector) {
                if (act.getComponent() != null)
                    System.out.println(c++ + ".\t" + act);
            }

			chimp.printFullPlan();
			chimp.drawPlanHierarchy(100);
			chimp.drawRectangularRegion();
		}


		
    }

}
