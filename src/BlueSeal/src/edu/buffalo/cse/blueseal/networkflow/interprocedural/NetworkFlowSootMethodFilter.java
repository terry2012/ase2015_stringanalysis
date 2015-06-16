package edu.buffalo.cse.blueseal.networkflow.interprocedural;

import org.apache.log4j.Logger;

import soot.SootMethod;
import soot.jimple.toolkits.annotation.purity.SootMethodFilter;
import soot.jimple.toolkits.callgraph.EdgePredicate;

public class NetworkFlowSootMethodFilter implements SootMethodFilter {

	static Logger logger = Logger.getLogger(NetworkFlowSootMethodFilter.class);
	
	public boolean want(SootMethod m){
		return true;
		//		return m.getDeclaringClass().isApplicationClass()&&
//				m.hasActiveBody();
	}

}
