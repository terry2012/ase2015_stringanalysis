package edu.buffalo.cse.blueseal.networkflow.intraprocedural;

import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.internal.JimpleLocal;

/**
 * 
 * Wrapper class to hold information about JIdentityStmt of use to network flow detection.  Captures passed in parameter, class variables, etc.
 * 
 * @author delvecchio
 *
 */
public class JIdentityStmtSummary extends NetworkFlowUnitSummary {

	
	/**
	 * Creates a wrapper for a JIdentityStatement.  Includes logic to handle different assignments made to:
	 *   - String
	 * 
	 * Others to be added.
	 * 
	 * @param unit
	 * @param keyPart 
	 */
	public JIdentityStmtSummary(JIdentityStmt unit, String keyPart) {
		setUnit(unit);
		setUrlPart(unit.toString());
		setCurrentUnitMatchName(unit.leftBox.getValue().toString()+keyPart);
		interProceduralParameters.add(unit.toString());
		setNextUnitMatchName("");
		setIntraProcedural(false);
	}

	
}
