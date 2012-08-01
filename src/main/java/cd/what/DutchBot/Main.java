/**
 * 
 */
package cd.what.DutchBot;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;
import org.jibble.pircbot.IrcException;

/**
 * @author DutchDude
 * 
 */
public class Main {

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException, IrcException,
			InterruptedException, ConfigurationException,
			InstantiationException, IllegalAccessException {
		String configfile = "irc.properties";
		DutchBot bot = null;

		Options options = new Options();
		options.addOption(OptionBuilder
				.withLongOpt("config")
				.withArgName("configfile")
				.hasArg()
				.withDescription(
						"Load configuration from configfile, or use the default irc.cfg")
				.create("c"));
		options.addOption(OptionBuilder.withLongOpt("server")
				.withArgName("<url>").hasArg()
				.withDescription("Connect to this server").create("s"));
		options.addOption(OptionBuilder.withLongOpt("port").hasArg()
				.withArgName("port")
				.withDescription("Connect to the server with this port")
				.create("p"));
		options.addOption(OptionBuilder.withLongOpt("password").hasArg()
				.withArgName("password")
				.withDescription("Connect to the server with this password")
				.create("pw"));
		options.addOption(OptionBuilder.withLongOpt("nick").hasArg()
				.withArgName("nickname")
				.withDescription("Connect to the server with this nickname")
				.create("n"));
		options.addOption(OptionBuilder.withLongOpt("nspass").hasArg()
				.withArgName("password")
				.withDescription("Sets the password for NickServ").create("ns"));
		options.addOption("h", "help", false, "Displays this menu");

		try {
			CommandLineParser parser = new GnuParser();
			CommandLine cli = parser.parse(options, args);
			if (cli.hasOption("h")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("DutchBot", options);
				return;
			}
			// check for override config file
			if (cli.hasOption("c"))
				configfile = cli.getOptionValue("c");

			bot = new DutchBot(configfile);

			// Read the cli parameters
			if (cli.hasOption("pw"))
				bot.setServerPassword(cli.getOptionValue("pw"));
			if (cli.hasOption("s"))
				bot.setServerAddress(cli.getOptionValue("s"));
			if (cli.hasOption("p"))
				bot.setIrcPort(Integer.parseInt(cli.getOptionValue("p")));
			if (cli.hasOption("n"))
				bot.setBotName(cli.getOptionValue("n"));
			if (cli.hasOption("ns"))
				bot.setNickservPassword(cli.getOptionValue("ns"));

		} catch (ParseException e) {
			System.err.println("Error parsing command line vars "
					+ e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("DutchBot", options);
			System.exit(1);
		}

		boolean result = bot.tryConnect();

		if (result)
			bot.logMessage("Connected");
		else {
			System.out.println(" Connecting failed :O");
			System.exit(1);
		}
	}
}
