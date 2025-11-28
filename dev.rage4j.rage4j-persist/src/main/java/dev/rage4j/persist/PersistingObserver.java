package dev.rage4j.persist;

import dev.rage4j.asserts.AssertionObserver;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.model.EvaluationAggregation;
import dev.rage4j.model.Sample;

/**
 * An {@link AssertionObserver} implementation that persists evaluation results
 * to an {@link EvaluationStore}. Use this to automatically record evaluations
 * when using rage4j-assert.
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * EvaluationStore store = Rage4jPersist.csv("target/evaluations");
 * RageAssert rageAssert = new OpenAiLLMBuilder().fromApiKey(key);
 * rageAssert.addObserver(new PersistingObserver(store));
 *
 * rageAssert.given()
 *     .question("What is AI?")
 *     .groundTruth("...")
 *     .when()
 *     .answer(llm::chat)
 *     .then()
 *     .assertFaithfulness(0.7); // Automatically recorded!
 * </pre>
 */
public class PersistingObserver implements AssertionObserver
{

	private final EvaluationStore store;

	/**
	 * Creates a new PersistingObserver that writes to the specified store.
	 *
	 * @param store
	 *            The store to persist evaluations to.
	 */
	public PersistingObserver(EvaluationStore store)
	{
		this.store = store;
	}

	@Override
	public void onEvaluation(Sample sample, Evaluation evaluation, boolean passed)
	{
		EvaluationAggregation aggregation = new EvaluationAggregation(sample);
		aggregation.put(evaluation);
		store.store(aggregation);
	}
}
