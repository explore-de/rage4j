package dev.rage4j.asserts;

import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.model.Sample;

/**
 * Observer interface for monitoring assertion evaluations. Implementations can
 * be used to record, log, or react to assertion results.
 *
 * <p>
 * Example usage with rage4j-persist:
 * </p>
 *
 * <pre>
 * RageAssert rageAssert = new OpenAiLLMBuilder().fromApiKey(key);
 * rageAssert.addObserver(new PersistingObserver(store));
 *
 * rageAssert.given()
 * 	.question("What is AI?")
 * 	.groundTruth("...")
 * 	.when()
 * 	.answer(llm::chat)
 * 	.then()
 * 	.assertFaithfulness(0.7); // Observer is notified
 * </pre>
 */
@FunctionalInterface
public interface AssertionObserver
{

	/**
	 * Called after an assertion evaluation is performed.
	 *
	 * @param sample
	 *            The sample that was evaluated.
	 * @param evaluation
	 *            The evaluation result.
	 * @param passed
	 *            Whether the assertion passed (true) or failed (false).
	 */
	void onEvaluation(Sample sample, Evaluation evaluation, boolean passed);
}
