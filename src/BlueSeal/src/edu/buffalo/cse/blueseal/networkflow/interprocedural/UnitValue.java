package edu.buffalo.cse.blueseal.networkflow.interprocedural;

import java.util.HashSet;

import soot.*;

public class UnitValue {

	private UnitWrapper unit;
	private Value value;
	
	public UnitValue(UnitWrapper unit, Value value){
		this.setUnitWrapper(unit);
		this.setValue(value);
	}

	public UnitWrapper getUnitWrapper() {
		return unit;
	}

	public void setUnitWrapper(UnitWrapper unit) {
		this.unit = unit;
	}

	public  Value getValue() {
		return value;
	}

	public void setValue( Value value) {
		this.value = value;
	}
	
	public boolean equals(Object obj){
		boolean equals = true;
		if(obj instanceof UnitValue){
			UnitValue unitValue = (UnitValue)obj;
			if(unit.toString().equals(unitValue.getUnitWrapper().toString()) && value.toString().equals(unitValue.getValue().toString())){
				equals = true;
			}
			else{
				equals = false;
			}
		}
		else{
			equals = false;
		}
		return equals;
	}
	
}
