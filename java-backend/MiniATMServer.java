import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class MiniATMServer {
    private static final String ACCOUNT = "1234567890";
    private static final String PIN = "1234";
    private static final Path HTML = Path.of(
        "/home/sachin/Mini-ATM-DevOps-POC/browser-frontend/index.html"
    );

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(
            new InetSocketAddress("127.0.0.1", 8080), 0
        );

        server.createContext("/api/health", exchange ->
            sendJson(exchange, 200,
                "{\"status\":\"running\",\"application\":\"Mini ATM\"}")
        );

        server.createContext("/api/login", exchange -> {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendJson(exchange, 405,
                    "{\"success\":false,\"message\":\"Use POST\"}");
                return;
            }

            String body = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
            );
            Map<String, String> form = new HashMap<>();

            for (String pair : body.split("&")) {
                String[] parts = pair.split("=", 2);
                String key = URLDecoder.decode(
                    parts[0], StandardCharsets.UTF_8
                );
                String value = parts.length > 1
                    ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
                form.put(key, value);
            }

            boolean valid =
                ACCOUNT.equals(form.getOrDefault("account", "")) &&
                PIN.equals(form.getOrDefault("pin", ""));

            if (valid) {
                sendJson(exchange, 200,
                    "{\"success\":true,\"message\":\"Login successful\"}");
            } else {
                sendJson(exchange, 401,
                    "{\"success\":false,\"message\":\"Invalid account or PIN\"}");
            }
        });

        server.createContext("/", exchange -> {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"message\":\"Use GET\"}");
                return;
            }
            try {
                byte[] html = Files.readAllBytes(HTML);
                exchange.getResponseHeaders().set(
                    "Content-Type", "text/html; charset=UTF-8"
                );
                exchange.sendResponseHeaders(200, html.length);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(html);
                }
            } catch (IOException e) {
                sendJson(exchange, 500,
                    "{\"message\":\"Could not load frontend\"}");
            }
        });

        server.start();
        System.out.println("Mini ATM frontend and API on port 8080");
    }

    private static void sendJson(
        HttpExchange exchange, int status, String response
    ) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set(
            "Content-Type", "application/json; charset=UTF-8"
        );
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
