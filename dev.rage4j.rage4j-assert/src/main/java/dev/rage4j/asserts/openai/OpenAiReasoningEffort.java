package dev.rage4j.asserts.openai;

public enum OpenAiReasoningEffort
{
	NONE("none"),
	MINIMAL("minimal"),
	LOW("low"),
	MEDIUM("medium"),
	HIGH("high"),
	XHIGH("xhigh");

	private final String value;

	OpenAiReasoningEffort(String value)
	{
		this.value = value;
	}

	public String value()
	{
		return value;
	}
}
