package net.technearts.rip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.eclipse.jetty.http.HttpMethod;

/**
 * Requisição http (rfc 2612):
 */
public class HttpRequestParser {
    private String reqLine;
    private HashMap<String, String> reqHeaders;
    private StringBuilder msgBody;

    public HttpRequestParser() {
        reqHeaders = new HashMap<>();
        msgBody = new StringBuilder();
    }

    /**
     * Parse da requisição.
     * 
     * @param request
     *            A requisição http.
     * @throws IOException
     *             Qualquer erro de leitura.
     * @throws HttpFormatException
     *             Qualquer erro de formato
     */
    public void parseRequest(String request) throws IOException, HttpFormatException {
        parseRequest(new BufferedReader(new StringReader(request)));
    }

    public void parseRequest(BufferedReader reader) throws IOException, HttpFormatException {
        setRequestLine(reader.readLine());
        String header = reader.readLine();
        while (header != null && header.length() > 0) {
            appendHeaderParameter(header);
            header = reader.readLine();
        }
        String bodyLine = reader.readLine();
        while (bodyLine != null) {
            appendMessageBody(bodyLine);
            bodyLine = reader.readLine();
        }
    }

    /**
     * Seção 5.1: a linha da requisição começar com o método, seguido
     * da URI e versão do protocolo, terminando com CRLF.
     * 
     * @return a linha da requisição
     */
    public String getRequestLine() {
        return reqLine;
    }

    private void setRequestLine(String requestLine) throws HttpFormatException {
        if (requestLine == null || requestLine.length() == 0) {
            throw new HttpFormatException("Invalid Request-Line: " + requestLine);
        }
        reqLine = requestLine;
    }

    private void appendHeaderParameter(String header) throws HttpFormatException {
        int idx = header.indexOf(':');
        if (idx == -1) {
            throw new HttpFormatException("Invalid Header Parameter: " + header);
        }
        reqHeaders.put(header.substring(0, idx), header.substring(idx + 1, header.length()));
    }

    /**
     * Corpo da mensagem (body). Ver diferença entre corpo da mensagem e
     * corpo da entidade (seção 14.41)
     * 
     * @return o corpo da mensagem
     */
    public String getMessageBody() {
        return msgBody.toString();
    }

    private void appendMessageBody(String bodyLine) {
        msgBody.append(bodyLine).append("\r\n");
    }

    /**
     * Lista de cabeçalhos ver seções: 4.5, 5.3, 7.1 da rfc 2616
     * 
     * @param headerName
     *            o cabeçalho
     * @return o valor do cabeçalho ou null.
     */
    public String getHeaderParam(String headerName) {
        return reqHeaders.get(headerName);
    }

    public HttpMethod getMethod() {
        HttpMethod method = HttpMethod.fromString(getRequestLine().split(" ")[0]);
        if (method == null) {
            throw new IllegalArgumentException();
        }
        return method;
    }

    public URL getUrl() {
        String protocol = "HTTP";
        String host = getHeaderParam("Host");
        int port = 80;
        String file = getRequestLine().split(" ")[1];
        try {
            return new URL(protocol, host, port, file);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException();
        }
    }
}