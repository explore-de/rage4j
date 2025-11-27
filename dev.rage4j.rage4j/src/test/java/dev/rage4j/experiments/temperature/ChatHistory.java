package dev.rage4j.experiments.temperature;

import dev.rage4j.model.Sample;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ChatHistory
{
	public static Sample buildList()
	{
		List<String> context = getContext();

		String question = "User: I’m about to copy all this into my digital calendar. Can you give me a concise but complete summary of each event, including the exact date(s), the order they happen in, and how old I will be at each one?";

		String answer = "AI: Sure, here’s a consolidated summary of your upcoming timeline, with dates, order, and your age at each point:\n\n" +
			"1) Friend’s wedding\n" +
			"   - Date: April 19, 2025\n" +
			"   - Order: This is the first major event coming up after today (March 10, 2025).\n" +
			"   - Your age: You will still be 29 years old. Your 30th birthday doesn’t happen until later in the summer.\n\n" +
			"2) Short trip\n" +
			"   - Dates: May 11–14, 2025\n" +
			"   - Order: This comes after the wedding and gives you a bit of a break before your thesis milestones.\n" +
			"   - Your age: You’ll still be 29 during this trip as well.\n" +
			"   - Note: It’s nicely positioned a few weeks before your draft is due, so it shouldn’t clash with your heaviest thesis push.\n\n" +
			"3) Thesis full-draft deadline\n" +
			"   - Date: Tuesday, June 3, 2025\n" +
			"   - Order: This is your first major thesis milestone after the trip.\n" +
			"   - Your age: Still 29.\n" +
			"   - Context: Your supervisor wants the complete draft at this point so there’s nearly a full month to revise before final submission.\n\n" +
			"4) Final thesis deadline\n" +
			"   - Date: June 30, 2025\n" +
			"   - Order: This is the last big academic deadline before your birthday.\n" +
			"   - Your age: You are still 29 here, with a little over a week before your birthday.\n" +
			"   - Spacing: It falls a bit more than a month after your draft date, giving you several weeks to incorporate feedback.\n\n" +
			"5) Your birthday\n" +
			"   - Date: August 8, 2025\n" +
			"   - Order: This is the final event in this sequence, after all your major academic responsibilities are done.\n" +
			"   - Your age: You will turn 29 on this date, marking the start of your 29th year.\n" +
			"   - Spacing from thesis deadline: Your birthday is just over a month after the final thesis submission, giving you some time to decompress before celebrating.\n\n" +
			"In chronological order, with dates and ages, that’s:\n" +
			"- April 19, 2025 – Wedding (age 29)\n" +
			"- May 11–14, 2025 – Short trip (age 29)\n" +
			"- June 3, 2025 – Thesis full-draft due (age 29)\n" +
			"- June 30, 2025 – Final thesis deadline (age 29)\n" +
			"- August 8, 2025 – Birthday, when you turn 29";

		return Sample.builder()
			.withQuestion(question)
			.withAnswer(answer)
			.withContextsList(context)
			.build();
	}

	private static @NotNull List<String> getContext()
	{
		List<String> context = new ArrayList<>();
		context.add("User: Hi, I need help organizing my next few months. For reference, let's assume today is March 10, 2025.");
		context.add("AI: Got it—today is March 10, 2025. Tell me the key events and deadlines you’re trying to organize, and I’ll help you lay them out on a timeline.");

		context.add("User: Here are the main things:\n- My thesis final deadline is June 30, 2025.\n- My friend’s wedding is on April 19, 2025.\n- I’m planning a short trip from May 10 to May 13, 2025.\n- My 30th birthday is on August 8, 2025.\nI mostly want to see how these fit together.");
		context.add("AI: Let me restate those with dates:\n- Friend’s wedding: April 19, 2025\n- Short trip: May 10–13, 2025\n- Thesis final deadline: June 30, 2025\n- Your 30th birthday: August 8, 2025\n\nFrom today (March 10, 2025):\n- The wedding is 40 days away.\n- The trip starts 61 days from now.\n- The thesis deadline is 112 days from now.\n- Your 30th birthday is 151 days from now.\n\nWe can now work backwards from your thesis deadline and around these events to make a plan.");

		context.add("User: How many weeks are there between the wedding on April 19 and the start of the trip on May 10, 2025?");
		context.add("AI: April 19 to May 10, 2025 is 21 days. That’s exactly 3 weeks between the wedding and the start of your trip.");

		context.add("User: My thesis supervisor wants a full draft 4 weeks before the final deadline on June 30, 2025. What calendar date is that?");
		context.add("AI: Four weeks is 28 days. If your final deadline is June 30, 2025, then:\n- Subtracting 28 days from June 30 lands on June 2, 2025.\n\nSo your full draft should be ready by June 2, 2025. June 2, 2025 is a Monday.");

		context.add("User: Can you list all these events in chronological order, from April through August?");
		context.add("AI: In chronological order, starting in April:\n\n1) April 19, 2025 – Friend’s wedding\n2) May 10–13, 2025 – Short trip\n3) June 2, 2025 – Thesis full-draft deadline (for your supervisor)\n4) June 30, 2025 – Final thesis deadline\n5) August 8, 2025 – Your 30th birthday\n\nThat’s the sequence from April through August.");

		context.add("User: For context, I was born on August 8, 1995. How old am I today, on March 10, 2025, and how old will I be on my birthday this year?");
		context.add("AI: You were born on August 8, 1995.\n\n- On today’s assumed date, March 10, 2025, you are 29 years old (you turned 29 on August 8, 2024).\n- On August 8, 2025, you will turn 30 years old.");

		context.add("User: Between my draft deadline on June 2 and the final deadline on June 30, how many days do I have to revise?");
		context.add("AI: From June 2, 2025 (draft deadline) to June 30, 2025 (final deadline) is 28 days if you count June 3 through June 30. So you have 28 days between submitting your full draft and the final thesis deadline.");

		context.add("User: I don’t want big tasks too close to my birthday. How far before my August 8, 2025 birthday is the thesis final deadline on June 30?");
		context.add("AI: From June 30, 2025 to August 8, 2025:\n- June 30 to July 30 is 30 days.\n- Then from July 30 to August 8 is 9 more days.\n\nSo the final thesis deadline is 39 days before your August 8, 2025 birthday.");
		return context;
	}
}
