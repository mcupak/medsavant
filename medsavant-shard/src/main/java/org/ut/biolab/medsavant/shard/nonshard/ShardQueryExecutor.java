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
import java.util.concurrent.Callable;

import org.hibernate.shards.cfg.ShardConfiguration;

/**
 * Thread executing a query on a shard.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class ShardQueryExecutor implements Callable<Object> {

    private ShardConfiguration config;
    private String query = "";
    private QueryType qType;
    private static Connection conn = null;

    public ShardQueryExecutor(ShardConfiguration config, String query, QueryType qType) {
        this.config = config;
        this.query = query;
        this.qType = qType;
    }

    /**
     * Obtains a connection to the database and executes a query.
     * 
     * @return query result if available, shard id otherwise
     * 
     */
    public Object call() {
        Object res = null;

        switch (qType) {
        case SELECT:
            connect(true);
            res = executeSelect();
            break;
        case UPDATE:
            connect(true);
            executeUpdate();
            res = config.getShardId();
            break;
        case SELECT_WITHOUT_RESULT:
            connect(true);
            executeSelectWithoutResult();
            res = config.getShardId();
            break;
        case DB:
            // database does not exist yet, we don't want to connect to it
            connect(false);
            executeUpdate();
            res = config.getShardId();
            break;
        }

        disconnect();

        return res;
    }

    /**
     * Executes a query with return value.
     * 
     * @return result
     */
    private Object executeSelect() {
        Object res = null;

        PreparedStatement s = null;
        try {
            s = conn.prepareStatement(query);
        } catch (SQLException e) {
            System.err.println("Failed to create query.");
        }

        ResultSet r = null;
        try {
            r = s.executeQuery();

            // return first result
            r.next();
            res = r.getObject(0);
        } catch (SQLException e) {
            System.err.println("Failed to execute query.");
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
     * Executes a query without waiting for the return value.
     * 
     */
    private void executeSelectWithoutResult() {
        PreparedStatement s = null;
        try {
            s = conn.prepareStatement(query);
        } catch (SQLException e) {
            System.err.println("Failed to create query.");
        }

        try {
            s.executeQuery();
        } catch (SQLException e) {
            System.err.println("Failed to execute query.");
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close the statement.");
                }
            }
        }
    }

    /**
     * Executes a query without a return value.
     * 
     */
    private void executeUpdate() {
        try {
            conn.createStatement().executeUpdate(query);
        } catch (SQLException e) {
            System.err.println("Failed to execute update.");
        }
    }

    /**
     * Obtains connection.
     */
    private void connect(boolean connectToDB) {
        try {
            Class.forName(NonShardDBUtils.JDBC_DRIVER);
            conn = DriverManager.getConnection(connectToDB ? config.getShardUrl() : ShardConfigurationUtil.getServerForShard(config.getShardId()), config.getShardUser(),
                    config.getShardPassword());
        } catch (ClassNotFoundException e) {
            System.err.println("Driver not loaded: " + NonShardDBUtils.JDBC_DRIVER);
        } catch (SQLException e) {
            System.err.println("Error connecting to shard: " + config.getShardUrl());
        }
    }

    /**
     * Closes the connection.
     */
    private void disconnect() {
        try {
            if (conn != null)
                conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }
}
