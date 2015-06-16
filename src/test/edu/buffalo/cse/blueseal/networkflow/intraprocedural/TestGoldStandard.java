package edu.buffalo.cse.blueseal.networkflow.intraprocedural;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;

import soot.G;
import soot.Unit;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowBackwardFlowAnalysis;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowInterProceduralMain;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowInterproceduralAnalysis;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.UnitWrapper;
import edu.buffalo.cse.blueseal.networkflow.summary.ApkAnalysisSummary;
import edu.buffalo.cse.blueseal.networkflow.summary.HtmlSummaryGenerator;
import edu.buffalo.cse.blueseal.networkflow.summary.StringExtractionUtils;
import edu.buffalo.cse.blueseal.networkflow.summary.SootString;
import edu.uci.ics.jung.graph.DelegateTree;

public class TestGoldStandard {

	static Logger logger = Logger.getLogger(TestGoldStandard.class);
	
	@Test
	public void testAndroidFlashcard() {
		G.reset();
		String[] args = new String[]{
		"C:\\apk\\AndroidFlashcards.apk","unit_test_output","unitTestAPK",
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
		logger.debug("About to write summary");
		HtmlSummaryGenerator.writeHtmlSummaryFilesForAnalysis();
		List<SootString> urlStrings =  apkAnalysisSummary.getSootStrings();
		assertTrue("There are not two URL strings " + urlStrings.size(), urlStrings.size() == 4);
		List<String> identifiers = new ArrayList<String>();
		identifiers.add("com.secretsockssoftware.androidflashcards.DownloadableLessonList.run");
		identifiers.add("com.secretsockssoftware.androidflashcards.LessonDownload.doDownload");
		for(SootString urlString : urlStrings){
			DelegateTree<UnitWrapper, String> tree = urlString.getDelegateTree();
			StringExtractionUtils.prettyPrint(tree, urlString.getSootMethod());
			String signature = tree.getRoot().getSootMethod().getDeclaringClass().getName() + "." + tree.getRoot().getSootMethod().getName();
			if(signature.equals("com.secretsockssoftware.androidflashcards.DownloadableLessonList.run")){
				identifiers.remove("com.secretsockssoftware.androidflashcards.DownloadableLessonList.run");
			}
			else if(signature.equals("com.secretsockssoftware.androidflashcards.LessonDownload.doDownload")){
				Collection<UnitWrapper> unitWrappers = tree.getVertices();
				for (UnitWrapper unitWrapper : unitWrappers) {
					signature = unitWrapper.getSootMethod().getDeclaringClass().getName() + "." + unitWrapper.getSootMethod().getName();
					if(signature.equals("com.secretsockssoftware.androidflashcards.LessonDownload.run")){
						identifiers.remove("com.secretsockssoftware.androidflashcards.LessonDownload.doDownload");						
					}
				}
			}
		}
		if(identifiers.size() != 0){
			fail("Did not find the String of interest and size was " + identifiers.size());
		}
	}

	
	@Test
	public void testGpodRoid() {
		G.reset();
		String[] args = new String[]{
		//"C:\\apk\\AndroidFlashcards.apk",
		//"C:\\apk\\FirstApplication.apk",
		//"C:\\apk\\RadioStudent.apk",
		//"C:\\apk\\BlogReader.apk",
		"C:\\apk\\GpodRoid.apk","unit_test_output","unitTestAPK",
		//"C:\\apk\\SplashActivity.apk",
		//"C:\\apk\\BrowseScriptureActivity.apk",
		//"C:\\apk\\LogoActivity.apk",
		//"C:\\apk\\Video_Activity.apk",
		//"C:\\apk\\Collepi.apk",
		//"C:\\apk\\PicView.apk",
		"resources/log4j.xml"
		};
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
		logger.debug("About to write summary");
		HtmlSummaryGenerator.writeHtmlSummaryFilesForAnalysis();
		List<SootString> urlStrings =  apkAnalysisSummary.getSootStrings();
		assertTrue("There are not four URL strings " + urlStrings.size(), urlStrings.size() == 4);
		List<String> identifiers = new ArrayList<String>();
		
		
		//com.unitedcoders.android.gpodroid.GPodderActions.LoadImageFromWebOperations	com.unitedcoders.android.gpodroid.activity.PlayerActivity.onCreate	String
		//com.unitedcoders.android.gpodroid.services.DownloadService.downloadPodcast	com.unitedcoders.android.gpodroid.PodcastElement.getDownloadurl	Class
		//com.unitedcoders.gpodder.GpodderAPI.getDevices			Modified String
		//com.unitedcoders.gpodder.GpodderAPI.getDownloadList			Modified String

		identifiers.add("com.unitedcoders.android.gpodroid.GPodderActions.LoadImageFromWebOperations");
		identifiers.add("com.unitedcoders.android.gpodroid.services.DownloadService$1.run");
		identifiers.add("com.unitedcoders.gpodder.GpodderAPI.getDevices");
		identifiers.add("com.unitedcoders.gpodder.GpodderAPI.getDownloadList");

		for(SootString urlString : urlStrings){
			DelegateTree<UnitWrapper, String> tree = urlString.getDelegateTree();
			StringExtractionUtils.prettyPrint(tree, urlString.getSootMethod());
			String signature = tree.getRoot().getSootMethod().getDeclaringClass().getName() + "." + tree.getRoot().getSootMethod().getName();
			if(signature.equals("com.unitedcoders.android.gpodroid.GPodderActions.LoadImageFromWebOperations")){
				identifiers.remove("com.unitedcoders.android.gpodroid.GPodderActions.LoadImageFromWebOperations");
			}
			else if(signature.equals("com.unitedcoders.android.gpodroid.services.DownloadService$1.run")){
				Collection<UnitWrapper> unitWrappers = tree.getVertices();
				for (UnitWrapper unitWrapper : unitWrappers) {
					signature = unitWrapper.getSootMethod().getDeclaringClass().getName() + "." + unitWrapper.getSootMethod().getName();
					if(signature.equals("com.unitedcoders.android.gpodroid.PodcastElement.getDownloadurl")){
						identifiers.remove("com.unitedcoders.android.gpodroid.services.DownloadService$1.run");						
					}
				}
			}
			else if(signature.equals("com.unitedcoders.gpodder.GpodderAPI.getDevices")){
				identifiers.remove("com.unitedcoders.gpodder.GpodderAPI.getDevices");				
			}
			else if(signature.equals("com.unitedcoders.gpodder.GpodderAPI.getDownloadList")){
				identifiers.remove("com.unitedcoders.gpodder.GpodderAPI.getDownloadList");
				
			}
		}
		if(identifiers.size() != 0){
			fail("Did not find the String of interest and size was " + identifiers.size());
		}
	}


	@Test
	public void testLogoActivity() {
		G.reset();
		String[] args = new String[]{
		//"C:\\apk\\AndroidFlashcards.apk",
		//"C:\\apk\\FirstApplication.apk",
		//"C:\\apk\\RadioStudent.apk",
		//"C:\\apk\\BlogReader.apk",
		//"C:\\apk\\GpodRoid.apk",
		//"C:\\apk\\SplashActivity.apk",
		//"C:\\apk\\BrowseScriptureActivity.apk",
		"C:\\apk\\LogoActivity.apk","unit_test_output","unitTestAPK",
		//"C:\\apk\\Video_Activity.apk",
		//"C:\\apk\\Collepi.apk",
		//"C:\\apk\\PicView.apk",
		"resources/log4j.xml"
		};
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
		logger.debug("About to write summary");
		HtmlSummaryGenerator.writeHtmlSummaryFilesForAnalysis();
		List<SootString> urlStrings =  apkAnalysisSummary.getSootStrings();
		assertTrue("There are not seven URL strings " + urlStrings.size(), urlStrings.size() == 7);
		List<String> identifiers = new ArrayList<String>();
		
		
		//com.unitedcoders.android.gpodroid.GPodderActions.LoadImageFromWebOperations	com.unitedcoders.android.gpodroid.activity.PlayerActivity.onCreate	String
		//com.unitedcoders.android.gpodroid.services.DownloadService.downloadPodcast	com.unitedcoders.android.gpodroid.PodcastElement.getDownloadurl	Class
		//com.unitedcoders.gpodder.GpodderAPI.getDevices			Modified String
		//com.unitedcoders.gpodder.GpodderAPI.getDownloadList			Modified String

		identifiers.add("com.unitedcoders.android.gpodroid.GPodderActions.LoadImageFromWebOperations");
		identifiers.add("com.unitedcoders.android.gpodroid.services.DownloadService$1.run");
		identifiers.add("com.unitedcoders.gpodder.GpodderAPI.getDevices");
		identifiers.add("com.unitedcoders.gpodder.GpodderAPI.getDownloadList");

		for(SootString urlString : urlStrings){
			DelegateTree<UnitWrapper, String> tree = urlString.getDelegateTree();
			StringExtractionUtils.prettyPrint(tree, urlString.getSootMethod());
			String signature = tree.getRoot().getSootMethod().getDeclaringClass().getName() + "." + tree.getRoot().getSootMethod().getName();
			if(signature.equals("com.unitedcoders.android.gpodroid.GPodderActions.LoadImageFromWebOperations")){
				identifiers.remove("com.unitedcoders.android.gpodroid.GPodderActions.LoadImageFromWebOperations");
			}
			else if(signature.equals("com.unitedcoders.android.gpodroid.services.DownloadService$1.run")){
				Collection<UnitWrapper> unitWrappers = tree.getVertices();
				for (UnitWrapper unitWrapper : unitWrappers) {
					signature = unitWrapper.getSootMethod().getDeclaringClass().getName() + "." + unitWrapper.getSootMethod().getName();
					if(signature.equals("com.unitedcoders.android.gpodroid.PodcastElement.getDownloadurl")){
						identifiers.remove("com.unitedcoders.android.gpodroid.services.DownloadService$1.run");						
					}
				}
			}
			else if(signature.equals("com.unitedcoders.gpodder.GpodderAPI.getDevices")){
				identifiers.remove("com.unitedcoders.gpodder.GpodderAPI.getDevices");				
			}
			else if(signature.equals("com.unitedcoders.gpodder.GpodderAPI.getDownloadList")){
				identifiers.remove("com.unitedcoders.gpodder.GpodderAPI.getDownloadList");
				
			}
		}
//		if(identifiers.size() != 0){
//			fail("Did not find the String of interest and size was " + identifiers.size());
//		}
	}

	
}
