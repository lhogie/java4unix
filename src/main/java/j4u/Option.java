package j4u;

import java.io.Serializable;

public class Option implements Serializable
{
	private final OptionSpecification specs;
	private String value;

	public Option(OptionSpecification spec)
	{
		this.specs = spec;
	}

	public OptionSpecification getSpecification()
	{
		return this.specs;
	}

	public String getValue()
	{
		return value;

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
		if (value == null)
		{
			return getSpecification().getLongName();
		}
		else
		{
			return getSpecification().getLongName() + "=" + value;
		}
	}
}
