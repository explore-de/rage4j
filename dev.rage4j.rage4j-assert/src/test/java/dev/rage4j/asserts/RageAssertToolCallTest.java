package dev.rage4j.asserts;

import dev.rage4j.asserts.exception.Rage4JToolCallException;
import dev.rage4j.asserts.toolref.ToolRef2;
import dev.rage4j.asserts.toolref.WeatherTools;
import dev.rage4j.model.ToolCall;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RageAssertToolCallTest
{
	private static final String QUESTION = "Wie wird das Wetter morgen in Berlin?";

	private RageAssert rageAssert;

	@BeforeEach
	void setUp()
	{
		rageAssert = new RageAssert(null);
	}

	@Test
	void testMethodReferenceExpectationMatchesRecordedToolCall()
	{
		double score = rageAssert.given()
			.question(QUESTION)
			.expectedToolCall(WeatherTools::getWeather)
			.withArgument("city", "Berlin")
			.withArgument("day", "tomorrow")
			.when()
			.answer("18 Grad und sonnig.")
			.toolCalls(List.of(ToolCall.of("getWeather").withArguments(Map.of("city", "Berlin", "day", "tomorrow"))))
			.then()
			.assertToolCallAccuracy(1.0)
			.getEvaluation()
			.getValue();

		assertEquals(1.0, score, 0.001);
	}

	@Test
	void testArgumentsCanBeSuppliedAsMap()
	{
		rageAssert.given()
			.question(QUESTION)
			.expectedToolCall(WeatherTools::getWeather)
			.withArguments(Map.of("city", "Berlin"))
			.when()
			.answer("18 Grad und sonnig.")
			.toolCalls(List.of(ToolCall.of("getWeather").withArgument("city", "Berlin")))
			.then()
			.assertToolCallAccuracy(1.0);
	}

	@Test
	void testArgumentsCanBeSuppliedAsJson()
	{
		rageAssert.given()
			.question(QUESTION)
			.expectedToolCall(WeatherTools::getWeather)
			.withArgumentsJson("{\"city\": \"Berlin\", \"day\": \"tomorrow\"}")
			.when()
			.answer("18 Grad und sonnig.")
			.toolCalls(List.of(ToolCall.of("getWeather").withArguments(Map.of("city", "Berlin", "day", "tomorrow"))))
			.then()
			.assertToolCallAccuracy(1.0);
	}

	@Test
	void testExplicitToolNameOverrideIsResolved()
	{
		rageAssert.given()
			.question(QUESTION)
			.expectedToolCall(WeatherTools::sendAlert)
			.when()
			.answer("Warnung verschickt.")
			.toolCalls(List.of(ToolCall.of("send_alert")))
			.then()
			.assertToolCallAccuracy(1.0);
	}

	@Test
	void testWrongArgumentFailsTheAssertion()
	{
		assertThrows(Rage4JToolCallException.class, () -> rageAssert.given()
			.question(QUESTION)
			.expectedToolCall(WeatherTools::getWeather)
			.withArgument("city", "Berlin")
			.when()
			.answer("18 Grad und sonnig.")
			.toolCalls(List.of(ToolCall.of("getWeather").withArgument("city", "Hamburg")))
			.then()
			.assertToolCallAccuracy(1.0));
	}

	@Test
	void testArgumentWithoutExpectedToolCallIsRejected()
	{
		assertThrows(IllegalStateException.class, () -> rageAssert.given()
			.question(QUESTION)
			.withArgument("city", "Berlin"));
	}

	@Test
	void testAssertNoToolCallPassesWhenNothingWasCalled()
	{
		rageAssert.given()
			.question("Wer hat dich gebaut?")
			.when()
			.answer("Ein Team von Entwicklern.")
			.toolCalls(List.of())
			.then()
			.assertNoToolCall();
	}

	@Test
	void testAssertNoToolCallFailsWhenSomethingWasCalled()
	{
		assertThrows(Rage4JToolCallException.class, () -> rageAssert.given()
			.question("Wer hat dich gebaut?")
			.when()
			.answer("Ein Team von Entwicklern.")
			.toolCalls(List.of(ToolCall.of("getWeather")))
			.then()
			.assertNoToolCall());
	}

	@Test
	void testAssertNoUnexpectedToolCallsDetectsAdditionalCall()
	{
		assertThrows(Rage4JToolCallException.class, () -> rageAssert.given()
			.question(QUESTION)
			.expectedToolCall(WeatherTools::getWeather)
			.when()
			.answer("18 Grad und sonnig.")
			.toolCalls(List.of(ToolCall.of("getWeather"), ToolCall.of("send_alert")))
			.then()
			.assertNoUnexpectedToolCalls());
	}

	@Test
	void testAssertNoUnexpectedToolCallsMatchesExpectationsOptimally()
	{
		rageAssert.given()
			.question(QUESTION)
			.expectedToolCall(WeatherTools::getWeather)
			.expectedToolCall(WeatherTools::getWeather)
			.withArgument("city", "Berlin")
			.when()
			.answer("18 Grad und sonnig.")
			.toolCalls(List.of(ToolCall.of("getWeather").withArgument("city", "Berlin"), ToolCall.of("getWeather").withArgument("city", "Hamburg")))
			.then()
			.assertNoUnexpectedToolCalls();
	}

	@Test
	void testAssertToolCallOrderAcceptsInterleavedCalls()
	{
		rageAssert.given()
			.question("Storniere meine Buchung.")
			.expectedToolCall("findCustomer")
			.expectedToolCall("cancelBooking")
			.when()
			.answer("Storniert.")
			.toolCalls(List.of(ToolCall.of("findCustomer"), ToolCall.of("getTime"), ToolCall.of("cancelBooking")))
			.then()
			.assertToolCallOrder();
	}

	@Test
	void testAssertToolCallOrderFailsOnSwappedCalls()
	{
		assertThrows(Rage4JToolCallException.class, () -> rageAssert.given()
			.question("Storniere meine Buchung.")
			.expectedToolCall("findCustomer")
			.expectedToolCall("cancelBooking")
			.when()
			.answer("Storniert.")
			.toolCalls(List.of(ToolCall.of("cancelBooking"), ToolCall.of("findCustomer")))
			.then()
			.assertToolCallOrder());
	}

	@Test
	void testMissingToolCallsAreReportedAsMisconfiguration()
	{
		ToolRef2<WeatherTools, String, String> toolReference = WeatherTools::getWeather;

		assertThrows(IllegalArgumentException.class, () -> rageAssert.given()
			.question(QUESTION)
			.expectedToolCall(toolReference)
			.when()
			.answer("18 Grad und sonnig.")
			.then()
			.assertToolCallAccuracy(1.0));
	}
}
