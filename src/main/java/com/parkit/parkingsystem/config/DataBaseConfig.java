package com.parkit.parkingsystem.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class DataBaseConfig {

    /**This class create the Logger.
     * and prepare and open the connection to the prod database
     * and closed the statement and the connection to the prod database
     * */

    private static final Logger logger = LogManager.getLogger("DataBaseConfig");

    public Connection getConnection() throws ClassNotFoundException, SQLException {
        logger.info("Create DB connection");
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/prod?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Europe/Paris", "root", "rootroot");
    }

    /**
     *
     * @param con
     * @throws logger.error if connection can't close
     */
    @SuppressWarnings("checkstyle:FinalParameters")
    public void closeConnection(Connection con) {
        if (con != null) {
            try {
                con.close();
                logger.info("Closing DB connection");
            } catch (SQLException e) {
                logger.error("Error while closing connection", e);
            }
        }
    }
    /**
     *
     * @param PreparedStatement ps
     * @throws logger.error if connection can't close
     */
    @SuppressWarnings({"checkstyle:FinalParameters", "checkstyle:JavadocMethod"})
    public void closePreparedStatement(PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
                logger.info("Closing Prepared Statement");
            } catch (SQLException e) {
                logger.error("Error while closing prepared statement", e);
            }
        }
    }
    /**
     *
     * @param ResultSet rs
     * @throws logger.error if connection can't close
     */
    @SuppressWarnings({"checkstyle:FinalParameters", "checkstyle:JavadocMethod"})
    public void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
                logger.info("Closing Result Set");
            } catch (SQLException e) {
                logger.error("Error while closing result set", e);
            }
        }
    }
}
