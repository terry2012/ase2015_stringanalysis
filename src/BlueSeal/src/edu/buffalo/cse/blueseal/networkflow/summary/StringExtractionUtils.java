package edu.buffalo.cse.blueseal.networkflow.summary;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.internal.JReturnStmt;
import soot.toolkits.graph.UnitGraph;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.UnitWrapper;
import edu.uci.ics.jung.graph.DelegateTree;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;;


public class StringExtractionUtils {
	
	static Logger logger = Logger.getLogger(StringExtractionUtils.class);
	public static String BEARS_STRING = "";
	
	public static void prettyPrintHTML(DelegateTree<UnitWrapper, String> delegateTree, SootMethod succesorMethod, File file) throws IOException{
		StringBuffer callChainBuffer = new StringBuffer();
		UnitWrapper unit = delegateTree.getRoot();
		callChainBuffer.append(escapeHtml4(succesorMethod.getSignature()));
		callChainBuffer.append("<br>");
		callChainBuffer.append(escapeHtml4(unit.toString()));
		callChainBuffer.append("<br>");
		
		try
		{
		    FileWriter fw = new FileWriter(file,true); //the true will append the new data
		    fw.write(callChainBuffer.toString());//appends the string to the file
		    fw.close();
		}
		catch(IOException ioe)
		{
		    System.err.println("IOException: " + ioe.getMessage());
		}
				
		callChainBuffer = null;
		printChildrenHTML(unit, delegateTree, "", file);			
	}

	private static void printChildrenHTML(UnitWrapper unitWrapper, DelegateTree<UnitWrapper, String> delegateTree, String offset, File file) throws IOException {
		Collection<UnitWrapper> children = delegateTree.getChildren(unitWrapper);
		for (UnitWrapper childUnit : children) {
			StringBuffer callChainBuffer = new StringBuffer();
			callChainBuffer.append(offset + escapeHtml4(delegateTree.getParentEdge(childUnit).toString()));
			callChainBuffer.append("<br>");
			callChainBuffer.append(offset + escapeHtml4(childUnit.toString()));
			callChainBuffer.append("<br>");
			try
			{
			    FileWriter fw = new FileWriter(file,true); //the true will append the new data
			    fw.write(callChainBuffer.toString());//appends the string to the file
			    fw.close();
			}
			catch(IOException ioe)
			{
			    System.err.println("IOException: " + ioe.getMessage());
			}
			callChainBuffer = null;
			printChildrenHTML(childUnit, delegateTree, offset + offset, file);			
		}
	}
	
	public static String prettyPrint(DelegateTree<UnitWrapper, String> delegateTree, SootMethod succesorMethod){
		StringBuffer callChainBuffer = new StringBuffer();
		UnitWrapper unit = delegateTree.getRoot();
		callChainBuffer.append(succesorMethod.getSignature());
		callChainBuffer.append("\r\n");
		callChainBuffer.append(unit.toString());
		callChainBuffer.append("\r\n");
		callChainBuffer.append(printChildren(unit, delegateTree, ""));
		return callChainBuffer.toString();
	}

	private static String printChildren(UnitWrapper unitWrapper, DelegateTree<UnitWrapper, String> delegateTree, String offset) {
		StringBuffer callChainBuffer = new StringBuffer();
		Collection<UnitWrapper> children = delegateTree.getChildren(unitWrapper);
		if(children != null){
			for (UnitWrapper childUnit : children) {
				Collection<String> inEdges = delegateTree.getInEdges(childUnit);
				String inEdgesString = "";
				for (String string : inEdges) {
					inEdgesString += inEdgesString +"," + string;
				}
				callChainBuffer.append(offset + childUnit.toString());
				callChainBuffer.append("  ");
				callChainBuffer.append(printChildren(childUnit, delegateTree, offset + offset));			
			}			
		}
		return callChainBuffer.toString();
	}

	public static ArrayList<DelegateTree<UnitWrapper, String>> findStringsConservative(
			ArrayList<DelegateTree<UnitWrapper, String>> delegateTrees) {
		ArrayList<DelegateTree<UnitWrapper, String>> stringBearingTrees = new ArrayList<DelegateTree<UnitWrapper,String>>();
		for (DelegateTree<UnitWrapper, String> delegateTree : delegateTrees) {
			if(ApkAnalysisSummary.isNotRootNode(delegateTree)){
				stringBearingTrees.add(delegateTree);
			}
		}
		return stringBearingTrees;
	}

	public static ArrayList<DelegateTree<UnitWrapper, String>> findStringsConservative(
			Map<SootMethod, ArrayList<DelegateTree<UnitWrapper, String>>> sums) {
		ArrayList<DelegateTree<UnitWrapper, String>> stringBearingTrees = new ArrayList<DelegateTree<UnitWrapper,String>>();
		Set<SootMethod> methods = sums.keySet();
		for (SootMethod sootMethod : methods) {
			ArrayList<DelegateTree<UnitWrapper, String>> trees = sums.get(sootMethod);
			for (DelegateTree<UnitWrapper, String> delegateTree : trees) {
				if(ApkAnalysisSummary.isNotRootNode(delegateTree)){
					stringBearingTrees.add(delegateTree);
				}
			}
		}
		return stringBearingTrees;
	}

	public static ArrayList<String> harvestString(
			DelegateTree<UnitWrapper, String> delegateTree) {
		ArrayList<String> strings = new ArrayList<String>();
		strings.add(getStringViaBfs(delegateTree.getRoot(), delegateTree));
		return strings;
	}
	
	private static String getStringViaBfs(UnitWrapper unit, DelegateTree<UnitWrapper, String> delegateTree) {
		String identifiedString = extractStringFromUnit(unit);
		//identifiedString = "Line number: " + identifiedString + " - " + unit.toString() + "\r\n";
		Collection<UnitWrapper> units = delegateTree.getChildren(unit);
		for (UnitWrapper childUnit : units) {
			identifiedString +=  getStringViaBfs(childUnit, delegateTree);
		}
		return identifiedString;
	}

	public static String extractStringFromUnit(UnitWrapper unitWrapper) {
		String identifiedString = BEARS_STRING;
		Pattern p = Pattern.compile("\"([^\"]*)\"");
		Matcher m = p.matcher(unitWrapper.toString());
		while (m.find()){
			//identifiedString = unit.getJavaSourceStartLineNumber() + ": " + m.group(1);
			identifiedString = m.group(1);
		}
		return identifiedString;
	}

	public static void main(String[] strings){
		Pattern p = Pattern.compile("\"([^\"]*)\"");
		Matcher m = p.matcher("I really want to \"find this\"");
		while (m.find()){
			String url = m.group(0);
			System.out.println(url);
			url = m.group(1);
			System.out.println(url);
			url = m.group(2);
			System.out.println(url);
		}
	}

	
	public static ArrayList<DelegateTree<UnitWrapper, String>> findIntraProceduralBasedStrings(Map<SootMethod, ArrayList<DelegateTree<UnitWrapper, String>>> graphSummaries){
		ArrayList<DelegateTree<UnitWrapper, String>> intrProceduralGraphs = new ArrayList<DelegateTree<UnitWrapper,String>>();
		
		for (SootMethod sootMethod : graphSummaries.keySet()) {
			ArrayList<DelegateTree<UnitWrapper, String>> graphs = graphSummaries.get(sootMethod);
			for (DelegateTree<UnitWrapper, String> delegateTree : graphs) {
				boolean isIntraProcedural = isTreeIntraProceduralString(delegateTree);
				if(isIntraProcedural){
					intrProceduralGraphs.add(delegateTree);
				}
			}			
		}
		
		return intrProceduralGraphs;
	}

	public static boolean isTreeIntraProceduralString(
			DelegateTree<UnitWrapper, String> delegateTree) {
		Collection<UnitWrapper> verticies = delegateTree.getVertices();
		boolean isIntraProcedural = true;
		for (UnitWrapper unitWrapper : verticies) {
			Unit unit = unitWrapper.getUnit();
			if(unit instanceof JReturnStmt){
				isIntraProcedural = false;
			}
			//TODO handle parameter cases
		}
		return isIntraProcedural;
	}

	public static ArrayList<DelegateTree<UnitWrapper, String>> findInterProceduralBasedStrings(Map<SootMethod, ArrayList<DelegateTree<UnitWrapper, String>>> graphSummaries){
		ArrayList<DelegateTree<UnitWrapper, String>> intrProceduralGraphs = new ArrayList<DelegateTree<UnitWrapper,String>>();
		
		for (SootMethod sootMethod : graphSummaries.keySet()) {
			ArrayList<DelegateTree<UnitWrapper, String>> graphs = graphSummaries.get(sootMethod);
			for (DelegateTree<UnitWrapper, String> delegateTree : graphs) {
				Collection<UnitWrapper> verticies = delegateTree.getVertices();
				boolean isInterProcedural = true;
				if(delegateTree.getRoot().getUnit() instanceof JReturnStmt){
					isInterProcedural = false;
				}
				else{
					for (UnitWrapper unit : verticies) {
						if(unit.getUnit() instanceof JReturnStmt){
							isInterProcedural = true;
						}
						//TODO handle parameter cases
					}					
				}
				if(isInterProcedural){
					intrProceduralGraphs.add(delegateTree);
				}
			}			
		}
		
		return intrProceduralGraphs;
	}


	public static ArrayList<DelegateTree<Unit, String>> findReturnBasedString(ArrayList<DelegateTree<Unit, String>> graphs){
		return null;
	}

	public static ArrayList<DelegateTree<Unit, String>> findParameterInBasedString(ArrayList<DelegateTree<Unit, String>> graphs){
		return null;
	}

	public static ArrayList<DelegateTree<UnitWrapper, String>> findIntraProceduralBasedStrings(
			ArrayList<DelegateTree<UnitWrapper, String>> stringTrees) {
		ArrayList<DelegateTree<UnitWrapper, String>> intrProceduralGraphs = new ArrayList<DelegateTree<UnitWrapper,String>>();
		for (DelegateTree<UnitWrapper, String> delegateTree : stringTrees) {
			boolean isIntraProcedural = isTreeIntraProceduralString(delegateTree);
			if(isIntraProcedural){
				intrProceduralGraphs.add(delegateTree);
			}
		}			
		return intrProceduralGraphs;
	}

}
