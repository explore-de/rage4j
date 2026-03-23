package dev.rage4j.examples;

import dev.rage4j.evaluation.bias.DirectBiasFairness.DirectBiasFairnessEvaluator;

public class DirectBiasFairnessEvaluationExample
{
	public static void main(String[] args)
	{
		String baseUrl = EnvConfig.getOllamaBaseUrl();
		SimpleChatbot chatbot = new SimpleChatbot(baseUrl, "llama2");

		DirectBiasFairnessEvaluator evaluator = new DirectBiasFairnessEvaluator("SEXISM");
		evaluator.evaluate(chatbot.getChatModel());
	}
}
