package edu.buffalo.cse.blueseal.networkflow.interprocedural;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import soot.Body;
import soot.Main;
import soot.PatchingChain;
import soot.SootClass;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.javaToJimple.jj.Version;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.ParameterRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.internal.IdentityRefBox;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.jimple.internal.JStaticInvokeExpr;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import sun.awt.SubRegionShowable;
import edu.buffalo.cse.blueseal.BSFlow.AbstractInterproceduralAnalysis;
import edu.buffalo.cse.blueseal.BSFlow.BSBackwardFlowAnalysis;
import edu.buffalo.cse.blueseal.BSFlow.BSForwardFlowAnalysis;
import edu.buffalo.cse.blueseal.BSFlow.DirectedCallGraph;
import edu.buffalo.cse.blueseal.BSFlow.InterProceduralMain;
import edu.buffalo.cse.blueseal.BSFlow.SootMethodFilter;
import edu.buffalo.cse.blueseal.networkflow.intraprocedural.NetworkFlowMethodAnalysis;
import edu.buffalo.cse.blueseal.networkflow.intraprocedural.NetworkFlowUnitSummary.UrlConstructor;
import edu.buffalo.cse.blueseal.networkflow.summary.ApkAnalysisSummary;
import edu.buffalo.cse.blueseal.networkflow.summary.StringExtractionUtils;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.TreeUtils;

public class NetworkFlowInterproceduralAnalysis extends
	soot.jimple.toolkits.annotation.purity.AbstractInterproceduralAnalysis {
	
	private void holdOnThisMethod(SootMethod method) {
		String signature = method.getDeclaringClass().getName() + "." + method.getName();
		// For gold std tests
		//if(signature.contains("com.passionteam.lightdd.a.b: int b(java.lang.String)")){
		if(method.getSignature().contains("com.geinimi.AdService: java.lang.String f()")){
			logger.debug("We need to hold here");
		}
		else{
			logger.debug("We have left this method");
		}
	}

	
	public static int GLOBAL_COUNTER = 0;
	private NetworkFlowSummary nfm;
	private ArrayList<DelegateTree<UnitWrapper, String>> allJungGraphs;
	private HashSet<DelegateTree<UnitWrapper, String>> allConnectableJungGraphs = new HashSet<DelegateTree<UnitWrapper, String>>();
	private ArrayList<DelegateTree<UnitWrapper, String>> returnJungGraphs = new ArrayList<DelegateTree<UnitWrapper, String>>();
	private ArrayList<DelegateTree<UnitWrapper, String>> parameterJungGraphs = new ArrayList<DelegateTree<UnitWrapper, String>>();
	private ArrayList<DelegateTree<UnitWrapper, String>> successorJungGraphs = new ArrayList<DelegateTree<UnitWrapper, String>>();
	private ArrayList<DelegateTree<UnitWrapper, String>> urlJungGraphs = new ArrayList<DelegateTree<UnitWrapper, String>>();
	private SootMethod sootmethod;
	public static ArrayList<Exception> exceptions = new ArrayList<Exception>();
	public static int successes = 0;
	public static Map<SootMethod, ExceptionalUnitGraph> EUG_MAP = new HashMap<SootMethod, ExceptionalUnitGraph>(); 
	private static Map<String, ArrayList<DelegateTree<UnitWrapper, String>>> STATIC_FIELD_GRAPHS =
			new HashMap<String, ArrayList<DelegateTree<UnitWrapper, String>>>();
	
	public static HashSet<DelegateTree<UnitWrapper, String>> MERGED_TREES = new HashSet<DelegateTree<UnitWrapper,String>>(); 
	
	static Logger logger = Logger.getLogger(NetworkFlowInterproceduralAnalysis.class);
	
	
	//the following map maintains method and its intraprocedural analysis result
	public static Map<SootMethod, Map<Unit, ArraySparseSet>> METHOD_SUMMARY = 
			new HashMap<SootMethod, Map<Unit, ArraySparseSet>>();

	//the following map maintains method and its intraprocedural analysis result
	public static Map<SootMethod, ArrayList<DelegateTree<UnitWrapper, String>>> GRAPH_SUMMARY = 
			new HashMap<SootMethod, ArrayList<DelegateTree<UnitWrapper, String>>>();


	// Purely for test ... what does have strings
	public static Map<String, ArrayList<UnitWrapper>> STRING_BEARING_UNITS = 
			new HashMap<String, ArrayList<UnitWrapper>>();

	
    //private DirectedGraph ourDirectedGraph;             // filtered trimed call-graph
    public static  Object objecty = new Object();
    public static List<NetworkFlowMethodAnalysis> NFMA = new ArrayList<NetworkFlowMethodAnalysis>();
	private static String fileContents = "";

	public ArrayList<DelegateTree<UnitWrapper, String>> getUrlJungGraphs() {
		return urlJungGraphs;
	}

	public ArrayList<DelegateTree<UnitWrapper, String>> getReturnJungGraphs() {
		return returnJungGraphs;
	}

	public ArrayList<DelegateTree<UnitWrapper, String>> getParameterJungGraphs() {
		return parameterJungGraphs;
	}

	public ArrayList<DelegateTree<UnitWrapper, String>> getSuccessorJungGraphs() {
		return successorJungGraphs;
	}
	
	public HashSet<DelegateTree<UnitWrapper, String>> getAllConnectableJungGraphs() {
		return allConnectableJungGraphs;
	}
	
	public static void addUnitIfStringBearing(UnitWrapper unit){
		String string = StringExtractionUtils.extractStringFromUnit(unit);
		if(!(string == StringExtractionUtils.BEARS_STRING)){
			String key = unit.getClass().toString();
			ArrayList<UnitWrapper> unitList = STRING_BEARING_UNITS.get(key);
			if(unitList != null){
				unitList.add(unit);
			}
			else{
				ArrayList<UnitWrapper> units = new ArrayList<UnitWrapper>();
				units.add(unit);
				STRING_BEARING_UNITS.put(key, units);				
			}
		}
	}
	
	
	public static String getSummaryStatisticsAsString(){
		return fileContents;
	}

	/**
	 * Provides a List of all NetworkFlowMethodAnalysis for all methods in APK.  If method has URL - you can trace path to all successor methods.
	 * @return
	 */
	public List<NetworkFlowMethodAnalysis> getNfma() {
		return NFMA;
	}

	public static Map<SootMethod, ExceptionalUnitGraph> getEugMap() {
		return EUG_MAP;
	}

	
	public NetworkFlowSummary getNetworkFlowSummary(){
		return nfm;
	}
	
	//AbstractInterproceduralAnalysis(CallGraph cg, SootMethodFilter filter, Iterator heads, boolean verbose) 
	public NetworkFlowInterproceduralAnalysis(CallGraph cg,
			soot.jimple.toolkits.annotation.purity.SootMethodFilter sootMethodFilter, Iterator heads, boolean verbose) {

		
		
		
		
		
		super(cg, sootMethodFilter, heads, verbose);
		METHOD_SUMMARY.clear();
		GRAPH_SUMMARY.clear();
		doAnalysis(true);
	}

	@Override
	protected Object newInitialSummary() {
		logger.debug("Called newInitialSummary()");
		ArrayList<DelegateTree<UnitWrapper, String>> newGraph = new ArrayList<DelegateTree<UnitWrapper, String>>();
		nfm = new NetworkFlowSummary(newGraph);
		return nfm;
	}

	@Override
	protected Object summaryOfUnanalysedMethod(SootMethod method) {
		logger.debug("Called summaryOfUnanalyzedMethod()");
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	protected void analyseMethod(SootMethod method, Object dst) {
		logger.debug("Called analyseMethod()");
		sootmethod = method;
		Body body = method.retrieveActiveBody();
		ExceptionalUnitGraph eug = new ExceptionalUnitGraph(body);
		List<SootMethod> successorGraph = dg.getSuccsOf(method);
		for (SootMethod sootMethod : successorGraph) {
			//logger.error(sootMethod.getSignature());
			if(sootMethod.getSignature().contains("com.mobclick.android.a: int a(java.lang.String,java.lang.String)")){
				logger.error(sootMethod.getSignature());				
			}
		}
		holdOnThisMethod(method);
		NetworkFlowBackwardFlowAnalysis res = new NetworkFlowBackwardFlowAnalysis(eug, successorGraph, method);		
		Map<Unit, ArraySparseSet> unitSummary = res.getUnitToSet();
		logger.debug("About to add a summary to methodSummary.");
		METHOD_SUMMARY.put(method, unitSummary);
		allJungGraphs = res.getGraph();
		
		allConnectableJungGraphs = new  HashSet<DelegateTree<UnitWrapper, String>>();
		returnJungGraphs.clear();
		parameterJungGraphs.clear();
		successorJungGraphs.clear();
		urlJungGraphs.clear();
		
		classifyGraphType(method, successorGraph);
		GRAPH_SUMMARY.remove(method);
		ArrayList<DelegateTree<UnitWrapper, String>> allConnectedJungGraphsList = new ArrayList<DelegateTree<UnitWrapper,String>>(allConnectableJungGraphs);
		GRAPH_SUMMARY.put(method, allConnectedJungGraphsList);
		nfm.setGraphs(allJungGraphs);
		if(EUG_MAP.get(method) == null){
			EUG_MAP.put(method, eug);
		}
		else{
			Set<SootMethod> storedMethod = EUG_MAP.keySet();
			logger.debug("Should not happen");
		}
		if(method.getSignature().contains("<init>()")){
			connectConstructorsToStringCallGraphs(method);
		}
		for(DelegateTree<UnitWrapper, String> currentMethodConnectableJungGraph: allConnectableJungGraphs){
			Collection<UnitWrapper> verticies = currentMethodConnectableJungGraph.getVertices();
			logger.debug("About to see if out DelegateTree can be merged with size: " + verticies.size());
			ArrayList<ArrayList<Object>> additions = new ArrayList<ArrayList<Object>>();
			ArrayList<ArrayList<Object>> initAdditions = new ArrayList<ArrayList<Object>>();
			ArrayList<ArrayList<Object>> paramAdditions = new ArrayList<ArrayList<Object>>();
			
			for(UnitWrapper currentMethodUnitWrapper : verticies){
			if(currentMethodUnitWrapper.toString().contains("execUpBin")){
				logger.debug("What units even mention this ... ");
			}
				Unit unit = currentMethodUnitWrapper.getUnit();
				//TODO What about for a Virutal Invoke ...
				if(unit instanceof JInvokeStmt){
					JInvokeStmt jInvokeStmt = (JInvokeStmt)unit;
					logger.debug("Have an invoke stmt");
					if(jInvokeStmt.getInvokeExprBox().getValue() instanceof InvokeExpr){
						InvokeExpr jSpecialInvokeExpr = (InvokeExpr)jInvokeStmt.getInvokeExprBox().getValue();
						
						if(jSpecialInvokeExpr.getArgCount() > 0){
							SootMethod specialInvokeMethod = jSpecialInvokeExpr.getMethod();
							if(specialInvokeMethod.getSignature().equals("com.passionteam.lightdd.CoreService: boolean a(java.lang.String,java.lang.String,android.os.Handler)")){
								logger.debug("hold here");
							}
							// If in the current graph for this method we find a pointer out to method call AND that method
							// call is in the list of successor graphs - proceed.
							if(successorGraph.contains(specialInvokeMethod)){
								// Get all the trees for the successor method.
								ArrayList<DelegateTree<UnitWrapper, String>> successorMethodTrees = NetworkFlowInterproceduralAnalysis.GRAPH_SUMMARY.get(specialInvokeMethod);
								int argCount = jSpecialInvokeExpr.getArgCount();
								for(int count = 0; count < argCount; count++){
									// Get the JimpleLocal for the pointer out method for the exact argument
									Value value = jSpecialInvokeExpr.getArg(count);
									if(value instanceof JimpleLocal){
										JimpleLocal jimpleLocal = (JimpleLocal)value;
										if(specialInvokeMethod != null){
											if(successorMethodTrees != null){
												// Iterate over the successor trees
												for(DelegateTree<UnitWrapper, String> succcessorTree : successorMethodTrees){
													Collection<UnitWrapper> unitWrappers = succcessorTree.getVertices();
													// Get each unit for the successor tree
													for (UnitWrapper successorTreeUnitWrapper : unitWrappers) {
														Unit succesorTreeArgUnit = successorTreeUnitWrapper.getUnit();
														if(succcessorTree.getChildCount(successorTreeUnitWrapper) == 0 &&
																succesorTreeArgUnit instanceof JIdentityStmt){
															JIdentityStmt successorTreeJIdentityStmt = (JIdentityStmt)succesorTreeArgUnit;
															if(successorTreeJIdentityStmt.getRightOp() instanceof ParameterRef){
																ParameterRef succesorTreeParameterRef = (ParameterRef)successorTreeJIdentityStmt.getRightOp();
																if(succesorTreeParameterRef.getIndex() == currentMethodUnitWrapper.getArgumentIndex() &&
																		succesorTreeParameterRef.getIndex() == count){
																	if(currentMethodUnitWrapper.toString().contains("println")){
																		logger.debug("Odd that we are adding this ... ");
																	}

																	ArrayList<Object> subtreeAdd = new ArrayList<Object>();
																	subtreeAdd.add(succcessorTree);
																	subtreeAdd.add(currentMethodConnectableJungGraph);
																	subtreeAdd.add(successorTreeUnitWrapper);
																	subtreeAdd.add(currentMethodUnitWrapper);
																	subtreeAdd.add(method);
																	initAdditions.add(subtreeAdd);	
																	break;
																}														
															}
														}
													}
												}																							
											}
										}
									}
								}	
							}									
						}
					}
				}
				else if(unit instanceof JAssignStmt){
					JAssignStmt jAssignStmt = (JAssignStmt)unit;
					// have to do it for a StaticFieldRef
					if(jAssignStmt.getRightOp() instanceof JInstanceFieldRef){
						handleInstanceFieldRef(method,
								currentMethodConnectableJungGraph, verticies,
								initAdditions, currentMethodUnitWrapper,
								jAssignStmt);
					}
					else if(jAssignStmt.getRightOp() instanceof JSpecialInvokeExpr ||
							jAssignStmt.getRightOp() instanceof JVirtualInvokeExpr){
						
						handleVirtualAndSpecialInvokeExpr(successorGraph,
								currentMethodConnectableJungGraph, additions,
								currentMethodUnitWrapper, unit, jAssignStmt, paramAdditions);
					}
					else if(jAssignStmt.getRightOp() instanceof IdentityRefBox){
						IdentityRefBox identityRefBox = (IdentityRefBox)jAssignStmt.getRightOp();
						if(identityRefBox.getValue() instanceof ParameterRef){
							ParameterRef parameterRef = (ParameterRef)identityRefBox.getValue();
							logger.debug("one step closa!");
						}						
					}
					else if(jAssignStmt.getRightOpBox().getValue() instanceof JStaticInvokeExpr){
						JStaticInvokeExpr jStaticInvokeExpr = (JStaticInvokeExpr)jAssignStmt.getRightOpBox().getValue();
						SootMethodRef sootMethodRef = jStaticInvokeExpr.getMethodRef();
						for(SootMethod succesorMethod : successorGraph){
							// If you can prove that one of the successor meths has the same signuature as an IMMEDIATE outgoing link, try and connect it
							if(succesorMethod.getSignature().equals(sootMethodRef.getSignature())){
								if(succesorMethod.getSignature().contains("getInputStream")){
									logger.debug("best we can do");
								}
								ArrayList<DelegateTree<UnitWrapper, String>> successorJungGraphs = GRAPH_SUMMARY.get(succesorMethod);
								if(successorJungGraphs != null){
									logger.debug("Successor methref is: " + succesorMethod.getSignature() + " with this many graphs within: " + successorJungGraphs.size());
									int callNumber = 0;
									//Iterate over each the successor meths graphs
									for(DelegateTree<UnitWrapper, String> successorJungGraph : successorJungGraphs){
										for (UnitWrapper successorUnitWrapper : successorJungGraph.getVertices()) {
											logger.debug("we stop here");
											if(successorUnitWrapper.getUnit() instanceof JReturnStmt){
												logger.debug("here we go");
												
												
												
												logger.debug("I WOULD HAVE CONNECTED HERE");
												//jungGraph.addEdge(unitOutset.toString(), unit, unitOutset);
												logger.debug("Successor vertex count: " + successorJungGraph.getVertexCount());
												logger.debug("Current vertex count: " + currentMethodConnectableJungGraph.getVertexCount());
												logger.debug("Unit connecting out is: " + unit);
												logger.debug("Unit connecting to is: " + successorJungGraph.getRoot());
												logger.debug("Successor graph root node is: " + successorJungGraph.getRoot());
												//GraphPrinter.prettyPrint(successorJungGraph, succesorMethod);
												ArrayList<Object> subtreeAdd = new ArrayList<Object>();
												if(currentMethodUnitWrapper.toString().contains("println")){
													logger.debug("Odd that we are adding this ... ");
												}

												subtreeAdd.add(currentMethodConnectableJungGraph);
												subtreeAdd.add(successorJungGraph);
												subtreeAdd.add(currentMethodUnitWrapper);
												subtreeAdd.add(succesorMethod.getSignature()+unit.toString());
												subtreeAdd.add(succesorMethod);
												additions.add(subtreeAdd);
												logger.debug(currentMethodConnectableJungGraph.getRoot());																																				

												
												
												
												
												
												
												
												
												
											}
											else if(successorUnitWrapper.getUnit() instanceof JIdentityStmt){
												//OHhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh we needs to handles the returns!!!!!!!!!!!!!!!!
												JIdentityStmt successorJIdentityStmt = (JIdentityStmt)successorUnitWrapper.getUnit();
												if(successorJIdentityStmt.getRightOp() instanceof ParameterRef){
													ParameterRef successorParameterRef = (ParameterRef)successorJIdentityStmt.getRightOp();
													//NEED AN ADDITIONAL CHECK HERE TO MAKE SURE IT IS THE RIGHT PARAMETER!!!
													if(currentMethodUnitWrapper.getArgumentIndex() == successorParameterRef.getIndex()){
														//SEE THAT IT HAS NO CHILDEN
														if(successorUnitWrapper.getSootMethod().getSignature().equals(succesorMethod.getSignature())){
															ArrayList<Object> subtreeAdd = new ArrayList<Object>();
															subtreeAdd.add(currentMethodConnectableJungGraph);
															subtreeAdd.add(successorJungGraph);
															subtreeAdd.add(currentMethodUnitWrapper);
															subtreeAdd.add(successorUnitWrapper);
															subtreeAdd.add(method);
															paramAdditions.add(subtreeAdd);				
															break;															
														}
													}
												}
												else{
													logger.debug("What else can a JIdentityStmt wrap .... ");
												}
											}
										}
									}	
								}
							}
						}
					}
					else if(jAssignStmt.toString().contains("parameter"));{
						logger.debug("stop here");
					}
				}
			}			
			
			for (ArrayList<Object> arrayList : initAdditions) {
				DelegateTree<UnitWrapper, String> treeIn = (DelegateTree<UnitWrapper, String>)arrayList.get(0);
				DelegateTree<UnitWrapper, String> subTreeIn = (DelegateTree<UnitWrapper, String>)arrayList.get(1);
				UnitWrapper leafVertexOfTreeIn = (UnitWrapper)arrayList.get(2);
				UnitWrapper rootVertextOfSubTreeIn = (UnitWrapper)arrayList.get(3);
				SootMethod subTreeSootMethod = (SootMethod)arrayList.get(4);
				// TODO should I remove the tree ...
				//trees.remove(index)
				try {
					//From the current tree - harvest out a subtree
					//Then conflate with other tree
					DelegateTree<UnitWrapper, String> subTree = (DelegateTree<UnitWrapper, String>)TreeUtils.getSubTree(subTreeIn, rootVertextOfSubTreeIn);
					DelegateTree<UnitWrapper, String> newTree = new DelegateTree<UnitWrapper, String>();
					newTree.addVertex(treeIn.getRoot());
					ApkAnalysisSummary.copyTree(treeIn.getRoot(), treeIn, newTree);
					TreeUtils.addSubTree(newTree, subTree, leafVertexOfTreeIn, createEdge("Line627",subTreeSootMethod));
					
					ArrayList<DelegateTree<UnitWrapper, String>> specialInvokeTrees = NetworkFlowInterproceduralAnalysis.GRAPH_SUMMARY.get(subTreeSootMethod);
					
					MERGED_TREES.add(treeIn);
					// CRITICAL CHANGE
					specialInvokeTrees.remove(subTreeIn);
					//remove tree in from this
					specialInvokeTrees.add(newTree);

					logger.error(StringExtractionUtils.prettyPrint(treeIn, method));
					logger.error("What is treeIn now???");					
				} catch (InstantiationException e) {
					StringExtractionUtils.prettyPrint(treeIn, leafVertexOfTreeIn.getSootMethod());
					StringExtractionUtils.prettyPrint(subTreeIn, leafVertexOfTreeIn.getSootMethod());
					//e.printStackTrace();
				} catch (IllegalAccessException e) {
					StringExtractionUtils.prettyPrint(treeIn, leafVertexOfTreeIn.getSootMethod());
					StringExtractionUtils.prettyPrint(subTreeIn, leafVertexOfTreeIn.getSootMethod());
					//e.printStackTrace();
				} catch (IllegalArgumentException e) {
					StringExtractionUtils.prettyPrint(treeIn, leafVertexOfTreeIn.getSootMethod());
					StringExtractionUtils.prettyPrint(subTreeIn, leafVertexOfTreeIn.getSootMethod());
					//e.printStackTrace();
				}

			}

			
			for (ArrayList<Object> arrayList : paramAdditions) {
				DelegateTree<UnitWrapper, String> subTree = (DelegateTree<UnitWrapper, String>)arrayList.get(0);
				DelegateTree<UnitWrapper, String> tree = (DelegateTree<UnitWrapper, String>)arrayList.get(1);
				UnitWrapper useless = (UnitWrapper)arrayList.get(2);
				UnitWrapper connectUnitWrapper = (UnitWrapper)arrayList.get(3);
				SootMethod sootMethod = (SootMethod)arrayList.get(4);
				// TODO should I remove the tree ...
				//trees.remove(index)
				try {
					StringExtractionUtils.prettyPrint(tree, connectUnitWrapper.getSootMethod());
					StringExtractionUtils.prettyPrint(subTree, connectUnitWrapper.getSootMethod());
					TreeUtils.addSubTree(tree, subTree, connectUnitWrapper, createEdge("Line661",sootMethod));												
					
				}catch (IllegalArgumentException e){
					StringExtractionUtils.prettyPrint(tree, connectUnitWrapper.getSootMethod());
					StringExtractionUtils.prettyPrint(subTree, connectUnitWrapper.getSootMethod());
					//e.printStackTrace();						
				}
				
			}
			
			for(ArrayList<Object> addition: additions){
				logger.debug("In additions - about to grow the tree where: ");
				logger.debug("Successor method ref is: " + (String)addition.get(3));
				logger.debug("number of successor verticies: " + ((DelegateTree<UnitWrapper, String>)addition.get(1)).getVertexCount());
				logger.debug("number of preds verticies: " + ((DelegateTree<UnitWrapper, String>)addition.get(0)).getVertexCount());
				DelegateTree<UnitWrapper, String> tree = (DelegateTree<UnitWrapper, String>)addition.get(0);
				DelegateTree<UnitWrapper, String> subTree = (DelegateTree<UnitWrapper, String>)addition.get(1);
				UnitWrapper unitWrapper = (UnitWrapper)addition.get(2);
				String edgeName = (String)addition.get(3) + NetworkFlowInterproceduralAnalysis.GLOBAL_COUNTER++;
				//This will not work!!!  I do not think!
				String edge = tree.findEdge(unitWrapper, subTree.getRoot());
				logger.debug("********************");
				logger.debug("PARENT TREE");
				logger.debug("********************");
				StringExtractionUtils.prettyPrint(tree, method);
				logger.debug("********************");
				logger.debug("SUBTREE");
				logger.debug("********************");
				StringExtractionUtils.prettyPrint(subTree, (SootMethod)addition.get(4));
				logger.debug("edge is called " + edge);
				//Check to see that they are not already combined
				//If there is no connecting edge - add it
				if(unitWrapper.toString().contains("println")){
					logger.debug("Odd that we are adding this ... ");
				}

				try{
					if(((SootMethod)addition.get(4)).getSignature().equals(method.getSignature())){
						logger.debug("Not good ..... not sure why the method itself is included in here.");
					}
					else if(edge == null || edge.equals("null")){
						if(tree.containsVertex(subTree.getRoot())){
							if(subTree.getRoot().toString().contains("println")){
								logger.debug("Here we go");
							}
							logger.debug("This tree has already been added - just connect vertices");
							logger.debug("Node added via subtree is: " + subTree.getRoot());
							String oldEdgeName = tree.findEdge(unitWrapper, subTree.getRoot());
							tree.removeEdge(oldEdgeName);
							String newEdgeName = oldEdgeName + "edge:" + edgeName;
							logger.debug("Creating multiple edges: " + newEdgeName);
							if(method.getSignature().contains("com.iease.logic.MainService: void doTask(com.iease.logic.Task")){
								logger.debug("Stop here");
							}
							tree.addEdge(createEdge("Line568",(SootMethod)addition.get(4)), unitWrapper, subTree.getRoot());
						}
						else{
							logger.debug("These trees must grow together.");
							if(method.getSignature().contains("com.iease.logic.MainService: void doTask(com.iease.logic.Task")){
								logger.debug("Stop here");
							}
							TreeUtils.addSubTree(tree, subTree, unitWrapper, createEdge("Line575", (SootMethod)addition.get(4)));												
						}
					}
					//If the edge does exist - make sure it is not the same one
					else if(! edge.equals(edgeName)){
						logger.debug("edge is called " + edge);
						logger.debug("edgename is called " + edgeName);
						if(method.getSignature().contains("com.iease.logic.MainService: void doTask(com.iease.logic.Task")){
							logger.debug("Stop here");
						}
						TreeUtils.addSubTree(tree, subTree, unitWrapper, createEdge("Line585",(SootMethod)addition.get(4)));										
					}
					else{
						logger.debug("I would not have combined trees here ... which needs further investigation.");
						throw new IllegalArgumentException("Custom case where trees not combined");
					}
					logger.debug("number of preds verticies post add: " + ((DelegateTree<Unit, String>)addition.get(0)).getVertexCount());		
					successes++;
				}
				catch(Exception e){
					StringExtractionUtils.prettyPrint(tree, unitWrapper.getSootMethod());
					StringExtractionUtils.prettyPrint(subTree, unitWrapper.getSootMethod());
					//e.printStackTrace();
					exceptions.add(e);
					logger.debug(e.getMessage());
					logger.debug("life goes on ... ");
					
					Collection<UnitWrapper> verts = subTree.getVertices();
					for (UnitWrapper unit2 : verts) {
						if(tree.containsVertex(unit2)){
							logger.debug("I have this vertex: " + unit2);
						}
						else{
							logger.debug("I DO NOT have this vertex: " + unit2);							
						}
						
					}
				}
			}

		}
	}

	private void handleVirtualAndSpecialInvokeExpr(
			List<SootMethod> successorGraph,
			DelegateTree<UnitWrapper, String> currentMethodConnectableJungGraph,
			ArrayList<ArrayList<Object>> additions,
			UnitWrapper currentMethodUnitWrapper, Unit unit,
			JAssignStmt jAssignStmt, ArrayList<ArrayList<Object>> paramAdditions) {
		SootMethodRef sootMethodRef = getMethodRef(jAssignStmt);
		logger.debug("Special invokemethref is: " + sootMethodRef);
		for(SootMethod succesorMethod : successorGraph){
			// If you can prove that one of the successor meths has the same signuature as an IMMEDIATE outgoing link, try and connect it
			if(succesorMethod.getSignature().equals(sootMethodRef.getSignature())){
				ArrayList<DelegateTree<UnitWrapper, String>> successorJungGraphs = GRAPH_SUMMARY.get(succesorMethod);
				if(successorJungGraphs != null){
					logger.debug("Successor methref is: " + succesorMethod.getSignature() + " with this many graphs within: " + successorJungGraphs.size());
					int callNumber = 0;
					//Iterate over each the successor meths graphs
					for(DelegateTree<UnitWrapper, String> successorJungGraph : successorJungGraphs){
						callNumber++;
						logger.debug("In call to successorgraph" + callNumber);		
						if(successorJungGraph.getRoot().getUnit() instanceof JReturnStmt){
						//Collection<Unit> collection = successorJungGraph.getVertices();
						//for (Unit unitOutset : collection) {
						//	if(unitOutset instanceof JReturnStmt){
								// So .... this is a problem if it is tucked within  ... for sure
								if(! currentMethodConnectableJungGraph.containsVertex(successorJungGraph.getRoot())){
									logger.debug("I WOULD HAVE CONNECTED HERE");
									//jungGraph.addEdge(unitOutset.toString(), unit, unitOutset);
									logger.debug("Successor vertex count: " + successorJungGraph.getVertexCount());
									logger.debug("Current vertex count: " + currentMethodConnectableJungGraph.getVertexCount());
									logger.debug("Unit connecting out is: " + unit);
									logger.debug("Unit connecting to is: " + successorJungGraph.getRoot());
									logger.debug("Successor graph root node is: " + successorJungGraph.getRoot());
									//GraphPrinter.prettyPrint(successorJungGraph, succesorMethod);
									ArrayList<Object> subtreeAdd = new ArrayList<Object>();
									if(currentMethodUnitWrapper.toString().contains("println")){
										logger.debug("Odd that we are adding this ... ");
									}

									subtreeAdd.add(currentMethodConnectableJungGraph);
									subtreeAdd.add(successorJungGraph);
									subtreeAdd.add(currentMethodUnitWrapper);
									subtreeAdd.add(succesorMethod.getSignature()+unit.toString());
									subtreeAdd.add(succesorMethod);
									additions.add(subtreeAdd);
									logger.debug(currentMethodConnectableJungGraph.getRoot());																																				
								}
								else{
									logger.debug("These two graphs were already connected.");
								}	
						}
						else{
							for (UnitWrapper successorUnitWrapper : successorJungGraph.getVertices()) {
								logger.debug("we stop here");
								if(successorUnitWrapper.getUnit() instanceof JIdentityStmt){
									
									JIdentityStmt successorJIdentityStmt = (JIdentityStmt)successorUnitWrapper.getUnit();
									if(successorJIdentityStmt.getRightOp() instanceof ParameterRef){
										ParameterRef successorParameterRef = (ParameterRef)successorJIdentityStmt.getRightOp();
										if(currentMethodUnitWrapper.getArgumentIndex() == successorParameterRef.getIndex()){
											if(currentMethodUnitWrapper.toString().contains("println")){
												logger.debug("Odd that we are adding this ... ");
											}
											ArrayList<Object> subtreeAdd = new ArrayList<Object>();
											subtreeAdd.add(currentMethodConnectableJungGraph);
											subtreeAdd.add(successorJungGraph);
											subtreeAdd.add(currentMethodUnitWrapper);
											subtreeAdd.add(successorUnitWrapper);
											subtreeAdd.add(sootmethod);
											paramAdditions.add(subtreeAdd);				
											break;
										}
									}
									else{
										logger.debug("What else can a JIdentityStmt wrap .... ");
									}
								}
							}

							
						}	
					}
				}
				else{
					logger.debug("TODO to handle this");
				}
			}
		}
	}

	private void handleInstanceFieldRef(
			SootMethod method,
			DelegateTree<UnitWrapper, String> currentMethodConnectableJungGraph,
			Collection<UnitWrapper> verticies,
			ArrayList<ArrayList<Object>> initAdditions,
			UnitWrapper currentMethodUnitWrapper, JAssignStmt jAssignStmt) {
		logger.debug("Go out and find that class ... ");
		JInstanceFieldRef jInstanceFieldRef = (JInstanceFieldRef)jAssignStmt.getRightOp();
		SootFieldRef sootFieldRef = jInstanceFieldRef.getFieldRef();
		logger.debug("Got the ref");
		SootClass sootClass = sootFieldRef.declaringClass();
		String className = sootClass.getName();
		String signature = sootFieldRef.getSignature();
		logger.debug("Classname is: " + className);
		logger.debug("Signature: " + signature);
			Set<SootMethod> keys = NetworkFlowInterproceduralAnalysis.GRAPH_SUMMARY.keySet();
			boolean found = false;
			for (SootMethod sootMethod : keys) {
				//<firstapplication.MainActivity: void <init>()>
				//logger.debug(sootMethod.getDeclaringClass().getName());
				if(sootMethod.getDeclaringClass().getName().equals(className)){
					logger.debug("we need to match on something");
					//logger.debug(sootMethod.getSignature());
					if(sootMethod.getSignature().equals("<init>()")){
						logger.debug("we need to match on something");																			
						found = true;
						ArrayList<DelegateTree<UnitWrapper, String>> initTrees = NetworkFlowInterproceduralAnalysis.GRAPH_SUMMARY.get(sootMethod);
						for (DelegateTree<UnitWrapper, String> initTree : initTrees) {
							Collection<UnitWrapper> initVerticies = currentMethodConnectableJungGraph.getVertices();
							for (UnitWrapper constructorUnitWrapper : verticies) {
								Unit unit2 = constructorUnitWrapper.getUnit();
								logger.debug("So simple");
								if(unit2 instanceof JAssignStmt){
									JAssignStmt jAssignStmt2 = (JAssignStmt)unit2;
									if(jAssignStmt2.getLeftOp() instanceof JInstanceFieldRef){
										JInstanceFieldRef jInstanceFieldRef2 = (JInstanceFieldRef)jAssignStmt2.getLeftOp(); 
										if(jInstanceFieldRef.getField().getName().equals(jInstanceFieldRef2.getField().getName())){
											logger.debug("gotcha");
											if(currentMethodUnitWrapper.toString().contains("println")){
												logger.debug("Odd that we are adding this ... ");
											}

											ArrayList<Object> subtreeAdd = new ArrayList<Object>();
											subtreeAdd.add(currentMethodConnectableJungGraph);
											subtreeAdd.add(initTree);
											subtreeAdd.add(currentMethodUnitWrapper);
											subtreeAdd.add(constructorUnitWrapper);
											subtreeAdd.add(method);
											initAdditions.add(subtreeAdd);
											break;
										}												
									}
								}
							}
						}
					}
				}
			}
			if(!found){
				logger.debug("We need to log it");
				ArrayList<DelegateTree<UnitWrapper, String>> trees = STATIC_FIELD_GRAPHS.get(method.getDeclaringClass().toString());
				if(trees == null){
					trees = new ArrayList<DelegateTree<UnitWrapper, String>>();
					trees.add(currentMethodConnectableJungGraph);
					STATIC_FIELD_GRAPHS.put(method.getDeclaringClass().toString(), trees);									
				}
				else{
					trees.add(currentMethodConnectableJungGraph);
				}
			}
			else{
				logger.debug("It unified");
			}
	}

	private String createEdge(String prefix, SootMethod sootMethod) {
		String edge = prefix + "_" + GLOBAL_COUNTER++ + "|" + sootMethod.getSignature();
		if(sootMethod.getSignature().contains("com.iease.logic.MainService: void doTask(com.iease.logic.Task")){
			logger.debug("Stop here");
		}
		return edge;
	}


	private void connectConstructorsToStringCallGraphs(SootMethod method) {
		logger.debug("time unify!");
		ArrayList<DelegateTree<UnitWrapper, String>> trees =  STATIC_FIELD_GRAPHS.get(method.getDeclaringClass().getName());
		if(trees != null){
			ArrayList<ArrayList<Object>> additions = new ArrayList<ArrayList<Object>>();
			for (DelegateTree<UnitWrapper, String> tree : trees) {
				Collection<UnitWrapper> unitWrappers = tree.getVertices();
				for (UnitWrapper unitWrapper : unitWrappers) {
					Unit unit = unitWrapper.getUnit();
					if(unit instanceof JAssignStmt){
						JAssignStmt jAssignStmt = (JAssignStmt)unit;
						if(jAssignStmt.getRightOp() instanceof JInstanceFieldRef){
							logger.debug("Go out and find that class ... ");
							JInstanceFieldRef jInstanceFieldRef = (JInstanceFieldRef)jAssignStmt.getRightOp();
							SootFieldRef sootFieldRef = jInstanceFieldRef.getFieldRef();
							logger.debug("Got the ref");
							SootClass sootClass = sootFieldRef.declaringClass();
							String className = sootClass.getName();
							if(className.equals(method.getDeclaringClass().getName())){
								
								logger.debug("Time to do another for loop ... ");
								for(DelegateTree<UnitWrapper, String> jungGraph: allJungGraphs){
									Collection<UnitWrapper> verticies = jungGraph.getVertices();
									for (UnitWrapper unitWrapper2 : verticies) {
										Unit unit2 = unitWrapper2.getUnit();
										logger.debug("So simple");
										if(unit2 instanceof JAssignStmt){
											JAssignStmt jAssignStmt2 = (JAssignStmt)unit2;
											if(jAssignStmt2.getLeftOp() instanceof JInstanceFieldRef){
												JInstanceFieldRef jInstanceFieldRef2 = (JInstanceFieldRef)jAssignStmt2.getLeftOp(); 
												if(jInstanceFieldRef.getField().getName().equals(jInstanceFieldRef2.getField().getName())){
													logger.debug("gotcha");
													if(unitWrapper.toString().contains("println")){
														logger.debug("Odd that we are adding this ... ");
													}

													ArrayList<Object> subtreeAdd = new ArrayList<Object>();
													subtreeAdd.add(tree);
													subtreeAdd.add(jungGraph);
													subtreeAdd.add(unitWrapper);
													subtreeAdd.add(unitWrapper2);
													//subtreeAdd.add(succesorMethod.getSignature()+unit.toString());
													//subtreeAdd.add(succesorMethod);
													additions.add(subtreeAdd);
												}												
											}
										}
									}
								}								
							}
						}
					}
				}				
			}
			for (ArrayList<Object> arrayList : additions) {
				DelegateTree<UnitWrapper, String> jungGraph1 = (DelegateTree<UnitWrapper, String>)arrayList.get(0);
				DelegateTree<UnitWrapper, String> classFieldJungGraph2 = (DelegateTree<UnitWrapper, String>)arrayList.get(1);
				UnitWrapper unitWrapper1 = (UnitWrapper)arrayList.get(2);
				UnitWrapper classFieldUnitWrapper2 = (UnitWrapper)arrayList.get(3);
				// TODO should I remove the tree ...
				//trees.remove(index)
				try {
					DelegateTree<UnitWrapper, String> subTree = (DelegateTree<UnitWrapper, String>)TreeUtils.getSubTree(classFieldJungGraph2, classFieldUnitWrapper2);
					TreeUtils.addSubTree(jungGraph1, subTree, unitWrapper1, unitWrapper1.toString()+GLOBAL_COUNTER++);												
					
				} catch (InstantiationException e) {
					//e.printStackTrace();
				} catch (IllegalAccessException e) {
					//e.printStackTrace();
				} catch (IllegalArgumentException e){
					//e.printStackTrace();						
				}
				
			}
		}
	}

	public boolean addSubTree(DelegateTree<UnitWrapper, String> tree, DelegateTree<UnitWrapper, String> subTree, UnitWrapper unitWrapper, SootMethod treeMethod, SootMethod subTreeMethod){
		boolean added = false;
		try {
			TreeUtils.addSubTree(tree, subTree, unitWrapper, unitWrapper.toString()+GLOBAL_COUNTER++);				
		}catch (IllegalArgumentException e){
			StringExtractionUtils.prettyPrint(tree, treeMethod);
			StringExtractionUtils.prettyPrint(subTree, subTreeMethod);
			//e.printStackTrace();						
		}
		return added;
	}
	
	
	private void classifyGraphType(SootMethod currentSootMethod, List<SootMethod> successorGraph) {
		Set<String> methodSignatures = new HashSet<String>();
		for (SootMethod sootMethod : successorGraph) {
			methodSignatures.add(sootMethod.getSignature());
		}
		int returns = 0;
		int params = 0;
		int successors = 0;
		int urls = 0;
		for(DelegateTree<UnitWrapper, String> jungGraph : allJungGraphs){
			if(jungGraph.getRoot().getUnit() instanceof JReturnStmt){
				returns++;
				returnJungGraphs.add(jungGraph);
			}
			Collection<UnitWrapper> verticies = jungGraph.getVertices();
			for (UnitWrapper unitWrapper : verticies) {
				if(unitWrapper.getUnit() instanceof JIdentityStmt){
					params++;
					parameterJungGraphs.add(jungGraph);
					break;
				}
				Iterator<String> iter = methodSignatures.iterator();
				while(iter.hasNext()){
					String signature = iter.next();
					if(unitWrapper.getUnit().toString().contains(signature)){
						successors++;
						successorJungGraphs.add(jungGraph);
						break;
					}									
				}
				if(unitWrapper.toString().contains("java.net.URL:")){
					urls++;
					urlJungGraphs.add(jungGraph);
					break;
				}
			}
		}
		addToAllConnectableJunGraphs(returnJungGraphs);
		addToAllConnectableJunGraphs(parameterJungGraphs);
		addToAllConnectableJunGraphs(successorJungGraphs);
		addToAllConnectableJunGraphs(urlJungGraphs);
		logger.debug("Total Jung Graphs: " + allJungGraphs.size());
		logger.debug("Return Jung Graphs: " + returns);
		logger.debug("Params Jung Graphs: " + params);
		logger.debug("Succs Jung Graphs: " + successors);
		logger.debug("Url Jung Graphs: " + urls);
		logger.debug("holder");
	}
	
	private void addToAllConnectableJunGraphs(ArrayList<DelegateTree<UnitWrapper, String>> list){
		for (DelegateTree<UnitWrapper, String> delegateTree : list) {
			allConnectableJungGraphs.add(delegateTree);
		}
	}
	

	private SootMethodRef getMethodRef(JAssignStmt jAssignStmt) {
		SootMethodRef sootMethodRef = null;
		if(jAssignStmt.getRightOp() instanceof JSpecialInvokeExpr){
			JSpecialInvokeExpr jSpecialInvokeExpr = (JSpecialInvokeExpr)jAssignStmt.getRightOp();
			sootMethodRef = jSpecialInvokeExpr.getMethodRef();			
		}
		else if(jAssignStmt.getRightOp() instanceof JVirtualInvokeExpr){
			JVirtualInvokeExpr jSpecialInvokeExpr = (JVirtualInvokeExpr)jAssignStmt.getRightOp();
			sootMethodRef = jSpecialInvokeExpr.getMethodRef();			
		}
		else if(jAssignStmt.getRightOp() instanceof JInstanceFieldRef){
			JInstanceFieldRef jInstanceFieldRef = (JInstanceFieldRef)jAssignStmt.getRightOp();
			//jInstanceFieldRef.get
			logger.debug("TODO must handle instance fields here.");
		}
		
		return sootMethodRef;
	}

	private NetworkFlowMethodAnalysis performIntraproceduralAnalysis(
			SootMethod method, List<SootMethod> inset, List<SootMethod> outset) {
		NetworkFlowMethodAnalysis analysis = new NetworkFlowMethodAnalysis(method, inset, outset);
		NFMA.add(analysis);
		String key = analysis.getSootMethod().getDeclaringClass() + "." + analysis.getSootMethod().getName();
		if( key.equals("com.omniture.RequestHandlerSe13.requestConnect") || key.equals("com.google.ads.ac.run") || key.equals("bbc.mobile.news.model.Article.getAsUrl")){
			logger.debug("We wll solve this!!");
		}
		analysis.doAnalysis();
		List<String> urls = analysis.getDetectedUrls();
		if(urls.size() > 0){
			logger.debug("+++++++++++++++++ Have URLS in method " + method.getDeclaringClass() + "." + method.getName() + " +++++++++++++++++++");
			if( key.equals("com.omniture.RequestHandlerSe13.requestConnect") || key.equals("com.google.ads.ac.run") || key.equals("bbc.mobile.news.model.Article.getAsUrl")){
				logger.debug("We are at the correct point to analyze requestConnect");
				PatchingChain<Unit> units = method.getActiveBody().getUnits();
//				for (Unit unit : units) {
//					logger.debug(unit.toString());
//				}
			}
			logger.debug("Have found this many URLs: " + urls.size());
			logger.debug("Ran NetworkFlowMethodAnalysis on " + method.getName() + " and number of URLs is: " + analysis.getDetectedUrls().size());
			String allUrls = "";
			for (String string : urls) {
				logger.debug("URL is: " + string);
				if(allUrls.length() == 0){
					allUrls = string;						
				}
				else{
					allUrls = allUrls + " | " + string;						
					
				}
			}
			logger.debug("Is IntraProcedural: " + analysis.isIntraProcedural());		
			String insetStr = "";
			String outsetStr = "";
			if(! analysis.isIntraProcedural()){
				List<String> outsetToUrl = analysis.getInterProceduralUrlChainOutSet();
				List<Unit> inSetToUrl = analysis.getInterProceduralUrlChainInSet();
				for (String string : outsetToUrl) {
					logger.debug("InterProcedural outset touch pt: " + string);
					// "\r\n"
					if(outsetStr.length() == 0){
						outsetStr = string;						
					}
					else{
						outsetStr = outsetStr + " | " + string;						
						
					}
				}
				for (Unit unit : inSetToUrl) {
					logger.debug("InterProcedural inset touch pt: " + unit);
					if(insetStr.length() == 0){
						insetStr = unit.toString();						
					}
					else{
						insetStr = insetStr + " | " + unit.toString();						
						
					}
				}
			}
			String trueInset = "";
			for (SootMethod insetMethod : inset) {
				if(trueInset.length() == 0){
					trueInset = insetMethod.getName();						
				}
				else{
					trueInset = trueInset + " | " + insetMethod.getName();						
					
				}
				
			}
			fileContents = fileContents + generateCsv(method.getDeclaringClass(),method.getName(),analysis.getUrlConstructorType(),urls.size(),allUrls,analysis.isIntraProcedural(), trueInset, insetStr, outsetStr);

			logger.debug("++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		}
		return analysis;
	}
	
	private String generateCsv(SootClass declaringClass, String name, UrlConstructor urlConstructor, int size,
			String urls, boolean intraProcedural, String trueInset, String insetStr,
			String outsetStr) {
		String line = 
					InterProceduralMain.currentAppName() + "," +
					declaringClass.toString() + "," +
					name + "," +
					urlConstructor + "," +
					size + "," +
					urls + "," +
					intraProcedural + "," +
					trueInset + "," +
					insetStr + "," +
					outsetStr + "\r\n";
		return line;
	}

	@Override
	protected void merge(Object in1, Object in2, Object out) {
		logger.debug("Calling merger in NetworkFlowInterproceduralAnalysis()");
		// TODO Auto-generated method stub

	}

	@Override
	protected void copy(Object sr, Object dst) {
		logger.debug("Calling copy here");
	}

	@Override
	protected void applySummary(Object arg0, Stmt arg1, Object arg2, Object arg3) {
		logger.debug("Calling applySummary in NetworkFlowInterproceduralAnalysis()");
	}

	public static  Map<SootMethod, Map<Unit, ArraySparseSet>> getSummaries() {
		return METHOD_SUMMARY;
	}

	public static Map<SootMethod, ArrayList<DelegateTree<UnitWrapper, String>>> getGraphSummaries() {
		return GRAPH_SUMMARY;
	}
	
	public static List<NetworkFlowMethodAnalysis> getNetworkFlowMethodAnalysisList(){
		return NFMA;
	}
}
