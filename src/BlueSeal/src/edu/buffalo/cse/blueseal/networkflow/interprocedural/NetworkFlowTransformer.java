package edu.buffalo.cse.blueseal.networkflow.interprocedural;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.buffalo.cse.blueseal.BSFlow.AsyncTaskResolver;
import edu.buffalo.cse.blueseal.BSFlow.BSInterproceduralAnalysis;
import edu.buffalo.cse.blueseal.BSFlow.BSInterproceduralTransformer;
import edu.buffalo.cse.blueseal.BSFlow.CgTransformer;
import edu.buffalo.cse.blueseal.BSFlow.LayoutFileParser;
import edu.buffalo.cse.blueseal.BSFlow.MessengerResolver;
import edu.buffalo.cse.blueseal.BSFlow.SootMethodFilter;
import edu.buffalo.cse.blueseal.blueseal.EntryPointsMapLoader;

import soot.Body;
import soot.Hierarchy;
import soot.IntType;
import soot.Local;
import soot.MethodOrMethodContext;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Transformer;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.NewExpr;
import soot.jimple.NullConstant;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.SimpleLiveLocals;
import soot.toolkits.scalar.SmartLocalDefs;
import soot.util.Chain;
import soot.util.queue.QueueReader;

public class NetworkFlowTransformer extends SceneTransformer {

	static Logger logger = Logger.getLogger(NetworkFlowTransformer.class);
	
	private static NetworkFlowTransformer flowTransformer = new NetworkFlowTransformer();
	
    @Override
    protected void internalTransform(String arg0, Map arg1) {
    	logger.debug("In internalTransform NetworkFlowTransformer");
        List<SootMethod> entryPoints = CgTransformer.entryPoints;
        CallGraph cg = CgTransformer.cg;
        
       	logger.debug("Call graph size is: " + cg.size());
       	logger.debug("Entry points are: " + entryPoints.size());
        
        NetworkFlowInterproceduralAnalysis inter = 
				new NetworkFlowInterproceduralAnalysis(cg, new NetworkFlowSootMethodFilter(), 
						entryPoints.iterator(), false);

    }

	public static Transformer instance() {
		return flowTransformer;
	}


}
