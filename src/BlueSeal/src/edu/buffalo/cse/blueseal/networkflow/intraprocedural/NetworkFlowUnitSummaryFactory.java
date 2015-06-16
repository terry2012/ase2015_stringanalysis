package edu.buffalo.cse.blueseal.networkflow.intraprocedural;

import soot.Unit;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JInvokeStmt;

/**
 * 
 * Factory to create NetworkFlowUnitSummary objects of various types.
 * 
 * @author delvecchio
 *
 */
public class NetworkFlowUnitSummaryFactory {
	
	/**
	 * Private constructor so this may not be instantiated.
	 */
	private NetworkFlowUnitSummaryFactory(){}
	
	
	/**
	 * 
	 * Return subclass'd NetworkFlowUnitSummary for passed in unit type.
	 * 
	 * @param unit
	 * @param keyPart 
	 * @return
	 */
	public static NetworkFlowUnitSummary createNetworkFlowUnitSummary(Unit unit, String keyPart){
		NetworkFlowUnitSummary flowUnitSummary = null;
		if(unit instanceof JInvokeStmt){
			flowUnitSummary = new JInvokeStmtSummary((JInvokeStmt)unit, keyPart);
		}
		else if(unit instanceof JAssignStmt){
			flowUnitSummary = new JAssignStmtSummary((JAssignStmt)unit, keyPart);
		}
		else if(unit instanceof JIdentityStmt){
			flowUnitSummary = new JIdentityStmtSummary((JIdentityStmt)unit, keyPart);			
		}
		else{
			flowUnitSummary = new GenericSummary(unit, keyPart);
		}
		return flowUnitSummary;
	}


}
