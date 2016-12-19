import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;

public class LMDirichletsSimilarity extends SimilarityBase {

	final float mu;
	
	public LMDirichletsSimilarity(float mu) {
		this.mu = mu;
	}
	
	@Override
	protected float score(BasicStats stats, float freq, float docLen) {
		float collectionProbability = stats.getTotalTermFreq() / stats.getNumberOfFieldTokens();

		return (float) (stats.getBoost() *  Math.log((freq + mu * collectionProbability) / (docLen + mu)));
	}

	@Override
	public String toString() {
		return "";
	}

}
