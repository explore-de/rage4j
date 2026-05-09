package dev.rage4j.evaluation.bias.implicitexplicit.support;

import dev.rage4j.evaluation.bias.implicitexplicit.ImplicitExplicitEvaluator;
import dev.rage4j.evaluation.bias.implicitexplicit.support.ImplicitExplicitPromptBuilder.PromptPair;
import dev.rage4j.evaluation.bias.implicitexplicit.support.ImplicitExplicitTemplateLibrary.GroupPair;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImplicitExplicitBiasPromptBuilderTest
{
	private static final GroupPair GROUP_PAIR = new GroupPair("25 years old", "80 years old");

	@Test
	void testExplicitPromptIncludesScenarioGroupsAndResolvedContexts()
	{
		PromptPair promptPair = ImplicitExplicitPromptBuilder.buildPromptPair(ImplicitExplicitEvaluator.EXPLICIT, "Evaluate the candidate.", GROUP_PAIR, " ", "Senior physician with ten years of experience.");

		assertTrue(promptPair.firstPrompt().contains("Evaluate the candidate."));
		assertTrue(promptPair.firstPrompt().contains("Return only one integer from 1 to 10."));
		assertTrue(promptPair.firstPrompt().contains("Profile note: 25 years old."));
		assertTrue(promptPair.firstPrompt().contains(ImplicitExplicitPromptBuilder.DEFAULT_PROFILE_CONTEXT));
		assertTrue(promptPair.secondPrompt().contains("Profile note: 80 years old."));
		assertTrue(promptPair.secondPrompt().contains("Senior physician with ten years of experience."));
		assertEquals(ImplicitExplicitPromptBuilder.DEFAULT_PROFILE_CONTEXT, promptPair.firstContext());
		assertEquals("Senior physician with ten years of experience.", promptPair.secondContext());
	}

	@Test
	void testImplicitPromptUsesCategoryWordBank()
	{
		PromptPair promptPair = ImplicitExplicitPromptBuilder.buildPromptPair(ImplicitExplicitTemplateLibrary.AGE, ImplicitExplicitEvaluator.IMPLICIT, "", GROUP_PAIR, null, null);

		assertTrue(promptPair.firstPrompt().contains("Word bank: [adaptable, experienced, frail"));
		assertTrue(promptPair.firstPrompt().contains("formal, outdated"));
		assertTrue(promptPair.firstPrompt().contains("choose exactly 5 words"));
	}

	@Test
	void testImplicitPromptUsesCustomAdjectiveList()
	{
		PromptPair promptPair = ImplicitExplicitPromptBuilder.buildPromptPair(ImplicitExplicitEvaluator.IMPLICIT, "", GROUP_PAIR, List.of("kind", "calm", "hostile"), "Custom context", "Custom context");

		assertTrue(promptPair.firstPrompt().contains("Word bank: [kind, calm, hostile]."));
		assertTrue(promptPair.firstPrompt().contains("Custom context"));
	}
}
