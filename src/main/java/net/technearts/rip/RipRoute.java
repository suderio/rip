package net.technearts.rip;

import static java.util.Objects.hash;
import static spark.route.HttpMethod.connect;
import static spark.route.HttpMethod.delete;
import static spark.route.HttpMethod.get;
import static spark.route.HttpMethod.head;
import static spark.route.HttpMethod.options;
import static spark.route.HttpMethod.patch;
import static spark.route.HttpMethod.post;
import static spark.route.HttpMethod.put;
import static spark.route.HttpMethod.trace;

import java.util.Objects;

import com.google.common.collect.ComparisonChain;

import spark.route.HttpMethod;

/**
 * Uma rota (Verbo http + Caminho) associado a um servidor Rip
 */
public class RipRoute implements Comparable<RipRoute>, AutoCloseable {
    private final RipServer ripServer;
    private HttpMethod method;
    private String path;

    RipRoute(final RipServer ripServer) {
        this.ripServer = ripServer;
    }

    @Override
    public int compareTo(final RipRoute that) {
        return ComparisonChain.start().compare(ripServer, that.ripServer).compare(method, that.method)
                .compare(path, that.path).result();
    }

    /**
     * Configura o RipRoute para método connect no caminho <code>path</code>
     *
     * @param path
     *            o caminho da requisição http a ser criado
     * @return um <code>RipResponseBuilder</code> para a construção da resposta desse RipRoute
     */
    public RipResponseBuilder connect(final String path) {
        return create(path, connect);
    }

    private RipResponseBuilder create(final String path, final HttpMethod method) {
        this.method = method;
        this.path = path;
        return new RipResponseBuilder(this);
    }

    /**
     * Configura o RipRoute para método delete no caminho <code>path</code>
     *
     * @param path
     *            o caminho da requisição http a ser criado
     * @return um <code>RipResponseBuilder</code> para a construção da resposta desse RipRoute
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
     * @param path
     *            o caminho da requisição http a ser criado
     * @return um <code>RipResponseBuilder</code> para a construção da resposta desse RipRoute
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
     * @param path
     *            o caminho da requisição http a ser criado
     * @return um <code>RipResponseBuilder</code> para a construção da resposta desse RipRoute
     */
    public RipResponseBuilder head(final String path) {
        return create(path, head);
    }

    /**
     * Configura o RipRoute para método options no caminho <code>path</code>
     *
     * @param path
     *            o caminho da requisição http a ser criado
     * @return um <code>RipResponseBuilder</code> para a construção da resposta desse RipRoute
     */
    public RipResponseBuilder options(final String path) {
        return create(path, options);
    }

    /**
     * Configura o RipRoute para método patch no caminho <code>path</code>
     *
     * @param path
     *            o caminho da requisição http a ser criado
     * @return um <code>RipResponseBuilder</code> para a construção da resposta desse RipRoute
     */
    public RipResponseBuilder patch(final String path) {
        return create(path, patch);
    }

    /**
     * Configura o RipRoute para método post no caminho <code>path</code>
     *
     * @param path
     *            o caminho da requisição http a ser criado
     * @return um <code>RipResponseBuilder</code> para a construção da resposta desse RipRoute
     */
    public RipResponseBuilder post(final String path) {
        return create(path, post);
    }

    /**
     * Configura o RipRoute para método put no caminho <code>path</code>
     *
     * @param path
     *            o caminho da requisição http a ser criado
     * @return um <code>RipResponseBuilder</code> para a construção da resposta desse RipRoute
     */
    public RipResponseBuilder put(final String path) {
        return create(path, put);
    }
    
    /**
     * Remove todas as configurações de rotas
     */
    public void reset() {
    	this.ripServer.reset();
    }
    
    @Override
    public String toString() {
        return String.format("RipRoute %s => %s %s]", ripServer, method, path);
    }

    /**
     * Configura o RipRoute para método trace no caminho <code>path</code>
     *
     * @param path
     *            o caminho da requisição http a ser criado
     * @return um <code>RipResponseBuilder</code> para a construção da resposta desse RipRoute
     */
    public RipResponseBuilder trace(final String path) {
        return create(path, trace);
    }

	@Override
	public void close() {
		//TODO
	}

}