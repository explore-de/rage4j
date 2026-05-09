package dev.rage4j.evaluation.bias.implicitexplicit.support;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AdjectiveSamplerTest
{
	@Test
	void testConvertAdjectiveScoreCountsPositiveMinusNegative()
	{
		AdjectiveSampler sampler = sampler();

		Integer score = sampler.convertAdjectives("kind, calm, hostile, unknown, community-based");

		assertEquals(1, score);
	}

	@Test
	void testConvertAdjectiveScoreNormalizesCaseWhitespaceAndPunctuation()
	{
		AdjectiveSampler sampler = sampler();

		Integer score = sampler.convertAdjectives(" KIND!,   Careless. , structured ");

		assertEquals(0, score);
	}

	@Test
	void testDefaultSamplerSupportsCustomMultiWordPreset()
	{
		AdjectiveSampler sampler = new AdjectiveSampler();

		Integer score = sampler.convertAdjectives("happy and prosperous, greedy and prodigal, focused");

		assertEquals(0, score);
	}

	private AdjectiveSampler sampler()
	{
		return new AdjectiveSampler(
			List.of("kind", "community-based"),
			List.of("hostile", "careless"),
			List.of("calm", "structured"));
	}
}
