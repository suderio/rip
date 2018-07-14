package net.technearts.rip;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.servlet.SparkFilter;

public class RipWebFilter extends SparkFilter {
  private static final Logger LOG = LoggerFactory.getLogger(RipWebFilter.class);

  @Override
  public void destroy() {
    LOG.info("Rip Filter finalizando");
    super.destroy();
    LOG.info("Rip Filter finalizado");
  }

  @Override
  public void doFilter(final ServletRequest request,
      final ServletResponse response, final FilterChain chain)
      throws IOException, ServletException {
    super.doFilter(request, response, chain);
  }

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
    LOG.info("Rip Filter iniciando");
    super.init(filterConfig);
    LOG.info("Rip Filter iniciado");
  }
}
