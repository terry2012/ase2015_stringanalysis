package edu.buffalo.cse.blueseal.networkflow.intraprocedural;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

import soot.G;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowBackwardFlowAnalysis;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowInterProceduralMain;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowInterproceduralAnalysis;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.SootTimer;
import edu.buffalo.cse.blueseal.networkflow.summary.ApkAnalysisSummary;
import edu.buffalo.cse.blueseal.networkflow.summary.HtmlSummaryGenerator;

public class TestMalGnome {

	//public static String TEST_DIR = "C:\\malgenome\\MalGnome_APKS_Less_100k\\";
	//public static String TEST_DIR = "C:\\malgenome\\MalGnome_APKS_100-199\\";
	//public static String TEST_DIR = "C:\\boa_apk\\";
	public static String TEST_DIR = "C:\\malgenome\\MalGnome_APKs\\";
	//public static String TEST_DIR = "/Users/justindelvecchio/blueseal/malgenome/MalGnome_APKs/less100/";
	//public static String TEST_DIR = "C:\\malgenome\\MalGnome_APKS_100-199\\";
	//public static String TEST_DIR = "C:\\malgenome\\MalGnome_APKS_200-299\\";
	//public static String TEST_DIR = "C:\\malgenome\\MalGnome_APKS_300-399\\";
	//public static String TEST_DIR = "C:\\malgenome\\MalGnome_APKS_400-499\\";
	//public static String TEST_DIR = "C:\\malgenome\\MalGnome_APKS_500-599\\";
	//public static String TEST_DIR = "C:\\malgenome\\MalGnome_APKS_600-799\\";

	//public static String TEST_DIR = "C:\\playstore_apk\\playstoreless100k\\";
	//public static String TEST_DIR = "C:\\playstore_apk\\playstore100k\\";
	//public static String TEST_DIR = "C:\\playstore_apk\\playstore200k\\";
	//public static String TEST_DIR = "C:\\playstore_apk\\playstore300k\\";
	//public static String TEST_DIR = "C:\\playstore_apk\\playstore400k\\";
	//public static String TEST_DIR = "C:\\playstore_apk\\playstore500k\\";
	//public static String TEST_DIR = "C:\\malgenome\\MalGnome_APKS_Less_500k\\";
	
	@Test
	//@Ignore
	public void testMalgnomeLess100K() {
		//Here is a change
		File malgnomeDir = new File(TEST_DIR);
		String[] fileNames = malgnomeDir.list();
		
		for (int i = 0; i < 1; i++) {
				// in 100-199
				//if(fileNames[i].contains("696287b89f9c01178db1adc00bca09d7f730b254")){
				//if(fileNames[i].contains("1a613334f30005925d95bbfd845caa065a15682b")){
					G.reset();
					SootTimer.startTimer();
					String[] args = new String[]{TEST_DIR + fileNames[i], "TEST", fileNames[i]};
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
				//}
		}
		HtmlSummaryGenerator.writeHtmlSummaryFilesForAnalysis();
	}
	
}
