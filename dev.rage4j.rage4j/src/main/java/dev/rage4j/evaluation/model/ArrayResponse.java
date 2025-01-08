package dev.rage4j.evaluation.model;

/**
 * Represents a response containing an array of strings, used in the AiServices
 * of the RAGE4j library to get the list of strings as a response from
 * Langchain4j. Class is initiated automatically and is needed only for service
 * purposes.
 */
public class ArrayResponse
{
	private String[] items;

	public ArrayResponse(String[] items)
	{
		this.items = items;
	}

	public ArrayResponse()
	{
		// no args
	}

	public String[] getItems()
	{
		return items;
	}

	public void setItems(String[] items)
	{
		this.items = items;
	}

	public int getLength()
	{
		return items == null ? 0 : items.length;
	}
}
