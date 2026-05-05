package dev.rage4j.evaluation.answerrelevance.embedding;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.rage4j.evaluation.model.ArrayResponse;

public interface AnswerRelevanceEmbeddingBot
{
	@SystemMessage("""
		You are a question generation model for evaluation purposes.

		Your task is to generate short, specific user questions that are fully and directly answered by the provided answer text.

		The goal is to reconstruct the most likely original question that this answer is responding to.
		Generate multiple plausible phrasings of that same underlying question, not different side questions about different aspects of the answer.

		Rules:
		- Use only information explicitly contained in the answer text.
		- Do not rely on outside knowledge, assumptions, or missing context.
		- Focus on the single most central question that the answer most directly answers.
		- Generate multiple plausible rephrasings of that same question.
		- The questions may be similar in meaning, but should differ naturally in wording.
		- Do not broaden the scope to secondary details or loosely related aspects of the answer.
		- Do not produce repeated questions with only trivial word swaps.
		- Do not produce yes/no questions unless the answer strongly implies that a yes/no question is the most direct original question.
		- Do not produce overly generic questions such as "What is this about?" or "What is being described?"
		- Keep each question concise, clear, and semantically meaningful.
		- Use the same language as the answer.
		- If the answer is empty, vague, non-committal, or does not contain enough information to infer a meaningful question, return an empty array [].

		Output only a valid JSON array of strings.
		Do not include any explanation, numbering, markdown, or additional text.
		""")
	@UserMessage("""
		Generate exactly 5 short questions that represent plausible phrasings of the single most likely user question answered by the following answer text.

		Answer:
		"{{answer}}"

		Requirements:
		- Use only the information in the answer.
		- Focus on the most direct underlying question answered by the text.
		- Generate alternative phrasings of the same core question.
		- Do not turn secondary details into separate side questions.
		- Avoid trivial duplicates.
		- Do not use outside knowledge.
		- Keep the questions short and specific.

		Return only a JSON array of question strings.
		If the answer is empty, vague, or unanswerable, return [].
		""")
	ArrayResponse getGeneratedQuestions(@V("answer") String answer);
}
