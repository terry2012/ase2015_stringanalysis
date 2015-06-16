package edu.buffalo.cse.blueseal.networkflow.intraprocedural;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Test;

import soot.G;
import soot.SootMethod;
import soot.Unit;
import soot.dava.internal.javaRep.DLengthExpr;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowInterProceduralMain;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowInterproceduralAnalysis;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.UnitWrapper;
import edu.buffalo.cse.blueseal.networkflow.summary.StringExtractionUtils;
import edu.uci.ics.jung.graph.DelegateTree;

public class GraphPrinterTest {

	static Logger logger = Logger.getLogger(GraphPrinterTest.class);
	
	@Test
	public void testStringDetectionConservative() throws ClassNotFoundException {
		G.reset();
		String[] args = new String[]{"C:\\apk\\FirstApplication.apk", "unit_test_output","unitTestAPK","resources/log4j.xml"};
		//String[] args = new String[]{"C:\\apk\\ReturnMethodWithTraceSameClass.apk", "resources/log4j.xml"};
		NetworkFlowInterProceduralMain.main(args);
		Map<SootMethod, ArrayList<DelegateTree<UnitWrapper,String>>> sums = NetworkFlowInterproceduralAnalysis.getGraphSummaries();
		Set<SootMethod> methods = sums.keySet();
		for (SootMethod sootMethod : methods) {
			if(sootMethod.getName().contains("createsMultipleString(")){
				testStringCreation(sums, sootMethod, 0);
			}
			if(sootMethod.getName().contains("createsMultipleString1")){
				testStringCreation(sums, sootMethod, 3);
			}
			if(sootMethod.getName().contains("createsMultipleString2")){
				testStringCreation(sums, sootMethod, 3);
			}
			if(sootMethod.getName().contains("createsMultipleString3")){
				testStringCreation(sums, sootMethod, 2);
			}
			
		}
	}

	private void testStringCreation(
			Map<SootMethod, ArrayList<DelegateTree<UnitWrapper, String>>> sums,
			SootMethod sootMethod, int expected) {
		logger.debug("In sootmethods itr " + sootMethod.getName());
		ArrayList<DelegateTree<UnitWrapper,String>> delegateTrees= sums.get(sootMethod);
		ArrayList<DelegateTree<UnitWrapper,String>> stringDelegateTrees = StringExtractionUtils.findStringsConservative(delegateTrees);
		for (DelegateTree<UnitWrapper, String> delegateTree : stringDelegateTrees) {
			StringExtractionUtils.prettyPrint(delegateTree, sootMethod);
			logger.debug("--------------------------------------------------");
			
		}
		
		assertTrue("Found incorrect number of Strings for a conservative approach with " + stringDelegateTrees.size() + " when expected " + expected, stringDelegateTrees.size() == expected);
	}

	@Test
	public void testStringCreationCountsPerApplicationConservative() {
		G.reset();
		String[] args = new String[]{"C:\\apk\\FirstApplication.apk", "unit_test_output","unitTestAPK","resources/log4j.xml"};
		//String[] args = new String[]{"C:\\apk\\ReturnMethodWithTraceSameClass.apk", "resources/log4j.xml"};
		NetworkFlowInterProceduralMain.main(args);
		Map<SootMethod, ArrayList<DelegateTree<UnitWrapper,String>>> sums = NetworkFlowInterproceduralAnalysis.getGraphSummaries();
		ArrayList<DelegateTree<UnitWrapper,String>> stringDelegateTrees = StringExtractionUtils.findStringsConservative(sums);
		logger.debug("Number of strings is: " + stringDelegateTrees.size());
		assertTrue("Number of string bearing trees is: " + stringDelegateTrees.size(), stringDelegateTrees.size() > 10);
		
	}

	@Test
	public void testStringCreationAsSingleToken() {
		G.reset();
		String[] args = new String[]{"C:\\apk\\FirstApplication.apk", "unit_test_output","unitTestAPK","resources/log4j.xml"};
		NetworkFlowInterProceduralMain.main(args);
		Map<SootMethod, ArrayList<DelegateTree<UnitWrapper,String>>> sums = NetworkFlowInterproceduralAnalysis.getGraphSummaries();
		Set<SootMethod> methods = sums.keySet();

		for (SootMethod sootMethod : methods) {
			if(sootMethod.getName().contains("createsMultipleString1")){
				testStringCreation(sums, sootMethod);
			}
		}
	}

	private void testStringCreation(
			Map<SootMethod, ArrayList<DelegateTree<UnitWrapper, String>>> sums,
			SootMethod sootMethod) {
		logger.debug("In sootmethods itr " + sootMethod.getName());
		ArrayList<DelegateTree<UnitWrapper,String>> delegateTrees= sums.get(sootMethod);
		ArrayList<DelegateTree<UnitWrapper,String>> stringDelegateTrees = StringExtractionUtils.findStringsConservative(delegateTrees);
		for (DelegateTree<UnitWrapper, String> delegateTree : stringDelegateTrees){
			ArrayList<String> strings = StringExtractionUtils.harvestString(delegateTree);
			for (String string : strings) {
				logger.debug("----------------------------");
				logger.debug("found string of length: " + string.split("\r\n").length + "   \r\n" +
						string);
				logger.debug("----------------------------");
			}
		}
	}

	
}
