package edu.buffalo.cse.blueseal.networkflow.intraprocedural;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;

import soot.G;
import soot.SootMethod;
import soot.Unit;
import soot.toolkits.scalar.ArraySparseSet;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowBackwardFlowAnalysis;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowInterProceduralMain;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowInterproceduralAnalysis;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.UnitWrapper;
import edu.buffalo.cse.blueseal.networkflow.summary.ApkAnalysisSummary;
import edu.buffalo.cse.blueseal.networkflow.summary.StringExtractionUtils;
import edu.buffalo.cse.blueseal.networkflow.summary.HtmlSummaryGenerator;
import edu.uci.ics.jung.graph.DelegateTree;

public class TaintDroidTest {

	// To integrate R
	//http://blog.comsysto.com/2013/07/10/java-r-integration-with-jri-for-on-demand-predictions/
	
	static Logger logger = Logger.getLogger(TaintDroidTest.class);
	
	@Test
	@Ignore 
	public void testTaintDroidApplication() throws ClassNotFoundException {
		//Others are: 
		//com.antivirus-1 
		//com.bestbuy.android-1 
		//com.google.android.gms-1
		String[] apkNames = new String[]{
				//"bbc.mobile.news.ww-1.apk", 
				//"com.antivirus-1.apk", 
				//"com.aol.mobile.moviefone-1.apk", 
				//"com.apksoftware.compass-1.apk",
				//"com.berobo.android.scanner-1.apk",
				//"com.bestbuy.android-1.apk", 
				//"com.ceen.mangaviewer-1.apk",
				"com.chris.android.mydaysfree-1.apk"
				//"com.couponclipper.SavvyShopper-1.apk"
				//"com.google.android.gms-1.apk"
				};
		for (int i = 0; i < apkNames.length; i++) {
			G.reset();
			long startTime = System.nanoTime();
			String[] args = new String[]{"C:\\blueseal\\src\\Examples\\Taintdroid_APKs\\" + apkNames[i], "resources/log4j.xml"};
			NetworkFlowInterProceduralMain.main(args);
			Map<SootMethod, Map<Unit, ArraySparseSet>> sums = NetworkFlowInterproceduralAnalysis.getSummaries();
			Set<SootMethod> methods = sums.keySet();
			int globalIn = 0;
			for (SootMethod sootMethod : methods) {
				Map<Unit, ArraySparseSet> unitsToASS = sums.get(sootMethod);
				assertNotNull("Map<Unit, ArraySparseSet> null when should have been initialized.", unitsToASS);
				assertTrue("There were no Units present in Map<Unit, ArraySparseSet>", unitsToASS.keySet().size() > 0);
				Set<Unit> units = unitsToASS.keySet();
				for (Unit unit : units) {
					if(unitsToASS.get(unit).size() > 0){
						globalIn++;
					}					
				}
			}
			
			Map<SootMethod, ArrayList<DelegateTree<UnitWrapper,String>>> sumsTot = NetworkFlowInterproceduralAnalysis.getGraphSummaries();

//			for (SootMethod sootMethod : methods) {
//				ArrayList<DelegateTree<Unit,String>> delegateTrees= sumsTot.get(sootMethod);
//				ArrayList<DelegateTree<Unit,String>> stringDelegateTrees = GraphPrinter.findStringsConservative(delegateTrees);				
//				for (DelegateTree<Unit, String> delegateTree : stringDelegateTrees){
//					ArrayList<String> strings = GraphPrinter.harvestString(delegateTree);
//					for (String string : strings) {
//						logger.debug("----------------------------");
//						logger.debug("found string of length: " + string.split("\r\n").length + "   \r\n" +
//								string);
//						logger.debug("----------------------------");
//					}
//				}
//			}

			for (SootMethod sootMethod : methods) {
				ArrayList<DelegateTree<UnitWrapper,String>> delegateTrees= sumsTot.get(sootMethod);
				ArrayList<DelegateTree<UnitWrapper,String>> stringDelegateTrees = StringExtractionUtils.findStringsConservative(delegateTrees);				
				for (DelegateTree<UnitWrapper, String> delegateTree : stringDelegateTrees){
					ArrayList<String> strings = StringExtractionUtils.harvestString(delegateTree);
					for (String string : strings) {
						logger.debug(string);
					}
				}
			}

			
			//assertTrue("We do not have the correct number of anticipated String graphs, have: " + sumsOfStrings.size(), sumsOfStrings.size() == 5);

			
			logger.debug("Have this many in gloablIn " + globalIn + " out of a possible " + methods.size());
			System.out.println("Have this many in gloablIn " + globalIn + " out of a possible " + methods.size());
			assertTrue("We did not identify a globalInset", globalIn > 10);
			long endTime = System.nanoTime();
			long duration = (endTime - startTime); 		
			System.out.println("Duration is: " + (duration/1000000000.0));
			logger.debug("Have this many successes: " + NetworkFlowInterproceduralAnalysis.successes);
			Map<SootMethod, ArrayList<DelegateTree<UnitWrapper,String>>> graphSummaries = NetworkFlowInterproceduralAnalysis.getGraphSummaries();
			ArrayList<DelegateTree<UnitWrapper,String>> stringDelegateTrees = StringExtractionUtils.findStringsConservative(graphSummaries);
			logger.debug("Number of strings is: " + stringDelegateTrees.size());
			assertTrue("Number of string bearing trees is: " + stringDelegateTrees.size(), stringDelegateTrees.size() > 600);
			
			//assertTrue("Have exceptions in TaintDroid app when should not have: " + NetworkFlowInterproceduralAnalysis.exceptions.size(), NetworkFlowInterproceduralAnalysis.exceptions.size() == 0);
			HtmlSummaryGenerator.registerApkForAnalysis(
					new ApkAnalysisSummary(args[0], 
					NetworkFlowInterproceduralAnalysis.getSummaries(), 
					NetworkFlowInterproceduralAnalysis.getGraphSummaries(),
					NetworkFlowInterproceduralAnalysis.getEugMap(),
					NetworkFlowBackwardFlowAnalysis.getClassFieldAssignedValues(),
					duration));
		}
		
		HtmlSummaryGenerator.writeHtmlSummaryFilesForAnalysis();
	} 
}
