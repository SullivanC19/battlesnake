package com.battlesnake.montecarlo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static spark.Spark.*;

/**
 * random baseline strategy
 */
public class Snake {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final Handler HANDLER = new Handler();
    private static final Logger LOG = LoggerFactory.getLogger(Snake.class);

    public static void main(String[] args) {
        String port = System.getProperty("PORT");
        if (port == null) {
            LOG.info("Using default port: {}", port);
            port = "8080";
        } else {
            LOG.info("Found system provided port: {}", port);
        }
        port(Integer.parseInt(port));
        get("/", HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/start", HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/move", HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/end", HANDLER::process, JSON_MAPPER::writeValueAsString);
    }

    /**
     * Handler class for dealing with the routes set up in the main method.
     */
    public static class Handler {

        /**
         * For the start/end request
         */
        private static final Map<String, String> EMPTY = new HashMap<>();

        /**
         * Generic processor that prints out the request and response from the methods.
         *
         */
        public Map<String, String> process(Request req, Response res) {
            try {
                JsonNode parsedRequest = JSON_MAPPER.readTree(req.body());
                String uri = req.uri();
                LOG.info("{} called with: {}", uri, req.body());
                Map<String, String> snakeResponse;
                switch (uri) {
                    case "/":
                        snakeResponse = index();
                        break;
                    case "/start":
                        snakeResponse = start(parsedRequest);
                        break;
                    case "/move":
                        snakeResponse = move(parsedRequest);
                        break;
                    case "/end":
                        snakeResponse = end(parsedRequest);
                        break;
                    default:
                        throw new IllegalAccessError("Strange call made to the snake: " + uri);
                }

                LOG.info("Responding with: {}", JSON_MAPPER.writeValueAsString(snakeResponse));

                return snakeResponse;
            } catch (JsonProcessingException e) {
                LOG.warn("Something went wrong!", e);
                return null;
            }
        }

        public static Properties loadProperties() {
            Properties props = null;
            try {
                props = new Properties();
                InputStream is = Snake.class.getResourceAsStream("config.properties");
                props.load(is);
            } catch (Exception e) {
                LOG.error("Unable to read config file");
                System.exit(2);
            }

            return props;
        }

        /**
         * Set up snake
         *
         * @return a response back to the engine containing the Battlesnake setup values.
         */
        public Map<String, String> index() {
            Properties props = loadProperties();
            Map<String, String> response = new HashMap<>();
            response.put("apiversion",  props.getProperty("apiversion"));
            response.put("author",      props.getProperty("author"));
            response.put("color",       props.getProperty("color"));
            response.put("head",        props.getProperty("head"));
            response.put("tail",        props.getProperty("tail"));
            return response;
        }


        /**
         * Ask Strategy class to return move given compiled move request object and provide it as response
         *
         * @return response map with move
         */
        public Map<String, String> move(JsonNode moveRequestObj) {
            MoveRequest moveRequest = new MoveRequest(moveRequestObj);
            final String move = Strategy.move(moveRequest);
            Map<String, String> response = new HashMap<>();
            response.put("move", move);
            return response;
        }

        public Map<String, String> start(JsonNode startRequest) {
            Strategy.start();
            return EMPTY;
        }

        public Map<String, String> end(JsonNode endRequest) {
            Strategy.end();
            return EMPTY;
        }
    }

}
