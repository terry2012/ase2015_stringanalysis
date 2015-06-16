package edu.buffalo.cse.blueseal.networkflow.summary;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.map.HashedMap;

/**
 * 
 * Class that holds information that allows for a comparison of two strings based on the calling methods used in construction of the string.  
 * Provides for different ways to score Strings.
 * @author delvecchio
 *
 */
public class StringSimilarityScore {
	

	private boolean hasMatch;
	private String primaryName;
	private String comparisonName;
	private double score;
	private Map<String, Integer> matches;
	private static Object[] METHODS_LIST = TfidfGenerator.UGLY_MATRIX.keySet().toArray();
	
	public Map<String, Integer> getMatches(){
		return matches;
	}
	
	public boolean isHasMatch() {
		return hasMatch;
	}

	public String getPrimaryName() {
		return primaryName;
	}

	public String getComparisonName() {
		return comparisonName;
	}

	public double getScore() {
		return score;
	}

	/**
	 * Take the lookup key for the String and its row array and compare with another string and its row array.
	 * @author delvecchio
	 *
	 */
	public StringSimilarityScore(String primaryName, int primaryArrayPosition, String comparisonName, int comparisonArrayPosition){
		this.primaryName = primaryName;
		this.comparisonName = comparisonName;
		this.matches = new HashMap<String, Integer>();  
		scoreMataching(primaryArrayPosition, comparisonArrayPosition);
	}

	private void scoreMataching(int primaryArrayPosition, int comparisonArrayPosition ) {
		//TODO doing this to SKIP the holder one.  Dirty trick - TODO means we need to address the holder issue.
		
		int compositeMatch = 0;
		int compositeNonMatch = 0;
		for (int i = 0; i < TfidfGenerator.MATRIX.length; i++) {
			int primaryValue = TfidfGenerator.MATRIX[i][primaryArrayPosition];
			int comparisonValue = TfidfGenerator.MATRIX[i][comparisonArrayPosition];
			// Have a match
			if(! METHODS_LIST[i].toString().equals("holder")){
				if(primaryValue > 0 && comparisonValue > 0){
					if(primaryValue > comparisonValue){
						compositeMatch += comparisonValue;
						// TODO this may be a bullshit score.
						compositeNonMatch += primaryValue - comparisonValue;
						matches.put(METHODS_LIST[i].toString(), primaryValue - comparisonValue);
					}
					else if(primaryValue < comparisonValue){
						compositeMatch += primaryValue;
						// TODO this may be a bullshit score.
						compositeNonMatch += comparisonValue - primaryValue; 					
						matches.put(METHODS_LIST[i].toString(), comparisonValue - primaryValue);
					}
					else{
						compositeMatch += primaryValue;				
						matches.put(METHODS_LIST[i].toString(), primaryValue);					
					}
				}
				else if(primaryValue == 0 && comparisonValue > 0){
					compositeNonMatch += comparisonValue;
				}
				else if(primaryValue > 0 && comparisonValue == 0){
					compositeNonMatch += primaryValue;
				}				
			}
		}
		if(compositeMatch > 0){
			hasMatch = true;
			score = (double)compositeMatch / (compositeMatch + compositeNonMatch);
		}
		else{
			hasMatch = false;
		}
	}
	

}
