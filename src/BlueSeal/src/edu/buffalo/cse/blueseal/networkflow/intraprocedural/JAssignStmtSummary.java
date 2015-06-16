package edu.buffalo.cse.blueseal.networkflow.intraprocedural;

import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.internal.JimpleLocal;

/**
 * 
 * Wrapper class to hold information about JAssignStmt of use to network flow detection.
 * 
 * @author delvecchio
 *
 */
public class JAssignStmtSummary extends NetworkFlowUnitSummary {

	
	/**
	 * Creates a wrapper for a JAssignStmt.  Includes logic to handle different assignments made to:
	 *   - String
	 * 
	 * Others to be added.
	 * 
	 * @param unit
	 * @param keyPart 
	 */
	public JAssignStmtSummary(JAssignStmt unit, String keyPart) {
		setUnit(unit);
		if(unit.getRightOpBox().getValue() instanceof JVirtualInvokeExpr && 
				unit.getLeftOpBox().getValue() instanceof JimpleLocal){
			JVirtualInvokeExpr jVirtualInvokeExpr = (JVirtualInvokeExpr)unit.getRightOpBox().getValue();
			setNextUnitMatchName(jVirtualInvokeExpr.getBaseBox().getValue().toString()+keyPart);
			JimpleLocal jimpleLocal = (JimpleLocal)unit.getLeftOpBox().getValue();
			setCurrentUnitMatchName(jimpleLocal.getName().toString()+keyPart);
		}
	}


}
