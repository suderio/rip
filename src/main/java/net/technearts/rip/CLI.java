package net.technearts.rip;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.technearts.rip.RipServer.localhost;

public class CLI {
    private static final Logger logger = LoggerFactory.getLogger(CLI.class);

    public static void main(String[] args) throws ParseException {
        CommandLine line = commandLine(args);
        String workDir = line.getOptionValue("w", "/");
        RipRoute $ = localhost(7777, workDir);
    }

    private static CommandLine commandLine(String... args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption(
                Option.builder("w").longOpt("workDir").desc("Working Directory").hasArg().build());
        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }
}
