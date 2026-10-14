import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class MiniATMServer {
    private static final String DB = System.getenv().getOrDefault(
        "ATM_DB_PATH",
        "/home/sachin/Mini-ATM-DevOps-POC/atm.db"
    );
    private static final Path HTML = Path.of(
        System.getenv().getOrDefault("ATM_HTML_PATH", "/app/browser-frontend/index.html")
    );

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(
            new InetSocketAddress("0.0.0.0", 8080), 0
        );

        server.createContext("/api/health", e ->
            sendJson(e, 200, "{\"status\":\"running\"}")
        );

        server.createContext("/api/login", e -> {
            if (!"POST".equals(e.getRequestMethod())) {
                sendJson(e, 405, "{\"message\":\"Use POST\"}");
                return;
            }
            try {
                Map<String,String> f = form(e);
                try (Connection c = db();
                     PreparedStatement p = c.prepareStatement(
                         "SELECT 1 FROM accounts WHERE account_number=? AND pin=?")) {
                    p.setString(1, f.getOrDefault("account", ""));
                    p.setString(2, f.getOrDefault("pin", ""));
                    try (ResultSet r = p.executeQuery()) {
                        if (r.next())
                            sendJson(e, 200, "{\"success\":true}");
                        else
                            sendJson(e, 401, "{\"success\":false}");
                    }
                }
            } catch (Exception ex) {
                error(e, ex);
            }
        });

        server.createContext("/api/balance", e -> {
            if (!"GET".equals(e.getRequestMethod())) {
                sendJson(e, 405, "{\"message\":\"Use GET\"}");
                return;
            }
            try {
                String account = query(e).getOrDefault("account", "");
                try (Connection c = db();
                     PreparedStatement p = c.prepareStatement(
                         "SELECT balance FROM accounts WHERE account_number=?")) {
                    p.setString(1, account);
                    try (ResultSet r = p.executeQuery()) {
                        if (r.next())
                            sendJson(e, 200, "{\"balance\":" + r.getLong(1) + "}");
                        else
                            sendJson(e, 404, "{\"message\":\"Account not found\"}");
                    }
                }
            } catch (Exception ex) {
                error(e, ex);
            }
        });

        server.createContext("/api/deposit", e -> transaction(e, true));
        server.createContext("/api/withdraw", e -> transaction(e, false));

        server.createContext("/api/statement", e -> {
            if (!"GET".equals(e.getRequestMethod())) {
                sendJson(e, 405, "{\"message\":\"Use GET\"}");
                return;
            }
            try {
                String account = query(e).getOrDefault("account", "");
                StringBuilder out = new StringBuilder("{\"transactions\":[");
                try (Connection c = db();
                     PreparedStatement p = c.prepareStatement(
                         "SELECT transaction_type,amount,transaction_time " +
                         "FROM transactions WHERE account_number=? ORDER BY id DESC LIMIT 10")) {
                    p.setString(1, account);
                    try (ResultSet r = p.executeQuery()) {
                        boolean first = true;
                        while (r.next()) {
                            if (!first) out.append(",");
                            first = false;
                            out.append("{\"type\":\"")
                               .append(r.getString(1))
                               .append("\",\"amount\":").append(r.getLong(2))
                               .append(",\"time\":\"").append(r.getString(3)).append("\"}");
                        }
                    }
                }
                out.append("]}");
                sendJson(e, 200, out.toString());
            } catch (Exception ex) {
                error(e, ex);
            }
        });

        server.createContext("/", e -> {
            if (!"GET".equals(e.getRequestMethod())) {
                sendJson(e, 405, "{\"message\":\"Use GET\"}");
                return;
            }
            try {
                byte[] html = Files.readAllBytes(HTML);
                e.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                e.sendResponseHeaders(200, html.length);
                try (OutputStream o = e.getResponseBody()) {
                    o.write(html);
                }
            } catch (IOException ex) {
                sendJson(e, 500, "{\"message\":\"Could not load frontend\"}");
            }
        });

        server.start();
        System.out.println("Mini ATM with SQLite started on port 8080");
    }

    private static Connection db() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + DB);
    }

    private static void transaction(HttpExchange e, boolean deposit) throws IOException {
        if (!"POST".equals(e.getRequestMethod())) {
            sendJson(e, 405, "{\"message\":\"Use POST\"}");
            return;
        }
        try {
            Map<String,String> f = form(e);
            String account = f.getOrDefault("account", "");
            long amount = Long.parseLong(f.getOrDefault("amount", "0"));
            if (amount <= 0) {
                sendJson(e, 400, "{\"message\":\"Amount must be positive whole rupees\"}");
                return;
            }

            try (Connection c = db()) {
                c.setAutoCommit(false);
                try {
                    long balance;
                    try (PreparedStatement p = c.prepareStatement(
                        "SELECT balance FROM accounts WHERE account_number=?")) {
                        p.setString(1, account);
                        try (ResultSet r = p.executeQuery()) {
                            if (!r.next()) {
                                c.rollback();
                                sendJson(e, 404, "{\"message\":\"Account not found\"}");
                                return;
                            }
                            balance = r.getLong(1);
                        }
                    }

                    if (!deposit && amount > balance) {
                        c.rollback();
                        sendJson(e, 400, "{\"message\":\"Insufficient balance\"}");
                        return;
                    }

                    long newBalance = deposit ? balance + amount : balance - amount;
                    try (PreparedStatement p = c.prepareStatement(
                        "UPDATE accounts SET balance=? WHERE account_number=?")) {
                        p.setLong(1, newBalance);
                        p.setString(2, account);
                        p.executeUpdate();
                    }
                    try (PreparedStatement p = c.prepareStatement(
                        "INSERT INTO transactions(account_number,transaction_type,amount) VALUES(?,?,?)")) {
                        p.setString(1, account);
                        p.setString(2, deposit ? "DEPOSIT" : "WITHDRAW");
                        p.setLong(3, amount);
                        p.executeUpdate();
                    }
                    c.commit();
                    sendJson(e, 200, "{\"success\":true,\"balance\":" + newBalance + "}");
                } catch (Exception ex) {
                    c.rollback();
                    throw ex;
                }
            }
        } catch (Exception ex) {
            error(e, ex);
        }
    }

    private static Map<String,String> form(HttpExchange e) throws IOException {
        return parse(new String(e.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
    }

    private static Map<String,String> query(HttpExchange e) {
        return parse(e.getRequestURI().getRawQuery() == null ? "" :
                     e.getRequestURI().getRawQuery());
    }

    private static Map<String,String> parse(String body) {
        Map<String,String> m = new HashMap<>();
        for (String pair : body.split("&")) {
            if (pair.isEmpty()) continue;
            String[] parts = pair.split("=", 2);
            String k = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String v = parts.length > 1 ?
                URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            m.put(k, v);
        }
        return m;
    }

    private static void error(HttpExchange e, Exception ex) throws IOException {
        ex.printStackTrace();
        sendJson(e, 500, "{\"message\":\"Database operation failed\"}");
    }

    private static void sendJson(HttpExchange e, int status, String response)
        throws IOException {
        byte[] b = response.getBytes(StandardCharsets.UTF_8);
        e.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        e.getResponseHeaders().set("Cache-Control", "no-store");
        e.sendResponseHeaders(status, b.length);
        try (OutputStream o = e.getResponseBody()) {
            o.write(b);
        }
    }
}
