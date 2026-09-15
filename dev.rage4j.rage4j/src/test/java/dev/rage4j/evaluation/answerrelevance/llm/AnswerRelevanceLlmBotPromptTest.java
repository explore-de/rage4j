package dev.rage4j.evaluation.answerrelevance.llm;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.rage4j.LoggingTestWatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the placement of the question and the answer in the judge prompt; see
 * {@link AnswerRelevanceLlmBot} for why they must be in the user message.
 */
@ExtendWith(LoggingTestWatcher.class)
class AnswerRelevanceLlmBotPromptTest
{
	private static final List<String> PLACEHOLDERS = List.of("{{question}}", "{{answer}}");

	@Test
	void everyMethodPassesQuestionAndAnswerInTheUserMessage()
	{
		Method[] methods = AnswerRelevanceLlmBot.class.getDeclaredMethods();
		assertTrue(methods.length > 0, "the bot interface declares no methods");

		for (Method method : methods)
		{
			UserMessage userMessage = method.getAnnotation(UserMessage.class);
			assertNotNull(userMessage, method.getName() + " has no @UserMessage");
			String userText = String.join("\n", userMessage.value());
			for (String placeholder : PLACEHOLDERS)
			{
				assertTrue(userText.contains(placeholder),
					method.getName() + ": " + placeholder + " is missing from the @UserMessage");
			}
		}
	}

	@Test
	void noMethodPassesQuestionOrAnswerInTheSystemMessage()
	{
		for (Method method : AnswerRelevanceLlmBot.class.getDeclaredMethods())
		{
			SystemMessage systemMessage = method.getAnnotation(SystemMessage.class);
			if (systemMessage == null)
			{
				continue;
			}
			String systemText = String.join("\n", systemMessage.value());
			for (String placeholder : PLACEHOLDERS)
			{
				assertFalse(systemText.contains(placeholder),
					method.getName() + ": " + placeholder + " must not be in the @SystemMessage — "
						+ "judges that read the system message as instructions only do not find it there");
			}
		}
	}
}
