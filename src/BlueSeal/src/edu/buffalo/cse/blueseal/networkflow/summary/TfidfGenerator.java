package edu.buffalo.cse.blueseal.networkflow.summary;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import edu.buffalo.cse.blueseal.blueseal.Constants;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowInterproceduralAnalysis;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.UnitWrapper;
import edu.buffalo.cse.blueseal.networkflow.intraprocedural.InterproceduralTest;
import edu.uci.ics.jung.graph.DelegateTree;

import soot.*;
import soot.jimple.internal.JReturnStmt;

public class TfidfGenerator {

	static Logger logger = Logger.getLogger(TfidfGenerator.class);
	
	public static HashMap<String, ArrayList<TfidfCounter>> UGLY_MATRIX = new HashMap<String, ArrayList<TfidfCounter>>();
	public static HashMap<String, ArrayList<String>> REVERSE_LOOKUP_UGLY_MATRIX = new HashMap<String, ArrayList<String>>();
	public static int[][] MATRIX;
	public static Map<String, ArrayList<StringSimilarityScore>> CLASSIFIER; 


	private static void addClassificationPage(String page, String fileName){
		File file = new File(fileName.replace("apksummaries\\", "apksummaries\\classifications"));
		logger.debug(file.getPath());
		try {
			FileUtils.writeStringToFile(file, page, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	private static void addMatrixSummaryResultsPage(String matrix) {
		File file = new File(Constants.OUTPUT_DIR + File.separator + "summary" + File.separator + "matrix.html");
		logger.debug(file.getPath());
		try {
			FileUtils.writeStringToFile(file, matrix, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public static void generateClassificationBasedOnUnitSimilarity(ApkAnalysisSummary apkAnalysisSummary){
		Set<String> methods = TfidfGenerator.UGLY_MATRIX.keySet();
		String[] stringNames = new String[apkAnalysisSummary.getTotalStringCount()];
		Map<String, Integer> methodLookUp = new TreeMap<String, Integer>();
		int counter = 0;
		for(String methodSignature : methods){
			if(methodSignature != null){
				methodLookUp.put(methodSignature, new Integer(counter));
				counter++;				
			}
			else{
				logger.debug("huh?");
			}
		}
		MATRIX = new int[methods.size()][apkAnalysisSummary.getTotalStringCount()];
		Map<SootMethod, ArrayList<DelegateTree<UnitWrapper, String>>> graphSummariesMap = apkAnalysisSummary.getGraphSummariesMap();
		Set<SootMethod> sootMethods = graphSummariesMap.keySet();
		
		int stringPosition = 0;
		int stringNameIndex = 0;
		for (SootMethod sootMethod : sootMethods) {
			ArrayList<DelegateTree<UnitWrapper, String>> delegateTrees = graphSummariesMap.get(sootMethod);
			int i = 1;
			for (DelegateTree<UnitWrapper, String> delegateTree : delegateTrees) {
				if(ApkAnalysisSummary.isNotRootNode(delegateTree)){
					String stringName = "String " + i++;
					String reverseLookUpKey = PerApkSummaryGenerator.getStringHtmlFileName(apkAnalysisSummary, sootMethod, stringName);
					stringNames[stringNameIndex] = reverseLookUpKey;
					stringNameIndex++;
					ArrayList<String> methodSignaturesForString = REVERSE_LOOKUP_UGLY_MATRIX.get(reverseLookUpKey);
					for (String string : methodSignaturesForString) {
						ArrayList<TfidfCounter> tfidfCounters = UGLY_MATRIX.get(string);
						TfidfCounter tfidfCounter = null;
						for (TfidfCounter tfidfCounter2 : tfidfCounters) {
							if(tfidfCounter2 != null && tfidfCounter2.getName().equals(reverseLookUpKey)){
								tfidfCounter = tfidfCounter2;
								break;
							}
						}
						if(tfidfCounter != null){
							MATRIX[methodLookUp.get(string).intValue()][stringPosition] = tfidfCounter.getCount();													
						}
						else{
							logger.debug("Failed: " + sootMethod.getName());
						}
					}
					stringPosition++;
				}
			}
		}
		
		int innerLoop = methods.size();
		int outerLoop = apkAnalysisSummary.getTotalStringCount();
		
		
		
		StringBuffer matrixResults = new StringBuffer();
		matrixResults.append("<!DOCTYPE html><html><head><title>SAC Output Summary</title></head><body>");

		matrixResults.append("<table border=\"1\" style=\"border-collapse: collapse;\">");
		matrixResults.append("<tr>");		
		matrixResults.append("<th>StringName</th>");
		for(String methodSignature : methods){
			logger.debug("Adding this: " + methodSignature);
			matrixResults.append("<th>" + methodSignature.replaceAll("<", "").replaceAll(">", "") + "</th>");
		}
		matrixResults.append("</tr>");		
		for(int i = 0; i < outerLoop; i++){
			matrixResults.append("<tr>");
			matrixResults.append("<td>");
			matrixResults.append(stringNames[i]);						
			matrixResults.append("</td>");
			for(int j = 0; j < innerLoop; j++){
				matrixResults.append("<td>");
				matrixResults.append(MATRIX[j][i]);
				matrixResults.append("</td>");
			}
			matrixResults.append("</tr>");
			matrixResults.append("\n");
		}
		matrixResults.append("</body></html>"); 
		addMatrixSummaryResultsPage(matrixResults.toString());
		
		CLASSIFIER = new HashMap<String, ArrayList<StringSimilarityScore>>();
		
		for(int i = 0; i < outerLoop; i++){
			ArrayList<StringSimilarityScore> similarityScores = new ArrayList<StringSimilarityScore>();
			for(int j = 0; j < outerLoop; j++){
				if(j != i){
					StringSimilarityScore stringSimilarityScore = new StringSimilarityScore(stringNames[i], i, stringNames[j], j);
					if(stringSimilarityScore.getScore() > 0.5){
						similarityScores.add(stringSimilarityScore);						
					}
				}
			}
	        Collections.sort(similarityScores,new SimilarityScoreComparator<StringSimilarityScore>());
	        CLASSIFIER.put(stringNames[i], similarityScores);
    		StringBuffer classifierResults= new StringBuffer();
    		classifierResults.append("<!DOCTYPE html><html><head><title>SAC Output Summary</title></head><body>");
	        for (StringSimilarityScore stringSimilarityScore : similarityScores) {


	    		classifierResults.append("<table border=\"1\" style=\"width:100%; height:100%; border-collapse: collapse;\">");
	    		classifierResults.append("<tr>");		
	    		classifierResults.append("<th>Method</th>");
	    		classifierResults.append("<th>Frequency</th>");
	    		classifierResults.append("</tr>");		
	    		classifierResults.append("<tr>");
	    		classifierResults.append("<td>");
	    		classifierResults.append("Score: " + stringSimilarityScore.getScore());	    		
	    		classifierResults.append("</td>");
	    		classifierResults.append("<td>");
	    		classifierResults.append("</td>");
	    		classifierResults.append("</tr>");

	    		Set<String> keys = stringSimilarityScore.getMatches().keySet();
	    		
	    		for (String string : keys) {
			    		classifierResults.append("<tr>");
			    		classifierResults.append("<td>");
			    		classifierResults.append("<textarea style=\"width: 100%; height: 100%; border: none; background-color: #B0C4DE;\">");
		    			classifierResults.append(string);
			    		classifierResults.append("</textarea>");
			    		classifierResults.append("</td>");
			    		classifierResults.append("<td>");
		    			classifierResults.append(stringSimilarityScore.getMatches().get(string));
			    		classifierResults.append("</td>");
			    		classifierResults.append("</tr>");						
				}
	    		classifierResults.append("</table>");
	    		classifierResults.append("<BR>");
	    	}	        
			classifierResults.append("</body></html>"); 
	        if(similarityScores.size() > 0){
				addClassificationPage(classifierResults.toString(), stringNames[i]);	        	
	        }
		}
	}
}
