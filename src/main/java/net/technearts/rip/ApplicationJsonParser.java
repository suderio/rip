package net.technearts.rip;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

public class ApplicationJsonParser extends AbstractParser {
  /**
   * 
   */
  private static final long serialVersionUID = 6067511513029742242L;
  private static final Set<MediaType> SUPPORTED_TYPES = Collections
      .singleton(MediaType.application("json"));

  @Override
  public Set<MediaType> getSupportedTypes(ParseContext context) {
    return SUPPORTED_TYPES;
  }

  @Override
  public void parse(InputStream stream, ContentHandler handler,
      Metadata metadata, ParseContext context)
      throws IOException, SAXException, TikaException {
    try (JsonParser parser = new JsonFactory().createParser(stream)) {
      while (!parser.isClosed()) {
        parser.nextToken();
      }
    }
  }

}
