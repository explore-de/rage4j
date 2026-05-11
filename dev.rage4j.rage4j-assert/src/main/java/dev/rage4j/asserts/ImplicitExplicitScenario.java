package dev.rage4j.asserts;

import dev.rage4j.evaluation.bias.implicitexplicit.ImplicitExplicitEvaluator;
import dev.rage4j.evaluation.bias.implicitexplicit.support.ImplicitExplicitPromptBuilder;
import dev.rage4j.evaluation.bias.implicitexplicit.support.ImplicitExplicitPromptBuilder.PromptPair;
import dev.rage4j.evaluation.bias.implicitexplicit.support.ImplicitExplicitTemplateLibrary;
import dev.rage4j.evaluation.bias.implicitexplicit.support.ImplicitExplicitTemplateLibrary.AdjectivePreset;
import dev.rage4j.evaluation.bias.implicitexplicit.support.ImplicitExplicitTemplateLibrary.GroupPair;

import java.util.Arrays;
import java.util.List;

public final class ImplicitExplicitScenario
{
	private final ImplicitExplicitMode mode;
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

	public ImplicitExplicitMode mode()
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

	private PromptPair buildPromptPair()
	{
		String modeValue = mode.value();
		if (ImplicitExplicitEvaluator.IMPLICIT.equals(modeValue))
		{
			if (adjectivePreset != null)
			{
				List<String> adjectiveWordBankEntries = Arrays.asList(ImplicitExplicitTemplateLibrary.adjectiveWordBank(adjectivePreset).split(", "));
				return ImplicitExplicitPromptBuilder.buildPromptPair(modeValue, "", groupPair, adjectiveWordBankEntries, qualifications, qualifications);
			}
			return ImplicitExplicitPromptBuilder.buildPromptPair(category, modeValue, "", groupPair, qualifications, qualifications);
		}

		return ImplicitExplicitPromptBuilder.buildPromptPair(modeValue, "", groupPair, qualifications, qualifications);
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
			return ImplicitExplicitPromptBuilder.DEFAULT_PROFILE_CONTEXT;
		}
		return qualifications.trim();
	}

	public static final class Builder
	{
		private ImplicitExplicitMode mode;
		private String category;
		private String firstGroup;
		private String secondGroup;
		private String qualifications;
		private AdjectivePreset adjectivePreset;

		private Builder()
		{
		}

		public Builder mode(ImplicitExplicitMode mode)
		{
			this.mode = mode;
			return this;
		}

		public Builder category(String category)
		{
			this.category = category;
			return this;
		}

		public Builder category(ImplicitExplicitCategory category)
		{
			this.category = category == null ? null : category.value();
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
			if (mode == ImplicitExplicitMode.IMPLICIT && adjectivePreset == null
				&& (category == null || category.isBlank()))
			{
				throw new IllegalStateException("Implicit bias scenarios require either a category or an adjective preset.");
			}
			return new ImplicitExplicitScenario(this);
		}
	}
}
