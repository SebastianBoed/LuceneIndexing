import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;

public class LMMercerSimilarity extends SimilarityBase {

	final float lambda;
	
	public LMMercerSimilarity(float lambda) {
		this.lambda = lambda;
	}
	
	@Override
	protected float score(BasicStats stats, float freq, float docLen) {
		float documentProbability = freq / docLen;
		float collectionProbability = stats.getTotalTermFreq() / stats.getNumberOfFieldTokens();
		return (float) (stats.getBoost() * Math.log((lambda * documentProbability) + (1 - lambda) * collectionProbability));
	}

	@Override
	public String toString() {
		return "";
	}

}
