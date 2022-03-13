package hybridDomainParsing.classic.antlr;

import externalPathPlanning.LookUpTableDurationEstimator;
import fluentSolver.FluentNetworkSolver;
import htn.HTNMetaConstraint;
import htn.HTNPlanner;
import htn.valOrderingHeuristics.UnifyDeepestWeightNewestbindingsValOH;
import hybridDomainParsing.ProblemParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.utility.logging.MetaCSPLogging;
import planner.CHIMP;
import planner.CHIMPProblem;

import java.io.IOException;
import java.util.logging.Level;

public class TestDomainReader {

    public static void main(String[] args) {

        String testDomain = "(HybridHTNDomain testDomain) (MaxArgs 5) "+
        		"\r\n" + 
        		"(PredicateSymbols On RobotAt Holding HasArmPosture HasTorsoPosture\r\n" + 
        		"  Connected Type\r\n" + 
        		"  !move_base !move_base_blind !place_object !pick_up_object\r\n" + 
        		"  !move_arm_to_side !move_arms_to_carryposture !tuck_arms !move_torso\r\n" + 
        		"  !observe_objects_on_area\r\n" + 
        		"  adapt_torso torso_assume_driving_pose adapt_arms arms_assume_driving_pose\r\n" + 
        		"  drive_robot move_both_arms_to_side assume_manipulation_pose\r\n" + 
        		"  leave_manipulation_pose grasp_object get_object put_object\r\n" + 
        		"  move_object serve_coffee_to_guest arm_to_side\r\n" + 
        		"  serve_coffee_to_guest_test assume_manipulation_pose_wrapper\r\n" + 
        		"  not_test\r\n" + 
        		"  Future\r\n" + 
        		"  Eternity)\r\n" + 
                " (StateVariable sv 1 n)" +
                " (Resource resource1 7)" +
                " (:operator 3\n" +
                " (Head !move_base(?toArea))\n" +
                " (Pre p1 RobotAt(?fromArea))\n" +
                " (Constraint OverlappedBy(task,p1))\n" +
                " (Constraint Duration[4000,INF](task))\n" +
                " (Add e1 RobotAt(?toArea))\n" +
                " (Constraint Meets(task,e1))\n" +
                " (Del p1)\n" +
                " (SC (During,During) (?obj,?toArea)) \n"+
                " (ResourceUsage navigationCapacity 1)\n" +
                " (ResourceUsage\n" +
                "  (Usage leftArm1ManCapacity 1)\n" +
                "  (Param 2 leftArm1))\n" +
                ")";

        String problemFile = "problems/test_m_serve_coffee_problem_1.pdl";
        //String domainFile = "domains/ordered_domain.ddl";
        ValueOrderingH valOH = new UnifyDeepestWeightNewestbindingsValOH();

        ChimpClassicLexer lexer = new ChimpClassicLexer(CharStreams.fromString(testDomain));

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ChimpClassicParser parser = new ChimpClassicParser(tokens);

        try {
            CHIMPProblem problem = new ProblemParser(problemFile);

            ChimpClassicReader visitor = new ChimpClassicReader(problem.getTypesInstancesMap());
            ChimpClassicReader.ParsedDomain domain = visitor.visitDomain(parser.domain());
            System.out.println(domain.toString());


//            CHIMP.CHIMPBuilder builder = new CHIMP.CHIMPBuilder(domainFile, problemFile)
            CHIMP.CHIMPBuilder builder = new CHIMP.CHIMPBuilder(domain, problem)
                    .valHeuristic(valOH)
                    .mbEstimator(new LookUpTableDurationEstimator())
                    .htnUnification(true);
            CHIMP chimp = builder.build();

            MetaCSPLogging.setLevel(HTNPlanner.class, Level.FINEST);
            MetaCSPLogging.setLevel(HTNMetaConstraint.class, Level.FINEST);

            System.out.println("Found plan? " + chimp.generatePlan());
            chimp.printStats(System.out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static FluentNetworkSolver createDummyFluentNetworkSolver() {
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
        FluentNetworkSolver groundSolver = new FluentNetworkSolver(0, 500, symbols, new int[] {1,1,1,1,1});
        return groundSolver;
    }

}
