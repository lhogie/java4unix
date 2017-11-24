package java4unix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandLine
{
	private final List<Object> elements = new ArrayList<>();

	public CommandLine(List<String> args, CommandLineSpecification specs)
			throws UnsupportedOptionException, InvalidOptionValueException
	{
		while ( ! args.isEmpty())
		{
			String arg = args.remove(0);

			// if this is a specified option that says that the next element is
			// NOT an option
			// like in "rm -- -f" if you wqnt to erase a file named "-f"
			if (arg.equals("--"))
			{
				// adds the next positional parameter as an argument
				elements.add(args.remove(0));
			}
			else
			{
				// this is an option
				if (arg.startsWith("-"))
				{
					boolean isLong = arg.startsWith("--");
					int pos = arg.indexOf('=');
					String name = isLong ? arg.substring(0, pos > 0 ? pos : arg.length())
							: arg;
					OptionSpecification spec = specs.getOptionSpecification(name);

					// no specifications exist for this parameter, which means
					// that the option does not exist
					if (spec == null)
					{
						throw new UnsupportedOptionException("option \"" + arg
								+ "\" is not supported. Please use \"--help\"");
					}
					else
					{
						Option option = new Option(spec, true);

						// if the option requires a value
						if (spec.getValueRegexp() != null)
						{
							if (isLong)
							{
								if (pos < 0)
								{
									throw new InvalidOptionValueException(
											"empty value for option " + name);
								}
								else
								{
									option.setValue(arg.substring(pos + 1));
								}
							}
							else
							{
								option.setValue(args.remove(0));
							}
						}

						elements.add(option);
					}
				}
				// this argument is not an option, it's hence a parameter
				else
				{
					elements.add(arg);
				}
			}
		}
	}

	public List<String> findParameters()
	{
		List<String> args = new ArrayList<>();

		for (Object p : elements)
		{
			if (p instanceof String)
			{
				args.add((String) p);
			}
		}

		return Collections.unmodifiableList(args);
	}

	public List<Option> findOptions()
	{
		List<Option> options = new ArrayList<>();

		for (Object p : elements)
		{
			if (p instanceof Option)
			{
				options.add((Option) p);
			}
		}

		return Collections.unmodifiableList(options);
	}

	@Override
	public String toString()
	{
		String s = "";

		for (Object e : elements)
		{
			if (e instanceof Option)
			{
				Option o = (Option) e;

				if (o.isSpecifiedOnTheCommandLine())
				{
					s += o;
				}
			}
			else
			{
				s += e;
			}
		}
		
		return s;
	}

	public Option findOption(OptionSpecification spec)
	{
		if (spec == null)
			throw new NullPointerException("null option specification");

		for (Option option : findOptions())
		{
			if (option.getSpecification() == spec)
			{
				return option;
			}
		}

		// if the option has not been specified by the user
		Option requestedOption = new Option(spec, false);
		this.elements.add(requestedOption);
		return requestedOption;
	}

	public List<Object> getElements()
	{
		return this.elements;
	}

}
