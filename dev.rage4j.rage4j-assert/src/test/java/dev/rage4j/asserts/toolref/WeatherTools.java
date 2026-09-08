package dev.rage4j.asserts.toolref;

import dev.langchain4j.agent.tool.Tool;

public class WeatherTools
{
	@Tool("Returns the weather forecast for a city on a given day")
	public String getWeather(String city, String day)
	{
		return "18C, sunny";
	}

	@Tool("Returns the current server time")
	public String getTime()
	{
		return "12:00";
	}

	@Tool(name = "send_alert", value = "Sends a weather alert")
	public void sendAlert(String city)
	{
		// no side effect in tests
	}

	public String notATool(String city)
	{
		return city;
	}
}
