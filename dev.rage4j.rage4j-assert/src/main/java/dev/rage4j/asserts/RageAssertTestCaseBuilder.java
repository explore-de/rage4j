package dev.rage4j.asserts;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.rage4j.asserts.toolref.ToolNameResolver;
import dev.rage4j.asserts.toolref.ToolRef0;
import dev.rage4j.asserts.toolref.ToolRef1;
import dev.rage4j.asserts.toolref.ToolRef2;
import dev.rage4j.asserts.toolref.ToolRef3;
import dev.rage4j.asserts.toolref.ToolRef4;
import dev.rage4j.model.Rage4jImage;
import dev.rage4j.model.ToolArguments;
import dev.rage4j.model.ToolCall;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
	private final List<ToolCall> expectedToolCalls = new ArrayList<>();
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

	public RageAssertTestCaseBuilder expectedToolCall(String toolName)
	{
		Objects.requireNonNull(toolName, "toolName");
		expectedToolCalls.add(ToolCall.of(toolName));
		return this;
	}

	public <T> RageAssertTestCaseBuilder expectedToolCall(ToolRef0<T> toolReference)
	{
		return expectedToolCall(ToolNameResolver.resolve(toolReference));
	}

	public <T, A> RageAssertTestCaseBuilder expectedToolCall(ToolRef1<T, A> toolReference)
	{
		return expectedToolCall(ToolNameResolver.resolve(toolReference));
	}

	public <T, A, B> RageAssertTestCaseBuilder expectedToolCall(ToolRef2<T, A, B> toolReference)
	{
		return expectedToolCall(ToolNameResolver.resolve(toolReference));
	}

	public <T, A, B, C> RageAssertTestCaseBuilder expectedToolCall(ToolRef3<T, A, B, C> toolReference)
	{
		return expectedToolCall(ToolNameResolver.resolve(toolReference));
	}

	public <T, A, B, C, D> RageAssertTestCaseBuilder expectedToolCall(ToolRef4<T, A, B, C, D> toolReference)
	{
		return expectedToolCall(ToolNameResolver.resolve(toolReference));
	}

	public RageAssertTestCaseBuilder withArgument(String argumentName, Object value)
	{
		Objects.requireNonNull(argumentName, "argumentName");
		int lastIndex = requireExpectedToolCallIndex();
		expectedToolCalls.set(lastIndex, expectedToolCalls.get(lastIndex).withArgument(argumentName, value));
		return this;
	}

	public RageAssertTestCaseBuilder withArguments(Map<String, Object> arguments)
	{
		Objects.requireNonNull(arguments, "arguments");
		int lastIndex = requireExpectedToolCallIndex();
		expectedToolCalls.set(lastIndex, expectedToolCalls.get(lastIndex).withArguments(arguments));
		return this;
	}

	public RageAssertTestCaseBuilder withArgumentsJson(String argumentsJson)
	{
		return withArguments(ToolArguments.fromJson(argumentsJson));
	}

	private int requireExpectedToolCallIndex()
	{
		if (expectedToolCalls.isEmpty())
		{
			throw new IllegalStateException("An expectedToolCall must be set before arguments are added.");
		}
		return expectedToolCalls.size() - 1;
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
			List.copyOf(expectedToolCalls),
			judgeChatModel,
			evaluatedChatModel,
			embeddingModel,
			evaluationMode);
	}
}
