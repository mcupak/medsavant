/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.shard.nonshard;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Helpers for direct database access.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class NonShardDBUtils {

    private static final String SHOW_DB_QUERY = "SHOW DATABASES";
    private static final String SHOW_TABLES_QUERY = "SHOW TABLES";
    public static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

    /**
     * Retrieve the list of databases on the given server.
     * 
     * @param url
     *            connection URL to the server
     * @param user
     *            user name
     * @param password
     *            password
     * @return list of databases
     */
    public static List<String> getDatabases(String url, String user, String password) {
        List<String> res = new ArrayList<String>();
        PreparedStatement s = null;
        ResultSet r = null;

        try {
            Class.forName(JDBC_DRIVER);
            Connection conn = DriverManager.getConnection(url, user, password);

            s = conn.prepareStatement(SHOW_DB_QUERY);
            r = s.executeQuery();

            while (r.next()) {
                res.add(r.getString(1));
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Driver not loaded: " + JDBC_DRIVER);
        } catch (SQLException e) {
            System.err.println("Error connecting to: " + url);
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close the statement.");
                }
            }
            if (r != null) {
                try {
                    r.close();
                } catch (SQLException e) {
                    System.err.println("Resultset could not be closed.");
                }
            }
        }
        return res;
    }

    /**
     * Retrieves the list of tables in the given database.
     * 
     * @param url
     *            database connection URL
     * @param user
     *            user name
     * @param password
     *            password
     * @return list of tables
     */
    public static List<String> getTables(String url, String user, String password) {
        List<String> res = new ArrayList<String>();
        PreparedStatement s = null;
        ResultSet r = null;

        try {
            Class.forName(JDBC_DRIVER);
            Connection conn = DriverManager.getConnection(url, user, password);

            s = conn.prepareStatement(SHOW_TABLES_QUERY);
            r = s.executeQuery();

            while (r.next()) {
                res.add(r.getString(1));
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Driver not loaded: " + JDBC_DRIVER);
        } catch (SQLException e) {
            System.err.println("Error connecting to: " + url);
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close the statement.");
                }
            }
            if (r != null) {
                try {
                    r.close();
                } catch (SQLException e) {
                    System.err.println("Resultset could not be closed.");
                }
            }
        }
        return res;
    }
}
