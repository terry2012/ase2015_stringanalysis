package edu.buffalo.cse.blueseal.networkflow.intraprocedural;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Test;

import soot.G;
import soot.SootMethod;
import soot.Unit;
import soot.toolkits.scalar.ArraySparseSet;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowInterProceduralMain;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowInterproceduralAnalysis;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.UnitValue;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.UnitWrapper;
import edu.buffalo.cse.blueseal.networkflow.summary.StringExtractionUtils;
import edu.uci.ics.jung.graph.DelegateTree;

public class IntraproceduralTest {

	static Logger logger = Logger.getLogger(IntraproceduralTest.class);
	public String APK_LOCATIONS = "/Users/justindelvecchio/";
	
	
	@Test
	public void testIntraproceduralSummaryNotNull() throws ClassNotFoundException {
		G.reset();
		String[] args = new String[]{"C:\\apk\\FirstApplication.apk","unit_test_output","unitTestAPK","resources/log4j.xml"};
		NetworkFlowInterProceduralMain.main(args);
		assertNotNull("Intraprocedural summaries is null.", NetworkFlowInterproceduralAnalysis.getSummaries());
		assertTrue("Intraprocedural summaries is empty.", NetworkFlowInterproceduralAnalysis.getSummaries().size() > 0);
		Map<SootMethod, Map<Unit, ArraySparseSet>> sums = NetworkFlowInterproceduralAnalysis.getSummaries();
		Set<SootMethod> methods = sums.keySet();
		for (SootMethod sootMethod : methods) {
			Map<Unit, ArraySparseSet> unitsToASS = sums.get(sootMethod);
			assertNotNull("Map<Unit, ArraySparseSet> null when should have been initialized.", unitsToASS);
			assertTrue("There were no Units present in Map<Unit, ArraySparseSet>", unitsToASS.keySet().size() > 0);
		}
	}

	@Test
	public void testOutsetCaptured() throws ClassNotFoundException {
		G.reset();
		String[] args = new String[]{"C:\\apk\\FirstApplication.apk", "unit_test_output","unitTestAPK","resources/log4j.xml"};
		NetworkFlowInterProceduralMain.main(args);
		Map<SootMethod, ArrayList<DelegateTree<UnitWrapper,String>>> sums = NetworkFlowInterproceduralAnalysis.getGraphSummaries();
		Set<SootMethod> methods = sums.keySet();
		for (SootMethod sootMethod : methods) {
			if(sootMethod.getName().contains("onCreate") && sootMethod.getDeclaringClass().toString().equals("jmdv.cse586.MainActivity")){
				for(DelegateTree<UnitWrapper,String> graph : sums.get(sootMethod)){
					assertNotNull("Graph is null when should not be.", graph);
					assertTrue("The graph is empty when should not be", graph.getVertexCount() > 0);
					logger.debug("Size of vertex is: " + graph.getVertexCount());					
				}
			}				
		}
	} 
	
	@Test
	public void testUnitValueEqualsMethod(){
		G.reset();
		String[] args = new String[]{"C:\\apk\\FirstApplication.apk", "unit_test_output","unitTestAPK","resources/log4j.xml"};
		NetworkFlowInterProceduralMain.main(args);
		Map<SootMethod, ArrayList<DelegateTree<UnitWrapper,String>>> sums = NetworkFlowInterproceduralAnalysis.getGraphSummaries();
		Set<SootMethod> methods = sums.keySet();
		for (SootMethod sootMethod : methods) {
			if(sootMethod.getName().contains("onCreate") && sootMethod.getDeclaringClass().toString().equals("jmdv.cse586.MainActivity")){
				for(DelegateTree<UnitWrapper,String> graph : sums.get(sootMethod)){
					Collection<UnitWrapper> units = graph.getVertices();
					for(UnitWrapper unit : units){
						UnitValue unitValue1 = new UnitValue(unit, unit.getUnit().getUseBoxes().get(0).getValue());
						UnitValue unitValue2 = new UnitValue(unit, unit.getUnit().getUseBoxes().get(0).getValue());
						assertTrue(unitValue1.equals(unitValue2));
					}
					if(units.size() > 1){
						logger.debug("In assert not equals case");
						Unit unit1 = (Unit)units.toArray()[0];
						Unit unit2 = (Unit)units.toArray()[1];
						UnitValue unitValue1 = new UnitValue(UnitWrapper.getSingleArgumentOrLessUnitWrapper(unit1, sootMethod), unit1.getUseBoxes().get(0).getValue());
						UnitValue unitValue2 = new UnitValue(UnitWrapper.getSingleArgumentOrLessUnitWrapper(unit2, sootMethod), unit2.getUseBoxes().get(0).getValue());
						assertTrue(! unitValue1.equals(unitValue2));
						
					}
				}
			}				
		}

	}
	
	@Test
	public void testIntraproceduralGraphSizeIsCorrect(){
		//returnMethodWithValue
		G.reset();
		String[] args = new String[]{"C:\\apk\\FirstApplication.apk", "unit_test_output","unitTestAPK","resources/log4j.xml"};
		NetworkFlowInterProceduralMain.main(args);
		Map<SootMethod, ArrayList<DelegateTree<UnitWrapper,String>>> sums = NetworkFlowInterproceduralAnalysis.getGraphSummaries();
		Set<SootMethod> methods = sums.keySet();
		for (SootMethod sootMethod : methods) {
			if(sootMethod.getName().contains("returnMethodWithValue(")){
				ArrayList<DelegateTree<UnitWrapper,String>> graphs = sums.get(sootMethod);
				for(DelegateTree<UnitWrapper,String> graph : graphs){
					assertNotNull("Graph is null when should not be.", graph);
					assertTrue("The graph vertex size is not correct " + graph.getVertexCount(), graph.getVertexCount() == 10);
					logger.debug("Size of vertex is: " + graph.getVertexCount());					
				}
			}				
		}
	}
	
	@Test
	public void testGraphCreation() throws ClassNotFoundException {
		G.reset();
		String[] args = new String[]{"C:\\apk\\FirstApplication.apk", "unit_test_output","unitTestAPK","resources/log4j.xml"};
		NetworkFlowInterProceduralMain.main(args);
		Map<SootMethod, ArrayList<DelegateTree<UnitWrapper,String>>> sums = NetworkFlowInterproceduralAnalysis.getGraphSummaries();
		assertNotNull("Graphs are null when should not be", sums);
		assertTrue("The graph is empty",!sums.isEmpty());
		Set<SootMethod> methods = sums.keySet();
		for (SootMethod sootMethod : methods) {
			if(sootMethod.getName().contains("returnMethodWithValue(")){
				for(DelegateTree<UnitWrapper,String> graph : sums.get(sootMethod)){
					int vertexSize = graph.getVertices().size();
					logger.debug("Size is " + vertexSize);
					assertTrue("No vertex were present or not enough where vertex size is: " + vertexSize, vertexSize > 5);
					logger.debug(graph.toString());
				}
			}
		}
	}

	@Test
	public void testGraphCreationCapturesAllPossibleStrings() throws ClassNotFoundException {
		G.reset();
		String[] args = new String[]{"C:\\apk\\FirstApplication.apk", "unit_test_output","unitTestAPK","resources/log4j.xml"};
		//String[] args = new String[]{"C:\\apk\\ReturnMethodWithTraceSameClass.apk", "resources/log4j.xml"};
		NetworkFlowInterProceduralMain.main(args);
		int identifiedStrings = 0;
		Map<SootMethod, ArrayList<DelegateTree<UnitWrapper,String>>> sums = NetworkFlowInterproceduralAnalysis.getGraphSummaries();
		Set<SootMethod> methods = sums.keySet();
		for (SootMethod sootMethod : methods) {
			logger.debug("In sootmethods itr " + sootMethod.getName());
			if(sootMethod.getName().contains("createsMultipleString2")){
				ArrayList<DelegateTree<UnitWrapper,String>> delegateTrees= sums.get(sootMethod);
				assertTrue("There should be 4 DelegateTrees but there are: " + delegateTrees.size(), delegateTrees.size() == 3);
			}
		}

	}

	
	
}
