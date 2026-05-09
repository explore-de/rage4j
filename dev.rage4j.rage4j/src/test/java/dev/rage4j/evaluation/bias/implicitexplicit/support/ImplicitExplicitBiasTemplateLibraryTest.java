package dev.rage4j.evaluation.bias.implicitexplicit.support;

import dev.rage4j.evaluation.bias.implicitexplicit.support.ImplicitExplicitTemplateLibrary.AdjectivePreset;
import dev.rage4j.evaluation.bias.implicitexplicit.support.ImplicitExplicitTemplateLibrary.ConfiguredGroupPair;
import dev.rage4j.evaluation.bias.implicitexplicit.support.ImplicitExplicitTemplateLibrary.GroupPair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImplicitExplicitBiasTemplateLibraryTest
{
	@Test
	void testGroupPairRejectsMissingAttributes()
	{
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new GroupPair("25 years old", " "));
		assertEquals("Both groupPair attributes must be provided.", exception.getMessage());
	}

	@Test
	void testAdjectivePresetForKnownCategory()
	{
		AdjectivePreset preset = ImplicitExplicitTemplateLibrary.adjectivePresetFor(ImplicitExplicitTemplateLibrary.AGE);

		assertTrue(preset.positiveAdjectives().contains("adaptable"));
		assertTrue(preset.negativeAdjectives().contains("forgetful"));
		assertTrue(preset.neutralAdjectives().contains("structured"));
	}

	@Test
	void testAdjectiveWordBankInterleavesSentimentGroups()
	{
		AdjectivePreset preset = AdjectivePreset.builder()
			.positive("positive one", "positive two")
			.negative("negative one", "negative two")
			.neutral("neutral one")
			.build();

		String wordBank = ImplicitExplicitTemplateLibrary.adjectiveWordBank(preset);

		assertEquals("positive one, neutral one, negative one, positive two, negative two", wordBank);
	}

	@Test
	void testConfiguredGroupPairRejectsMissingGroupPair()
	{
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new ConfiguredGroupPair(null, ImplicitExplicitTemplateLibrary.CUSTOM, null));
		assertEquals("groupPair must not be null", exception.getMessage());
	}
}
