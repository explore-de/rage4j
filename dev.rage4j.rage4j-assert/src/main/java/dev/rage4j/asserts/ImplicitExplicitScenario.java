package dev.rage4j.asserts;

import dev.rage4j.evaluation.bias.ImplicitExplicitBias.ImplicitExplicitBiasEvaluator;
import dev.rage4j.evaluation.bias.ImplicitExplicitBias.support.ImplicitExplicitBiasPromptBuilder;
import dev.rage4j.evaluation.bias.ImplicitExplicitBias.support.ImplicitExplicitBiasPromptBuilder.PromptPair;
import dev.rage4j.evaluation.bias.ImplicitExplicitBias.support.ImplicitExplicitBiasTemplateLibrary;
import dev.rage4j.evaluation.bias.ImplicitExplicitBias.support.ImplicitExplicitBiasTemplateLibrary.AdjectivePreset;
import dev.rage4j.evaluation.bias.ImplicitExplicitBias.support.ImplicitExplicitBiasTemplateLibrary.GroupPair;

import java.util.Arrays;
import java.util.List;

public final class ImplicitExplicitScenario
{
	private final ImplicitExplicitBiasMode mode;
	private final String category;
	private final GroupPair groupPair;
	private final String qualifications;
	private final AdjectivePreset adjectivePreset;
	private final String question;
	private final String comparisonQuestion;

	private ImplicitExplicitScenario(Builder builder)
	{
		this.mode = builder.mode;
		this.category = normalizeCategory(builder.category);
		this.groupPair = new GroupPair(builder.firstGroup, builder.secondGroup);
		this.qualifications = resolveQualifications(builder.qualifications);
		this.adjectivePreset = builder.adjectivePreset;

		PromptPair promptPair = buildPromptPair();
		this.question = promptPair.firstPrompt();
		this.comparisonQuestion = promptPair.secondPrompt();
	}

	public static Builder builder()
	{
		return new Builder();
	}

	public ImplicitExplicitBiasMode mode()
	{
		return mode;
	}

	public String category()
	{
		return category;
	}

	public GroupPair groupPair()
	{
		return groupPair;
	}

	public String qualifications()
	{
		return qualifications;
	}

	public AdjectivePreset adjectivePreset()
	{
		return adjectivePreset;
	}

	public String question()
	{
		return question;
	}

	public String comparisonQuestion()
	{
		return comparisonQuestion;
	}

	public String effectiveCategory()
	{
		return category != null ? category : ImplicitExplicitBiasTemplateLibrary.CUSTOM;
	}

	private PromptPair buildPromptPair()
	{
		String modeValue = mode.value();
		if (ImplicitExplicitBiasEvaluator.IMPLICIT.equals(modeValue))
		{
			if (adjectivePreset != null)
			{
				List<String> adjectiveWordBankEntries = Arrays.asList(
					ImplicitExplicitBiasTemplateLibrary.adjectiveWordBank(adjectivePreset).split(", "));
				return ImplicitExplicitBiasPromptBuilder.buildPromptPair(
					modeValue,
					"",
					groupPair,
					adjectiveWordBankEntries,
					qualifications,
					qualifications);
			}
			return ImplicitExplicitBiasPromptBuilder.buildPromptPair(
				effectiveCategory(),
				modeValue,
				"",
				groupPair,
				qualifications,
				qualifications);
		}

		return ImplicitExplicitBiasPromptBuilder.buildPromptPair(
			modeValue,
			"",
			groupPair,
			qualifications,
			qualifications);
	}

	private static String normalizeCategory(String category)
	{
		if (category == null || category.isBlank())
		{
			return null;
		}
		return category.trim().toUpperCase();
	}

	private static String resolveQualifications(String qualifications)
	{
		if (qualifications == null || qualifications.isBlank())
		{
			return ImplicitExplicitBiasPromptBuilder.DEFAULT_PROFILE_CONTEXT;
		}
		return qualifications.trim();
	}

	public static final class Builder
	{
		private ImplicitExplicitBiasMode mode;
		private String category;
		private String firstGroup;
		private String secondGroup;
		private String qualifications;
		private AdjectivePreset adjectivePreset;

		private Builder()
		{
		}

		public Builder mode(ImplicitExplicitBiasMode mode)
		{
			this.mode = mode;
			return this;
		}

		public Builder category(String category)
		{
			this.category = category;
			return this;
		}

		public Builder groupPair(String firstGroup, String secondGroup)
		{
			this.firstGroup = firstGroup;
			this.secondGroup = secondGroup;
			return this;
		}

		public Builder qualifications(String qualifications)
		{
			this.qualifications = qualifications;
			return this;
		}

		public Builder adjectivePreset(AdjectivePreset adjectivePreset)
		{
			this.adjectivePreset = adjectivePreset;
			return this;
		}

		public ImplicitExplicitScenario build()
		{
			if (mode == null)
			{
				throw new IllegalStateException("ImplicitExplicitBiasScenario requires a mode.");
			}
			if (firstGroup == null || firstGroup.isBlank() || secondGroup == null || secondGroup.isBlank())
			{
				throw new IllegalStateException("ImplicitExplicitBiasScenario requires both groupPair values.");
			}
			if (mode == ImplicitExplicitBiasMode.IMPLICIT && adjectivePreset == null
				&& (category == null || category.isBlank()))
			{
				throw new IllegalStateException("Implicit bias scenarios require either a category or an adjective preset.");
			}
			return new ImplicitExplicitScenario(this);
		}
	}
}
