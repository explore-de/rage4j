package dev.rage4j.evaluation.axcel;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AxcelResponseParserTest
{
	private static final String EXAMPLE = """
		Let’s verify the factual accuracy of the derived text step by step:
		
		1. John is interacting with an AI:
		- **Derived Text:** Sure thing John! Here are some facts about space..."
		- **Source Text:** User: Hello, my name is John. AI: Hello John, how can I assist you today?
		- **Verification:** Correct. Rating: 5
		
		2. The AI is providing facts about space to John:
		- **Derived Text:** Sure thing John! Here are some facts about space..."
		- **Source Text:** User: Good one! Do you know any facts about space?
		- **Verification:** The derived text makes a claim about providing facts, but the source text does not provide any actual facts about space. Therefore, this cannot be verified. Rating: 1
		
		3. The AI told a joke to John:
		- **Derived Text:** Sure thing John! Here are some facts about space..."
		- **Source Text:** User: Can you tell me a joke? AI: Sure! Why don't scientists trust atoms? Because they make up everything!
		- **Verification:** The derived text does not mention this fact. Rating: N/A
		
		Overall, we can conclude that some of the facts presented in the derived text are aligned with the source. For the fact that cannot be verified, we are assuming that the factual information isn't provided in the source text.
		""";
	private final AxcelResponseParser parser = new AxcelResponseParser();

	@Test
	void shouldParseExampleResponseFile() throws IOException
	{
		List<AxcelFactEvaluation> facts = parser.parse(EXAMPLE);

		assertEquals(3, facts.size());
		assertEquals(5, facts.get(0).rating());
		assertEquals(1, facts.get(1).rating());
		assertNull(facts.get(2).rating());
		assertTrue(facts.get(0).title().startsWith("John is interacting with an AI"));
	}
}
