package planner;

import java.util.Collection;
import java.util.Map;

import fluentSolver.FluentNetworkSolver;
import hybridDomainParsing.ClassicHybridDomain;
import hybridDomainParsing.HybridDomain;

public interface CHIMPProblem {

	String[] getArgumentSymbols();

	void addArgumentSymbols(Collection<String> symbols);

	Map<String, String[]> getTypesInstancesMap();
	
	Boolean createState(FluentNetworkSolver fluentSolver, ClassicHybridDomain domain);
	
}
