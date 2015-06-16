package edu.buffalo.cse.blueseal.networkflow.intraprocedural;

import java.io.File;

import org.junit.Test;

import soot.G;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowBackwardFlowAnalysis;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowInterProceduralMain;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowInterproceduralAnalysis;
import edu.buffalo.cse.blueseal.networkflow.summary.ApkAnalysisSummary;
import edu.buffalo.cse.blueseal.networkflow.summary.HtmlSummaryGenerator;

public class Driver {
	
	public static void main (String[] args){
		G.reset();
		String[] sootArgs = new String[]{args[0], args[1], args[2]};
		long startTime = System.nanoTime();
		NetworkFlowInterProceduralMain.main(sootArgs);
		long endTime = System.nanoTime();
		ApkAnalysisSummary apkAnalysisSummary = new ApkAnalysisSummary(
				args[0], 
				NetworkFlowInterproceduralAnalysis.getSummaries(), 
				NetworkFlowInterproceduralAnalysis.getGraphSummaries(),
				NetworkFlowInterproceduralAnalysis.getEugMap(),
				NetworkFlowBackwardFlowAnalysis.getClassFieldAssignedValues(),
				endTime - startTime);
		HtmlSummaryGenerator.registerApkForAnalysis(apkAnalysisSummary);												
		HtmlSummaryGenerator.writeHtmlSummaryFilesForAnalysis();
	}
	
}
