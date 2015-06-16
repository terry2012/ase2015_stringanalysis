package edu.buffalo.cse.blueseal.networkflow.interprocedural;

import soot.SootMethod;
import soot.Unit;

/**
 * Holder for Units placed in Jung DelegateTree. Needed as a strict Unit based
 * DelegateTree would leave it impossible to differentiate multiple strings that
 * come from different argument constructors. Also need to harvest strings from
 * DelegateTrees and not combine strings that come from different arguments.
 * 
 * @author delvecchio
 * 
 */
public class UnitWrapper {

	public boolean isMultiArgument() {
		return argumentIndex > -1;
	}

	public Unit getUnit() {
		return unit;
	}

	public int getArgumentIndex() {
		return argumentIndex;
	}

	private Unit unit;
	private int argumentIndex;
	private SootMethod sootMethod;

	public static UnitWrapper getSingleArgumentOrLessUnitWrapper(Unit unit, SootMethod sootMethod) {
		return new UnitWrapper(unit, sootMethod);
	}

	public static UnitWrapper getMultiArgumentUnitWrapper(Unit unit, int index, SootMethod sootMethod) {
		return new UnitWrapper(unit, index, sootMethod);
	}

	private UnitWrapper(Unit unit, SootMethod newSootMethod) {
		this.unit = unit;
		argumentIndex = -1;
		sootMethod = newSootMethod;
	}

	private UnitWrapper(Unit unit, int index, SootMethod newSootMethod) {
		this.unit = unit;
		argumentIndex = index;
		this.sootMethod = newSootMethod;
	}
	
	public String toString(){
		String prefix = "";
		if(argumentIndex >= 0){
			prefix = "Arg index: " + argumentIndex; 			
		}
		return  prefix + " " + unit.toString(); 
	}

	public boolean equals(Object obj){
		if(! (obj instanceof UnitWrapper)){
			return false;
		}
		else{
			UnitWrapper unitWrapper = (UnitWrapper)obj;
			if(! unitWrapper.isMultiArgument()){
				return unit.equals(unitWrapper.getUnit());
			}
			else{
				if(argumentIndex != unitWrapper.getArgumentIndex()){
					return false;
				}
				else{
					return unit.equals(unitWrapper.getUnit());					
				}
			}
		}
	}

	public SootMethod getSootMethod() {
		return sootMethod;
	}

	
}
