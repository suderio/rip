package net.technearts.rip;

import static spark.Spark.stop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.servlet.SparkApplication;

public abstract class RipWebApp implements SparkApplication {
    private static final Logger logger = LoggerFactory.getLogger(RipWebApp.class);

    @Override
    public void init() {
        logger.warn("#####################################################");
        logger.warn("###                                               ###");
        logger.warn("###              Iniciando RipWebApp              ###");
        logger.warn("###                                               ###");
        logger.warn("#####################################################");
        try {
            setup();
        } catch (final Exception e) {
            logger.warn("#####################################################");
            logger.warn("RipWebApp não iniciou corretamente");
            logger.warn("Erro: {}", e.getMessage());
            logger.warn("#####################################################");
            stop();
            destroy();
        }
        logger.warn("#####################################################");
        logger.warn("###                                               ###");
        logger.warn("###               RipWebApp Iniciado              ###");
        logger.warn("###                                               ###");
        logger.warn("#####################################################");
    }

    public abstract void setup() throws Exception;
}