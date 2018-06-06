package j4u;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandLineSpecification
{
	final Set<OptionSpecification> optionSpecifications = new HashSet<OptionSpecification>();
	final List<ArgumentSpecification> argumentSpecifications = new ArrayList<ArgumentSpecification>();

	public final OptionSpecification getOptionSpecification(String name)
	{
		for (OptionSpecification thisOptionSpecification : getOptionSpecifications())
		{
			if (thisOptionSpecification.getLongName().equals(name)
					|| (thisOptionSpecification.getShortName() != null
							&& thisOptionSpecification.getShortName().equals(name)))
			{
				return thisOptionSpecification;
			}
		}

		return null;
	}

	public Set<OptionSpecification> getOptionSpecifications()
	{
		return optionSpecifications;
	}

	public List<ArgumentSpecification> getArgumentsSpecifications()
	{
		return argumentSpecifications;
	}

	@Override
	public String toString()
	{
		return toString(false).toString();
	}

	public StringBuffer toString(boolean printUtilityOptions)
	{
		StringBuffer buf = new StringBuffer();

		for (OptionSpecification spec : getOptionSpecifications())
		{
			if ( ! spec.getLongName().startsWith("--X")
					|| (spec.getLongName().startsWith("--X") && printUtilityOptions))
			{
				buf.append("\n\t");
				buf.append(spec.getLongName());

				if (spec.getValueRegexp() != null)
				{
					buf.append("=" + spec.getValueRegexp());
				}

				if (spec.getShortName() != null)
				{
					buf.append("\n\t ");
					buf.append(spec.getShortName());

					if (spec.getValueRegexp() != null)
					{
						buf.append(" " + spec.getValueRegexp());
					}
				}

				buf.append("\n\t\t");
				buf.append(spec.getDescription());

				if (spec.getDefaultValue() != null)
				{
					buf.append("\n\t\tDefault value is \"");
					buf.append(spec.getDefaultValue());
					buf.append("\".");
				}
			}
		}

		return buf;

	}
}
