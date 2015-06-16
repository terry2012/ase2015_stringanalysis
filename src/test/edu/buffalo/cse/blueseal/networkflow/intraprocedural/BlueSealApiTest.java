package edu.buffalo.cse.blueseal.networkflow.intraprocedural;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import soot.G;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowBackwardFlowAnalysis;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowInterProceduralMain;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowInterproceduralAnalysis;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.UnitWrapper;
import edu.buffalo.cse.blueseal.networkflow.summary.ApkAnalysisSummary;
import edu.buffalo.cse.blueseal.networkflow.summary.HtmlSummaryGenerator;

public class BlueSealApiTest {

	static Logger logger = Logger.getLogger(BlueSealApiTest.class);
	
	@Test
	@Ignore
	public void testUrlDetectionObjectBased() throws ClassNotFoundException {
		// TODO
		// 1. Create URL objects in TestApplication
		// DONE!
		// 2. Ensure coverage for multiple constructor types
		// DONE
		// 3. Create API call that accepts the Unit & Method for the URL object and returns the String
		//   3a. Will need to find the graph that houses the Unit (might be tough ... less so if we get the Method)
		//   3b. Will need to trace the graph to find the String object passed in as a parameter ... or rather trace back from it. Or rather find 
		//       the graph that houses it as a root.
		// 4. Send back the String (or ops?) that represent the String.
		// DONE
		G.reset();
		String[] args = new String[]{"C:\\apk\\FirstApplication.apk", "resources/log4j.xml"};
		long startTime = System.nanoTime();
		NetworkFlowInterProceduralMain.main(args);
		long endTime = System.nanoTime();
		ApkAnalysisSummary apkAnalysisSummary = new ApkAnalysisSummary(
				args[0], 
				NetworkFlowInterproceduralAnalysis.getSummaries(), 
				NetworkFlowInterproceduralAnalysis.getGraphSummaries(),
				NetworkFlowInterproceduralAnalysis.getEugMap(),
				NetworkFlowBackwardFlowAnalysis.getClassFieldAssignedValues(),
				endTime - startTime);
		HtmlSummaryGenerator.registerApkForAnalysis(apkAnalysisSummary);
		fail("we need this test");
		//String url = apkAnalysisSummary.getUrlforUnit(null, null);
		//assertTrue("This should be the yahoo url", url.equals("http://www.yahoo.com/"));	
		//createUrlFromStringOneArgConstructor
	}
	
}
