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

  public RipResponse(final Map<String, Function<Request, String>> attributes,
      final String template, final int status) {
    this.attributes = attributes;
    content = template;
    this.status = status;
  }

  public RipResponse(final String body, final int status) {
    content = body;
    this.status = status;
  }
}