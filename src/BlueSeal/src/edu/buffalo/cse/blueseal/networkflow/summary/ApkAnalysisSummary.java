package edu.buffalo.cse.blueseal.networkflow.summary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.ParameterRef;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JimpleLocal;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowInterproceduralAnalysis;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.UnitWrapper;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.util.TreeUtils;

/**
 * Class holds all Intraprocedural and Interprocedural results for a single APK analysis in one location.  Used
 * by HtmlSummaryGenerator to create statistics of run.
 * 
 * @author delvecchio
 *
 */
public class ApkAnalysisSummary {
	
	static Logger logger = Logger.getLogger(ApkAnalysisSummary.class);
	
	private String apkName;
	private Map<SootMethod, Map<Unit, ArraySparseSet>> summariesMap = new HashMap<SootMethod, Map<Unit, ArraySparseSet>>();
	private Map<SootMethod, ArrayList<DelegateTree<UnitWrapper, String>>> graphSummariesMap = new HashMap<SootMethod, ArrayList<DelegateTree<UnitWrapper, String>>>();
	private Map<SootMethod, ExceptionalUnitGraph> eugMap = new HashMap<SootMethod, ExceptionalUnitGraph>();
	private long runTime;
	private String relativeHtmlFileName;
	private int totalStringCount = 0;
	private List<SootString> urlStringsList = new ArrayList<SootString>();
	private Map<String, ArrayList<DelegateTree<UnitWrapper, String>>> classAssignments;
	public static HashMap<String, Integer> URL_METHOD_SIGNATURE_COUNTS = new HashMap<String, Integer>(); 
	public static HashMap<String, Integer> URL_METHOD_SIGNATURE_SPREAD_COUNTS = new HashMap<String, Integer>(); 
	public static HashMap<String, Integer> REFLECTION_METHOD_SIGNATURE_COUNTS = new HashMap<String, Integer>(); 
	public static HashMap<String, Integer> REFLECTION_METHOD_SIGNATURE_SPREAD_COUNTS = new HashMap<String, Integer>();
	
	//Feng: added for Intent analysis
	public static HashMap<String, Integer> INTENT_METHOD_SIGNATURE_COUNTS = new HashMap<String, Integer>(); 
	public static HashMap<String, Integer> INTENT_METHOD_SIGNATURE_SPREAD_COUNTS = new HashMap<String, Integer>(); 
	
	public static HashMap<String, Integer> STRING_TYPES = new HashMap<String, Integer>();
	
	public static HashMap<String, ArrayList<String>> URL_METHOD_SIGNATURE_VALUES = new HashMap<String, ArrayList<String>>(); 
	public static HashMap<String, ArrayList<String>> REFLECTION_METHOD_SIGNATURE_VALUES = new HashMap<String, ArrayList<String>>(); 
	//Feng: added for Intent analysis
	public static HashMap<String, ArrayList<String>> INTENT_METHOD_SIGNATURE_VALUES = new HashMap<String, ArrayList<String>>();
	
	public ApkAnalysisSummary(String apkName,
			Map<SootMethod, Map<Unit, ArraySparseSet>> map,
			Map<SootMethod, ArrayList<DelegateTree<UnitWrapper, String>>> graphSummaries, 
			Map<SootMethod, ExceptionalUnitGraph> eugMap, 
			Map<String, ArrayList<DelegateTree<UnitWrapper, String>>> classFieldAssignments,
			long runTime){
		this.apkName = apkName;
		this.summariesMap.putAll(map);
		this.graphSummariesMap.putAll(graphSummaries);
		this.eugMap.putAll(eugMap);
		this.runTime = runTime;
		this.classAssignments = classFieldAssignments;
		calculateTotalNumberOfStrings();
		findSootStrings();
		printSootStringTypes();
	}

	public ArrayList<DelegateTree<UnitWrapper, String>> getClassAssignments(JInstanceFieldRef jInstanceFieldRef){
		return classAssignments.get(jInstanceFieldRef);
	}

	private void printSootStringTypes(){
		Set<String> keys = STRING_TYPES.keySet();
		for (String key : keys) {
			System.out.println("Key: " + key);
		}
	}
	
	private String getKeyBasedOnType(Unit unit){
		String type = "";
		if(unit instanceof JReturnStmt){
			JReturnStmt returnStmt = (JReturnStmt)unit;
			type = returnStmt.getOp().toString();
		}
		else if(unit instanceof JAssignStmt){
			JAssignStmt assignStm = (JAssignStmt)unit;
			Value rightValuebox = assignStm.getRightOpBox().getValue();
			Value leftValuebox = assignStm.getLeftOpBox().getValue();
			if(leftValuebox instanceof FieldRef){
				FieldRef fieldRef = (FieldRef)leftValuebox;
				type = fieldRef.getFieldRef().toString();
			}
			else{
				if(rightValuebox instanceof InvokeExpr){
					InvokeExpr invokeExpr = (InvokeExpr)rightValuebox;
					type = invokeExpr.getMethodRef().getSignature().toString();									
				}
				else{
					type = "bah";
				}
			}
		}
		else if(unit instanceof JInvokeStmt){
			JInvokeStmt invokeStmt = (JInvokeStmt)unit;
			type = invokeStmt.getInvokeExpr().getMethodRef().getSignature();
		}
		return type;
	}
	
	private void findSootStrings() {
		HashSet<DelegateTree<UnitWrapper, String>> foundTrees = new HashSet<DelegateTree<UnitWrapper, String>>();
		for(SootMethod sootMethod : graphSummariesMap.keySet()){
			for(DelegateTree<UnitWrapper, String> tree : graphSummariesMap.get(sootMethod)){
				String key = tree.getRoot().getUnit().getClass().toString();
				key = getKeyBasedOnType(tree.getRoot().getUnit());
				STRING_TYPES.put(key, new Integer(0));
				if(! NetworkFlowInterproceduralAnalysis.MERGED_TREES.contains(tree)){
					boolean isURL = tree.getRoot().getUnit().toString().contains("java.net.URL:");
					boolean isReflection = ((tree.getRoot().getUnit().toString().contains("java.lang.Class forName") || 
							tree.getRoot().getUnit().toString().contains("getDeclaredMethods")));
					
					//Feng: 05/14/2015 added to find Intent string
					boolean isIntent =  (tree.getRoot().getUnit().toString().contains("android.content.Intent: void <init>(java.lang.String)"));
					
					if(! (tree.getRoot().getUnit() instanceof JReturnStmt)){
					//if((isURL || isReflection || isIntent ) && ! foundTrees.contains(tree)){
						String type = getKeyBasedOnType(tree.getRoot().getUnit());
						/**
						if(isURL){
							type = SootString.URL_TYPE;
						}
						else if(isReflection){
							type = SootString.REFLECTION_TYPE;							
						}else if(isIntent){
							type = SootString.INTENT_TYPE;
						}
						*/
						logger.debug("Stop here");
						logger.debug(tree.getRoot().getUnit().toString());
						boolean splitTrees = true;
						ArrayList<DelegateTree<UnitWrapper, String>> newTrees = splitTrees(tree, tree.getRoot());
						if(newTrees.size() > 0){
							for (DelegateTree<UnitWrapper, String> delegateTree : newTrees) {
								urlStringsList.add(new SootString(delegateTree, sootMethod, type));						
								foundTrees.add(delegateTree);
							}						
						}
						else{
							urlStringsList.add(new SootString(tree, sootMethod, type));						
							foundTrees.add(tree);						
						}
					}					
				}
			}			
		}
		
		Set<String> urlMethodSignaturesSet = new HashSet<String>();
		Set<String> reflectionMethodSignaturesSet = new HashSet<String>();
		
		//Feng: added for Intent analysis
		/**
		Set<String> intentMethodSignaturesSet = new HashSet<String>();
		
		for (SootString urlString : urlStringsList) {
			// Set the Map and Set to the correct type
			HashMap<String, Integer> METHOD_SIGNATURE_COUNTS = null;
			Set<String> methodSignaturesSet = null;
			if(urlString.isUrlType()){
				METHOD_SIGNATURE_COUNTS = URL_METHOD_SIGNATURE_COUNTS;
				methodSignaturesSet = urlMethodSignaturesSet;
			}
			else if(urlString.isReflectionType()){
				METHOD_SIGNATURE_COUNTS = REFLECTION_METHOD_SIGNATURE_COUNTS;
				methodSignaturesSet = reflectionMethodSignaturesSet;
			}else if(urlString.isIntentType()){
				//Feng: added for Intent analysis
				METHOD_SIGNATURE_COUNTS = INTENT_METHOD_SIGNATURE_COUNTS;
				methodSignaturesSet = intentMethodSignaturesSet;
			}
			
			ArrayList<String> methodSignatures = urlString.getMethodSignatures();
			for (String methodSignature : methodSignatures) {
				Integer count = METHOD_SIGNATURE_COUNTS.get(methodSignature);
				if(count == null){
					count = new Integer(1);
					METHOD_SIGNATURE_COUNTS.put(methodSignature, count);
				}
				else{
					count =  new Integer(count.intValue() + 1);
					METHOD_SIGNATURE_COUNTS.put(methodSignature, count);
				}
			}
			methodSignaturesSet.addAll(methodSignatures);
		}
		
		for (String distinctMethodSignature : urlMethodSignaturesSet) {
			Integer count = URL_METHOD_SIGNATURE_SPREAD_COUNTS.get(distinctMethodSignature);
			if(count == null){
				count = new Integer(1);
				URL_METHOD_SIGNATURE_SPREAD_COUNTS.put(distinctMethodSignature, count);
			}
			else{
				count =  new Integer(count.intValue() + 1);
				URL_METHOD_SIGNATURE_SPREAD_COUNTS.put(distinctMethodSignature, count);
			}
			
		}

		for (String distinctMethodSignature : reflectionMethodSignaturesSet) {
			Integer count = REFLECTION_METHOD_SIGNATURE_SPREAD_COUNTS.get(distinctMethodSignature);
			if(count == null){
				count = new Integer(1);
				REFLECTION_METHOD_SIGNATURE_SPREAD_COUNTS.put(distinctMethodSignature, count);
			}
			else{
				count =  new Integer(count.intValue() + 1);
				REFLECTION_METHOD_SIGNATURE_SPREAD_COUNTS.put(distinctMethodSignature, count);
			}	
		}

		for (SootString urlString : urlStringsList) {
			HashMap<String, ArrayList<String>> METHOD_SIGNATURE_VALUES = null;
			Set<String> methodSignaturesSet = null;
			if(urlString.isUrlType()){
				METHOD_SIGNATURE_VALUES = URL_METHOD_SIGNATURE_VALUES;
				methodSignaturesSet = urlMethodSignaturesSet;
			}
			else if(urlString.isReflectionType()){
				METHOD_SIGNATURE_VALUES = REFLECTION_METHOD_SIGNATURE_VALUES;
				methodSignaturesSet = reflectionMethodSignaturesSet;
			}else if(urlString.isIntentType()){
				METHOD_SIGNATURE_VALUES = INTENT_METHOD_SIGNATURE_VALUES;
				methodSignaturesSet = intentMethodSignaturesSet;
			}

			// 1. Iterate over the verticies
			Collection<UnitWrapper> verticies = urlString.getDelegateTree().getVertices();
			for (UnitWrapper unitWrapper : verticies) {
				// Iterate over the distinct method signatures.  This should catch each Unit instance
				for (String distinctMethodSignature : methodSignaturesSet) {				
					// If the unit has the signature - see if a text value can be ripped out
					if(unitWrapper.getUnit().toString().contains(distinctMethodSignature)){
						Pattern p = Pattern.compile("\"([^\"]*)\"");
						Matcher m = p.matcher(unitWrapper.getUnit().toString().toString());
						while (m.find()){
							// If a text value can be ripped out - add it in!
							if(m.group(1).length() > 0){
								ArrayList<String> values = METHOD_SIGNATURE_VALUES.get(distinctMethodSignature);
								if(values == null){
									values = new ArrayList<String>(); 
									values.add(m.group(1));
									METHOD_SIGNATURE_VALUES.put(distinctMethodSignature, values);
								}
								else{
									values.add(m.group(1));
								}
							}
						}			
						Unit potentialArgsUnit = unitWrapper.getUnit();

						
						if(potentialArgsUnit instanceof JInvokeStmt){
							JInvokeStmt invokeStmt = (JInvokeStmt)potentialArgsUnit;
							int argCount = invokeStmt.getInvokeExpr().getArgCount();
							if(argCount > 0){
								String argumentValues = getArgumentValuesAsString(urlString.getSootMethod(), distinctMethodSignature, potentialArgsUnit, invokeStmt.getInvokeExpr());
								if(argumentValues.length() > 0){
									ArrayList<String> values = METHOD_SIGNATURE_VALUES.get(distinctMethodSignature);
									if(values == null){
										values = new ArrayList<String>(); 
										values.add(argumentValues);
										METHOD_SIGNATURE_VALUES.put(distinctMethodSignature, values);
									}
									else{
										values.add(argumentValues);
									}
									
								}
							}
						}
						else if(potentialArgsUnit instanceof JAssignStmt){
							JAssignStmt assignStmt = (JAssignStmt)potentialArgsUnit;
							if(assignStmt.getRightOpBox().getValue() instanceof InvokeExpr){
								InvokeExpr invokeExpr = (InvokeExpr)assignStmt.getRightOp();
								int argCount = invokeExpr.getArgCount();
								if(argCount > 0){
									String argumentValues = getArgumentValuesAsString(urlString.getSootMethod(), distinctMethodSignature, potentialArgsUnit, invokeExpr);
									if(argumentValues.length() > 0){
										ArrayList<String> values = METHOD_SIGNATURE_VALUES.get(distinctMethodSignature);
										if(values == null){
											values = new ArrayList<String>(); 
											values.add(argumentValues);
											METHOD_SIGNATURE_VALUES.put(distinctMethodSignature, values);
										}
										else{
											values.add(argumentValues);
										}
										
									}
								}
							}
						}
					}
				}
			}
		
		}		
		*/
	}

	private String getArgumentValuesAsString(SootMethod sootMethod, String distinctMethodSignature, Unit unit, InvokeExpr invokeExpr) {
		StringBuffer argumentValues = new StringBuffer();
		List<Value> arguments = invokeExpr.getArgs();
		int count = invokeExpr.getArgCount();
		for(int i = 0; i < count; i++){
			if(invokeExpr.getArg(i) instanceof JimpleLocal){
				for(DelegateTree<UnitWrapper, String> tree : graphSummariesMap.get(sootMethod)){
					if(tree.getRoot().getUnit().equals(unit) && tree.getRoot().getArgumentIndex() == i){
						logger.debug("hold here as its great you got here");
						ArrayList<String> classAssignStrings = StringExtractionUtils.harvestString(tree);
						argumentValues.append(SootString.getMatchesAsStringFromList(classAssignStrings, true, false));
					}
				}
			}
		}
		return argumentValues.toString();
	}

	private ArrayList<DelegateTree<UnitWrapper, String>> splitTrees(DelegateTree<UnitWrapper, String> tree,
			UnitWrapper parent) {
		Collection<UnitWrapper> children = tree.getChildren(parent);
		ArrayList<DelegateTree<UnitWrapper, String>> trees = new ArrayList<DelegateTree<UnitWrapper,String>>();
		if(parent.getUnit() instanceof JIdentityStmt){
			JIdentityStmt jIdentityStmt = (JIdentityStmt)parent.getUnit();
			if(jIdentityStmt.getRightOp() instanceof ParameterRef){
				if(children.size() > 0){
					int childrenSize = children.size(); 
					HashMap<String, DelegateTree<UnitWrapper, String>> subTrees = new HashMap<String, DelegateTree<UnitWrapper,String>>();
					//1. make four subtrees
					for(UnitWrapper childUnitWrapper : children){
						try {
							//2. be sure to save the edge name!
							DelegateTree<UnitWrapper, String> subTree = (DelegateTree<UnitWrapper, String>)TreeUtils.getSubTree(tree, childUnitWrapper);
							subTrees.put(tree.getParentEdge(childUnitWrapper), subTree);
							logger.debug("Here is the new tree");
						} catch (InstantiationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					for(int i = 0; i < childrenSize; i++){
						DelegateTree<UnitWrapper, String> newTree = new DelegateTree<UnitWrapper, String>();
						newTree.addVertex(tree.getRoot());
						copyTree(tree.getRoot(), tree, newTree);
						trees.add(newTree);
					}
					
					
					Object[] edges = subTrees.keySet().toArray();
					for (int i = 0; i < edges.length; i++) {
						DelegateTree<UnitWrapper, String> newTree = trees.get(i);
						for (int j = 0; j < edges.length; j++) {
							if(j != i){
								DelegateTree<UnitWrapper, String> subTree = subTrees.get(edges[j]);
								Collection<String> edgesToRemove = subTree.getEdges();
								for (String edgeToRemove : edgesToRemove) {
									newTree.removeEdge(edgeToRemove);
								}
								Collection<UnitWrapper> veticiesToRemove = subTree.getVertices();
								for (UnitWrapper unitWrapper : veticiesToRemove) {
									newTree.removeVertex(unitWrapper);
								}								
							}
						}
					}
					
					logger.debug("If this works ...");

				}
			}
		}
		
		if(trees.size() > 0){
			ArrayList<DelegateTree<UnitWrapper, String>> masterAdditionalTrees = new ArrayList<DelegateTree<UnitWrapper,String>>();
			for (int i = 0; i < trees.size(); i++) {
				ArrayList<DelegateTree<UnitWrapper, String>> additionalTrees = splitTrees(trees.get(i), (UnitWrapper)children.toArray()[i]);
				//
				if(additionalTrees.size() > 0){
					masterAdditionalTrees.addAll(additionalTrees);
				}
				else{
					masterAdditionalTrees.add(trees.get(i));
				}
			}
			return masterAdditionalTrees;
		}
		//Start looking at the children
		else if(children != null && children.size() == 1){
			// recursion with no operations
			return splitTrees(tree, (UnitWrapper)children.toArray()[0]);
		}
		else{
			// base case
			return trees;
		}
	}
	
	public static void copyTree(UnitWrapper parent,
			DelegateTree<UnitWrapper, String> tree,
			DelegateTree<UnitWrapper, String> newTree) {
		Collection<UnitWrapper> children = tree.getChildren(parent);
		if(children != null){
			for (UnitWrapper child : children) {
				newTree.addChild(tree.getParentEdge(child), parent, child);
				copyTree(child, tree, newTree);
			}			
		}
	}

	public int getTotalStringCount(){
		return totalStringCount;
	}
	
	private void calculateTotalNumberOfStrings() {
		for(ArrayList<DelegateTree<UnitWrapper, String>> delegateTrees : graphSummariesMap.values()){
			for (DelegateTree<UnitWrapper, String> delegateTree : delegateTrees) {
				if(isNotRootNode(delegateTree)){
					totalStringCount++;					
				}				
			}
		}
	}

	public static boolean isNotRootNode(DelegateTree<UnitWrapper, String> delegateTree) {
		//return !(delegateTree.getRoot() instanceof JReturnStmt);
		return true;
	}

	public Map<SootMethod, ExceptionalUnitGraph> getEugMap(){
		return eugMap;
	}
	
	public String getApkName() {
		return apkName;
	}

	public Map<SootMethod, Map<Unit, ArraySparseSet>> getSummariesMap() {
		return summariesMap;
	}

	public Map<SootMethod, ArrayList<DelegateTree<UnitWrapper, String>>> getGraphSummariesMap() {
		return graphSummariesMap;
	}

	public long getRunTime() {
		return runTime;
	}

	public void setRelativeHtmlFileLocation(String newFileName) {
		relativeHtmlFileName = newFileName;
	}

	public String getRelativeHtmlFileLocation() {
		return relativeHtmlFileName;
	}

	public List<SootString> getSootStrings() {
		return urlStringsList;
	}
	
//	public ArrayList<UrlObject> getUrlforUnit(SootMethod sootMethod, Unit unit, Type {content_provider_uri}){
//		String url = "";
//		return url;
//	}

	
}
