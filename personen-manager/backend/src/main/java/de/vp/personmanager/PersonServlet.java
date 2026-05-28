package de.vp.personmanager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

/**
 * Central servlet that handles operations of Data forwarded by front end:
 *
 *   POST /api/person          — add a single person (JSON body from add-person.html)
 *   GET  /api/search          — search persons   (?q=... from search.html)
 *   POST /api/delete          — delete a person  (JSON body: {"id": ...} from search.html)
 *
 * Database connection details are read from servlet init-params defined in web.xml.
 * Responses are plain JSON.
 */
@MultipartConfig  // Required for reading multipart/form-data file uploads
public class PersonServlet extends HttpServlet {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // Single DAO instance — owns the HikariCP pool for the lifetime of this servlet
    private PersonDAO personDAO;

    
    // LIFECYCLE

    @Override
    public void init() throws ServletException {
        String dbUrl      = getInitParameter("dbUrl");
        String dbUser     = getInitParameter("dbUser");
        String dbPassword = getInitParameter("dbPassword");

        if (dbUrl == null || dbUser == null || dbPassword == null) {
            throw new ServletException(
            		"Missing required init-params: dbUrl, dbUser, dbPassword."
            		);
        }

        try {
            personDAO = new PersonDAO(dbUrl, dbUser, dbPassword);
        } catch (Exception e) {
            throw new ServletException("Failed to initialize database connection pool: "
                + e.getMessage(), e);
        }
    }

    @Override
    public void destroy() {
        // Shut the connection pool down cleanly when Tomcat undeploys the app
        if (personDAO != null) {
            personDAO.shutdown();
        }
    }

    
    // ROUTING

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getServletPath();

        if ("/api/search".equals(path)) {
            handleSearch(req, resp);
        } else {
            sendError(resp, 405, "Method not allowed for " + path);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getServletPath();

        switch (path) {
            case "/api/person":
                handleAddPerson(req, resp);
                break;
            case "/api/delete":
                handleDeletePerson(req, resp);
                break;
            default:
                sendError(resp, 404, "Unknown endpoint: " + path);
        }
    }

    
    // HANDLERS

    /**
     * POST /api/person
     * Body: JSON object with fields name, lastname, street, postalCode, city, country
     */
    private void handleAddPerson(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String body = readBody(req);

        Person person;
        try {
            person = parseSinglePerson(body);
        } catch (IllegalArgumentException e) {
            sendError(resp, 400, "Invalid request body: " + e.getMessage());
            return;
        }

        try {
            personDAO.addPerson(person);
            sendJson(resp, 200, "{\"status\":\"ok\",\"message\":\"Person added.\"}");
        } catch (SQLException e) {
            sendError(resp, 500, "Database error: " + e.getMessage());
        }
    }

    /**
     * GET /api/search?q=term1,term2
     * Returns a JSON array of matching Person objects.
     */
    private void handleSearch(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String q = req.getParameter("q");
        if (q == null || q.trim().isEmpty()) {
            sendError(resp, 400, "Missing query parameter: q");
            return;
        }

        try {
            List<Person> results = personDAO.search(q);
            sendJson(resp, 200, MAPPER.writeValueAsString(results));
        } catch (SQLException e) {
            sendError(resp, 500, "Database error: " + e.getMessage());
        }
    }


    /**
     * POST /api/delete
     * Body: JSON object with field id
     */
    private void handleDeletePerson(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String body = readBody(req);
        JsonNode node;
        try {
            node = MAPPER.readTree(body);
        } catch (IOException e) {
            sendError(resp, 400, "Invalid JSON body.");
            return;
        }

        JsonNode idNode = node.get("id");
        if (idNode == null || idNode.isNull() || !idNode.isInt()) {
            sendError(resp, 400, "Missing or invalid field: id");
            return;
        }

        int id = idNode.asInt();

        try {
            personDAO.deletePerson(id);
            sendJson(resp, 200, "{\"status\":\"ok\",\"message\":\"Person deleted.\"}");
        } catch (SQLException e) {
            sendError(resp, 500, "Database error: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Parsing helpers
    // -------------------------------------------------------------------------

    /**
     * Parses a JSON object string into a single Person.
     * Throws IllegalArgumentException if any required field is missing or blank.
     */
    private Person parseSinglePerson(String json) throws IOException {
        JsonNode node = MAPPER.readTree(json);
        return new Person(
            requireField(node, "name"),
            requireField(node, "lastname"),
            requireField(node, "street"),
            requireField(node, "postalCode"),
            requireField(node, "city"),
            requireField(node, "country")
        );
    }

    
    /**
     * Extracts a required, non-blank string field from a JSON node.
     */
    private String requireField(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || value.asText().trim().isEmpty()) {
            throw new IllegalArgumentException("Missing or empty field: " + field);
        }
        return value.asText().trim();
    }

    // -------------------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------------------

    private String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private void sendJson(HttpServletResponse resp, int status, String json) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json);
    }

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        sendJson(resp, status,
            "{\"status\":\"error\",\"message\":" + MAPPER.writeValueAsString(message) + "}");
    }
}
