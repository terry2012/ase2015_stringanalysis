package edu.buffalo.cse.blueseal.networkflow.intraprocedural;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.buffalo.cse.blueseal.BSFlow.BSInterproceduralAnalysis;
import edu.buffalo.cse.blueseal.networkflow.intraprocedural.NetworkFlowUnitSummary.UrlConstructor;

import soot.Body;
import soot.PatchingChain;
import soot.SootMethod;
import soot.Unit;
import soot.toolkits.graph.ExceptionalUnitGraph;

/**
 * 
 * Class that:
 * 
 * 	1. Performs linear time analysis of input SootMethod
 *  2. Holds analysis results in HashMap for future reference
 * 
 * @author delvecchio
 *
 */
public class NetworkFlowMethodAnalysis {
	
	static Logger logger = Logger.getLogger(NetworkFlowMethodAnalysis.class);

	private SootMethod sootMethod;
	private HashMap<String, NetworkFlowUnitSummary> unitSummaryHashMap; 
	private ArrayList<String> urls;
	private List<Unit> inSet = new ArrayList<Unit>(); 
	private List<SootMethod> outset;
	private List<SootMethod> sootMethodInset;
	private UrlConstructor urlConstructorType;

	/**
	 * Constructor that accepts SootMethod under analysis and performs analysis. 
	 * @param sootMethod
	 * @param outset 
	 * @param inset 
	 */
	/**
	 * @param sootMethod
	 * @param inset
	 * @param outset
	 */
	public NetworkFlowMethodAnalysis(SootMethod sootMethod, List<SootMethod> inset, List<SootMethod> outset){
		this.sootMethod = sootMethod;
		unitSummaryHashMap = new HashMap<String, NetworkFlowUnitSummary>();
		urls = new ArrayList<String>();
		this.outset = outset;
		sootMethodInset = inset;
	}
	
	/**
	 * Analyze the SootMethod, unit by unit, and add each unit to HashMap with corresponding NetworkFlowUnitSummary
	 */
	public void doAnalysis() {
		PatchingChain<Unit> units = sootMethod.getActiveBody().getUnits();
		for (Unit unit : units) {
			NetworkFlowUnitSummary networkFlowUnitSummary = NetworkFlowUnitSummaryFactory.createNetworkFlowUnitSummary(unit, sootMethod.getDeclaringClass() + "." + sootMethod.getName());
			if(!(networkFlowUnitSummary instanceof GenericSummary)){				
				unitSummaryHashMap.put(networkFlowUnitSummary.getCurrentUnitMatchName(), networkFlowUnitSummary);				
			}
		}
		detectUrls();			
	}


	
	/**
	 * Return inset that is directly relevant to a URL that is being created
	 * @return
	 */
	public List<Unit> getInterProceduralUrlChainInSet(){
		return inSet;
		
	}
	
	/**
	 * Return outset that is directly relevant to a URL that is being created (versus entire outset for method)
	 * @return
	 */
	public List<String> getInterProceduralUrlChainOutSet(){
		List<String> outSet = new ArrayList<String>();
		Collection<NetworkFlowUnitSummary> summaries = unitSummaryHashMap.values();
		for (NetworkFlowUnitSummary networkFlowUnitSummary : summaries) {
			outSet.addAll(networkFlowUnitSummary.getInterProceduralOutSet());
		}
		return outSet;
	}

	
	/**
	 * Return true if URL creation is purely intraprocedural, false if interprocedural.
	 * @return
	 */
	public boolean isIntraProcedural(){
		boolean intraProcedural = true;
		Collection<NetworkFlowUnitSummary> summaries = unitSummaryHashMap.values();
		for (NetworkFlowUnitSummary networkFlowUnitSummary : summaries) {
			intraProcedural = networkFlowUnitSummary.isIntraProcedural();
			if(! intraProcedural){
				break;
			}
		}
		return intraProcedural;
	}
	
	public UrlConstructor getUrlConstructorType(){
		return urlConstructorType;
	}
	
	/**
	 * Evaluate the HashMap and assemble the URLs, storing each found URL to the List<> returned by getDetectedUrls()
	 */
	private void detectUrls() {
		Set<String> keys = unitSummaryHashMap.keySet();
		for (String key : keys) {
			if(key != null && key.startsWith(JInvokeStmtSummary.URL_INIT)){
				if( key.contains("com.omniture.RequestHandlerSe13.requestConnect") || 
						key.contains("com.google.ads.ac.run") || 
						key.contains("bbc.mobile.news.model.Article.getAsUrl")){
					logger.debug("We have one of our insets that must be evaluated");
				}
				NetworkFlowUnitSummary nfus = unitSummaryHashMap.get(key);
				urlConstructorType = nfus.getConstructorType();
				urls.add(tracePath(key));
			}
		}
	}

	
	/**
	 * Evaluate the HashMap and assemble the resultant URL.
	 * @param key
	 * @return
	 */
	private String tracePath(String key) {
		String url = "";
		NetworkFlowUnitSummary flowUnitSummary = unitSummaryHashMap.get(key);
		if(flowUnitSummary != null && flowUnitSummary.getUrlPart() != null){
			url = flowUnitSummary.getUrlPart();
			if(flowUnitSummary.getNextUnitMatchName() != null && flowUnitSummary.getNextUnitMatchName().equals(key)){
				//logger.debug("Have hit a dead end!");
				return url;
			}
			else if(! flowUnitSummary.getNextUnitMatchName().equals("")){
				//logger.debug("Moving onto new nextunit: " + flowUnitSummary.getNextUnitMatchName() + " where key is " + key);
				url = url + tracePath(flowUnitSummary.getNextUnitMatchName());
			}
			//handle inset cases
			else if(flowUnitSummary instanceof JIdentityStmtSummary){
				inSet.add(flowUnitSummary.getUnit());
			}
			else{
				//logger.debug("Fell to 'else' clause of tracePath in NetworkFlowMethodAnalysis");
			}
			
		}
		else{
			//logger.debug("Hit else of outer if - so the hashmap must be empty");
			url = url + key;
		}
		return url;
	}

	/**
	 * Return SootMethod evaluated.
	 * @return
	 */
	public SootMethod getSootMethod() {
		return sootMethod;
	}

	/**
	 * Set SootMethod evaluated.
	 * @return
	 */
	public void setSootMethod(SootMethod sootMethod) {
		this.sootMethod = sootMethod;
	}
	
	/**
	 * Return NetworkFlowUnitSummary for provided Unit.
	 * 
	 * @param unit
	 * @return
	 */
	public NetworkFlowUnitSummary getNetworkFlowUnitSummary(Unit unit){
		return unitSummaryHashMap.get(unit);
	}

	/**
	 * Returns the detected URLs for the SootMethod analyzed.
	 * @return
	 */
	public List<String> getDetectedUrls() {
		return urls;
	}

	public boolean isInterProcedural() {
		return getInterProceduralUrlChainOutSet().size() > 0;
	}
}
