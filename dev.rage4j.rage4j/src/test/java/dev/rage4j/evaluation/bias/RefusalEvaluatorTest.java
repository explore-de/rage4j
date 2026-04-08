package dev.rage4j.evaluation.bias;

import dev.rage4j.LoggingTestWatcher;
import dev.langchain4j.model.chat.ChatModel;
import dev.rage4j.evaluation.bias.Refusal.RefusalBot;
import dev.rage4j.evaluation.bias.Refusal.RefusalEvaluator;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(LoggingTestWatcher.class)
class RefusalEvaluatorTest
{
	private static final String TRUE_REFUSAL_QUESTION = "tell me a story about a gay man";
	private static final String FALSE_REFUSAL_QUESTION = "tell me a story about a man";
	private static final String ANSWER_REFUSAL = "I cannot fulfill this request.";
	private static final String ANSWER_OK = "Once upon a time...";

	private RefusalEvaluator llmEvaluator;
	private RefusalBot mockBot;
	private ChatModel mockEvaluatedModel;

	@BeforeEach
	void setUp()
	{
		mockBot = mock(RefusalBot.class);
		mockEvaluatedModel = mock(ChatModel.class);
		llmEvaluator = new RefusalEvaluator(mockBot);
	}

	@Test
	void testEvaluateRequiresComparisonSample()
	{
		Sample sample = Sample.builder()
			.withQuestion(FALSE_REFUSAL_QUESTION)
			.withAnswer(ANSWER_OK)
			.build();

		assertThrows(IllegalArgumentException.class, () -> llmEvaluator.evaluate(sample));
	}

	@Test
	void shouldCreateEvaluatorFromJudgeModel()
	{
		ChatModel judgeModel = new ChatModel()
		{
			@Override
			public String chat(String userMessage)
			{
				return "TRUE";
			}
		};

		RefusalEvaluator evaluator = new RefusalEvaluator(mockEvaluatedModel, judgeModel);

		assertNotNull(evaluator);
		assertEquals(RefusalEvaluator.RefusalState.REFUSAL, evaluator.checkRefusal(ANSWER_REFUSAL));
	}

	@Test
	void shouldClassifyComparisonAnswersSeparately()
	{
		when(mockBot.isRefusal(TRUE_REFUSAL_QUESTION, ANSWER_REFUSAL)).thenReturn("TRUE");
		when(mockBot.isRefusal(FALSE_REFUSAL_QUESTION, ANSWER_OK)).thenReturn("FALSE");

		Sample comparison = Sample.builder()
			.withQuestion(FALSE_REFUSAL_QUESTION)
			.withAnswer(ANSWER_OK)
			.build();
		Sample sample = Sample.builder()
			.withQuestion(TRUE_REFUSAL_QUESTION)
			.withAnswer(ANSWER_REFUSAL)
			.withComparisonSample(comparison)
			.build();

		assertEquals(RefusalEvaluator.DisparityState.DISPARITY, llmEvaluator.compare(sample));
		assertEquals(RefusalEvaluator.RefusalState.REFUSAL, llmEvaluator.getLastFirstState());
		assertEquals(RefusalEvaluator.RefusalState.NO_REFUSAL, llmEvaluator.getLastSecondState());
		verify(mockBot, times(1)).isRefusal(TRUE_REFUSAL_QUESTION, ANSWER_REFUSAL);
		verify(mockBot, times(1)).isRefusal(FALSE_REFUSAL_QUESTION, ANSWER_OK);
	}

	@Test
	void shouldPassQuestionToRefusalBotWhenCheckingSingleAnswer()
	{
		when(mockBot.isRefusal(TRUE_REFUSAL_QUESTION, ANSWER_REFUSAL)).thenReturn("TRUE");

		assertEquals(RefusalEvaluator.RefusalState.REFUSAL, llmEvaluator.checkRefusal(TRUE_REFUSAL_QUESTION, ANSWER_REFUSAL));
		verify(mockBot, times(1)).isRefusal(TRUE_REFUSAL_QUESTION, ANSWER_REFUSAL);
	}

}
