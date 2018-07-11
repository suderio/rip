package net.technearts.rip;

import static java.util.Arrays.asList;
import static net.technearts.rip.OP.AND;
import static net.technearts.rip.OP.OR;
import static org.eclipse.jetty.http.HttpStatus.NOT_FOUND_404;
import static org.eclipse.jetty.http.HttpStatus.OK_200;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import lombok.Data;
import spark.ModelAndView;
import spark.Request;
import spark.Route;
import spark.TemplateEngine;
import spark.TemplateViewRoute;
import spark.template.freemarker.FreeMarkerEngine;

enum OP {
	AND, OR;
}

@Data
class RipResponse {
	private String content;
	private Map<String, Function<Request, String>> attributes;
	private int status;

	public RipResponse(final String body, final int status) {
		this.content = body;
		this.status = status;
	}

	public RipResponse(Map<String, Function<Request, String>> attributes, String template, int status) {
		this.attributes = attributes;
		this.content = template;
		this.status = status;
	}
}

/**
 * Um construtor de respostas com base no conteúdo do body da requisição http.
 */
public class RipResponseBuilder {
	private static final Map<RipRoute, Map<Predicate<Request>, RipResponse>> conditions = new LinkedHashMap<>();
	private static final Map<RipRoute, Route> routes = new LinkedHashMap<>();
	private static final Map<RipRoute, TemplateViewRoute> templateRoutes = new LinkedHashMap<>();
	private static final Logger logger = LoggerFactory.getLogger(RipResponseBuilder.class);
	private static final Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
	static {
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions(false);
		try {
			cfg.setDirectoryForTemplateLoading(new File("./"));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private RipRoute route;
	private Predicate<Request> condition;
	private OP op = AND;

	RipResponseBuilder(final RipRoute route) {
		logger.info("Criando RipResponseBuilder para requisição {} {}", route.getMethod(), route.getPath());
		this.route = route;
		if (!conditions.containsKey(route)) {
			conditions.put(route, new LinkedHashMap<Predicate<Request>, RipResponse>());
		}
		if (!routes.containsKey(route)) {
			routes.put(route, (req, res) -> {
				final Optional<Map.Entry<Predicate<Request>, RipResponse>> optional = conditions.get(route).entrySet()
						.stream().filter(entry -> entry.getKey().test(req)).findFirst();
				RipResponse response;
				String result;
				if (optional.isPresent()) {
					response = optional.get().getValue();
					logger.debug("Respondendo com \n{}", response.getContent());
					res.status(response.getStatus());
					result = response.getContent();
				} else {
					res.status(NOT_FOUND_404);
					logger.debug("Resposta para {} {} não encontrada", route.getMethod(), route.getPath());
					result = "";
				}
				res.header("Content-Type", contentType(result));
				return result;
			});
		}
		if (!templateRoutes.containsKey(route)) {
			templateRoutes.put(route, (req, res) -> {
				final Optional<Map.Entry<Predicate<Request>, RipResponse>> optional = conditions.get(route).entrySet()
						.stream().filter(entry -> entry.getKey().test(req)).findFirst();
				RipResponse response;
				ModelAndView result;
				if (optional.isPresent()) {
					response = optional.get().getValue();
					logger.debug("Respondendo com \n{}", response.getContent());
					res.status(response.getStatus());
					Map<String, Object> attributes = new HashMap<>();
					for (Map.Entry<String, Function<Request, String>> f : response.getAttributes().entrySet()) {
						attributes.put(f.getKey(), f.getValue().apply(req));
					}
					result = new ModelAndView(attributes, response.getContent());
				} else {
					res.status(NOT_FOUND_404);
					logger.debug("Resposta para {} {} não encontrada", route.getMethod(), route.getPath());
					result = null;
				}
				// res.header("Content-Type", contentType(result));
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
		op = AND;
		return this;
	}

	/**
	 * Verifica se o body da requisição http contém determinada sequência
	 *
	 * @param content o conteúdo a ser checado no body
	 * @return this
	 */
	public RipResponseBuilder contains(final String content) {
		final Predicate<Request> newCondition = req -> req.body().contains(content);
		updateConditions(newCondition);
		return this;
	}

	/**
	 * Verifica se o body da requisição http contém todas as sequências informadas
	 *
	 * @param contents os conteúdos a serem checados no body
	 * @return this
	 */
	public RipResponseBuilder containsAll(final String... contents) {
		final Predicate<Request> newCondition = req -> Arrays.asList(contents).stream().allMatch(req.body()::contains);
		updateConditions(newCondition);
		return this;
	}

	/**
	 * Verifica se o body da requisição http contém alguma das sequências informadas
	 *
	 * @param contents os conteúdos a serem checados no body
	 * @return this
	 */
	public RipResponseBuilder containsAny(final String... contents) {
		final Predicate<Request> newCondition = req -> Arrays.asList(contents).stream().anyMatch(req.body()::contains);
		updateConditions(newCondition);
		return this;
	}

	private String contentType(final String body) {
		String result = "text/html;charset=utf-8";
		if (isValidJSON(body)) {
			result = "application/json";
		} else if (isValidXML(body)) {
			if (body.contains("soap:Envelope") && body.contains("soap:Body") && body.contains("soap:Header")) {
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

	private void createTemplateMethod() {
		// TODO utilizar Configuration para alterar caminho dos templates
		// TODO usar método render do FreeMarkerEngine em vez de passar como argumento
		final TemplateEngine templateEngine = new FreeMarkerEngine(cfg);
		switch (route.getMethod()) {
		case connect:
			route.getRipServer().service.connect(route.getPath(), templateRoutes.get(route), templateEngine);
			break;
		case delete:
			route.getRipServer().service.delete(route.getPath(), templateRoutes.get(route), templateEngine);
			break;
		case get:
			route.getRipServer().service.get(route.getPath(), templateRoutes.get(route), templateEngine);
			break;
		case head:
			route.getRipServer().service.head(route.getPath(), templateRoutes.get(route), templateEngine);
			break;
		case options:
			route.getRipServer().service.options(route.getPath(), templateRoutes.get(route), templateEngine);
			break;
		case patch:
			route.getRipServer().service.patch(route.getPath(), templateRoutes.get(route), templateEngine);
			break;
		case post:
			route.getRipServer().service.post(route.getPath(), templateRoutes.get(route), templateEngine);
			break;
		case put:
			route.getRipServer().service.put(route.getPath(), templateRoutes.get(route), templateEngine);
			break;
		case trace:
			route.getRipServer().service.trace(route.getPath(), templateRoutes.get(route), templateEngine);
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
		} catch (final IOException e) {
			valid = false;
		}
		return valid;
	}

	private boolean isValidXML(final String xml) {
		boolean valid = false;
		try (InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"))) {
			final SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			saxParser.parse(stream, new DefaultHandler());
			valid = true;
		} catch (SAXException | ParserConfigurationException | IOException e) {
			valid = false;
		}
		return valid;
	}

	/**
	 * Verifica se o body da requisição http contém determinada sequência
	 *
	 * @param content o conteúdo a ser checado no body
	 * @return this
	 */
	public RipResponseBuilder matches(final Predicate<Request> condition) {
		updateConditions(condition);
		return this;
	}

	/**
	 * Verifica se o body da requisição http contém todas as sequências informadas
	 *
	 * @param contents os conteúdos a serem checados no body
	 * @return this
	 */
	public RipResponseBuilder matchesAll(@SuppressWarnings("unchecked") final Predicate<Request>... conditions) {
		final Predicate<Request> newCondition = asList(conditions).stream().reduce(req -> true, Predicate::and);
		updateConditions(newCondition);
		return this;
	}

	/**
	 * Verifica se o body da requisição http contém alguma das sequências informadas
	 *
	 * @param contents os conteúdos a serem checados no body
	 * @return this
	 */
	public RipResponseBuilder matchesAny(@SuppressWarnings("unchecked") final Predicate<Request>... conditions) {
		final Predicate<Request> newCondition = asList(conditions).stream().reduce(req -> false, Predicate::or);
		updateConditions(newCondition);
		return this;
	}

	/**
	 * Operador lógico OU
	 *
	 * @return this
	 */
	public RipResponseBuilder or() {
		op = OR;
		return this;
	}

	/**
	 * Cria uma resposta com o conteúdo do arquivo informado. Essa é uma operação
	 * terminal.
	 *
	 * @param withFile o caminho relativo para o arquivo, com raiz em
	 *                 src/main/resources
	 */
	public void respond(final Path withFile) {
		respond(withFile, OK_200);
	}

	/**
	 * Cria uma resposta com o conteúdo do arquivo informado, retornando o
	 * <code>status</code> http. Essa é uma operação terminal.
	 *
	 * @param withFile o caminho relativo para o arquivo, com raiz em
	 *                 src/main/resources
	 * @param status   o status de retorno
	 */
	public void respond(final Path withFile, final int status) {
		try {
			respond(new String(Files.readAllBytes(withFile)), status);
		} catch (final IOException e) {
			respond("Arquivo não encontrado.", NOT_FOUND_404);
		}
	}

	/**
	 * Cria uma resposta com o conteúdo do arquivo informado, retornando o
	 * <code>status</code> http. Essa é uma operação terminal.
	 *
	 * @param response o conteúdo do corpo da mensagem de retorno
	 */
	public void respond(final String response) {
		respond(response, OK_200);
	}

	/**
	 * Cria uma resposta com o conteúdo do arquivo informado, retornando o
	 * <code>status</code> http. Essa é uma operação terminal.
	 *
	 * @param response o conteúdo do corpo da mensagem de retorno
	 * @param status   o status de retorno
	 */
	public void respond(final String response, final int status) {
		if (condition == null) {
			condition = s -> true;
		}
		final RipResponse res = new RipResponse(response, status);
		conditions.get(route).put(condition, res);
		createMethod();
	}

	private void updateConditions(final Predicate<Request> newCondition) {
		if (condition == null) {
			condition = newCondition;
		} else {
			switch (op) {
			case OR:
				condition = condition.or(newCondition);
				break;
			case AND:
				condition = condition.and(newCondition);
				break;
			}
		}
	}

	/**
	 * Cria uma resposta utilizando um arquivo de template, substituindo as
	 * variáveis no arquivo pelo resultado de cada aplicação da função.
	 * 
	 * O mapa é alterado através de um <code>Consumer</code> para conveniência
	 * 
	 * @param template  o arquivo de template
	 * @param consumers lista de alterações ao mapa de variáveis X funções
	 */
	@SafeVarargs
	public final void buildResponse(final String template,
			Consumer<Map<String, Function<Request, String>>>... consumers) {
		buildResponse(template, OK_200, consumers);
	}

	/**
	 * Cria uma resposta utilizando um arquivo de template, substituindo as
	 * variáveis no arquivo pelo resultado de cada aplicação da função.
	 * 
	 * @param template  o arquivo de template
	 * @param consumers lista de alterações ao mapa de variáveis X funções
	 */
	public final void buildResponse(final String template, Map<String, Function<Request, String>> attributes) {
		buildResponse(template, OK_200, attributes);
	}

	/**
	 * Cria uma resposta utilizando um arquivo de template, substituindo as
	 * variáveis no arquivo pelo resultado de cada aplicação da função.
	 * 
	 * O mapa é alterado através de um <code>Consumer</code> para conveniência
	 * 
	 * @param template  o arquivo de template
	 * @param status    o status de retorno
	 * @param consumers lista de alterações ao mapa de variáveis X funções
	 */
	@SafeVarargs
	public final void buildResponse(final String template, final int status,
			Consumer<Map<String, Function<Request, String>>>... consumers) {
		Map<String, Function<Request, String>> attributes = new HashMap<>();
		for (Consumer<Map<String, Function<Request, String>>> consumer : consumers) {
			consumer.accept(attributes);
		}
		buildResponse(template, status, attributes);
	}

	/**
	 * Cria uma resposta utilizando um arquivo de template, substituindo as
	 * variáveis no arquivo pelo resultado de cada aplicação da função.
	 * 
	 * @param template  o arquivo de template
	 * @param status    o status de retorno
	 * @param consumers lista de alterações ao mapa de variáveis X funções
	 */
	public final void buildResponse(final String template, final int status,
			Map<String, Function<Request, String>> attributes) {
		if (condition == null) {
			condition = s -> true;
		}
		final RipResponse res = new RipResponse(attributes, template, status);
		conditions.get(route).put(condition, res);
		createTemplateMethod();
	}

}
