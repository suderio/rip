package net.technearts.rip;

import static spark.route.HttpMethod.connect;
import static spark.route.HttpMethod.delete;
import static spark.route.HttpMethod.get;
import static spark.route.HttpMethod.head;
import static spark.route.HttpMethod.options;
import static spark.route.HttpMethod.patch;
import static spark.route.HttpMethod.post;
import static spark.route.HttpMethod.put;
import static spark.route.HttpMethod.trace;

import spark.route.HttpMethod;

/**
 * Uma rota (Verbo http + Caminho) associado a um servidor Rip
 */
public class RipRoute {
    private final RipServer ripServer;
    private HttpMethod method;
    private String path;

    RipRoute(final RipServer ripServer) {
        this.ripServer = ripServer;
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
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RipRoute other = (RipRoute) obj;
        if (method != other.method) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        if (ripServer == null) {
            if (other.ripServer != null) {
                return false;
            }
        } else if (!ripServer.equals(other.ripServer)) {
            return false;
        }
        return true;
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
        final int prime = 31;
        int result = 1;
        result = prime * result + (method == null ? 0 : method.hashCode());
        result = prime * result + (path == null ? 0 : path.hashCode());
        result = prime * result + (ripServer == null ? 0 : ripServer.hashCode());
        return result;
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
}