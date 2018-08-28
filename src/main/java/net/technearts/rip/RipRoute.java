package net.technearts.rip;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.hash;
import static java.util.Optional.ofNullable;
import static org.eclipse.jetty.http.HttpStatus.NOT_FOUND_404;
import static spark.route.HttpMethod.connect;
import static spark.route.HttpMethod.delete;
import static spark.route.HttpMethod.get;
import static spark.route.HttpMethod.head;
import static spark.route.HttpMethod.options;
import static spark.route.HttpMethod.patch;
import static spark.route.HttpMethod.post;
import static spark.route.HttpMethod.put;
import static spark.route.HttpMethod.trace;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ComparisonChain;

import freemarker.template.Configuration;
import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.TemplateEngine;
import spark.TemplateViewRoute;
import spark.route.HttpMethod;
import spark.template.freemarker.FreeMarkerEngine;

/**
 * Uma rota (Verbo http + Caminho) associado a um servidor Rip
 */
public class RipRoute implements Comparable<RipRoute>, AutoCloseable {
	private static final Logger LOG = LoggerFactory.getLogger(RipRoute.class);
	private static final Configuration CFG = FreemarkerConfiguration.getDefaultConfiguration();
	/* TODO esses dois devem ser criados/retornados por um Factory */
	private static final Map<RipRoute, Map<Predicate<Request>, RipResponse>> CONDITIONS = new LinkedHashMap<>();
	private static final Map<RipRoute, BiFunction<Request, Response, String>> LOGS = new LinkedHashMap<>();

	private final RipServer ripServer;
	private HttpMethod method;
	private String path;
	Route route;
	TemplateViewRoute templateRoute;

	RipRoute(final RipServer ripServer) {
		this.ripServer = ripServer;
	}

	public void setCondition(Predicate<Request> predicate, RipResponse response) {
		putCondition(predicate, response);
		createMethod();
	}
	
	public void setTemplateCondition(Predicate<Request> predicate, RipResponse response) {
		putCondition(predicate, response);
		createTemplateMethod();;
	}
	
	private void putCondition(Predicate<Request> predicate, RipResponse response) {
		Map<Predicate<Request>, RipResponse> map = CONDITIONS.get(this);
		if (map == null) {
			map = new LinkedHashMap<Predicate<Request>, RipResponse>();
			CONDITIONS.put(this, map);
		}
		map.put(predicate, response);
	}

	public void addLog(BiFunction<Request, Response, String> log) {
		LOGS.put(this, log);
	}

	@Override
	public void close() {
		// TODO
	}

	@Override
	public int compareTo(final RipRoute that) {
		return ComparisonChain.start().compare(ripServer, that.ripServer).compare(method, that.method)
				.compare(path, that.path).result();
	}

	/**
	 * Configura o RipRoute para método connect no caminho <code>path</code>
	 *
	 * @param path o caminho da requisição http a ser criado
	 * @return um <code>RipResponseBuilder</code> para a construção da resposta
	 *         desse RipRoute
	 */
	public RipResponseBuilder connect(final String path) {
		return create(path, connect);
	}

	private String contentType(final byte[] stream) {
		MagicMatch match = null;
		try {
			match = Magic.getMagicMatch(stream, false);
			if (match.getSubMatches().size() > 0) {
				return match.getSubMatches().toArray()[0].toString();
			}
		} catch (MagicParseException | MagicMatchNotFoundException | MagicException | NullPointerException e) {
			return "text/html;charset=utf-8";
		}
		return match.getMimeType();
	}

	private Route getRoute() {
		return (req, res) -> {
			final Optional<Map.Entry<Predicate<Request>, RipResponse>> optional = CONDITIONS.get(this).entrySet()
					.stream().filter(entry -> entry.getKey().test(req)).findFirst();
			RipResponse response;
			String result;
			if (optional.isPresent()) {
				response = optional.get().getValue();
				LOG.debug("Requisição para {}:\n{}", req.pathInfo(), req.body());
				LOG.debug("Respondendo com \n{}", response.getContent());
				res.status(response.getStatus());
				result = response.getContent();
				if (response.getContentType() == null) {
					res.header("Content-Type", contentType(result.getBytes(UTF_8)));
				} else {
					res.header("Content-Type", response.getContentType());
				}
			} else {
				res.status(NOT_FOUND_404);
				LOG.warn("Resposta para {} {} não encontrada", this.getMethod(), this.getPath());
				result = "";
			}
			ofNullable(LOGS.get(this)).ifPresent(f -> f.apply(req, res));
			return result;
		};
	}

	private TemplateViewRoute getTemplateRoute() {
		return (req, res) -> {
			final Optional<Map.Entry<Predicate<Request>, RipResponse>> optional = CONDITIONS.get(this).entrySet()
					.stream().filter(entry -> entry.getKey().test(req)).findFirst();
			RipResponse response;
			ModelAndView result;
			if (optional.isPresent()) {
				response = optional.get().getValue();
				LOG.debug("Respondendo com \n{}", response.getContent());
				res.status(response.getStatus());
				final Map<String, Object> attributes = new HashMap<>();
				for (final Map.Entry<String, Function<Request, String>> f : response.getAttributes().entrySet()) {
					attributes.put(f.getKey(), f.getValue().apply(req));
				}
				if (response.getContentType() != null) {
					res.header("Content-Type", response.getContentType());
				} else {
					res.header("Content-Type", "text/plain");
				}
				result = new ModelAndView(attributes, response.getContent());
			} else {
				res.status(NOT_FOUND_404);
				LOG.debug("Resposta para {} {} não encontrada", this.getMethod(), this.getPath());
				result = null;
			}
			return result;
		};
	}

	private RipResponseBuilder create(final String path, final HttpMethod method) {
		this.method = method;
		this.path = path;
		this.route = getRoute();
		this.templateRoute = getTemplateRoute();
		return new RipResponseBuilder(this);
	}

	private void createMethod() {
		switch (getMethod()) {
		case connect:
			ripServer.service.connect(path, route);
			break;
		case delete:
			ripServer.service.delete(path, route);
			break;
		case get:
			ripServer.service.get(path, route);
			break;
		case head:
			ripServer.service.head(path, route);
			break;
		case options:
			ripServer.service.options(path, route);
			break;
		case patch:
			ripServer.service.patch(path, route);
			break;
		case post:
			ripServer.service.post(path, route);
			break;
		case put:
			ripServer.service.put(path, route);
			break;
		case trace:
			ripServer.service.trace(path, route);
			break;
		case after:
		case afterafter:
		case before:
		case unsupported:
		default:
			LOG.error("A opção {} não é suportada!", method);
			break;
		}
	}

	private void createTemplateMethod() {
		// TODO usar método render do FreeMarkerEngine em vez de passar como
		// argumento
		final TemplateEngine templateEngine = new FreeMarkerEngine(CFG);
		switch (getMethod()) {
		case connect:
			ripServer.service.connect(path, templateRoute, templateEngine);
			break;
		case delete:
			ripServer.service.delete(path, templateRoute, templateEngine);
			break;
		case get:
			ripServer.service.get(path, templateRoute, templateEngine);
			break;
		case head:
			ripServer.service.head(path, templateRoute, templateEngine);
			break;
		case options:
			ripServer.service.options(path, templateRoute, templateEngine);
			break;
		case patch:
			ripServer.service.patch(path, templateRoute, templateEngine);
			break;
		case post:
			ripServer.service.post(path, templateRoute, templateEngine);
			break;
		case put:
			ripServer.service.put(path, templateRoute, templateEngine);
			break;
		case trace:
			ripServer.service.trace(path, templateRoute, templateEngine);
			break;
		case after:
		case afterafter:
		case before:
		case unsupported:
		default:
			LOG.error("A opção {} não é suportada!", getMethod());
			break;
		}
	}

	/**
	 * Configura o RipRoute para método delete no caminho <code>path</code>
	 *
	 * @param path o caminho da requisição http a ser criado
	 * @return um <code>RipResponseBuilder</code> para a construção da resposta
	 *         desse RipRoute
	 */
	public RipResponseBuilder delete(final String path) {
		return create(path, delete);
	}

	@Override
	public boolean equals(final Object object) {
		if (object instanceof RipRoute) {
			final RipRoute that = (RipRoute) object;
			return Objects.equals(ripServer, that.ripServer) && Objects.equals(method, that.method)
					&& Objects.equals(path, that.path);
		}
		return false;
	}

	/**
	 * Configura o RipRoute para método get no caminho <code>path</code>
	 *
	 * @param path o caminho da requisição http a ser criado
	 * @return um <code>RipResponseBuilder</code> para a construção da resposta
	 *         desse RipRoute
	 */
	public RipResponseBuilder get(final String path) {
		return create(path, get);
	}

	HttpMethod getMethod() {
		return method;
	}

	String getPath() {
		return path;
	}

	RipServer getRipServer() {
		return ripServer;
	}

	@Override
	public int hashCode() {
		return hash(ripServer, method, path);
	}

	/**
	 * Configura o RipRoute para método head no caminho <code>path</code>
	 *
	 * @param path o caminho da requisição http a ser criado
	 * @return um <code>RipResponseBuilder</code> para a construção da resposta
	 *         desse RipRoute
	 */
	public RipResponseBuilder head(final String path) {
		return create(path, head);
	}

	/**
	 * Configura o RipRoute para método options no caminho <code>path</code>
	 *
	 * @param path o caminho da requisição http a ser criado
	 * @return um <code>RipResponseBuilder</code> para a construção da resposta
	 *         desse RipRoute
	 */
	public RipResponseBuilder options(final String path) {
		return create(path, options);
	}

	/**
	 * Configura o RipRoute para método patch no caminho <code>path</code>
	 *
	 * @param path o caminho da requisição http a ser criado
	 * @return um <code>RipResponseBuilder</code> para a construção da resposta
	 *         desse RipRoute
	 */
	public RipResponseBuilder patch(final String path) {
		return create(path, patch);
	}

	/**
	 * Configura o RipRoute para método post no caminho <code>path</code>
	 *
	 * @param path o caminho da requisição http a ser criado
	 * @return um <code>RipResponseBuilder</code> para a construção da resposta
	 *         desse RipRoute
	 */
	public RipResponseBuilder post(final String path) {
		return create(path, post);
	}

	/**
	 * Configura o RipRoute para método put no caminho <code>path</code>
	 *
	 * @param path o caminho da requisição http a ser criado
	 * @return um <code>RipResponseBuilder</code> para a construção da resposta
	 *         desse RipRoute
	 */
	public RipResponseBuilder put(final String path) {
		return create(path, put);
	}

	/**
	 * Remove todas as configurações de rotas
	 */
	public void reset() {
		ripServer.reset();
	}

	@Override
	public String toString() {
		return String.format("RipRoute %s => %s %s]", ripServer, method, path);
	}

	/**
	 * Configura o RipRoute para método trace no caminho <code>path</code>
	 *
	 * @param path o caminho da requisição http a ser criado
	 * @return um <code>RipResponseBuilder</code> para a construção da resposta
	 *         desse RipRoute
	 */
	public RipResponseBuilder trace(final String path) {
		return create(path, trace);
	}

}