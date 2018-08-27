package net.technearts.rip;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static net.technearts.rip.OP.AND;
import static net.technearts.rip.OP.OR;
import static org.eclipse.jetty.http.HttpStatus.NOT_FOUND_404;
import static org.eclipse.jetty.http.HttpStatus.OK_200;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

enum OP {
  AND, OR;
}

/**
 * Um construtor de respostas com base no conteúdo do body da requisição http.
 */
public class RipResponseBuilder {
  // TODO passar os três mapas para dentro do RipRoute
  private static final Map<RipRoute, Map<Predicate<Request>, RipResponse>> CONDITIONS = new LinkedHashMap<>();
  private static final Map<RipRoute, BiFunction<Request, Response, String>> LOGS = new LinkedHashMap<>();
  private static final Logger LOG = LoggerFactory
      .getLogger(RipResponseBuilder.class);

  private RipRoute route;
  private Predicate<Request> condition;
  private OP op = AND;

  RipResponseBuilder(final RipRoute route) {
    LOG.info("Criando RipResponseBuilder para requisição {} {}",
        route.getMethod(), route.getPath());
    this.route = route;
    if (!CONDITIONS.containsKey(route)) {
      CONDITIONS.put(route,
          new LinkedHashMap<Predicate<Request>, RipResponse>());
    }
    route.route = (req, res) -> {
      final Optional<Map.Entry<Predicate<Request>, RipResponse>> optional = CONDITIONS
          .get(route).entrySet().stream()
          .filter(entry -> entry.getKey().test(req)).findFirst();
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
        LOG.warn("Resposta para {} {} não encontrada", route.getMethod(),
            route.getPath());
        result = "";
      }
      ofNullable(LOGS.get(route)).ifPresent(f -> f.apply(req, res));
      return result;
    };
    route.templateRoute = (req, res) -> {
      final Optional<Map.Entry<Predicate<Request>, RipResponse>> optional = CONDITIONS
          .get(route).entrySet().stream()
          .filter(entry -> entry.getKey().test(req)).findFirst();
      RipResponse response;
      ModelAndView result;
      if (optional.isPresent()) {
        response = optional.get().getValue();
        LOG.debug("Respondendo com \n{}", response.getContent());
        res.status(response.getStatus());
        final Map<String, Object> attributes = new HashMap<>();
        for (final Map.Entry<String, Function<Request, String>> f : response
            .getAttributes().entrySet()) {
          attributes.put(f.getKey(), f.getValue().apply(req));
        }
        result = new ModelAndView(attributes, response.getContent());
      } else {
        res.status(NOT_FOUND_404);
        LOG.debug("Resposta para {} {} não encontrada", route.getMethod(),
            route.getPath());
        result = null;
      }
      // res.header("Content-Type", contentType(result));
      return result;
    };
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
      final Consumer<Map<String, Function<Request, String>>>... consumers) {
    buildResponse(template, OK_200, consumers);
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
      final Consumer<Map<String, Function<Request, String>>>... consumers) {
    final Map<String, Function<Request, String>> attributes = new HashMap<>();
    for (final Consumer<Map<String, Function<Request, String>>> consumer : consumers) {
      consumer.accept(attributes);
    }
    buildResponse(template, status, attributes);
  }

  /**
   * Cria uma resposta utilizando um arquivo de template, substituindo as
   * variáveis no arquivo pelo resultado de cada aplicação da função.
   * 
   * @param template   o arquivo de template
   * @param status     o status de retorno
   * @param attributes lista de alterações ao mapa de variáveis X funções
   */
  public final void buildResponse(final String template, final int status,
      final Map<String, Function<Request, String>> attributes) {
    if (condition == null) {
      condition = s -> true;
    }
    final RipResponse res = new RipResponse(attributes, template, status);
    CONDITIONS.get(route).put(condition, res);
    route.createTemplateMethod();
  }

  /**
   * Cria uma resposta utilizando um arquivo de template, substituindo as
   * variáveis no arquivo pelo resultado de cada aplicação da função.
   * 
   * @param template   o arquivo de template
   * @param attributes lista de alterações ao mapa de variáveis X funções
   */
  public final void buildResponse(final String template,
      final Map<String, Function<Request, String>> attributes) {
    buildResponse(template, OK_200, attributes);
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
    final Predicate<Request> newCondition = req -> Arrays.asList(contents)
        .stream().allMatch(req.body()::contains);
    updateConditions(newCondition);
    return this;
  }

  /**
   * Verifica se o body da requisição http contém alguma das sequências
   * informadas
   *
   * @param contents os conteúdos a serem checados no body
   * @return this
   */
  public RipResponseBuilder containsAny(final String... contents) {
    final Predicate<Request> newCondition = req -> Arrays.asList(contents)
        .stream().anyMatch(req.body()::contains);
    updateConditions(newCondition);
    return this;
  }

  private String contentType(byte[] stream) {
    MagicMatch match = null;
    try {
      match = Magic.getMagicMatch(stream, false);
      if (match.getSubMatches().size() > 0) {
        return match.getSubMatches().toArray()[0].toString();
      }
    } catch (MagicParseException | MagicMatchNotFoundException | MagicException
        | NullPointerException e) {
      return "text/html;charset=utf-8";
    }
    return match.getMimeType();
  }

  /**
   * Verifica se o body da requisição http contém determinada sequência
   *
   * @param condition a condição a ser checada
   * @return this
   */
  public RipResponseBuilder matches(final Predicate<Request> condition) {
    updateConditions(condition);
    return this;
  }

  /**
   * Verifica se o body da requisição http contém todas as sequências informadas
   *
   * @param conditions as condições a serem checadas
   * @return this
   */
  public RipResponseBuilder matchesAll(
      @SuppressWarnings("unchecked") final Predicate<Request>... conditions) {
    final Predicate<Request> newCondition = asList(conditions).stream()
        .reduce(req -> true, Predicate::and);
    updateConditions(newCondition);
    return this;
  }

  /**
   * Verifica se o body da requisição http contém alguma das sequências
   * informadas
   *
   * @param conditions as condições a serem checadas
   * @return this
   */
  public RipResponseBuilder matchesAny(
      @SuppressWarnings("unchecked") final Predicate<Request>... conditions) {
    final Predicate<Request> newCondition = asList(conditions).stream()
        .reduce(req -> false, Predicate::or);
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
   * Cria um log dos objetos Request/Response na chamada ao Route. Várias
   * chamadas ao método apenas substituem o log criado anteriormente.
   * 
   * @param f a Função que irá retornar a mensagem de log
   * @return this
   */
  public RipResponseBuilder log(BiFunction<Request, Response, String> f) {
    LOGS.put(this.route, f);
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
    respond(withFile, status, null);
  }
  
  public void respond(final Path withFile, String contentType) {
    respond(withFile, OK_200, contentType);
  }
  
  public void respond(final Path withFile, final int status, String contentType) {
    try {
	  respond(new String(Files.readAllBytes(withFile)), status, contentType);
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
  
  public void respond(final String response, String contentType) {
	    respond(response, OK_200, contentType);
	  }

  /**
   * Cria uma resposta com o conteúdo do arquivo informado, retornando o
   * <code>status</code> http. Essa é uma operação terminal.
   *
   * @param response o conteúdo do corpo da mensagem de retorno
   * @param status   o status de retorno
   */
  public void respond(final String response, final int status) {
	respond(response, status, null);
  }
  
  public void respond(final String response, final int status, final String contentType) {
	if (condition == null) {
	  condition = s -> true;
	}
	final RipResponse res = new RipResponse(response, status, contentType);
	CONDITIONS.get(route).put(condition, res);
	route.createMethod();
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

}
