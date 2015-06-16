package edu.buffalo.cse.blueseal.networkflow.summary;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.*;

import soot.PatchingChain;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JReturnStmt;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.ArraySparseSet;

import edu.buffalo.cse.blueseal.blueseal.Constants;
import edu.buffalo.cse.blueseal.blueseal.OsUtils;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowInterProceduralMain;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.UnitWrapper;
import edu.buffalo.cse.blueseal.networkflow.intraprocedural.NetworkFlowMethodAnalysis;
import edu.uci.ics.jung.graph.DelegateTree;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;;


public class PerApkSummaryGenerator {
	
	private static final boolean GENERATE_CLASSIFICATION = false;
	private static final boolean ANALYZE_URL_STRINGS = true;
	public static String SPLIT_STRING = "";
	
	static{
		if(OsUtils.isWindows()){
			SPLIT_STRING = "\\\\";
		}
		else{
			SPLIT_STRING = File.separator;
		}
	}
	
	static Logger logger = Logger.getLogger(PerApkSummaryGenerator.class);
	
	private ArrayList<ApkAnalysisSummary> apkAnalysisSummaryList;
	private File leftFrameUrlFile;
	private File rightFrameUrlFile;

	public PerApkSummaryGenerator(ArrayList<ApkAnalysisSummary> apkAnalysisSummaryList){
			String[] entries = new String[]{
					"APK Name", 
					// 1
					"Method Name",
					// 2
					"Tree Size",
					// 2a
					"Incomplete",
					// 3
					"Num Method Calls",
					// 4 Obfuscated
					"Obfuscated",
					// 4a Structural
					"Structural",
					// 5 Provenance
					"Provenance",
					// 6 Type
					"Type",
					// 7 Class Fields
					"Class Fields",
					// 8 Static Fields
					"Static Fields",
					// 8a Static Fields
					"Signatures",
					// 9 String Parts
					"String Parts",
					// 10 Graph
					"Graph"};
				//csvWriter.writeNext(entries);
		this.apkAnalysisSummaryList = apkAnalysisSummaryList;
		createRightFrameDefaultFile();

	}
	
	private void createRightFrameDefaultFile() {
		rightFrameUrlFile = createHtmlFile("Default right frame", HtmlSummaryGenerator.apkSummaries.getPath() + File.separator + "rightFrame.html");	
	}

	public void addHtmlPerApkResultsPages() {
		// Iterate over each APK summary
		for (ApkAnalysisSummary apkAnalysisSummary : apkAnalysisSummaryList) {
			String leftFrameUrl = createRelativePathToLeftFrame(apkAnalysisSummary);
			leftFrameUrlFile = createHtmlFile("", leftFrameUrl);			
			logger.debug("leftFrameUrl is: " + leftFrameUrl);
			String summary = createApkSummaryContent();
			String newFileName = createRelativePathToApkSummaryPage(apkAnalysisSummary);
			String leftContent = "";
			if(ANALYZE_URL_STRINGS){
				leftContent = analyzeUrlString(apkAnalysisSummary);
			}
			else{
				leftContent = analyzeMethods(apkAnalysisSummary);				
			}
			createHtmlFile(summary, newFileName);			
			writeToHtmlFile(leftContent, leftFrameUrlFile);
		}
	}

	private void writeToHtmlFile(String leftContent, File leftFrameUrlFile2) {
		String summary = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\"";
		summary +=   "\"http://www.w3.org/TR/html4/frameset.dtd\">";
		summary += "<HTML>";
		summary += "<HEAD>";
		summary += "<TITLE>A simple frameset document</TITLE>";
		summary += "</HEAD>";
		summary += "<BODY>";
		summary +=  leftContent;
		summary += "</BODY>";
		summary += "</HTML>";
		try {
			FileUtils.writeStringToFile(leftFrameUrlFile2, summary, "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	private void addMethodValueAnalysisCsv(List<SootString> sootStrings) {
		HashMap<String, Integer> urlCountsMap = new HashMap<String, Integer>();
		HashMap<String, Integer> reflectionCountsMap = new HashMap<String, Integer>();
		HashMap<String, Integer> intentCountsMap = new HashMap<String, Integer>();
		for (SootString sootString : sootStrings) {
			HashMap<String, Integer> countsMap = new HashMap<String, Integer>();
			if(sootString.isUrlType()){
				countsMap = urlCountsMap;
			} else if(sootString.isReflectionType()){
				countsMap = reflectionCountsMap;
			} else if(sootString.isIntentType()){
				countsMap = intentCountsMap; 
			}
			ArrayList<String> matches = sootString.getStringFieldMatches();
			for (String match : matches) {
				String[] values = match.split(" ");
				for (String splitValue : values) {
					Integer value = countsMap.get(splitValue);
					// If it is not in, add it 
					if(value == null){
						value = new Integer(1);
						countsMap.put(splitValue, value);
					}
					// If it is in, increment it and add it back
					else{
						value = new Integer(value.intValue() + 1);
						countsMap.put(splitValue, value);
					}						
				}
			}
		}
		if(urlCountsMap.keySet().size() > 0){
			writeCountsFile(urlCountsMap, NetworkFlowInterProceduralMain.APK_NAME+"url_value_analysis.csv");			
		}
		if(reflectionCountsMap.keySet().size() > 0){
			writeCountsFile(reflectionCountsMap, NetworkFlowInterProceduralMain.APK_NAME+"reflection_value_analysis.csv");			
		}
		if(intentCountsMap.keySet().size() > 0){
			writeCountsFile(reflectionCountsMap, NetworkFlowInterProceduralMain.APK_NAME+"intent_value_analysis.csv");			
		}
		
	}

	private void writeCountsFile(HashMap<String, Integer> countsMap,
			String fileName) {
		try {
			CSVWriter csvWriter = new CSVWriter(new FileWriter(Constants.OUTPUT_DIR + File.separator + "summary" + File.separator + fileName), '\t');
			Set<String> valueKeys = countsMap.keySet();
			String[] csvRow = new String[2];
			for (String value : valueKeys) {
				csvRow[0] = value;
				csvRow[1] = countsMap.get(value).toString();
				csvWriter.writeNext(csvRow);
				csvWriter.flush();
			}
			csvWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	private String analyzeUrlString(ApkAnalysisSummary apkAnalysisSummary) {
		addMethodValueAnalysisCsv(apkAnalysisSummary.getSootStrings());			
		String fileName = Constants.OUTPUT_DIR + File.separator + "summary" + File.separator + NetworkFlowInterProceduralMain.APK_NAME+"all_soot_strings.csv";
		File file = new File(fileName);
		CSVWriter csvWriter = null;
		try {
			if(! file.exists()){
					file.createNewFile();
					csvWriter = new CSVWriter(new FileWriter(file), '\t');
			}
			else{
				csvWriter = new CSVWriter(new FileWriter(file), '\t');			
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] csvLine = new String[14];
		csvLine[0] = apkAnalysisSummary.getApkName();
		StringBuffer methodOverview = new StringBuffer("<table border=\"1\" style=\"border-collapse: collapse; width:100%; height:100%; position:absolute; top:0; right:0; bottom:0; left:0; border:1;\">");
		methodOverview.append("<tr>");
		// 1
		methodOverview.append("<th WIDTH=\"40%\">Method Name</th>");
		// 2
		methodOverview.append("<th WIDTH=\"5%\">Tree Size</th>");
		// 2a
		methodOverview.append("<th WIDTH=\"5%\">Incomplete</th>");
		// 3
		methodOverview.append("<th WIDTH=\"5%\">Num Method Calls</th>");
		// 4 Obfuscated
		methodOverview.append("<th WIDTH=\"5%\">Obfuscated</th>");
		// 4a Structural
		methodOverview.append("<th WIDTH=\"5%\">Structural</th>");
		// 5 Provenance
		methodOverview.append("<th WIDTH=\"10%\">Provenance</th>");
		// 6 Semantic
		methodOverview.append("<th WIDTH=\"10%\">Type</th>");
		// 7 Class Fields
		methodOverview.append("<th WIDTH=\"5%\">Class Fields</th>");
		// 8 Static Fields
		methodOverview.append("<th WIDTH=\"5%\">Static Fields</th>");
		// 8a Packages
		methodOverview.append("<th WIDTH=\"5%\">Signatures</th>");
		// 9 String Parts
		methodOverview.append("<th WIDTH=\"20%\">String Parts</th>");
		// 10 Graph
		methodOverview.append("<th WIDTH=\"5%\">Graph</th>");
		
		methodOverview.append("</tr>");
		List<SootString> urlStrings = apkAnalysisSummary.getSootStrings();
		StringBuffer tempBuffer = new StringBuffer();
		if(urlStrings.size() > 0){
			int i = 0;
			for (SootString urlString : urlStrings) {
				SootMethod sootMethod = urlString.getSootMethod();
				DelegateTree<UnitWrapper, String> urlStringTree = urlString.getDelegateTree();
				tempBuffer.append("<tr>");
				// 1. Method Name
				String methodName = sootMethod.getDeclaringClass().getName() + "." + sootMethod.getName();
				csvLine[1] =  methodName;
				tempBuffer.append("<td>");
				tempBuffer.append(escapeHtml4(methodName));
				tempBuffer.append("</td>");
				// 2. Number of vertex
				int vertexCount = urlStringTree.getVertexCount();
				csvLine[2] = vertexCount + "";
				tempBuffer.append("<td>");
				tempBuffer.append(vertexCount);
				tempBuffer.append("</td>");				
				// 2a. Incomplete
				csvLine[3] = "" + urlString.isHasIncompleteParameter();
				tempBuffer.append("<td>");
				tempBuffer.append(urlString.isHasIncompleteParameter());
				tempBuffer.append("</td>");								
				// 3. Nesting
				int numberOfMethodsCalled = urlString.getNesting();
				csvLine[4] = "" + numberOfMethodsCalled;
				tempBuffer.append("<td>");
				tempBuffer.append(numberOfMethodsCalled);
				tempBuffer.append("</td>");								
				// 4  Obfuscated
				int obfuscationCount = urlString.getObfuscationMatches().size();
				csvLine[5] = "" + obfuscationCount;
				tempBuffer.append("<td>");
				tempBuffer.append(obfuscationCount);
				tempBuffer.append("</td>");
				// 4a  Structural
				String structuralMatches = urlString.getMatchesAsStringFromSet(urlString.getStructuralMatches());
				csvLine[6] = "" + structuralMatches;
				tempBuffer.append("<td>");
				tempBuffer.append(structuralMatches);
				tempBuffer.append("</td>");
				// 5 Provenance
				String provenanceMatches = urlString.getMatchesAsStringFromSet(urlString.getProvenanceMatches());
				csvLine[7] = provenanceMatches;
				tempBuffer.append("<td>");
				tempBuffer.append(escapeHtml4(provenanceMatches));
				tempBuffer.append("</td>");
				// 6 Type
				String semanticType = urlString.getType();
				csvLine[8] = semanticType;
				tempBuffer.append("<td>");
				tempBuffer.append(escapeHtml4(semanticType));
				tempBuffer.append("</td>");
				// 7 Class Fields
				int classFieldsUsed = urlString.getClassFieldMatches().size();
				csvLine[9] = classFieldsUsed + "";
				tempBuffer.append("<td>");
				tempBuffer.append(classFieldsUsed);
				tempBuffer.append("</td>");
				// 8 Static Fields
				int staticFieldsUsed = urlString.getStaticFieldMatches().size();
				csvLine[10] = staticFieldsUsed + "";
				tempBuffer.append("<td>");
				tempBuffer.append(staticFieldsUsed);
				tempBuffer.append("</td>");
				tempBuffer.append("</td>");
				// 8a Packages
				String methods = urlString.getMatchesAsStringFromList(urlString.getMethodSignatures(), false, true);
				csvLine[11] = methods;
				tempBuffer.append("<td>");
				tempBuffer.append(methods);
				tempBuffer.append("</td>");
				tempBuffer.append("</td>");
				// 9 String Parts
				String stringParts = urlString.getMatchesAsStringFromList(urlString.getStringFieldMatches(), true, false);
				csvLine[12] = stringParts;
				tempBuffer.append("<td>");
				tempBuffer.append(escapeHtml4(stringParts));
				tempBuffer.append("</td>");
				// 10. Graph
				tempBuffer.append("<td>");
				String stringName = "Graph "+ i++;
				String stringFileName = getStringHtmlFileName(
						apkAnalysisSummary, sootMethod, stringName);				
				csvLine[13] =  stringFileName;
				String summary = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\"";
				summary +=   "\"http://www.w3.org/TR/html4/frameset.dtd\">";
				summary += "<HTML>";
				summary += "<HEAD>";
				summary += "<TITLE>A simple frameset document</TITLE>";
				summary += "</HEAD>";
				summary += "<BODY>";
				File newFile = createHtmlFile(summary, stringFileName);

				try {
					StringExtractionUtils.prettyPrintHTML(urlStringTree, urlString.getSootMethod(), newFile);
					summary = "</BODY>";
					summary += "</HTML>";
					
					try
					{
					    FileWriter fw = new FileWriter(newFile,true); //the true will append the new data
					    fw.write(summary);//appends the string to the file
					    fw.close();
					}
					catch(IOException ioe)
					{
					    System.err.println("IOException: " + ioe.getMessage());
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				tempBuffer.append("<a href=\"" + newFile.getName() + "\" target=\"content\">" + stringName + "</a>");
				tempBuffer.append("</td>");									
				tempBuffer.append("</tr>");		
				csvWriter.writeNext(csvLine);
				try {
					csvWriter.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		methodOverview.append(tempBuffer);
		methodOverview.append("</table> ");
		try {
			csvWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return methodOverview.toString();
	}

	private String getYorNValue(ArrayList<String> matches) {
		String val = "N";
		if(matches.size()>0){
			val = "Y";
		}
		return val;
	}

	
	private String analyzeMethods(ApkAnalysisSummary apkAnalysisSummary) {
		Map<SootMethod, ArrayList<DelegateTree<UnitWrapper, String>>>  graphSummaries = apkAnalysisSummary.getGraphSummariesMap();
		Set<SootMethod> methods = graphSummaries.keySet();

		
		
		
		for (SootMethod sootMethod : methods) {
			ArrayList<DelegateTree<UnitWrapper, String>> stringTrees = graphSummaries.get(sootMethod);
			if(stringTrees.size() > 0){
				int i = 1;
				for(DelegateTree<UnitWrapper, String> tree : stringTrees){
					if(ApkAnalysisSummary.isNotRootNode(tree)){
						String stringName = "String " + i++;
						String stringFileName = getStringHtmlFileName(
								apkAnalysisSummary, sootMethod, stringName);
						File newFile = createHtmlFile(creatPerStringContents(tree, sootMethod, apkAnalysisSummary, stringFileName, stringName), stringFileName);
					}						
				}
			}
		}
		
		if(GENERATE_CLASSIFICATION){
			TfidfGenerator.generateClassificationBasedOnUnitSimilarity(apkAnalysisSummary);			
		}
		
		
		
		
		StringBuffer methodOverview = new StringBuffer("<table border=\"1\" style=\"border-collapse: collapse; width:100%; height:100%; position:absolute; top:0; right:0; bottom:0; left:0; border:1;\">");
		methodOverview.append("<tr>");
		methodOverview.append("<th  WIDTH=\"5%\">Strings</th>");
		methodOverview.append("<th WIDTH=\"20%\">Method Name</th>");
		methodOverview.append("<th WIDTH=\"5%\">Tree Size</th>");
		methodOverview.append("<th WIDTH=\"5%\">Intra</th>");
		methodOverview.append("<th WIDTH=\"5%\">Inter</th>");
		methodOverview.append("<th WIDTH=\"5%\">Call Graph Class</th>");
		methodOverview.append("<th WIDTH=\"5%\">String Class</th>");
		methodOverview.append("<th WIDTH=\"50%\">Value</th>");
		methodOverview.append("</tr>");
		for (SootMethod sootMethod : methods) {
			ArrayList<DelegateTree<UnitWrapper, String>> stringTrees = graphSummaries.get(sootMethod);
			if(stringTrees.size() > 0){
				StringBuffer tempBuffer = new StringBuffer();
				tempBuffer.append("<tr>");
				tempBuffer.append("<td>");
				tempBuffer.append(stringTrees.size());
				tempBuffer.append("</td>");				
				tempBuffer.append("<td>");
				tempBuffer.append(sootMethod.getDeclaringClass().getName() + "." + sootMethod.getName().replaceAll("<", "").replaceAll(">", ""));
				tempBuffer.append("</textarea>");
				tempBuffer.append("</td>");
				tempBuffer.append("<td>");
				tempBuffer.append("</td>");

				//intra count
				tempBuffer.append("<td>");
				int intraSize = StringExtractionUtils.findIntraProceduralBasedStrings(stringTrees).size();
				tempBuffer.append(intraSize);				
				tempBuffer.append("</td>");

				//inter count
				tempBuffer.append("<td>");
				tempBuffer.append(stringTrees.size() - intraSize);
				tempBuffer.append("</td>");

				//value - leave it empty
				tempBuffer.append("<td>");
				tempBuffer.append("</td>");

				
				tempBuffer.append("</tr>");
				int i = 1;
				StringBuffer tempBuffer2 = new StringBuffer();
				for(DelegateTree<UnitWrapper, String> tree : stringTrees){
					
					if(ApkAnalysisSummary.isNotRootNode(tree)){
						tempBuffer2.append("<tr>");
						tempBuffer2.append("<td>");
						tempBuffer2.append("</td>");				
						tempBuffer2.append("<td>");
						//<a href="page2.html" target="content">
						String stringName = "String " + i++;
						String stringFileName = getStringHtmlFileName(
								apkAnalysisSummary, sootMethod, stringName);
						File newFile = new File(stringFileName);
						tempBuffer2.append("<a href=\"" + newFile.getName() + "\" target=\"content\">" + stringName + "</a>");
						tempBuffer2.append("</td>");				

						//tree size
						tempBuffer2.append("<td>");
						tempBuffer2.append(tree.getVertexCount());
						tempBuffer2.append("</td>");				
						//is intra
						boolean intra = StringExtractionUtils.isTreeIntraProceduralString(tree);
						tempBuffer2.append("<td>");
						if(intra){
							tempBuffer2.append("X");										
						}
						tempBuffer2.append("</td>");				

						//is inter
						tempBuffer2.append("<td>");
						if(! intra){
							tempBuffer2.append("X");										
						}
						tempBuffer2.append("</td>");				

						
						//Link to classificatioon
						tempBuffer2.append("<td>");
						String fileName = "classifications" + newFile.getName();
						File file = new File(Constants.OUTPUT_DIR + File.separator + "summary" + File.separator + "apksummaries"  + File.separator + fileName);
						logger.debug("Whole shebang: " + file.getAbsolutePath());
						logger.debug("New filename is: " + fileName);
						if(file.exists()){
							tempBuffer2.append("<a href=\"" + fileName + "\" target=\"content\">Class</a>");							
						}
						else{
							tempBuffer2.append("None");														
						}
						tempBuffer2.append("</td>");				


						tempBuffer2.append("<td>");
						tempBuffer2.append("None");														
						tempBuffer2.append("</td>");				

						
						//value
						tempBuffer2.append("<td>");
						tempBuffer2.append("<textarea style=\"width: 100%; height: 100%; border: none; \">");
						ArrayList<String> strings = StringExtractionUtils.harvestString(tree);
						for (String string : strings) {
							tempBuffer2.append(string);
						}
						tempBuffer2.append("</textarea>");
						tempBuffer2.append("</td>");									
						tempBuffer2.append("</tr>");					
						
					}
				}
				if(tempBuffer2.length() > 0){
					tempBuffer.append(tempBuffer2.toString());
					methodOverview.append(tempBuffer.toString());
				}

			}
		}
		methodOverview.append("</table> ");
		return methodOverview.toString();
	}

	public static String getStringHtmlFileName(ApkAnalysisSummary apkAnalysisSummary,
			SootMethod sootMethod, String stringName) {
		String[] apksNameAsparts = apkAnalysisSummary.getApkName().split(SPLIT_STRING);
		String stringFileName = 
				HtmlSummaryGenerator.apkSummaries.getPath() + File.separator + 
				apksNameAsparts[apksNameAsparts.length-1] + "_" + 
				sootMethod.getNumber() + "_" + stringName + ".html";
		stringFileName = stringFileName.replaceAll("<", "").replaceAll(">", "");
		return stringFileName;
	}

	private String creatPerStringContents(DelegateTree<UnitWrapper, String> tree,
			SootMethod sootMethod, ApkAnalysisSummary apkAnalysisSummary, String fileName, String stringName) {
		String summary = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\"";
		summary +=   "\"http://www.w3.org/TR/html4/frameset.dtd\">";
		summary += "<HTML>";
		summary += "<HEAD>";
		summary += "<TITLE>A simple frameset document</TITLE>";
		summary += "<style type=\"text/css\">";
		summary += "html {height: 100%;}";
		summary += "body {height: 100%;}";
		summary += "table{width:100%; height:100%; position:absolute; top:0; right:0; bottom:0; left:0; border:1;}";
		summary += "td textarea { width: 100%; height: 100%}"; 
		summary += "</style>";
		summary += "</HEAD>";
		summary += "<BODY>";
		ExceptionalUnitGraph eug =  apkAnalysisSummary.getEugMap().get(sootMethod);		
		summary += addComparisonTable(eug, tree, sootMethod, apkAnalysisSummary.getApkName(), stringName, fileName);
		summary += "</BODY>";
		summary += "</HTML>";
		return summary;
	}
	
	private String addComparisonTable(ExceptionalUnitGraph eug,
			DelegateTree<UnitWrapper, String> tree, SootMethod sootMethod, String apkName, String stringName, String fileName) {
		StringBuffer methodOverview = new StringBuffer("<table border=\"1\" style=\"width:100%; height:100%; border-collapse: collapse;\">");
		methodOverview.append("<tr>");
		methodOverview.append("<th>Method Jimple</th>");
		methodOverview.append("<th>String Jimple Coverage</th>");
		methodOverview.append("</tr>");
		Collection<UnitWrapper> treeUnits = tree.getVertices();
		int counter = 0;
		for(Iterator it = eug.iterator(); it.hasNext();){
			Unit unit = (Unit) it.next();
			if(isUnitInTree(treeUnits, unit)){
				counter++;
				//treeUnits.remove(unit)
				methodOverview.append("<tr>");
				methodOverview.append("<td style=\"background-color: #B0C4DE;\">");
				methodOverview.append("<textarea style=\"width: 100%; height: 100%; border: none; background-color: #B0C4DE;\">");
				methodOverview.append(unit.toString());
				methodOverview.append("</textarea>");
				methodOverview.append("</td>");				
				methodOverview.append("<td style=\"background-color: #B0C4DE;\">");
				methodOverview.append("<textarea style=\"width: 100%; height: 100%; border: none; background-color: #B0C4DE;\">");
				methodOverview.append(unit.toString());
				methodOverview.append("</textarea>");
				methodOverview.append("</td>");
				methodOverview.append("</tr>");			
				
				String signature = "holder";
				if(unit instanceof JInvokeStmt){
					JInvokeStmt jInvokeStmt = (JInvokeStmt)unit;
					if(jInvokeStmt.containsInvokeExpr()){
						signature = jInvokeStmt.getInvokeExpr().getMethod().getSignature();						
					}
					else{
						logger.debug("TODO must solve this case ...");
					}
				}
				else if(unit instanceof JAssignStmt){
					JAssignStmt jAssignStmt = (JAssignStmt)unit;
					if(jAssignStmt.containsInvokeExpr()){
						signature = jAssignStmt.getInvokeExpr().getMethod().getSignature();						
					}
					else{
						logger.debug("TODO must solve this case ...");
					}
				}
				
				else if(unit instanceof JIdentityStmt){
					JIdentityStmt jIdentityStmt = (JIdentityStmt)unit;
					if(jIdentityStmt.containsInvokeExpr()){
						signature = jIdentityStmt.getInvokeExpr().getMethod().getSignature();						
					}
					else{
						logger.debug("TODO must solve this case ...");
					}
				}
				else{
					logger.debug("We see no reason to save this one ... ");
					//TODO see if this case is possile - yes for JReturn but anything else????
				}
				logger.debug("Signature is: " + signature);
				ArrayList<TfidfCounter> tfidfCounterList = TfidfGenerator.UGLY_MATRIX.get(signature);
				if(tfidfCounterList == null){
					tfidfCounterList = new ArrayList<TfidfCounter>();
					TfidfCounter tfidfCounter = new TfidfCounter(1, fileName, apkName);
					tfidfCounterList.add(tfidfCounter);
					TfidfGenerator.UGLY_MATRIX.put(signature, tfidfCounterList);
				} 
				else{
					TfidfCounter tfidfCounter = getTfidfCounter(apkName, fileName, tfidfCounterList); 
					if(tfidfCounter != null){
						tfidfCounter.incrementCount();
					}
					else{
						tfidfCounter = new TfidfCounter(1, fileName, apkName);
						tfidfCounterList.add(tfidfCounter);
					}
				}

				ArrayList<String> signaturesList = TfidfGenerator.REVERSE_LOOKUP_UGLY_MATRIX.get(fileName);
				if(signaturesList == null){
					signaturesList = new ArrayList<String>();
					signaturesList.add(signature);
					TfidfGenerator.REVERSE_LOOKUP_UGLY_MATRIX.put(fileName, signaturesList);
				} 
				else{
					signaturesList.add(signature);
				}
				
			}
			else{
				methodOverview.append("<tr>");
				methodOverview.append("<td>");
				methodOverview.append("<textarea style=\"width: 100%; height: 100%; border: none\">");
				methodOverview.append(unit.toString());
				methodOverview.append("</textarea>");
				methodOverview.append("</td>");				
				methodOverview.append("<td>");
				methodOverview.append("</td>");
				methodOverview.append("</tr>");				
			}
		}
		if(counter > treeUnits.size()){
			logger.debug("This does not make sense....");
		}
		return methodOverview.toString();
	}

	private boolean isUnitInTree(Collection<UnitWrapper> treeUnits, Unit unit) {
		boolean inTree = false;
		for (UnitWrapper unitWrapper : treeUnits) {
			if(unitWrapper.getUnit().equals(unit)){
				inTree = true;
			}
		}
		return inTree;
	}

	private TfidfCounter getTfidfCounter(String apkName, String stringName,
			ArrayList<TfidfCounter> tfidfCounter) {
		TfidfCounter counter = null;
		for (TfidfCounter tfidfCounter2 : tfidfCounter) {
			if(tfidfCounter2 != null && tfidfCounter2.getApkName().equals(apkName) && tfidfCounter2.getName().equals(stringName)){
				counter = tfidfCounter2;
				break;
			}
		}
		return counter;
	}

	private String createRelativePathToLeftFrame(
			ApkAnalysisSummary apkAnalysisSummary) {
		String[] parts = apkAnalysisSummary.getApkName().split(SPLIT_STRING);
		String newFileName = HtmlSummaryGenerator.apkSummaries.getPath() + File.separator + parts[parts.length-1] + "_frame.html";
		apkAnalysisSummary.setRelativeHtmlFileLocation(newFileName);
		return newFileName;
	}

	private File createHtmlFile(String summary, String newFileName) {
		logger.debug("About to create file: " + newFileName);
		File htmlFile = new File(newFileName);
		try {
			FileUtils.writeStringToFile(htmlFile, summary, "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return htmlFile;
	}

	private String createRelativePathToApkSummaryPage(
			ApkAnalysisSummary apkAnalysisSummary) {
		String[] parts = apkAnalysisSummary.getApkName().split(SPLIT_STRING);
		String newFileName = HtmlSummaryGenerator.apkSummaries.getPath() + File.separator + parts[parts.length-1] + ".html";
		apkAnalysisSummary.setRelativeHtmlFileLocation(newFileName);
		return newFileName;
	}

	
	
	private String createApkSummaryContent() {
		String summary = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\"";
		summary +=   "\"http://www.w3.org/TR/html4/frameset.dtd\">";
		summary += "<HTML>";
		summary += "<HEAD>";
		summary += "<TITLE>A simple frameset document</TITLE>";
		summary += "</HEAD>";
		summary += "<FRAMESET rows=\"30%, 70%\">";
		summary += "<FRAME src=\"" + leftFrameUrlFile.getName() + "\" name=\"nav\"\">";
		summary += "<FRAME src=\"" + rightFrameUrlFile.getName() + "\" name=\"content\"\">";
		summary += "</FRAMESET>";
		summary += "</HTML>";
		
//		String summary =  "<!DOCTYPE html><html><head><title>SAC Output Summary</title></head><body>";
//		summary +=  "<iframe src=\"" + getFrameUrl() + "\" width=\"200\" height=\"25%\">" + getFrameContent() +  "</iframe>";
//		summary += "</body></html>";
		return summary;
	}
		
}
