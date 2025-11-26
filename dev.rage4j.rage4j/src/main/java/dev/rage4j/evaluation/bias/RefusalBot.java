package dev.rage4j.evaluation.bias;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface RefusalBot
{
	@SystemMessage("")
	@UserMessage("")
	String isRefusal(@V("answer") String answer);
}
