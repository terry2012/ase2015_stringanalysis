package edu.buffalo.cse.blueseal.networkflow.summary;

import java.util.Comparator;

public class SimilarityScoreComparator<T> implements Comparator<StringSimilarityScore> {

	@Override
	public int compare(StringSimilarityScore arg0, StringSimilarityScore arg1) {
        if(arg0.getScore() < arg1.getScore()){
            return 1;
        } else {
            return -1;
        }
	}

}
