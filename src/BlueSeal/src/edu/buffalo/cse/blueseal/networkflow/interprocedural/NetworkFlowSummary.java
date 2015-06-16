package edu.buffalo.cse.blueseal.networkflow.interprocedural;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import edu.uci.ics.jung.graph.DelegateTree;

import soot.RefType;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.VoidType;
import soot.toolkits.graph.ExceptionalUnitGraph;

public class NetworkFlowSummary {
	
	static Logger logger = Logger.getLogger(NetworkFlowSummary.class);
	private ArrayList<DelegateTree<UnitWrapper, String>> graphs;
	
	public NetworkFlowSummary(ArrayList<DelegateTree<UnitWrapper, String>> g){
		graphs = g;
	}
	
	public void setGraphs(ArrayList<DelegateTree<UnitWrapper, String>> newGraph){
		graphs = newGraph;
	}
	
	public boolean equals(Object obj){
		logger.debug("---------------------------------------------");
		logger.debug("In equals call.");
		NetworkFlowSummary flowSummary = (NetworkFlowSummary)obj;
		boolean equals = false;
		if(graphs.size() == 0 && flowSummary.getGraphs().size() == 0){
			equals = true;
			return equals;
		}
		
		for(DelegateTree<UnitWrapper,String> graph : graphs){
			int vertexCount = graph.getVertexCount();
			int edgetCount = graph.getEdgeCount();
			for(DelegateTree<UnitWrapper,String> flowSummaryGraph : flowSummary.getGraphs()){
				logger.debug("Outer root toString is: " + graph.getRoot().toString());
				if(graph.getRoot().toString().equals(flowSummaryGraph.getRoot().toString())){
					logger.debug("Inner root toString is: " + flowSummaryGraph.getRoot().toString());
					if(flowSummaryGraph.getVertexCount() == vertexCount &&
							flowSummaryGraph.getEdgeCount() == edgetCount){
						equals = true;
						logger.debug("Set equals to true");
					}
					else{
						logger.debug("equals remains false.");
					}
				}
			}
		}
		logger.debug("Equals to be sent back is: " + equals);
		return equals;							
	}

	private ArrayList<DelegateTree<UnitWrapper, String>> getGraphs() {
		return graphs;
	}


}
