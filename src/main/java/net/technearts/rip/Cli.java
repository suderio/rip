package net.technearts.rip;

import static net.technearts.rip.RipServer.localhost;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cli {
  private static final Logger logger = LoggerFactory.getLogger(Cli.class);

  private static CommandLine commandLine(final String... args)
      throws ParseException {
    final CommandLineParser parser = new DefaultParser();
    final Options options = new Options();
    options.addOption(Option.builder("w").longOpt("workDir")
        .desc("Working Directory").hasArg().build());
    try {
      return parser.parse(options, args);
    } catch (final ParseException ex) {
      logger.error(ex.getMessage());
      throw ex;
    }
  }

  /**
   * 
   * @param args parâmetros de linha de comando.
   * @throws ParseException se os parâmetros estiverem errados.
   */
  public static void main(final String[] args) throws ParseException {
    final CommandLine line = commandLine(args);
    final String workDir = line.getOptionValue("w", "/");
    localhost(7777, workDir);
    // TODO ler reqs em workDir e processar as chamadas
  }
}
