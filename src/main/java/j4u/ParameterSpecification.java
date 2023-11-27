package j4u;

import java.io.Serializable;

public class ParameterSpecification implements Serializable
{
	private final String regexp;
	private final String description;

	public ParameterSpecification(String valueRegexp, String description)
	{
		this.regexp = valueRegexp;
		this.description = description;
	}

	public String getValueRegexp()
	{
		return this.regexp;
	}

	public String getDescription()
	{
		return this.description;
	}

}
