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
	private static final Logger logger = LoggerFactory.getLogger(RipServer.class);
	
	public String spyRequest(String host, int port, String req) {
		StringBuilder result = new StringBuilder();
		try (final Socket socket = new Socket(host, port);
				final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
				final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				final BufferedReader lin = new BufferedReader(new StringReader(req));) {
			logger.info(req);
			lin.lines().forEach(line -> {
				try {
					out.write(line + "\r\n");
				} catch (IOException e) {
					logger.error("Erro ao enviar request para o socker em " + host + ":" + port, e);
				}
			});
			out.write("\r\n");
			out.flush();

			in.lines().forEach(line -> result.append(line));
			logger.info(result.toString());
		} catch (IOException e) {
			logger.info("Erro ao espionar o request em " + host + ":" + port, e);
		}
		return result.toString();
	}
}
