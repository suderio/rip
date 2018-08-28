/*
jMimeMagic (TM) is a Java Library for determining the content type of files or streams
Copyright (C) 2003-2017 David Castro
*/
package net.sf.jmimemagic.detectors;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

import net.sf.jmimemagic.MagicDetector;

/**
 * DOCUMENT ME!
 *
 * @author $Author$
 * @version $Revision$
 */
public class JsonFileDetector implements MagicDetector {
  private static Log log = LogFactory.getLog(JsonFileDetector.class);

  /**
   * Creates a new TextFileDetector object.
   */
  public JsonFileDetector() {
    super();
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  @Override
  public String getDisplayName() {
    return "Json File Detector";
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  @Override
  public String[] getHandledExtensions() {
    return new String[] { "js", "json" };
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  @Override
  public String[] getHandledTypes() {
    return new String[] { "application/json" };
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  @Override
  public String getName() {
    return "jsonfiledetector";
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  @Override
  public String getVersion() {
    return "0.1";
  }

  /**
   * DOCUMENT ME!
   *
   * @param data       DOCUMENT ME!
   * @param offset     DOCUMENT ME!
   * @param length     DOCUMENT ME!
   * @param bitmask    DOCUMENT ME!
   * @param comparator DOCUMENT ME!
   * @param mimeType   DOCUMENT ME!
   * @param params     DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  @Override
  public String[] process(final byte[] data, final int offset, final int length,
      final long bitmask, final char comparator, final String mimeType,
      final Map<String, String> params) {
    log.debug("processing stream data");
    try (JsonParser parser = new JsonFactory()
        .createParser(new String(data, "UTF-8"))) {
      while (!parser.isClosed()) {
        parser.nextToken();
      }
      return new String[] { "application/json" };
    } catch (final IOException e) {
      log.debug("JsonFileDetector: failed to process data");
    }
    return null;
  }

  /**
   * DOCUMENT ME!
   *
   * @param file       DOCUMENT ME!
   * @param offset     DOCUMENT ME!
   * @param length     DOCUMENT ME!
   * @param bitmask    DOCUMENT ME!
   * @param comparator DOCUMENT ME!
   * @param mimeType   DOCUMENT ME!
   * @param params     DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  @Override
  public String[] process(final File file, final int offset, final int length,
      final long bitmask, final char comparator, final String mimeType,
      final Map<String, String> params) {
    log.debug("processing file data");
    try (BufferedInputStream is = new BufferedInputStream(
        new FileInputStream(file))) {
      final byte[] b = new byte[length];
      final int n = is.read(b, offset, length);
      if (n > 0) {
        return process(b, offset, length, bitmask, comparator, mimeType,
            params);
      }
    } catch (final IOException e) {
      log.info("JsonFileDetector: file " + file.getName());
    }
    return null;
  }
}
