package java4unix;

public class OptionSpecification extends ParameterSpecification
{

	private String longName;
	private String shortName;
	private String defaultValue;

	public OptionSpecification(String longName, String shortName, String valueRegexp,
			String defaultValue, String description)
	{
		super(valueRegexp, description);

		if ( ! longName.matches("--[a-zA-Z0-9]+(-[a-zA-Z0-9]+)*"))
			throw new IllegalArgumentException("invalid long name " + longName);

		if (shortName != null && ! shortName.matches("-[a-zA-Z0-9]"))
			throw new IllegalArgumentException(
					"short name does not match -[a-zA-Z0-9]: " + shortName);

		this.longName = longName;
		this.shortName = shortName;
		this.defaultValue = defaultValue;
	}

	public String getShortName()
	{
		return shortName;
	}

	public String getLongName()
	{
		return this.longName;
	}

	public String getDefaultValue()
	{
		return this.defaultValue;
	}

	public String toString()
	{
		return "***" + getLongName() + " = " + getValueRegexp() + "(default= "
				+ getDefaultValue() + ") " + getDescription() + "***";
	}

}
