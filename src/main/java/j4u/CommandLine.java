package j4u;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandLine implements Serializable {
	private final List<Object> arguments = new ArrayList<>();
	private final CommandLineSpecification specs;

	public CommandLine(String... args) {
		this(Arrays.asList(args), null);
	}

	public CommandLine(List<String> args, CommandLineSpecification specs)
			throws UnsupportedOptionException, InvalidOptionValueException {
		this.specs = specs;
		while ( ! args.isEmpty()) {
			String arg = args.remove(0);

			// if this is a specified option that says that the next element is
			// NOT an option
			// like in "rm -- -f" if you want to erase a file named "-f"
			if (arg.equals("--")) {
				// adds the next positional parameter as an argument
				arguments.add(args.remove(0));
			}
			else {
				// this is an option
				if (arg.startsWith("-")) {
					boolean isLong = arg.startsWith("--");
					int posOfEqualSign = arg.indexOf('=');
					boolean flag = posOfEqualSign < 0;
					String name = flag ? arg
							: arg.substring(0, flag ? posOfEqualSign : arg.length());

					OptionSpecification spec = specs.findOptionSpecification(name);

					// no specifications exist for this parameter, which
					// means that the option does not exist
					if (spec == null) {
						throw new UnsupportedOptionException("option \"" + arg
								+ "\" is not supported. Please use \"--help\"");
					}
					else {
						Option option = new Option(spec);

						// if the option requires a value
						if (spec.getValueRegexp() != null) {
							if (isLong) {
								if (posOfEqualSign < 0) {
									throw new InvalidOptionValueException(
											"empty value for option " + name);
								}
								else {
									option.setValue(arg.substring(posOfEqualSign + 1));
								}
							}
							else {
								option.setValue(args.remove(0));
							}
						}

						arguments.add(option);
					}
				}
				// this argument is not an option, it's hence a parameter
				else {
					arguments.add(arg);
				}
			}
		}
	}

	synchronized public final boolean isOptionSpecified(String optionName) {
		return findOption(optionName) != null;
	}

	public String getOptionValue(String optionName) {
		OptionSpecification specs = this.specs.findOptionSpecification(optionName);

		if (specs == null)
			throw new IllegalStateException("option " + optionName + " does not exist");

		String name = specs.getLongName() + "/" + specs.getShortName();

		if (specs.getValueRegexp() == null)
			throw new IllegalStateException("option " + name + " cannot have any value");

		Option o = findOption(optionName);

		if (o != null)
			return o.getValue();

		if (specs.getDefaultValue() != null) {
			return specs.getDefaultValue();
		}
		else {
			throw new IllegalArgumentException(
					"option " + name + " has not been assigned a value");
		}
	}

	public List<String> findParameters() {
		List<String> args = new ArrayList<>();

		for (Object p : arguments) {
			if (p instanceof String) {
				args.add((String) p);
			}
		}

		return Collections.unmodifiableList(args);
	}

	public List<Option> findOptions() {
		List<Option> options = new ArrayList<>();

		for (Object p : arguments) {
			if (p instanceof Option) {
				options.add((Option) p);
			}
		}

		return Collections.unmodifiableList(options);
	}

	@Override
	public String toString() {
		String s = "";

		for (Object e : arguments) {
			if (e instanceof Option) {
				Option o = (Option) e;
				s += o;
			}
			else {
				s += e;
			}
		}

		return s;
	}

	public List<Object> getArguments() {
		return Collections.unmodifiableList(this.arguments);
	}

	public Option findOption(String name) {
		for (Option o : findOptions()) {
			if (name.equals(o.getSpecification().getLongName())
					|| name.equals(o.getSpecification().getShortName())) {
				return o;
			}
		}

		return null;
	}

}
