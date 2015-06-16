package edu.buffalo.cse.blueseal.networkflow.interprocedural;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.buffalo.cse.blueseal.networkflow.intraprocedural.GenericSummary;
import edu.buffalo.cse.blueseal.networkflow.intraprocedural.JAssignStmtSummary;
import edu.buffalo.cse.blueseal.networkflow.intraprocedural.JIdentityStmtSummary;
import edu.buffalo.cse.blueseal.networkflow.intraprocedural.JInvokeStmtSummary;
import edu.buffalo.cse.blueseal.networkflow.intraprocedural.NetworkFlowUnitSummary.UrlConstructor;
import edu.buffalo.cse.blueseal.networkflow.summary.StringExtractionUtils;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.SparseMultigraph;

import soot.ArrayType;
import soot.RefType;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.internal.AbstractInvokeExpr;
import soot.jimple.internal.AbstractStmt;
import soot.jimple.internal.JArrayRef;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JInterfaceInvokeExpr;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JNewArrayExpr;
import soot.jimple.internal.JNewExpr;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JReturnVoidStmt;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.jimple.internal.JStaticInvokeExpr;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.internal.JimpleLocalBox;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class NetworkFlowBackwardFlowAnalysis extends BackwardFlowAnalysis{

	static Logger logger = Logger.getLogger(NetworkFlowBackwardFlowAnalysis.class);
	private Map<Unit, ArraySparseSet> unitToSet = new HashMap<Unit, ArraySparseSet>();
	private FlowSet emptySet = new ArraySparseSet();
	private ArrayList<DelegateTree<UnitWrapper, String>> graphs = new ArrayList<DelegateTree<UnitWrapper, String>>();
	private DirectedGraph directedGraph;
	private List<SootMethod> successorGraph;
	private SootMethod sootMethod;
	private ArrayList<Unit> orderedUnits = new ArrayList<Unit>();
	private static HashMap<String,ArrayList<DelegateTree<UnitWrapper, String>>> CLASS_FIELDS = new HashMap<String, ArrayList<DelegateTree<UnitWrapper, String>>>(); 
	
	public NetworkFlowBackwardFlowAnalysis(DirectedGraph graph, List<SootMethod> successorGraph, SootMethod newSootMethod) {		
		super(graph);
		this.successorGraph = successorGraph;
		this.sootMethod = newSootMethod;
		directedGraph = graph;
		logger.debug("We now have the backward flow analysis enabled");
		for(Iterator it = graph.iterator(); it.hasNext();){
			Unit unit = (Unit) it.next();
			unitToSet.put(unit, new ArraySparseSet());
			logger.debug("Created empty new ArraySparseSet() for " + unit.toString());
		}
		doAnalysis();
	}

	@Override
	protected Object newInitialFlow() {
		logger.debug("Called newInitialFlow()");
		return emptySet.clone();
	}

	@Override
	protected Object entryInitialFlow() {
		logger.debug("Called entryInitialFlow()");
		return emptySet.clone();
	}


	@Override
	protected void flowThrough(Object in, Object callNode, Object out) {
		// Poor mans approach to avoid having to work with Java Threads.
		Unit unitIn = (Unit) callNode;
		orderedUnits.add(unitIn);
		UnitWrapper unitWrapper = UnitWrapper.getSingleArgumentOrLessUnitWrapper(unitIn, sootMethod);
		List listOut = directedGraph.getPredsOf(unitIn);
		
		if(unitIn.toString().contains("$r3 = virtualinvoke $r3.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>($r0)")){
			logger.debug("Hold here");
		}
		logger.debug("Called flowThrough() on " + unitIn.toString());	
		logger.debug("In is: " + in);
		logger.debug("Out is: " + out);
		ArraySparseSet uFlowIntoThis = unitToSet.get(unitIn);
		ArraySparseSet outSet = (ArraySparseSet)out;
		ArraySparseSet inSet = (ArraySparseSet)in;
		Stmt stmt = (Stmt) unitIn;
		List<ValueBox> uses = unitIn.getUseBoxes();

		if(unitIn instanceof JInvokeStmt){
			processJInvokeUnit(unitIn, unitWrapper, outSet, inSet);
		}
		else if(unitIn instanceof JAssignStmt){
			processJAssignmentUnit(unitIn, unitWrapper, outSet, inSet);
		}
		else if(unitIn instanceof JIdentityStmt){
			processJIdentityStatement(unitIn, inSet);			
		}
		else if(unitIn instanceof JReturnStmt){
			processJReturnStatement(unitIn, unitWrapper, outSet);
		}
		else if(unitIn instanceof JReturnVoidStmt){
			logger.debug("holder");
		}
		else{
			logger.debug("holder");
			//End of the line ... End of the chain ...  I am not a string!
		}
		// If the inSet is unsatisfied at end - add it to outset
		if(inSet.size() > 0){
			List list = inSet.toList();
			for (Object object : list) {
				outSet.add(object);				
			}
		}
	}

	private void processJReturnStatement(Unit unitIn, UnitWrapper unitWrapper,
			ArraySparseSet outSet) {
		JReturnStmt jReturnStmt = (JReturnStmt)unitIn;
		Value value = jReturnStmt.getOp();
		if(value instanceof JimpleLocal){
			outSet.add(new UnitValue(unitWrapper,value));
			addNewGraph(unitWrapper);
		}
		else if(value instanceof StringConstant){
			addNewGraph(unitWrapper);			
		}
	}

	private void processJIdentityStatement(Unit unitIn, ArraySparseSet inSet) {
		JIdentityStmt jIdentityStmt = (JIdentityStmt)unitIn;
		for(Object obj : inSet){
			if(((UnitValue)obj).getValue() instanceof JimpleLocal){
				JimpleLocal jimpleLocal = (JimpleLocal)((UnitValue)obj).getValue();
				if(jimpleLocal.getName().equals(jIdentityStmt.leftBox.getValue().toString())){
					unitToSet.get(unitIn).add(obj);
					inSet.remove(obj);
					UnitValue unitValue = (UnitValue)obj;
					addTargetGraphToExtend((UnitValue)obj, UnitWrapper.getSingleArgumentOrLessUnitWrapper(unitIn, sootMethod));
				}
			}
			else if(((UnitValue)obj).getValue() instanceof JInstanceFieldRef){
				JInstanceFieldRef jInstanceFieldRef = (JInstanceFieldRef)((UnitValue)obj).getValue();
				if(jInstanceFieldRef.getBase().equals(jIdentityStmt.getLeftOp())){
					logger.debug("Good sign");
					inSet.remove(obj);
					UnitValue unitValue = (UnitValue)obj;
					addTargetGraphToExtend((UnitValue)obj, UnitWrapper.getSingleArgumentOrLessUnitWrapper(unitIn, sootMethod));						
				}
			}
			else if(((UnitValue)obj).getValue() instanceof StaticFieldRef){
				logger.debug("What to do here ...");
			}
		}
	}

	public static HashMap<String, ArrayList<DelegateTree<UnitWrapper, String>>> getClassFieldAssignedValues(){
		return CLASS_FIELDS;
	}
	
	private void processJAssignmentUnit(Unit unitIn, UnitWrapper unitWrapper,
			ArraySparseSet outSet, ArraySparseSet inSet) {
		JAssignStmt jAssignStmt = (JAssignStmt)unitIn;
		archiveClassVariable(unitIn, jAssignStmt, outSet);
		logger.debug(jAssignStmt.getRightOpBox());
		if(jAssignStmt.getRightOpBox().getValue() instanceof StringConstant){
			addJAssignmentRightSide(unitIn, unitWrapper, outSet, inSet,
					jAssignStmt);			
		}
		else if(jAssignStmt.getRightOpBox().getValue() instanceof FieldRef){
			logger.debug("Hanlde this case better!");
			FieldRef instanceFieldRef = (FieldRef)jAssignStmt.getRightOpBox().getValue();
			if(instanceFieldRef.getType() instanceof RefType){
				RefType refType = (RefType)instanceFieldRef.getType();
				//if(refType.getClassName().startsWith("java.lang.String")){
					addJAssignmentRightSide(unitIn, unitWrapper, outSet, inSet,
							jAssignStmt);								
				//}
			}			
		}
		else if(jAssignStmt.getRightOpBox().getValue() instanceof JStaticInvokeExpr){
			JStaticInvokeExpr jStaticInvokeExpr = (JStaticInvokeExpr)jAssignStmt.getRightOpBox().getValue(); 
			handleInvokeExprArgs(unitIn, outSet, inSet, jAssignStmt.leftBox.getValue().toString(),
					jStaticInvokeExpr);
		}
		else if(jAssignStmt.getRightOp() instanceof JSpecialInvokeExpr){
			JSpecialInvokeExpr jSpecialInvokeExpr = (JSpecialInvokeExpr)jAssignStmt.getRightOp(); //good
			SootMethodRef sooMethodRef = jSpecialInvokeExpr.getMethodRef();
			
			boolean addedToExistingGraph = false;
			for(Object obj : inSet){
				if(((UnitValue)obj).getValue() instanceof JimpleLocal){
					JimpleLocal jimpleLocal = (JimpleLocal)((UnitValue)obj).getValue();
					if(jimpleLocal.getName().equals(jAssignStmt.leftBox.getValue().toString())){
						addTargetGraphToExtend((UnitValue)obj, UnitWrapper.getSingleArgumentOrLessUnitWrapper(unitIn, sootMethod));
						addedToExistingGraph = true;
					}
				}
			}
			if(! addedToExistingGraph){
				for(SootMethod sootMethod : successorGraph){
					logger.debug(sootMethod.getSignature()  + " same sig as " + sooMethodRef.getSignature());
					if(sootMethod.getSignature().equals(sooMethodRef.getSignature())){
						logger.debug("Adding a pointer out here: " + unitIn);
						addNewGraph(unitWrapper);
					}
				}					
			}
			Object[] arguments = jSpecialInvokeExpr.getArgs().toArray();
			for (int j = 0; j < arguments.length; j++) {
				if(arguments[j] instanceof JimpleLocal){
					JimpleLocal jimpleLocal = (JimpleLocal)arguments[j];
					if(jimpleLocal.getType() instanceof RefType){
						RefType refType = (RefType)jimpleLocal.getType(); 
						if(refType.getClassName().startsWith("java.lang.String")){
							UnitWrapper refUnitWrapper = UnitWrapper.getMultiArgumentUnitWrapper(unitIn, j, sootMethod);
							addNewGraph(refUnitWrapper);
							outSet.add(new UnitValue(refUnitWrapper, jimpleLocal));									
						}
					}
				}
				else if(arguments[j] instanceof StringConstant){
					UnitWrapper refUnitWrapper = UnitWrapper.getMultiArgumentUnitWrapper(unitIn, j, sootMethod);
					addNewGraph(refUnitWrapper);
				}
			}
		}
		else if(jAssignStmt.getRightOp() instanceof JArrayRef){
			JArrayRef jArrayRef = (JArrayRef)jAssignStmt.getRightOp();
			if(jArrayRef.getBaseBox().getValue() instanceof JimpleLocal){
				JimpleLocal arrayRefJimpleLocal = (JimpleLocal)jArrayRef.getBaseBox().getValue();
				for(Object obj : inSet){
					if(((UnitValue)obj).getValue() instanceof JimpleLocal){
						JimpleLocal jimpleLocal = (JimpleLocal)((UnitValue)obj).getValue();
						if(jimpleLocal.getName().equals(jAssignStmt.leftBox.getValue().toString())){
							inSet.remove(obj);
							addTargetGraphToExtend((UnitValue)obj, unitWrapper);
							outSet.add(new UnitValue(unitWrapper,arrayRefJimpleLocal));									
						}
					}
				}
			}
		}
		else if(jAssignStmt.getRightOp() instanceof JVirtualInvokeExpr){
			JVirtualInvokeExpr jVirtualInvokeExpr = (JVirtualInvokeExpr)jAssignStmt.getRightOp();
				boolean addedToExistingGraph = false;
				boolean addedArguements = false;
				for(Object obj : inSet){
					if(((UnitValue)obj).getValue() instanceof JimpleLocal){
						JimpleLocal jimpleLocal = (JimpleLocal)((UnitValue)obj).getValue();
						if(jimpleLocal.getName().equals(jAssignStmt.leftBox.getValue().toString())){
							//unitToSet.get(unitIn).add(obj);
							inSet.remove(obj);
							//UnitValue unitValue = (UnitValue)obj;
							//addTargetGraphToExtend((UnitValue)obj, UnitWrapper.getSingleArgumentOrLessUnitWrapper(unitIn, sootMethod));
							if(unitWrapper.getUnit().toString().contains("$r2 = virtualinvoke $r2.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(\"post value==\")")){
								logger.debug("Hold here");
							}
							addTargetGraphToExtend((UnitValue)obj, unitWrapper);
							addedToExistingGraph = true;
							if(jAssignStmt.leftBox.getValue().toString().equals(jVirtualInvokeExpr.getBase().toString())){
								outSet.add(new UnitValue(unitWrapper,jimpleLocal));									
							}
							else{
								logger.debug("We need to handle this case better");
								outSet.add(new UnitValue(unitWrapper,(JimpleLocal)jVirtualInvokeExpr.getBase()));									
							}
							//Must add the arguments as pointers to the graph as well
							if(jVirtualInvokeExpr.getArgCount() > 0){
								int argCount = jVirtualInvokeExpr.getArgCount();
								for (int i = 0; i < argCount; i++) {
									if(jVirtualInvokeExpr.getArg(i) instanceof JimpleLocal){
										outSet.add(new UnitValue(unitWrapper,(JimpleLocal)jVirtualInvokeExpr.getArg(i)));				
										addedArguements = true;
									}
								}
							}

						}
					}
				}
				
				if(! addedArguements){
					Object[] arguments = jVirtualInvokeExpr.getArgs().toArray();
					for (int j = 0; j < arguments.length; j++) {
						if(arguments[j] instanceof JimpleLocal){
							JimpleLocal jimpleLocal = (JimpleLocal)arguments[j];
							if(jimpleLocal.getType() instanceof RefType){
								RefType refType = (RefType)jimpleLocal.getType(); 
								if(refType.getClassName().startsWith("java.lang.String")){
									UnitWrapper refUnitWrapper = UnitWrapper.getMultiArgumentUnitWrapper(unitIn, j, sootMethod);
									addNewGraph(refUnitWrapper);
									outSet.add(new UnitValue(refUnitWrapper, jimpleLocal));									
								}
							}
						}
					}	
				}

				if(! addedToExistingGraph){
					
					if(jVirtualInvokeExpr.getMethodRef().name().equals("toString") || jVirtualInvokeExpr.getMethodRef().getSignature().equals("java.lang.StringBuffer")
							 || jVirtualInvokeExpr.getMethodRef().name().equals("append")
							){
						logger.debug("Adding a new string");
						JVirtualInvokeExpr jVirtualInvokeExpr2 = (JVirtualInvokeExpr)jAssignStmt.rightBox.getValue();							 
						JimpleLocalBox jimpleLocalBox = (JimpleLocalBox)jVirtualInvokeExpr2.getBaseBox();
						outSet.add(new UnitValue(unitWrapper,jimpleLocalBox.getValue()));
						addNewGraph(unitWrapper);
					}
				}					

		}
		
		for(Object obj : inSet){
			if(((UnitValue)obj).getValue() instanceof JimpleLocal){
				JimpleLocal jimpleLocal = (JimpleLocal)((UnitValue)obj).getValue();
				if(jAssignStmt.leftBox.getValue() instanceof JArrayRef){
					logger.debug("big step here");
					JArrayRef jArrayRef = (JArrayRef)jAssignStmt.leftBox.getValue();
					logger.debug("Array ref stuff");
					if(jArrayRef.getBase().toString().equals(jimpleLocal.toString())){
						logger.debug("drudgerous");
						outSet.add(new UnitValue(unitWrapper, jArrayRef.getBase()));									
						inSet.remove(obj);
						addTargetGraphToExtend((UnitValue)obj, unitWrapper);

					}
				}
				else if(jimpleLocal.getName().equals(jAssignStmt.leftBox.getValue().toString())){
					if(jAssignStmt.getRightOpBox().getValue() instanceof JVirtualInvokeExpr){
						JVirtualInvokeExpr jVirtualInvokeExpr = (JVirtualInvokeExpr)jAssignStmt.getRightOpBox().getValue();
						outSet.add(new UnitValue(unitWrapper, jVirtualInvokeExpr.getBaseBox().getValue()));
						int argCount = jVirtualInvokeExpr.getArgCount();
						if(argCount > 0){
							for(int i = 0; i < argCount; i++){
								UnitWrapper jimpleLocalUnitWrapper = UnitWrapper.getMultiArgumentUnitWrapper(unitIn, i, sootMethod);
								Value value = jVirtualInvokeExpr.getArgBox(i).getValue();
								outSet.add(new UnitValue(jimpleLocalUnitWrapper, value));									
							}
						}
						inSet.remove(obj);
						UnitValue unitValue = (UnitValue)obj;
						addTargetGraphToExtend((UnitValue)obj, unitWrapper);
						if(jVirtualInvokeExpr.getArgCount() > 0){
							int jVirtArgCount = jVirtualInvokeExpr.getArgCount();
							for (int count = 0; count < jVirtArgCount; count++) {
								if(jVirtualInvokeExpr.getArg(count) instanceof JimpleLocal){
									UnitWrapper jimpleLocalUnitWrapper = UnitWrapper.getMultiArgumentUnitWrapper(unitIn, count, sootMethod);
									logger.debug("TODO: Add case to handle string consts");
									//@TODO Add this case to handle StringConstants ... at some point we need these
									outSet.add(new UnitValue(jimpleLocalUnitWrapper, (JimpleLocal)jVirtualInvokeExpr.getArg(count)));										
								}
							}
						}
					}
					else if(jAssignStmt.getRightOpBox().getValue() instanceof JStaticInvokeExpr){
						JStaticInvokeExpr jStaticInvokeExpr = (JStaticInvokeExpr)jAssignStmt.getRightOpBox().getValue();
						List list = jStaticInvokeExpr.getArgs();
						for (Object argument : list) {
							if(argument instanceof StringConstant){
								//We have found a string
								inSet.remove(obj);
								outSet.add(new UnitValue(unitWrapper, (StringConstant)argument));										
								UnitValue unitValue = (UnitValue)obj;
								addTargetGraphToExtend((UnitValue)obj, unitWrapper);
							}
							if(argument instanceof JimpleLocal){
								logger.debug("We are adding one more for sure.");
								inSet.remove(obj);
								outSet.add(new UnitValue(unitWrapper, (JimpleLocal)argument));										
								UnitValue unitValue = (UnitValue)obj;
								addTargetGraphToExtend((UnitValue)obj, unitWrapper);									
							}
						}		
					}
					else if(jAssignStmt.getRightOpBox().getValue() instanceof JCastExpr){
						JCastExpr jCastExpr = (JCastExpr)jAssignStmt.getRightOpBox().getValue();
						outSet.add(new UnitValue(unitWrapper, jCastExpr.getOpBox().getValue()));
						inSet.remove(obj);
						UnitValue unitValue = (UnitValue)obj;
						addTargetGraphToExtend((UnitValue)obj, unitWrapper);

					}
					else if(jAssignStmt.getRightOpBox().getValue() instanceof JNewExpr){
						JNewExpr jNewExpr = (JNewExpr)jAssignStmt.getRightOpBox().getValue();
						inSet.remove(obj);
						UnitValue unitValue = (UnitValue)obj;
						addTargetGraphToExtend((UnitValue)obj, UnitWrapper.getSingleArgumentOrLessUnitWrapper(unitIn, sootMethod));
					}
					else if(jAssignStmt.getRightOpBox().getValue() instanceof JimpleLocal){
						outSet.add(new UnitValue(unitWrapper, jAssignStmt.getRightOpBox().getValue()));
						inSet.remove(obj);
						addTargetGraphToExtend((UnitValue)obj, unitWrapper);
					}
					else if(jAssignStmt.getRightOpBox().getValue() instanceof JNewArrayExpr){
						inSet.remove(obj);
						addTargetGraphToExtend((UnitValue)obj, UnitWrapper.getSingleArgumentOrLessUnitWrapper(unitIn, sootMethod));							
					}
					else if(jAssignStmt.getRightOpBox().getValue() instanceof FieldRef){
						outSet.add(new UnitValue(unitWrapper, jAssignStmt.getRightOpBox().getValue()));
						inSet.remove(obj);
						addTargetGraphToExtend((UnitValue)obj, unitWrapper);														
					}
					else{
						logger.debug("TODO: Yet another case we need to handle ..... ");
					}
				}					
			}
		}
	}

	private void archiveClassVariable(Unit unitIn, JAssignStmt jAssignStmt, ArraySparseSet outSet) {
		if(jAssignStmt.getLeftOp() instanceof FieldRef){
			FieldRef jInstanceFieldRef = (FieldRef)jAssignStmt.getLeftOp(); 
			ArrayList<DelegateTree<UnitWrapper, String>> classFieldValues = CLASS_FIELDS.get(jInstanceFieldRef);
			if(classFieldValues == null){
				classFieldValues = new ArrayList<DelegateTree<UnitWrapper, String>>();
			}
			UnitWrapper unitWrapper = UnitWrapper.getSingleArgumentOrLessUnitWrapper(unitIn, sootMethod);
			DelegateTree<UnitWrapper, String> newTree = addNewGraph(unitWrapper);
			classFieldValues.add(newTree);
			CLASS_FIELDS.put(jInstanceFieldRef.toString(), classFieldValues);
			if(jAssignStmt.getRightOpBox().getValue() instanceof JimpleLocal){
				outSet.add(new UnitValue(unitWrapper, jAssignStmt.getRightOpBox().getValue()));
			}

			logger.debug("major tom");
		}
	}

	private void addJAssignmentRightSide(Unit unitIn, UnitWrapper unitWrapper,
			ArraySparseSet outSet, ArraySparseSet inSet, JAssignStmt jAssignStmt) {
		boolean addedToExistingGraph = false;
		for(Object obj : inSet){
			if(((UnitValue)obj).getValue() instanceof JimpleLocal){
				JimpleLocal jimpleLocal = (JimpleLocal)((UnitValue)obj).getValue();
				if(jimpleLocal.getName().equals(jAssignStmt.leftBox.getValue().toString())){
					addTargetGraphToExtend((UnitValue)obj, unitWrapper);
					addedToExistingGraph = true;
					inSet.remove(obj);
					outSet.add(new UnitValue(unitWrapper,jimpleLocal));
				}
			}
		}
		if(! addedToExistingGraph){
			logger.debug("Adding a pointer out here: " + unitIn);
			addNewGraph(unitWrapper);
		}
	}

	private void handleInvokeExprArgs(Unit unitIn, ArraySparseSet outSet,
			ArraySparseSet inSet, String leftValue,
			AbstractInvokeExpr jStaticInvokeExpr) {
		boolean found = false;
		for(Object obj : inSet){
			if(((UnitValue)obj).getValue() instanceof JimpleLocal){
				JimpleLocal jimpleLocal = (JimpleLocal)((UnitValue)obj).getValue();
				if(jimpleLocal.getName().equals(leftValue)){
					if (jStaticInvokeExpr.getArgCount() == 0){
						logger.debug("holder");
						//TODO strait up string
						if(! (jStaticInvokeExpr.getMethodRef().returnType() instanceof VoidType)){
							logger.debug("holder");
							inSet.remove(obj);
							UnitWrapper stringConstantUnitWrapper = UnitWrapper.getSingleArgumentOrLessUnitWrapper(unitIn, sootMethod);
							addTargetGraphToExtend((UnitValue)obj, stringConstantUnitWrapper);									
							found = true;							
						}
					}
					else{
						for(int argCount = 0; argCount < jStaticInvokeExpr.getArgCount(); argCount++){
							//TODO this is a tough one - what are you supposed to do with multiple args in a constructor ... can't keep adding the same unit but this code does.
							if(jStaticInvokeExpr.getArgBox(argCount).getValue() instanceof StringConstant){
								logger.debug("holder");
								inSet.remove(obj);
								UnitWrapper stringConstantUnitWrapper = UnitWrapper.getMultiArgumentUnitWrapper(unitIn, argCount, sootMethod);
								addTargetGraphToExtend((UnitValue)obj, stringConstantUnitWrapper);									
								found = true;
							}
							else if(jStaticInvokeExpr.getArgBox(argCount).getValue() instanceof JimpleLocal){
								UnitWrapper jimpleLocalUnitWrapper = UnitWrapper.getMultiArgumentUnitWrapper(unitIn, argCount, sootMethod);
								inSet.remove(obj);
								//addTargetGraphToExtend((UnitValue)obj, jimpleLocalUnitWrapper);
								addNewGraph(jimpleLocalUnitWrapper);																		
								outSet.add(new UnitValue(jimpleLocalUnitWrapper,jStaticInvokeExpr.getArgBox(argCount).getValue()));
								found = true;
							}
							else if(jStaticInvokeExpr.getArgBox(argCount).getValue() instanceof JIdentityStmt){
								logger.debug("holder");
								found = true;
							}		
						}
					}
				}
			}
		}
		if(! found){
			logger.debug("we go here");
			int argCount = jStaticInvokeExpr.getArgCount();
			for(int i = 0; i < argCount; i++){
				Value value = jStaticInvokeExpr.getArg(i);
				logger.debug("free");
				if( value.getType() instanceof RefType){
					RefType refType = (RefType)value.getType();
					String className = refType.getClassName();
					//TODO there are probably more ... 
					if(className.startsWith("java.lang.String")){
						UnitWrapper jimpleLocalUnitWrapper = UnitWrapper.getMultiArgumentUnitWrapper(unitIn, i, sootMethod);
						outSet.add(new UnitValue(jimpleLocalUnitWrapper,value));
						addNewGraph(jimpleLocalUnitWrapper);									
					}
				}
				else if(value.getType() instanceof ArrayType){
					ArrayType arrayType = (ArrayType)value.getType();
					if(arrayType.baseType instanceof RefType){
						RefType refType = (RefType)arrayType.baseType;
						String className = refType.getClassName();
						//TODO there are probably more ... 
						if(className.startsWith("java.lang.String")){
							UnitWrapper jimpleLocalUnitWrapper = UnitWrapper.getMultiArgumentUnitWrapper(unitIn, i, sootMethod);
							outSet.add(new UnitValue(jimpleLocalUnitWrapper,value));
							addNewGraph(jimpleLocalUnitWrapper);									
						}
					}
					logger.debug("now what");
				}
			}
		}
	}

	private void processJInvokeUnit(Unit unitIn, UnitWrapper unitWrapper,
			ArraySparseSet outSet, ArraySparseSet inSet) {
		JInvokeStmt unit = (JInvokeStmt)unitIn;
		logger.debug(unit);
		if(unit.getInvokeExprBox().getValue() instanceof JSpecialInvokeExpr 
				&& ! unit.toString().contains("void <init>(java.lang.String)>(\"Stub!\")") 
				&& ! unit.toString().contains("<java.lang.Exception: void <init>()>()")){
			JSpecialInvokeExpr jSpecialInvokeExpr = ((JSpecialInvokeExpr)unit.getInvokeExprBox().getValue());
			handleInvokeExprArgs(unitIn, outSet, inSet, jSpecialInvokeExpr.getBaseBox().getValue().toString(),
					jSpecialInvokeExpr);
			int argCount = jSpecialInvokeExpr.getArgCount();
			for(int i = 0; i < argCount; i++){
				if(jSpecialInvokeExpr.getArg(i) instanceof JimpleLocal){
					handleInvokeExprArgs(unitIn, outSet, inSet, jSpecialInvokeExpr.getArg(i).toString(),
							jSpecialInvokeExpr);									
				}
			}
		}
		else if(unit.getInvokeExprBox().getValue() instanceof JSpecialInvokeExpr ){
			logger.debug("holder");
		} // -- add the JVirtualInvokeExpr
		else if(unit.getInvokeExprBox().getValue() instanceof JVirtualInvokeExpr){
			logger.debug("And now I am here ... ");
			JVirtualInvokeExpr jVirtualInvokeExpr = (JVirtualInvokeExpr)unit.getInvokeExprBox().getValue();
			int argCount = jVirtualInvokeExpr.getArgCount();
			for(int i = 0; i < argCount; i++){
				if(jVirtualInvokeExpr.getArg(i) instanceof StringConstant){
					UnitWrapper jimpleLocalUnitWrapper = UnitWrapper.getMultiArgumentUnitWrapper(unitIn, i, sootMethod);
					logger.debug("We have a string constant");
					addNewGraph(jimpleLocalUnitWrapper);
				}
				else if(jVirtualInvokeExpr.getArg(i) instanceof JimpleLocal){
					JimpleLocal jimpleLocal = (JimpleLocal)jVirtualInvokeExpr.getArg(i);
					if(jimpleLocal.getType() instanceof RefType){
						RefType refType = (RefType)jimpleLocal.getType();
						if(refType.getClassName().startsWith("java.lang.String")){
							UnitWrapper jimpleLocalUnitWrapper = UnitWrapper.getMultiArgumentUnitWrapper(unitIn, i, sootMethod);
							logger.debug("We have a strig ref type");									
							outSet.add(new UnitValue(jimpleLocalUnitWrapper, jVirtualInvokeExpr.getArgBox(i).getValue()));
							addNewGraph(jimpleLocalUnitWrapper);
						}
					}
				}						
			}
		}
		else if(unit.getInvokeExprBox().getValue() instanceof JStaticInvokeExpr){
			JStaticInvokeExpr staticInvokeExpr = (JStaticInvokeExpr)unit.getInvokeExprBox().getValue(); 
			List args = staticInvokeExpr.getArgs();
			for (Object object : args) {
				if(object instanceof JimpleLocal){
					JimpleLocal jimpleLocal = (JimpleLocal)object;
					if(jimpleLocal.getType() instanceof RefType){
						RefType refType = (RefType)jimpleLocal.getType();
						if(refType.getClassName().startsWith("java.lang.String")){
							logger.debug("We have a strig ref type");									
							outSet.add(new UnitValue(unitWrapper, jimpleLocal));
							addNewGraph(unitWrapper);

						}								
					}
				}
				else if(object instanceof StringConstant){
					addNewGraph(unitWrapper);
				}
			}
		}
		else if(unit.getInvokeExprBox().getValue() instanceof JInterfaceInvokeExpr){
			JInterfaceInvokeExpr staticInvokeExpr = (JInterfaceInvokeExpr)unit.getInvokeExprBox().getValue(); 
			logger.debug("TODO , must handle this case better.  Ask Luke and Steve");
		}
		else{
			NetworkFlowInterproceduralAnalysis.addUnitIfStringBearing(UnitWrapper.getSingleArgumentOrLessUnitWrapper(unitIn, sootMethod));
		}
	}

	private DelegateTree<UnitWrapper, String> addNewGraph(UnitWrapper unitIn) {
//		if(graphs.size() == 0){
		DelegateTree<UnitWrapper, String> newTree = null;;
		boolean treeExists = false;
		for(DelegateTree<UnitWrapper, String> tree : graphs){
			if(tree.getRoot().equals(unitIn)){
				logger.debug("This tree already exists ...");
				treeExists = true;
				newTree = tree;
			}
		}
		if(! treeExists){
			logger.debug("Adding a brand new graph");
			newTree = new DelegateTree<UnitWrapper, String>();
			newTree.addVertex(unitIn);
			logger.debug("Vertex added to graph");
			logger.debug("Tree size is: " + newTree.getVertexCount());
			graphs.add(newTree);																			
		}
		return newTree; 
	}

	private boolean  addTargetGraphToExtend(UnitValue unitValue, UnitWrapper unitIn) {
		boolean added = false;
		if(unitIn.getUnit() instanceof AbstractStmt){
			AbstractStmt abstractStmt = (AbstractStmt)unitIn.getUnit();
			logger.debug("I must evaluate this many graphs for this method: " + graphs.size());
			logger.debug("UnitIn: " + unitIn);
			logger.debug("UnitValue: " + unitValue.getUnitWrapper());
			logger.debug("UnitIn Type is: " + unitIn.getClass());
			if(unitIn.toString().contains("$r3 = virtualinvoke r4.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(\"createAdvertisable=\")")){				logger.debug("hold");
			}
			if(unitIn.toString().contains("$r4 = virtualinvoke $r4.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(\"strToastString=\")")){
				logger.debug("hold");				
			}
			for(DelegateTree<UnitWrapper, String> tree : graphs){
				logger.debug("Adding to target graph");
				boolean treeContainsUnitValue = tree.containsVertex(unitValue.getUnitWrapper());
				if( treeContainsUnitValue){
					boolean treeContainsVertex = tree.containsVertex(unitIn);
					if(! treeContainsVertex){
						tree.addEdge(NetworkFlowInterproceduralAnalysis.GLOBAL_COUNTER++ + "", unitValue.getUnitWrapper(), unitIn);
						logger.debug("Tree size is: " + tree.getVertexCount());
						logger.debug("Successfully added Vertex");
						added = true;						
					}
					else{
						logger.debug("This tree already contains the vertex");
						added = true;
					}
				}
				else{
					logger.debug("I am not in this graph ..." + unitValue);
				}
			}			
			if(! added){
       				logger.debug("Have an issue at this point ... ");
			}
		}
		else{
			logger.debug("Adding something that is not an instanceof AbstractStmt");
		}
		return added;
	}

	@Override
	protected void merge(Object in1, Object in2, Object out) {
		logger.debug("Called merge()");		
		FlowSet i1 = (FlowSet) in1, i2 = (FlowSet) in2, o = (FlowSet) out;
		i1.union(i2, o);
	}

	@Override
	protected void copy(Object source, Object dest) {
		logger.debug("Called copy()");		
		FlowSet s = (FlowSet) source;
		FlowSet t = (FlowSet) dest;
		s.copy(t);	
	}

	public Map<Unit, ArraySparseSet> getUnitToSet() {
		return unitToSet;
	}

	public ArrayList<DelegateTree<UnitWrapper, String>> getGraph() {
		return graphs;
	}

}
