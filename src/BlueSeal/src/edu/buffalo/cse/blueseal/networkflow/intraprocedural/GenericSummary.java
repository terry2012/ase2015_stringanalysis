package edu.buffalo.cse.blueseal.networkflow.intraprocedural;

import soot.Unit;

/**
 * Generic NetworkFlowUnitSummary created for objects not of interest to network flow detection.
 * @author delvecchio
 *
 */
public class GenericSummary extends NetworkFlowUnitSummary {
	
	/**
	 * Creates basic GenericSummary object with no frills.
	 * @param unit
	 * @param keyPart 
	 */
	public GenericSummary(Unit unit, String keyPart){
		setUnit(unit);
		
		setUrlPart("Generic entry" + unit.toString());
		setNextUnitMatchName("");

		
	}

}
