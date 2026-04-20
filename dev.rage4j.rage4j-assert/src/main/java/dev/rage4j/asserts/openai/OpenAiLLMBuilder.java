package dev.rage4j.asserts.openai;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatRequestParameters;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.rage4j.asserts.LLMBuilder;
import dev.rage4j.asserts.RageAssert;

public class OpenAiLLMBuilder implements LLMBuilder
{
	private static final String DEFAULT_CHAT_MODEL = "gpt-4";
	private static final String DEFAULT_EMBEDDING_MODEL = "text-embedding-3-small";

	private String judgeChatModelName = DEFAULT_CHAT_MODEL;
	private String embeddingModelName = DEFAULT_EMBEDDING_MODEL;
	private String judgeReasoningEffort;

	public OpenAiLLMBuilder modelName(String modelName)
	{
		this.judgeChatModelName = modelName;
		return this;
	}

	public OpenAiLLMBuilder chatModelName(String modelName)
	{
		return judgeModelName(modelName);
	}

	public OpenAiLLMBuilder judgeModelName(String modelName)
	{
		this.judgeChatModelName = modelName;
		return this;
	}

	public OpenAiLLMBuilder embeddingModelName(String modelName)
	{
		this.embeddingModelName = modelName;
		return this;
	}

	public OpenAiLLMBuilder reasoningEffort(OpenAiReasoningEffort reasoningEffort)
	{
		return reasoningEffort(reasoningEffort == null ? null : reasoningEffort.value());
	}

	public OpenAiLLMBuilder reasoningEffort(String reasoningEffort)
	{
		return judgeReasoningEffort(reasoningEffort);
	}

	public OpenAiLLMBuilder chatReasoningEffort(OpenAiReasoningEffort reasoningEffort)
	{
		return chatReasoningEffort(reasoningEffort == null ? null : reasoningEffort.value());
	}

	public OpenAiLLMBuilder chatReasoningEffort(String reasoningEffort)
	{
		return judgeReasoningEffort(reasoningEffort);
	}

	public OpenAiLLMBuilder judgeReasoningEffort(OpenAiReasoningEffort reasoningEffort)
	{
		return judgeReasoningEffort(reasoningEffort == null ? null : reasoningEffort.value());
	}

	public OpenAiLLMBuilder judgeReasoningEffort(String reasoningEffort)
	{
		this.judgeReasoningEffort = normalizeReasoningEffort(reasoningEffort);
		return this;
	}

	@Override
	public RageAssert fromApiKey(String apiKey)
	{
		OpenAiChatModel judgeChatModel = createChatModel(apiKey, judgeChatModelName, judgeReasoningEffort);

		OpenAiEmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
			.apiKey(apiKey)
			.modelName(embeddingModelName)
			.build();

		return new RageAssert(judgeChatModel, embeddingModel);
	}

	private static OpenAiChatModel createChatModel(String apiKey, String modelName, String reasoningEffort)
	{
		OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
			.apiKey(apiKey)
			.modelName(modelName);

		if (reasoningEffort != null)
		{
			builder.defaultRequestParameters(OpenAiChatRequestParameters.builder()
				.reasoningEffort(reasoningEffort)
				.build());
		}

		return builder.build();
	}

	private static String normalizeReasoningEffort(String reasoningEffort)
	{
		if (reasoningEffort == null)
		{
			return null;
		}

		String normalizedReasoningEffort = reasoningEffort.trim().toLowerCase();

		return switch (normalizedReasoningEffort)
		{
			case "none", "minimal", "low", "medium", "high", "xhigh" -> normalizedReasoningEffort;
			default -> throw new IllegalArgumentException(
				"Unsupported reasoning effort '" + reasoningEffort
					+ "'. Supported values: none, minimal, low, medium, high, xhigh.");
		};
	}
}
