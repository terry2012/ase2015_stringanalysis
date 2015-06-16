package edu.buffalo.cse.blueseal.networkflow.intraprocedural;

import soot.Unit;
import soot.jimple.StringConstant;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.internal.JimpleLocal;

/**
 * 
 * Wrapper class to hold information about JInvokeStmt of use to network flow detection.
 * 
 * @author delvecchio
 *
 */
public class JInvokeStmtSummary extends NetworkFlowUnitSummary {
		
	public static String URL_INIT = "URL<init>";
	
	/**
	 * Creates a wrapper for a JInvokeStmt.  Includes logic to handle different constructor types for URL type.  Here, it is
	 * imperative that all cases for the URL constructor be handleded.
	 * 
	 * @param unit
	 * @param keyPart 
	 */
	public JInvokeStmtSummary(JInvokeStmt unit, String keyPart){
		setUnit(unit);
		if(unit.getInvokeExprBox().getValue() instanceof JSpecialInvokeExpr &&
			((JSpecialInvokeExpr)unit.getInvokeExprBox().getValue()).getMethodRef().declaringClass().getName().equals("java.net.URL") &&
			((JSpecialInvokeExpr)unit.getInvokeExprBox().getValue()).getMethodRef().name().equals("<init>")){
			setUrlNetworkFlowUnit(true);
			setHead(true);
			JSpecialInvokeExpr jSpecialInvokeExpr = ((JSpecialInvokeExpr)unit.getInvokeExprBox().getValue());
			if(jSpecialInvokeExpr.getArgCount() == 1){
				setConstructorType(UrlConstructor.SINGLE_ARG);
				setNextUnitMatchName(jSpecialInvokeExpr.getArgBox(0).getValue().toString()+keyPart);
			}
			else if(jSpecialInvokeExpr.getArgCount() > 1){
				setConstructorType(UrlConstructor.MULTI_ARG);
				setNextUnitMatchName("");	
				setUrlPart("three arg constructor");
			}
			setCurrentUnitMatchName(URL_INIT+jSpecialInvokeExpr.getBaseBox().getValue().toString()+keyPart);
		}
		else if(unit.getInvokeExprBox().getValue() instanceof JSpecialInvokeExpr &&
				((JSpecialInvokeExpr)unit.getInvokeExprBox().getValue()).getMethodRef().declaringClass().getName().equals("java.lang.StringBuilder") &&
				((JSpecialInvokeExpr)unit.getInvokeExprBox().getValue()).getMethodRef().name().equals("<init>")){
			JSpecialInvokeExpr jSpecialInvokeExpr = ((JSpecialInvokeExpr)unit.getInvokeExprBox().getValue());
			if (jSpecialInvokeExpr.getArgCount() == 0){
				setUrlPart("");
				setCurrentUnitMatchName(jSpecialInvokeExpr.getBaseBox().getValue().toString()+keyPart);
				setNextUnitMatchName("");								
				
			}
			else if(jSpecialInvokeExpr.getArgBox(0).getValue() instanceof StringConstant){
				setUrlPart(jSpecialInvokeExpr.getArgBox(0).getValue().toString());
				setCurrentUnitMatchName(jSpecialInvokeExpr.getBaseBox().getValue().toString()+keyPart);
				setNextUnitMatchName("");				
			}
			else if(jSpecialInvokeExpr.getArgBox(0).getValue() instanceof JimpleLocal){
				setUrlPart("");
				setCurrentUnitMatchName(jSpecialInvokeExpr.getBaseBox().getValue().toString()+keyPart);
				setNextUnitMatchName(jSpecialInvokeExpr.getArgBox(0).getValue().toString());								
			}
			else if(jSpecialInvokeExpr.getArgBox(0).getValue() instanceof JIdentityStmt){
				setUrlPart("");
				setCurrentUnitMatchName(jSpecialInvokeExpr.getBaseBox().getValue().toString()+keyPart);
				setNextUnitMatchName(jSpecialInvokeExpr.getArgBox(0).getValue().toString());								
			}
		}
		else if(unit.getInvokeExprBox().getValue() instanceof JSpecialInvokeExpr ){
			JSpecialInvokeExpr jSpecialInvokeExpr = ((JSpecialInvokeExpr)unit.getInvokeExprBox().getValue());
			setUrlPart("");
			setCurrentUnitMatchName(jSpecialInvokeExpr.getBaseBox().getValue().toString()+keyPart);
			setNextUnitMatchName("");
			interProceduralOutSet.add(((JSpecialInvokeExpr)unit.getInvokeExprBox().getValue()).getMethodRef().declaringClass().getName());
			setIntraProcedural(false);

		}

	}
	
}
