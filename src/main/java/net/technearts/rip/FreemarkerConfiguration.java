package net.technearts.rip;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

public class FreemarkerConfiguration extends Configuration {
  public static Configuration getDefaultConfiguration() {
    return new FreemarkerConfiguration();
  }

  private FreemarkerConfiguration() {
    super(VERSION_2_3_26);
    setDefaultEncoding("UTF-8");
    setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    setLogTemplateExceptions(false);
    try {
      File f;
      try {
        f = new File(RipResponseBuilder.class.getResource("/").toURI());
      } catch (final URISyntaxException e) {
        f = new File(RipResponseBuilder.class.getResource("/").getPath());
      }
      setDirectoryForTemplateLoading(f);
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
