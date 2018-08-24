package net.technearts.rip;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestSpy {
  private static final Logger logger = LoggerFactory.getLogger(RequestSpy.class);
  private String host;
  private int port;

  public RequestSpy(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public final String spyRequest(String req) {
    return spyRequest(host, port, req);
  }

  public static final String spyRequest(String host, int port, String req) {
    StringBuilder result = new StringBuilder();
    try (Socket socket = new Socket(host, port);
        BufferedWriter out = new BufferedWriter(
            new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
        BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));
        BufferedReader lin = new BufferedReader(new StringReader(req));) {
      logger.info(req);
      String line = null;
      while ((line = lin.readLine()) != null) {
        try {
          out.write(line + "\r\n");
        } catch (IOException e) {
          logger.error(
              "Erro ao enviar request para o socket em " + host + ":" + port,
              e);
        }
      }
      out.write("\r\n");
      out.flush();

      line = null;
      while ((line = in.readLine()) != null) {
        result.append(line);
      }
      logger.info(result.toString());
    } catch (IOException e) {
      logger.error("Erro ao espionar o request em " + host + ":" + port, e);
    }
    return result.toString();
  }
}
