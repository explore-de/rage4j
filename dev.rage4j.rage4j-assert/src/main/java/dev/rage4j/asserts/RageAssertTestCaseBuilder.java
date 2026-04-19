package dev.rage4j.asserts;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.rage4j.model.Rage4jImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RageAssertTestCaseBuilder
{
	private String question;
	private String groundTruth;
	private String context;
	private List<Rage4jImage> images;
	private String comparisonQuestion;
	private String comparisonGroundTruth;
	private String comparisonContext;
	private ImplicitExplicitScenario implicitExplicitScenario;
	private final ChatModel judgeChatModel;
	private final ChatModel evaluatedChatModel;
	private final EmbeddingModel embeddingModel;
	private final boolean evaluationMode;

	public RageAssertTestCaseBuilder(ChatModel chatModel, EmbeddingModel embeddingModel, boolean evaluationMode)
	{
		this(chatModel, chatModel, embeddingModel, evaluationMode);
	}

	public RageAssertTestCaseBuilder(ChatModel judgeChatModel, EmbeddingModel embeddingModel)
	{
		this(judgeChatModel, judgeChatModel, embeddingModel, false);
	}

	public RageAssertTestCaseBuilder(ChatModel judgeChatModel, ChatModel evaluatedChatModel, EmbeddingModel embeddingModel)
	{
		this(judgeChatModel, evaluatedChatModel, embeddingModel, false);
	}

	public RageAssertTestCaseBuilder(ChatModel judgeChatModel, ChatModel evaluatedChatModel, EmbeddingModel embeddingModel, boolean evaluationMode)
	{
		this.judgeChatModel = judgeChatModel;
		this.evaluatedChatModel = evaluatedChatModel;
		this.embeddingModel = embeddingModel;
		this.evaluationMode = evaluationMode;
	}

	public RageAssertTestCaseBuilder question(String question)
	{
		this.question = question;
		return this;
	}

	public RageAssertTestCaseBuilder groundTruth(String groundTruth)
	{
		this.groundTruth = groundTruth;
		return this;
	}

	public RageAssertTestCaseBuilder context(String context)
	{
		this.context = context;
		return this;
	}

	public RageAssertTestCaseBuilder contextList(List<String> contextList)
	{
		this.context = contextList == null ? null : String.join("\n", contextList);
		return this;
	}

	public RageAssertTestCaseBuilder image(Rage4jImage image)
	{
		Objects.requireNonNull(image, "image");
		if (this.images == null)
		{
			this.images = new ArrayList<>();
		}
		this.images.add(image);
		return this;
	}

	public RageAssertTestCaseBuilder images(List<Rage4jImage> images)
	{
		this.images = images == null ? null : new ArrayList<>(images);
		return this;
	}

	public RageAssertTestCaseBuilder context(String context)
	{
		this.contextList = context == null ? null : List.of(context);
		return this;
	}

	public RageAssertTestCaseBuilder comparisonQuestion(String comparisonQuestion)
	{
		this.comparisonQuestion = comparisonQuestion;
		return this;
	}

	public RageAssertTestCaseBuilder comparisonGroundTruth(String comparisonGroundTruth)
	{
		this.comparisonGroundTruth = comparisonGroundTruth;
		return this;
	}

	public RageAssertTestCaseBuilder comparisonContext(String comparisonContext)
	{
		this.comparisonContext = comparisonContext;
		return this;
	}

	public RageAssertTestCaseBuilder comparisonContextList(List<String> comparisonContextList)
	{
		this.comparisonContext = comparisonContextList == null ? null : String.join("\n", comparisonContextList);
		return this;
	}

	public RageAssertTestCaseBuilder implicitExplicitScenario(ImplicitExplicitScenario scenario)
	{
		this.implicitExplicitScenario = scenario;
		if (scenario == null)
		{
			return this;
		}

		this.question = scenario.question();
		this.comparisonQuestion = scenario.comparisonQuestion();
		this.context = scenario.qualifications();
		this.comparisonContext = scenario.qualifications();
		return this;
	}

	public RageAssertTestCaseBuilder comparisonContext(String comparisonContext)
	{
		this.comparisonContextList = comparisonContext == null ? null : List.of(comparisonContext);
		return this;
	}

	public RageAssertTestCaseGiven when()
	{
		return new RageAssertTestCaseGiven(
			question,
			groundTruth,
			context,
			images,
			comparisonQuestion,
			comparisonGroundTruth,
			comparisonContext,
			implicitExplicitScenario,
			judgeChatModel,
			evaluatedChatModel,
			embeddingModel,
			evaluationMode);
	}
}
