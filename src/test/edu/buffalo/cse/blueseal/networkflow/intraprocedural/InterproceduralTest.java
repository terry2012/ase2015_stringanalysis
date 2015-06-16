package edu.buffalo.cse.blueseal.networkflow.intraprocedural;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import soot.G;
import soot.PatchingChain;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JReturnStmt;
import soot.toolkits.scalar.ArraySparseSet;

import com.thoughtworks.xstream.XStream;

import edu.buffalo.cse.blueseal.BSFlow.BSInterproceduralAnalysis;
import edu.buffalo.cse.blueseal.blueseal.main;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowInterProceduralMain;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowInterproceduralAnalysis;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.UnitValue;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.UnitWrapper;
import edu.buffalo.cse.blueseal.networkflow.summary.StringExtractionUtils;
import edu.uci.ics.jung.graph.DelegateTree;

public class InterproceduralTest {
	
	static Logger logger = Logger.getLogger(InterproceduralTest.class);

	
	@Test
	@Ignore
	public void testApkAnalysisSummary() throws ClassNotFoundException {
		fail("Not implemented");
	}
	
	@Test
	public void testReturnMethodNoTrace() throws ClassNotFoundException {
		G.reset();
		String[] args = new String[]{"C:\\apk\\FirstApplication.apk", "unit_test_output","unitTestAPK","resources/log4j.xml"};
		NetworkFlowInterProceduralMain.main(args);
		Map<SootMethod, Map<Unit, ArraySparseSet>> sums = NetworkFlowInterproceduralAnalysis.getSummaries();
		Set<SootMethod> methods = sums.keySet();
		for (SootMethod sootMethod : methods) {
			if(sootMethod.getName().contains("returnMethodWithValue(")){
				Map<Unit, ArraySparseSet> unitsToASS = sums.get(sootMethod);
				assertNotNull("Map<Unit, ArraySparseSet> null when should have been initialized.", unitsToASS);
				assertTrue("There were no Units present in Map<Unit, ArraySparseSet>", unitsToASS.keySet().size() > 0);
				Set<Unit> units = unitsToASS.keySet();
				for (Unit unit : units) {
					assertTrue("All should be empty as there is NO outset to a class w/in pkg", unitsToASS.get(unit).size() == 0);					
				}				
			}
		}
	}

	@Test
	public void testReturnMethodWithTraceSameClass() throws ClassNotFoundException {
		G.reset();
		String[] args = new String[]{"C:\\apk\\FirstApplication.apk", "unit_test_output","unitTestAPK","resources/log4j.xml"};
		NetworkFlowInterProceduralMain.main(args);
		Map<SootMethod, Map<Unit, ArraySparseSet>> sums = NetworkFlowInterproceduralAnalysis.getSummaries();
		Set<SootMethod> methods = sums.keySet();
		for (SootMethod sootMethod : methods) {
			if(sootMethod.getName().contains("returnMethodWithValue(")){
				Map<Unit, ArraySparseSet> unitsToASS = sums.get(sootMethod);
				assertNotNull("Map<Unit, ArraySparseSet> null when should have been initialized.", unitsToASS);
				assertTrue("There were no Units present in Map<Unit, ArraySparseSet>", unitsToASS.keySet().size() > 0);
				Set<Unit> units = unitsToASS.keySet();
				boolean hasGlobalInset = false;
				for (Unit unit : units) {
					if(unitsToASS.get(unit).size() > 0){
						hasGlobalInset = true;
					}					
				}
				assertTrue("We did not identify a globalInset", hasGlobalInset);
			}				
		}
	} 
	
	@Test
	public void testMultipleReturnMethodWithTraceSameClass() throws ClassNotFoundException {
		G.reset();
		String[] args = new String[]{"C:\\apk\\FirstApplication.apk","unit_test_output","unitTestAPK", "resources/log4j.xml"};
		NetworkFlowInterProceduralMain.main(args);
		Map<SootMethod, ArrayList<DelegateTree<UnitWrapper,String>>> sums = NetworkFlowInterproceduralAnalysis.getGraphSummaries();
		Set<SootMethod> methods = sums.keySet();
		for (SootMethod sootMethod : methods) {
			if(sootMethod.getName().contains("onCreate") && sootMethod.getDeclaringClass().toString().equals("jmdv.cse586.MainActivity")){
				ArrayList<DelegateTree<UnitWrapper,String>> graph = sums.get(sootMethod);
				assertTrue("Should be at least 2 or more graphs present - one for each sting created but size is: " + graph.size(), graph.size() >= 2);
			}				
		}
	} 

	
	@Test
	public void testGraphConnectedBetweenMethods() throws ClassNotFoundException {
		G.reset();
		//ReturnMethodWithTraceSameClass
		String[] args = new String[]{"C:\\apk\\FirstApplication.apk", "unit_test_output","unitTestAPK","resources/log4j.xml"};
		NetworkFlowInterProceduralMain.main(args);
		Map<SootMethod, ArrayList<DelegateTree<UnitWrapper,String>>> sums = NetworkFlowInterproceduralAnalysis.getGraphSummaries();
		Set<SootMethod> methods = sums.keySet();
		for (SootMethod sootMethod : methods) {
			if(sootMethod.getName().contains("onCreate") && sootMethod.getDeclaringClass().toString().equals("jmdv.cse586.MainActivity")){
				logger.debug("Graphs size is: " + sums.get(sootMethod).size());
				boolean foundTargetGraph = false;
				for(DelegateTree<UnitWrapper,String> graph : sums.get(sootMethod)){
					logger.debug("About to print graph");
					logger.debug(graph);
					logger.debug(graph.getRoot());
					logger.debug("Graph size is: " + graph.getVertexCount());
					assertNotNull("Graph is null when should not be.", graph);
					if(graph.getVertexCount() == 10){
						foundTargetGraph = true;
					}
				}
				assertTrue("We did not get the expected vertex count to prove connected graphs", foundTargetGraph);
			}				
		}
	} 

	@Test
	public void testMultipleReturnMethodWithTraceSameClassDeepNesting() throws ClassNotFoundException {
		G.reset();
		String[] args = new String[]{"C:\\apk\\FirstApplication.apk","unit_test_output","unitTestAPK", "resources/log4j.xml"};
		NetworkFlowInterProceduralMain.main(args);
		Map<SootMethod, ArrayList<DelegateTree<UnitWrapper,String>>> sums = NetworkFlowInterproceduralAnalysis.getGraphSummaries();
		Set<SootMethod> methods = sums.keySet();
		for (SootMethod sootMethod : methods) {

			if(sootMethod.getName().contains("returnMethodWithValueCalledTwiceV4")){
				ArrayList<DelegateTree<UnitWrapper,String>> graphs = sums.get(sootMethod);
				assertTrue("Should be 3 graphs present: " + graphs.size(), graphs.size() == 2);
				boolean foundRightSizedGraph = false;
				for(DelegateTree<UnitWrapper,String> graph : graphs){
					StringExtractionUtils.prettyPrint(graph, sootMethod);
					int graphSize = graph.getVertexCount();
					logger.debug("Target graph size is: " + graphSize);
					if(graphSize > 6){
						foundRightSizedGraph = true;
					}
					logger.debug("Cummualtive graph size is: " + graphSize);
				}
				assertTrue("We have the nested graph we sought with size ", foundRightSizedGraph);				
			}
		}

		for (SootMethod sootMethod : methods) {

			if(sootMethod.getName().contains("returnMethodWithValueCalledTwiceV3")){
				logger.debug("On to returnMethodWithValueCalledTwiceV3");
				ArrayList<DelegateTree<UnitWrapper,String>> graphs = sums.get(sootMethod);
				boolean foundRightSizedGraph = false;
				for(DelegateTree<UnitWrapper,String> graph : graphs){
					StringExtractionUtils.prettyPrint(graph, sootMethod);
					int graphSize = graph.getVertexCount();
					logger.debug("Target graph size is: " + graphSize);
					if(graphSize > 15){
						foundRightSizedGraph = true;
					}
				}
				assertTrue("We have the nested graph we sought with size ", foundRightSizedGraph);				
			}
		}
			
		for (SootMethod sootMethod : methods) {

			if(sootMethod.getName().contains("returnMethodWithValueCalledTwiceV2")){
				logger.debug("On to returnMethodWithValueCalledTwiceV2");
				ArrayList<DelegateTree<UnitWrapper,String>> graphs = sums.get(sootMethod);
				boolean foundRightSizedGraph = false;
				for(DelegateTree<UnitWrapper,String> graph : graphs){
					StringExtractionUtils.prettyPrint(graph, sootMethod);
					int graphSize = graph.getVertexCount();
					logger.debug("Target graph size is: " + graphSize);

					if(graphSize > 24){
						foundRightSizedGraph = true;
					}
					logger.debug("Cummualtive graph size is: " + graphSize);
				}
				assertTrue("We have the nested graph we sought with size ", foundRightSizedGraph);				
			}
		}
	} 

	
	@Test
	public void testReturnMethodWithoutTraceRequired() throws ClassNotFoundException {
		G.reset();
		String[] args = new String[]{"C:\\apk\\FirstApplication.apk", "unit_test_output","unitTestAPK","resources/log4j.xml"};
		NetworkFlowInterProceduralMain.main(args);
		Map<SootMethod, ArrayList<DelegateTree<UnitWrapper,String>>> sums = NetworkFlowInterproceduralAnalysis.getGraphSummaries();
		Set<SootMethod> methods = sums.keySet();
		for (SootMethod sootMethod : methods) {

			if(sootMethod.getName().contains("onCreate") && sootMethod.getDeclaringClass().toString().equals("jmdv.cse586.MainActivity")){
				ArrayList<DelegateTree<UnitWrapper,String>> graphs = sums.get(sootMethod);
				boolean foundTargetGraph = false;
				for(DelegateTree<UnitWrapper,String> graph : graphs){
					StringExtractionUtils.prettyPrint(graph, sootMethod);
					int graphSize = graph.getVertexCount();
					logger.debug("Cummualtive graph size is: " + graphSize);
					if(graph.getVertexCount() == 76){
						foundTargetGraph = true;
					}
				}
				assertTrue("We did not get the expected vertex count to prove connected graphs", foundTargetGraph);
			}
		}
	}

	
	@Test
	@Ignore
	public void testParameterConnectionPassedIn() throws ClassNotFoundException {
		fail("Test and make sure this works.");
	}
	
}
