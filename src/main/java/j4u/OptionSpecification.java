package j4u;

import java.io.Serializable;

public class OptionSpecification extends ParameterSpecification implements Serializable
{
	private final String longName;
	private final String shortName;
	private final String defaultValue;

	public OptionSpecification(String longName, String shortName, String valueRegexp,
			Object defaultValue, String description)
	{
		super(valueRegexp, description);

		if ( ! longName.matches("--[a-zA-Z0-9]+(-[a-zA-Z0-9]+)*"))
			throw new IllegalArgumentException("invalid long name " + longName);

		if (shortName != null && ! shortName.matches("-[a-zA-Z0-9]"))
			throw new IllegalArgumentException(
					"short name does not match -[a-zA-Z0-9]: " + shortName);

		this.longName = longName;
		this.shortName = shortName;
		this.defaultValue = defaultValue == null ? null : defaultValue.toString();
	}

	public String getShortName()
	{
		return shortName;
	}

	public String getLongName()
	{
		return longName;
	}

	public String getDefaultValue()
	{
		return defaultValue;
	}

	@Override
	public String toString()
	{
		String s = getLongName();

		if (getShortName() != null)
		{
			s += "|" + getShortName();
		}

		if (getValueRegexp() != null)
		{
			s += "=" + getValueRegexp();
		}

		if (getDefaultValue() != null)
		{
			s += " (default=" + getDefaultValue() + ")";
		}

		return s + ": " + getDescription();
	}

}
