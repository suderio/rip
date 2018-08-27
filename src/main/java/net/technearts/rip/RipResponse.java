package net.technearts.rip;

import java.util.Map;
import java.util.function.Function;

import lombok.Data;
import spark.Request;

@Data
class RipResponse {
  private String content;
  private Map<String, Function<Request, String>> attributes;
  private int status;
  private String contentType;

  public RipResponse(final Map<String, Function<Request, String>> attributes,
      final String template, final int status, final String contentType) {
    this.attributes = attributes;
    content = template;
    this.status = status;
    this.contentType = contentType;
  }

  public RipResponse(final String body, final int status, final String contentType) {
    content = body;
    this.status = status;
    this.contentType = contentType;
  }
}