package dev.rage4j.evaluation.axcel;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface AxcelBot
{
	@SystemMessage("")
	@UserMessage("")
	String evaluate(String input);
}
