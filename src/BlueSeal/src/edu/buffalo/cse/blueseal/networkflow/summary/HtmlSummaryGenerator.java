package edu.buffalo.cse.blueseal.networkflow.summary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.spi.CharsetProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.*;
import ppg.cmds.NewProdCmd;
import edu.buffalo.cse.blueseal.blueseal.Constants;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowBackwardFlowAnalysis;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowInterProceduralMain;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowInterproceduralAnalysis;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.UnitWrapper;
import edu.buffalo.cse.blueseal.networkflow.intraprocedural.TaintDroidTest;
import edu.uci.ics.jung.graph.DelegateTree;
import soot.G;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.parser.node.APlusBinop;
import soot.toolkits.scalar.ArraySparseSet;
import sun.awt.CharsetString;

public class HtmlSummaryGenerator {
	
	static Logger logger = Logger.getLogger(HtmlSummaryGenerator.class);
	
	private static File summary = new File(Constants.OUTPUT_DIR + File.separator + "summary");
	public static File apkSummaries = new File(Constants.OUTPUT_DIR + File.separator + "summary" + File.separator + "apksummaries");
	private static ArrayList<ApkAnalysisSummary> apkAnalysisSummaryList = new ArrayList<ApkAnalysisSummary>();
	
	public static void writeHtmlSummaryFilesForAnalysis(){
		createHtmlSummaryDirectories();
		PerApkSummaryGenerator apkSummaryGenerator = new PerApkSummaryGenerator(apkAnalysisSummaryList);
		apkSummaryGenerator.addHtmlPerApkResultsPages();
		addHtmlSummaryResultsPage();
		if(ApkAnalysisSummary.URL_METHOD_SIGNATURE_COUNTS.keySet().size() > 0){
			addMethodAnalysisCsv(NetworkFlowInterProceduralMain.APK_NAME+"url_method_analysis.csv", ApkAnalysisSummary.URL_METHOD_SIGNATURE_COUNTS, ApkAnalysisSummary.URL_METHOD_SIGNATURE_SPREAD_COUNTS,
					ApkAnalysisSummary.URL_METHOD_SIGNATURE_VALUES);			
		}
		if(ApkAnalysisSummary.REFLECTION_METHOD_SIGNATURE_COUNTS.size() > 0){
			addMethodAnalysisCsv(NetworkFlowInterProceduralMain.APK_NAME+"refelction_method_analysis.csv", ApkAnalysisSummary.REFLECTION_METHOD_SIGNATURE_COUNTS, ApkAnalysisSummary.REFLECTION_METHOD_SIGNATURE_SPREAD_COUNTS,
					ApkAnalysisSummary.REFLECTION_METHOD_SIGNATURE_VALUES);			
		}
	}

	private static void addMethodAnalysisCsv(String fileName, 
			HashMap<String, Integer> signatureCounts, 
			HashMap<String, Integer> spreadCounts, 
			HashMap<String, ArrayList<String>> signatureValues) {
		CSVWriter csvWriter;
		try {
			csvWriter = new CSVWriter(new FileWriter(Constants.OUTPUT_DIR + File.separator + "summary" + File.separator + fileName), '\t');
			String[] entries = new String[4];
			Set<String> globalMethoSignatures = signatureCounts.keySet();
			for (String methodSignature : globalMethoSignatures) {
				entries[0] = methodSignature;
				entries[1] = signatureCounts.get(methodSignature).toString();
				entries[2] = spreadCounts.get(methodSignature).toString();
				ArrayList<String> matches = signatureValues.get(methodSignature);
				if(matches != null){
					entries[3] = SootString.getMatchesAsStringFromList(matches, true, false);					
				}
				else{
					entries[3] = "";										
				}
				csvWriter.writeNext(entries);
				csvWriter.flush();
			}
			csvWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private static void addHtmlSummaryResultsPage() {
		File file = new File(Constants.OUTPUT_DIR + File.separator + "summary" + File.separator + "summary.html");
		logger.debug(file.getPath());
		try {
			FileUtils.writeStringToFile(file, getOverallSummaryResultsPage(), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getOverallSummaryResultsPage(){
		String summary =  "<!DOCTYPE html><html><head><title>SAC Output Summary</title></head><body>";
		summary += addMethodOverviewSection();
		summary += "</body></html>"; 
		return summary;
	}
	
	private static String addMethodOverviewSection() {

		//style=\"width:100%\"
		StringBuffer methodOverview = new StringBuffer("<table border=\"1\" style=\"border-collapse: collapse;\">");
		methodOverview.append("<tr>");
		// 1
		methodOverview.append("<th>APK Name</th>");
		// 2
		methodOverview.append("<th>Runtime (s)</th>");
		// 3
		methodOverview.append("<th>Number of URLs</th>");
		// 4
		methodOverview.append("<th>Number of Methods</th>");
		// 5
		methodOverview.append("<th>Number of Methods w/ Strings</th>");
		// 6
		methodOverview.append("<th>Intraprocedural Strings</th>");
		// 7
		methodOverview.append("<th>Interprocedural Strings</th>");
		methodOverview.append("</tr>");
		for (ApkAnalysisSummary apkAnalysisSummary : apkAnalysisSummaryList) {
			methodOverview.append("<tr>");
			// 1
			methodOverview.append("<td>");
			//<a href="http://www.w3schools.com/html/">Visit our HTML tutorial</a> 
			String relativePath = apkAnalysisSummary.getRelativeHtmlFileLocation();//.replaceAll(Constants.OUTPUT_DIR + File.separator + "summary" + File.separator, "");//SFGDF
			int location = relativePath.indexOf("apksummaries");
			methodOverview.append("<a href=" + relativePath.substring(location) + ">" + apkAnalysisSummary.getApkName() + "</a>");			
			methodOverview.append("</td>");
			// 2
			methodOverview.append("<td>");
			methodOverview.append(apkAnalysisSummary.getRunTime()/1000000000.0);
			methodOverview.append("</td>");
			// 3
			methodOverview.append("<td>");
			methodOverview.append(apkAnalysisSummary.getSootStrings().size());
			methodOverview.append("</td>");
			// 4
			methodOverview.append("<td>");
			methodOverview.append(apkAnalysisSummary.getSummariesMap().size());
			methodOverview.append("</td>");
			// 5
			methodOverview.append("<td>");
			Map<SootMethod, ArrayList<DelegateTree<UnitWrapper, String>>> graphSummaries = apkAnalysisSummary.getGraphSummariesMap();
			Set<SootMethod> methods = graphSummaries.keySet();
			int stringCount = 0;
			for (SootMethod sootMethod : methods) {
				stringCount += graphSummaries.get(sootMethod).size();
			}
			methodOverview.append(stringCount);
			methodOverview.append("</td>");
			
			//findIntraProceduralBasedStrings
			// 6
			methodOverview.append("<td>");
			methodOverview.append(StringExtractionUtils.findIntraProceduralBasedStrings(graphSummaries).size());
			methodOverview.append("</td>");
			
			//findInterProceduralBasedStrings
			// 7
			methodOverview.append("<td>");
			methodOverview.append(StringExtractionUtils.findInterProceduralBasedStrings(graphSummaries).size());
			methodOverview.append("</td>");

			methodOverview.append("</tr>");			
		}
		methodOverview.append("</table> ");
		Set<String> stringBearningClasses = NetworkFlowInterproceduralAnalysis.STRING_BEARING_UNITS.keySet();
		for (String stringBearingClass : stringBearningClasses) {
				methodOverview.append("<BR>");
				
				methodOverview.append(stringBearingClass + ": " + NetworkFlowInterproceduralAnalysis.STRING_BEARING_UNITS.get(stringBearingClass).size());
		}

		return methodOverview.toString();
	}

    // Taken from http://stackoverflow.com/questions/779519/delete-files-recursively-in-java
	public static boolean deleteRecursive(File path) throws FileNotFoundException{
        if (!path.exists()) throw new FileNotFoundException(path.getAbsolutePath());
        boolean ret = true;
        if (path.isDirectory()){
            for (File f : path.listFiles()){
                ret = ret && deleteRecursive(f);
            }
        }
        return ret && path.delete();
    }
	
	public static void createHtmlSummaryDirectories() {
		if(summary.exists()){
			try {
				boolean worked = deleteRecursive(summary.getParentFile());
				logger.debug("did i work");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String newDirectory = Constants.OUTPUT_DIR + File.separator + "summary";
		summary = new File(newDirectory);
		boolean successful = summary.mkdirs();
		apkSummaries = new File(Constants.OUTPUT_DIR + File.separator + "summary" + File.separator + "apksummaries/");
		apkSummaries.mkdir();
		logger.debug("Directory deleted");
	}

	public static void main(String[] args){
		G.reset();
		args = new String[]{"C:\\apk\\FirstApplication.apk", "resources/log4j.xml"};
		long startTime = System.nanoTime();
		NetworkFlowInterProceduralMain.main(args);
		long endTime = System.nanoTime();
		HtmlSummaryGenerator.registerApkForAnalysis(new ApkAnalysisSummary(args[0], 
				NetworkFlowInterproceduralAnalysis.getSummaries(), 
				NetworkFlowInterproceduralAnalysis.getGraphSummaries(), 
				NetworkFlowInterproceduralAnalysis.getEugMap(),
				NetworkFlowBackwardFlowAnalysis.getClassFieldAssignedValues(),
				endTime-startTime));
		HtmlSummaryGenerator.writeHtmlSummaryFilesForAnalysis();

	}

	public static void registerApkForAnalysis(ApkAnalysisSummary apkAnalysisSummary) {
		apkAnalysisSummaryList.add(apkAnalysisSummary);
	}
	
}
