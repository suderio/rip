package net.technearts.rip;

import static spark.Service.ignite;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Service;

/**
 * Servidor Rip rodando em um Jetty, para criação de serviços rest (ou qualquer requisição http) a serem usados para
 * mocks de serviços reais. Os serviços respondem com base apenas no conteúdo do body da requisição.
 */
public class RipServer {
    private static final Logger logger = LoggerFactory.getLogger(RipServer.class);
    private static final Map<Integer, RipServer> instance = new HashMap<>();

    /**
     * Cria um RipServer na porta padrão (7777)
     *
     * @return Um RipRoute para criação das rotas
     */
    public static RipRoute localhost() {
        return localhost(7777);
    }

    /**
     * Cria um RipServer na porta <code>port</code>
     *
     * @param port
     *            a porta do servidor
     * @return Um RipRoute para criação das rotas
     */
    public static RipRoute localhost(final int port) {
        return localhost(port, "/");
    }

    /**
     * Cria um RipServer na porta <code>port</code>
     *
     * @param port
     *            a porta do servidor
     * @param location
     *            o local dos arquivos
     * @return Um RipRoute para criação das rotas
     */
    public static RipRoute localhost(final int port, final String location) {
        if (!instance.containsKey(port)) {
            logger.debug("Criando servidor local na porta {}", port);
            instance.put(port, new RipServer(port, location));
        }
        return new RipRoute(instance.get(port));
    }

    /**
     * Para o servidor na porta <code>port</code>
     *
     * @param port
     *            a porta do servidor
     */
    public static void stop(final int port) {
        if (!instance.containsKey(port)) {
            instance.get(port).service.stop();
        }
    }

    /**
     * Transforma um caminho de arquivo informado em um objeto <code>Path</code>
     *
     * @param fileName
     *            o caminho/nome do arquivo, com raiz em src/main/resources
     * @return Um <code>Path</code> para o arquivo
     */
    public static Path withFile(final String fileName) {
        try {
            return Paths.get(RipServer.class.getResource(fileName).toURI());
        } catch (final URISyntaxException e) {
            return null;
        }
    }

    private final int port;
    final Service service;

    private RipServer(final int port, final String location) {
        this.port = port;
        service = ignite();
        service.port(port);
        service.staticFiles.location(location);
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
        final RipServer other = (RipServer) obj;
        return port == other.port;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + port;
        return result;
    }

    @Override
    public String toString() {
        return String.format("RipServer %s", port);
    }
}
