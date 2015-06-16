package edu.buffalo.cse.blueseal.networkflow.intraprocedural;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;

import soot.G;
import soot.SootMethod;
import soot.Unit;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowBackwardFlowAnalysis;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowInterProceduralMain;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowInterproceduralAnalysis;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.UnitWrapper;
import edu.buffalo.cse.blueseal.networkflow.summary.ApkAnalysisSummary;
import edu.buffalo.cse.blueseal.networkflow.summary.HtmlSummaryGenerator;
import edu.uci.ics.jung.graph.DelegateTree;

public class HtmlSummaryTest {

	static Logger logger = Logger.getLogger(HtmlSummaryTest.class);
	
	@Test
	public void testHtmlSummaryCreation() {
		G.reset();
		String[] args = new String[]{
		"C:\\apk\\AndroidFlashcards.apk",
		"unit_test_output",
		"unitTestAPK",
		//"C:\\apk\\FirstApplication.apk",
		//"C:\\apk\\RadioStudent.apk",
		//"C:\\apk\\BlogReader.apk",
		//"C:\\apk\\GpodRoid.apk",
		//"C:\\apk\\SplashActivity.apk",
		//"C:\\apk\\BrowseScriptureActivity.apk",
		//"C:\\apk\\LogoActivity.apk",
		//"C:\\apk\\Video_Activity.apk",
		//"C:\\apk\\Collepi.apk",
		//"C:\\apk\\PicView.apk",
		"resources/log4j.xml"
		};
		//String[] args = new String[]{"C:\\apk\\BlogReader.apk", "resources/log4j.xml"};
		long startTime = System.nanoTime();
		NetworkFlowInterProceduralMain.main(args);
		long endTime = System.nanoTime();
		HtmlSummaryGenerator.registerApkForAnalysis(new ApkAnalysisSummary(
				args[0], 
				NetworkFlowInterproceduralAnalysis.getSummaries(), 
				NetworkFlowInterproceduralAnalysis.getGraphSummaries(),
				NetworkFlowInterproceduralAnalysis.getEugMap(),
				NetworkFlowBackwardFlowAnalysis.getClassFieldAssignedValues(),
				endTime - startTime));
		logger.debug("About to write summary");
		Set<String> stringBearningClasses = NetworkFlowInterproceduralAnalysis.STRING_BEARING_UNITS.keySet();
		for (String stringBearingClass : stringBearningClasses) {
			logger.debug("Srtring bearing class: " + stringBearingClass);
			ArrayList<UnitWrapper> stringBearingUnits = NetworkFlowInterproceduralAnalysis.STRING_BEARING_UNITS.get(stringBearingClass);
			for (UnitWrapper unitWrapper : stringBearingUnits) {
				logger.debug(unitWrapper.toString());
			}
		}
		HtmlSummaryGenerator.writeHtmlSummaryFilesForAnalysis();
	}

	
}
