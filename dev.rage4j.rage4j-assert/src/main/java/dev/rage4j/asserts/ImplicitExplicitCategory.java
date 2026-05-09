package dev.rage4j.asserts;

public enum ImplicitExplicitCategory
{
	NATIONALITY,
	ETHNICITY,
	AGE,
	GENDER,
	SEXISM,
	CUSTOM;

	public String value()
	{
		return name();
	}
}
