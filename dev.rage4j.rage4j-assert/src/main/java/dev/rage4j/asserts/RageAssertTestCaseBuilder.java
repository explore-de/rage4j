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
	private final ChatModel chatModel;
	private final EmbeddingModel embeddingModel;
	private final boolean evaluationMode;

	public RageAssertTestCaseBuilder(ChatModel chatModel, EmbeddingModel embeddingModel, boolean evaluationMode)
	{
		this.chatModel = chatModel;
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

	/**
	 * Attaches a single image to the test case. Calling this method opts the
	 * test case into multimodal evaluation; downstream LLM-based assertions
	 * (e.g. {@code assertFaithfulness}) will pass the images to the configured
	 * {@link ChatModel}, which therefore must be vision-capable.
	 */
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

	/**
	 * Replaces the image list of this test case. Pass {@code null} or an empty
	 * list to clear it.
	 */
	public RageAssertTestCaseBuilder images(List<Rage4jImage> images)
	{
		this.images = images == null ? null : new ArrayList<>(images);
		return this;
	}

	public RageAssertTestCaseGiven when()
	{
		return new RageAssertTestCaseGiven(question, groundTruth, context, images, chatModel, embeddingModel, evaluationMode);
	}
}
