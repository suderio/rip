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
    private RipServer ripServer;
    private HttpMethod method;
    private String path;

    RipRoute(RipServer ripServer) {
        this.ripServer = ripServer;
    }

    private RipResponseBuilder create(String path, HttpMethod method) {
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
    public RipResponseBuilder delete(String path) {
        return create(path, delete);
    }

    /**
     * Configura o RipRoute para método connect no caminho <code>path</code>
     * 
     * @param path
     *            o caminho da requisição http a ser criado
     * @return um <code>RipResponseBuilder</code> para a construção da resposta desse RipRoute
     */
    public RipResponseBuilder connect(String path) {
        return create(path, connect);
    }

    /**
     * Configura o RipRoute para método get no caminho <code>path</code>
     * 
     * @param path
     *            o caminho da requisição http a ser criado
     * @return um <code>RipResponseBuilder</code> para a construção da resposta desse RipRoute
     */
    public RipResponseBuilder get(String path) {
        return create(path, get);
    }

    /**
     * Configura o RipRoute para método head no caminho <code>path</code>
     * 
     * @param path
     *            o caminho da requisição http a ser criado
     * @return um <code>RipResponseBuilder</code> para a construção da resposta desse RipRoute
     */
    public RipResponseBuilder head(String path) {
        return create(path, head);
    }

    /**
     * Configura o RipRoute para método options no caminho <code>path</code>
     * 
     * @param path
     *            o caminho da requisição http a ser criado
     * @return um <code>RipResponseBuilder</code> para a construção da resposta desse RipRoute
     */
    public RipResponseBuilder options(String path) {
        return create(path, options);
    }

    /**
     * Configura o RipRoute para método patch no caminho <code>path</code>
     * 
     * @param path
     *            o caminho da requisição http a ser criado
     * @return um <code>RipResponseBuilder</code> para a construção da resposta desse RipRoute
     */
    public RipResponseBuilder patch(String path) {
        return create(path, patch);
    }

    /**
     * Configura o RipRoute para método post no caminho <code>path</code>
     * 
     * @param path
     *            o caminho da requisição http a ser criado
     * @return um <code>RipResponseBuilder</code> para a construção da resposta desse RipRoute
     */
    public RipResponseBuilder post(String path) {
        return create(path, post);
    }

    /**
     * Configura o RipRoute para método put no caminho <code>path</code>
     * 
     * @param path
     *            o caminho da requisição http a ser criado
     * @return um <code>RipResponseBuilder</code> para a construção da resposta desse RipRoute
     */
    public RipResponseBuilder put(String path) {
        return create(path, put);
    }

    /**
     * Configura o RipRoute para método trace no caminho <code>path</code>
     * 
     * @param path
     *            o caminho da requisição http a ser criado
     * @return um <code>RipResponseBuilder</code> para a construção da resposta desse RipRoute
     */
    public RipResponseBuilder trace(String path) {
        return create(path, trace);
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
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RipRoute other = (RipRoute) obj;
        if (method != other.method)
            return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (ripServer == null) {
            if (other.ripServer != null)
                return false;
        } else if (!ripServer.equals(other.ripServer))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((ripServer == null) ? 0 : ripServer.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return String.format("RipRoute %s => %s %s]", ripServer, method, path);
    }
}