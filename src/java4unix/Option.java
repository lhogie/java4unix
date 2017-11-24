package java4unix;

public class Option
{
	private final OptionSpecification specs;
	private String value;
	private final boolean specified;

	public Option(OptionSpecification spec, boolean specifiedOnTheCommandLine)
	{
		this.specs = spec;
		this.specified = specifiedOnTheCommandLine;
	}

	public OptionSpecification getSpecification()
	{
		return this.specs;
	}

	public String getValue()
	{
		if (this.value == null)
		{
			return getSpecification().getDefaultValue();
		}
		else
		{
			return this.value;
		}
	}

	protected void setValue(String v) throws InvalidOptionValueException
	{
		if (v == null)
			throw new NullPointerException();

		if (getSpecification().getValueRegexp() == null)
		{
			throw new InvalidOptionValueException("option '"
					+ getSpecification().getLongName() + "' does not accept any value");
		}
		else
		{
			if (v.matches(getSpecification().getValueRegexp()))
			{
				System.out.println(v +"=>"+ specs.getLongName());

				this.value = v;
			}
			else
			{
				throw new InvalidOptionValueException("value '" + v + "' for option '"
						+ getSpecification().getLongName() + "' does not match '"
						+ getSpecification().getValueRegexp() + "'");
			}
		}
	}

	@Override
	public String toString()
	{
		if (getValue() == null)
		{
			return getSpecification().getLongName();
		}
		else
		{
			return getSpecification().getLongName() + "=" + getValue();
		}
	}

	public boolean isSpecifiedOnTheCommandLine()
	{
		return specified;
	}
}
