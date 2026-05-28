package de.vp.personmanager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.*;

/**
 * Data Access Object for Person.
 * Manages its own HikariCP connection pool.

 *
 * The table inside mySQL:
 *
 *   CREATE TABLE person (
 *       id         INT          AUTO_INCREMENT PRIMARY KEY,
 *       name       VARCHAR(100) NOT NULL,
 *       lastname   VARCHAR(100) NOT NULL,
 *       street     VARCHAR(200) NOT NULL,
 *       postalCode VARCHAR(20)  NOT NULL,
 *       city       VARCHAR(100) NOT NULL,
 *       country    VARCHAR(100) NOT NULL,
 *       CONSTRAINT uq_person UNIQUE (name, lastname, street)
 *   );
 */
public class PersonDAO {

    private final HikariDataSource dataSource;


    // Establishing connection pool
	public PersonDAO(String jdbcUrl, String user, String password) {

		HikariConfig config = new HikariConfig();

		config.setJdbcUrl(jdbcUrl);
		config.setUsername(user);
		config.setPassword(password);

		config.setDriverClassName("com.mysql.cj.jdbc.Driver");

		config.setMaximumPoolSize(10);
		config.setMinimumIdle(2);
		config.setConnectionTimeout(30000);
		config.setIdleTimeout(600000);
		config.setMaxLifetime(1800000);

		config.setPoolName("PersonDAOPool");

		this.dataSource = new HikariDataSource(config);
	}

	// CLosing Connection Pool
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    // ADD SINGLE PERSON (called from add-person.html)
    /**
     * Inserts one Person inside a transaction.
     * Rolls back and throws if anything goes wrong.
     */
    public void addPerson(Person person) throws SQLException {
        final String SQL =
            "INSERT INTO person (name, lastname, street, postalCode, city, country) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(SQL)) {

                stmt.setString(1, person.getName());
                stmt.setString(2, person.getLastname());
                stmt.setString(3, person.getStreet());
                stmt.setString(4, person.getPostalCode());
                stmt.setString(5, person.getCity());
                stmt.setString(6, person.getCountry());

                stmt.executeUpdate();
                conn.commit();

            } catch (SQLException e) {
                try { conn.rollback(); } catch (SQLException ex) { e.addSuppressed(ex); }
                throw e;
            }
        }
    }

    // SEARCH (called from search.html)

    /**
     * Searches all text columns using LIKE.
     *
     * The query string may contain multiple terms,( z.B. "Max, Mustermann").
     * Each term produces a separate group of LIKE conditions joined by OR,
     * and all groups are joined by AND — i.e. every term must match at least
     * one column.
     *
     * Single term example:
     *   WHERE (name LIKE ? OR lastname LIKE ? OR street LIKE ? OR ...)
     *
     * Two terms example:
     *   WHERE (name LIKE ? OR lastname LIKE ? OR ...)
     *     AND (name LIKE ? OR lastname LIKE ? OR ...)
     */
    public List<Person> search(String queryString) throws SQLException {

        // Split on commas; trim whitespace from each term
        String[] rawTerms = queryString.split(",");
        List<String> terms = new ArrayList<>();
        for (String t : rawTerms) {
            String trimmed = t.trim();
            if (!trimmed.isEmpty()) {
                terms.add("%" + trimmed + "%");
            }
        }

        if (terms.isEmpty()) {
            return Collections.emptyList();
        }

        // Columns to search across — must match the table definition
        String[] columns = {"name", "lastname", "street", "postalCode", "city", "country"};

        // Build dynamic SQL
        StringBuilder sql = new StringBuilder(
            "SELECT id, name, lastname, street, postalCode, city, country FROM person WHERE ");

        List<String> termClauses = new ArrayList<>();
        for (int i = 0; i < terms.size(); i++) {
            StringBuilder termClause = new StringBuilder("(");
            for (int c = 0; c < columns.length; c++) {
                if (c > 0) termClause.append(" OR ");
                termClause.append(columns[c]).append(" LIKE ?");
            }
            termClause.append(")");
            termClauses.add(termClause.toString());
        }

        sql.append(String.join(" AND ", termClauses));

        List<Person> results = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // Bind parameters: for each term, bind once per column
            int paramIndex = 1;
            for (String term : terms) {
                for (int c = 0; c < columns.length; c++) {
                    stmt.setString(paramIndex++, term);
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Person p = new Person(
                        rs.getString("name"),
                        rs.getString("lastname"),
                        rs.getString("street"),
                        rs.getString("postalCode"),
                        rs.getString("city"),
                        rs.getString("country")
                    );
                    p.setId(rs.getInt("id"));
                    results.add(p);
                }
            }
        }

        return results;
    }

    // DELETE PERSON (called from search.html)
    /**
     * Deletes one Person by primary key inside a transaction.
     * Rolls back and throws if anything goes wrong.
     */
    public void deletePerson(int id) throws SQLException {
        final String SQL = "DELETE FROM person WHERE id = ?";

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(SQL)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
                conn.commit();

            } catch (SQLException e) {
                try { conn.rollback(); } catch (SQLException ex) { e.addSuppressed(ex); }
                throw e;
            }
        }
    }
}
