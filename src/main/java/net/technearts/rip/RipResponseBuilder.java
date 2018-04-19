package net.technearts.rip;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

import static net.technearts.rip.OP.AND;
import static net.technearts.rip.OP.OR;

import lombok.Data;
import spark.Route;

enum OP {
    AND, OR;
}

@Data
class RipResponse {
    private String body;
    private int status;

    public RipResponse(String body, int status) {
        this.body = body;
        this.status = status;
    }
}

/**
 * Um construtor de respostas com base no conteúdo do body da requisição http.
 */
public class RipResponseBuilder {
    private static final Map<RipRoute, Map<Predicate<String>, RipResponse>> conditions = new LinkedHashMap<>();
    private static final Map<RipRoute, Route> routes = new LinkedHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(RipResponseBuilder.class);
    private RipRoute route;
    private Predicate<String> condition;
    private OP op = AND;

    RipResponseBuilder(RipRoute route) {
        logger.info("Criando RipResponseBuilder para requisição {} {}", route.getMethod(), route.getPath());
        this.route = route;
        if (!conditions.containsKey(route)) {
            conditions.put(route, new LinkedHashMap<Predicate<String>, RipResponse>());
        }
        if (!routes.containsKey(route)) {
            routes.put(route, (req, res) -> {
                Optional<Map.Entry<Predicate<String>, RipResponse>> optional = conditions.get(route).entrySet().stream()
                        .filter(entry -> entry.getKey().test(req.body())).findFirst();
                RipResponse response;
                String result;
                if (optional.isPresent()) {
                    response = optional.get().getValue();
                    logger.debug("Respondendo com \n{}", response.getBody());
                    res.status(response.getStatus());
                    result = response.getBody();
                } else {
                    res.status(HttpStatus.NOT_FOUND_404);
                    logger.debug("Resposta para {} {} não encontrada", route.getMethod(), route.getPath());
                    result = "";
                }
                res.header("Content-Type", contentType(result));
                return result;
            });
        }
    }

    /**
     * Operador lógico E
     * 
     * @return this
     */
    public RipResponseBuilder and() {
        this.op = AND;
        return this;
    }

    /**
     * Verifica se o body da requisição http contém determinada sequência
     * 
     * @param content
     *            o conteúdo a ser checado no body
     * @return this
     */
    public RipResponseBuilder contains(final String content) {
        Predicate<String> newCondition = body -> body.contains(content);
        updateConditions(newCondition);
        return this;
    }

    /**
     * Verifica se o body da requisição http contém todas as sequências informadas
     * 
     * @param contents
     *            os conteúdos a serem checados no body
     * @return this
     */
    public RipResponseBuilder containsAll(final String... contents) {
        Predicate<String> newCondition = body -> Arrays.asList(contents).stream()
                .allMatch(body::contains);
        updateConditions(newCondition);
        return this;
    }

    /**
     * Verifica se o body da requisição http contém alguma das sequências informadas
     * 
     * @param contents
     *            os conteúdos a serem checados no body
     * @return this
     */
    public RipResponseBuilder containsAny(final String... contents) {
        Predicate<String> newCondition = body -> Arrays.asList(contents).stream()
                .anyMatch(body::contains);
        updateConditions(newCondition);
        return this;
    }

    private String contentType(String body) {
        String result = "text/html;charset=utf-8";
        if (isValidJSON(body)) {
            result = "application/json";
        } else if (isValidXML(body)) {
            if (body.contains("soap:Envelope") && body.contains("soap:Body")
                    && body.contains("soap:Header")) {
                result = "application/soap+xml";
            } else {
                result = "application/xml";
            }
        }
        return result;
    }

    private void createMethod() {
        switch (route.getMethod()) {
        case connect:
            route.getRipServer().service.connect(route.getPath(), routes.get(route));
            break;
        case delete:
            route.getRipServer().service.delete(route.getPath(), routes.get(route));
            break;
        case get:
            route.getRipServer().service.get(route.getPath(), routes.get(route));
            break;
        case head:
            route.getRipServer().service.head(route.getPath(), routes.get(route));
            break;
        case options:
            route.getRipServer().service.options(route.getPath(), routes.get(route));
            break;
        case patch:
            route.getRipServer().service.patch(route.getPath(), routes.get(route));
            break;
        case post:
            route.getRipServer().service.post(route.getPath(), routes.get(route));
            break;
        case put:
            route.getRipServer().service.put(route.getPath(), routes.get(route));
            break;
        case trace:
            route.getRipServer().service.trace(route.getPath(), routes.get(route));
            break;
        case after:
        case afterafter:
        case before:
        case unsupported:
        default:
            logger.error("A opção {} não é suportada!", route.getMethod());
            break;
        }
    }

    private boolean isValidJSON(final String json) {
        boolean valid = false;
        try (JsonParser parser = new JsonFactory().createParser(json)) {
            while (!parser.isClosed()) {
                parser.nextToken();
            }
            valid = true;
        } catch (IOException e) {
            valid = false;
        }
        return valid;
    }

    private boolean isValidXML(final String xml) {
        boolean valid = false;
        try (InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"))) {
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(stream, new DefaultHandler());
            valid = true;
        } catch (SAXException | ParserConfigurationException | IOException e) {
            valid = false;
        }
        return valid;
    }

    /**
     * Operador lógico OU
     * 
     * @return this
     */
    public RipResponseBuilder or() {
        this.op = OR;
        return this;
    }

    /**
     * Cria uma resposta com o conteúdo do arquivo informado.
     * Essa é uma operação terminal.
     * 
     * @param withFile
     *            o caminho relativo para o arquivo, com raiz em src/main/resources
     */
    public void respond(Path withFile) {
        respond(withFile, HttpStatus.OK_200);
    }

    /**
     * Cria uma resposta com o conteúdo do arquivo informado, retornando o <code>status</code> http.
     * Essa é uma operação terminal.
     * 
     * @param withFile
     *            o caminho relativo para o arquivo, com raiz em src/main/resources
     * @param status
     *            o status de retorno
     */
    public void respond(Path withFile, int status) {
        try {
            respond(new String(Files.readAllBytes(withFile)), status);
        } catch (IOException e) {
            respond("Arquivo não encontrado.", HttpStatus.NOT_FOUND_404);
        }
    }

    /**
     * Cria uma resposta com o conteúdo do arquivo informado, retornando o <code>status</code> http.
     * Essa é uma operação terminal.
     * 
     * @param response
     *            o conteúdo do corpo da mensagem de retorno
     */
    public void respond(String response) {
        respond(response, HttpStatus.OK_200);
    }

    /**
     * Cria uma resposta com o conteúdo do arquivo informado, retornando o <code>status</code> http.
     * Essa é uma operação terminal.
     * 
     * @param response
     *            o conteúdo do corpo da mensagem de retorno
     * @param status
     *            o status de retorno
     */
    public void respond(String response, int status) {
        if (condition == null) {
            condition = s -> true;
        }
        RipResponse res = new RipResponse(response, status);
        conditions.get(route).put(condition, res);
        createMethod();
    }

    private void updateConditions(Predicate<String> newCondition) {
        if (condition == null) {
            condition = newCondition;
        } else {
            switch (this.op) {
            case OR:
                condition = condition.or(newCondition);
                break;
            case AND:
                condition = condition.and(newCondition);
                break;
            }
        }
    }
}
