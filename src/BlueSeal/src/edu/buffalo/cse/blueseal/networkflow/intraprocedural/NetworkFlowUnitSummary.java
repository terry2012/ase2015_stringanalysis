package edu.buffalo.cse.blueseal.networkflow.intraprocedural;

import java.util.ArrayList;
import java.util.List;

import soot.Unit;

/**
 * Provides an 'easy access' summary for a Unit for the purposes of network flow, or URL, detection.  
 * 
 * Consolidates and provides access to Unit information necessary to create detected URL strings. 
 * 
 * @author delvecchio
 *
 */
public abstract class NetworkFlowUnitSummary {
	
	private Unit unit;
	private String nextUnit = "";
	private String currentUnit = "";
	private String urlPart = "";
	private boolean urlNetworkFlowUnit = false;
	private boolean isHead = false;
	private boolean isTail = false;
	private boolean isIntraProcedural = true;
	protected List<String> interProceduralOutSet = new ArrayList<String>();
	protected List<String> interProceduralParameters = new ArrayList<String>();
	private UrlConstructor constructorType;
	
	public enum UrlConstructor {
	    SINGLE_ARG, MULTI_ARG 
	}
	
	
	/**
	 * Returns the in set for InterProcedural analysis.
	 * @return
	 */
	public List<String> getInterProceduralInSet(){
		return interProceduralParameters;
	}

	
	/**
	 * Returns the out set for InterProcedural analysis.
	 * @return
	 */
	public List<String> getInterProceduralOutSet(){
		return interProceduralOutSet;
	}
	
	
	/**
	 * Identifies if the analysis proves to be IntraProcedural related only.  If the value returned is false - this indicates the analysis 
	 * requires further InterProcedural analysis.
	 * @return
	 */
	public boolean isIntraProcedural() {
		return isIntraProcedural;
	}


	/**
	 * Set to 'true' if the URL can be detected via IntraProcedural analysis alone, 'false' if not (i.e. requires InterProcedural).
	 * @param isIntraProcedural
	 */
	public void setIntraProcedural(boolean isIntraProcedural) {
		this.isIntraProcedural = isIntraProcedural;
	}


	/**
	 * Returns either part of or an entire URL string.  Depends upon how the URL is constructed.
	 * @return
	 */
	public String getUrlPart(){
		return urlPart;
	}
	

	/**
	 * Add the part of the URL this Unit holds.
	 * @param part
	 */
	public void setUrlPart(String part){
		urlPart = urlPart + part;
	}
	
	
	/**
	 * Set the current Unit's match name.  The match name allows you to construct the network flow detection chain.
	 * @param currentUnit
	 */
	public void setCurrentUnitMatchName(String currentUnit) {
		this.currentUnit = currentUnit;
	}

	/**
	 *  Get the current Unit's match name.  The match name allows you to construct the network flow detection chain.
	 * @return
	 */
	public String getCurrentUnitMatchName() {
		return currentUnit;
	}

	
	/**
	 * Set the wrapped Unit summarized.
	 * 
	 * @param unit
	 */
	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	/**
	 * Set the next Unit to be traversed in network flow detection scenario.
	 * @param nextUnit
	 */
	public void setNextUnitMatchName(String nextUnit) {
		this.nextUnit = nextUnit;
	}

	/**
	 * Returns wrapped Unit for this Summary object.
	 * @return
	 */
	public Unit getUnit() {
		return unit;
	}

	/**
	 * 
	 * Returns the next Unit pointed to by the JInvokeStmt and the next logical Unit to be evaluated as a String value.
	 * 
	 * @return
	 */
	public String getNextUnitMatchName() {
		return nextUnit;
	}

	
	/**
	 * Returns true if this is the tail or terminus of a network flow detection path.
	 * @return
	 */
	public boolean isTail() {
		return isTail;
	}

	/**
	 * Sets whether this Unit is the tail or terminus of a network flow detection path.  
	 * 
	 * @param isTail
	 */
	public void setTail(boolean isTail) {
		this.isTail = isTail;
	}

	/**
	 * Set whether this NetworkFlowUnitSummary is the head of a network flow detection chain.  For
	 * example, it is true if this is a wrapper for a Unit that creates a URL object.
	 * @param isHead
	 */
	public void setHead(boolean isHead) {
		this.isHead = isHead;
	}


	/**
	 * Set java.net.URL type constructor.  Is it .
	 * @param isHead
	 */
	public void setConstructorType(UrlConstructor urlConstructor) {
		constructorType = urlConstructor;
	}


	/**
	 * Get java.net.URL type constructor.  Is it .
	 * @param isHead
	 */
	public UrlConstructor getConstructorType() {
		return constructorType;
	}

	
	/**
	 * Returns true if this is the head of a network flow chain.  That is, a new URL object constructed,
	 * or a call to setURL on a URL object.  Indicates the NetworkFlowUnitSummary object is the head and
	 * start point to be traversed.
	 *  
	 * @return
	 */
	public boolean isHead() {
		return isHead;
	}

	/**
	 * 
	 * Assign whether the Unit is part of a URL network flow detection.  True if it is, false if not.
	 * 
	 * @param urlNetworkFlowUnit
	 */
	public void setUrlNetworkFlowUnit(boolean urlNetworkFlowUnit) {
		this.urlNetworkFlowUnit = urlNetworkFlowUnit;
	}

	/**
	 * True if Unit is part of a URL network flow detection, false if not.
	 * 
	 * @return
	 */
	public boolean isUrlNetworkFlowUnit(){
		return urlNetworkFlowUnit;
	}

	
}
