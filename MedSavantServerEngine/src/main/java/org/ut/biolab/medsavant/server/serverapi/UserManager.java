/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.server.serverapi;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.server.db.PooledConnection;
import org.ut.biolab.medsavant.shared.model.UserLevel;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.UserManagerAdapter;


/**
 *
 * @author mfiume
 */
public class UserManager extends MedSavantServerUnicastRemoteObject implements UserManagerAdapter {

    private static final Log LOG = LogFactory.getLog(UserManager.class);

    private static UserManager instance;

    public static synchronized UserManager getInstance() throws RemoteException, SessionExpiredException {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    private UserManager() throws RemoteException, SessionExpiredException {
    }
    
    @Override
    public String[] getUserNames(String sessID) throws SQLException, SessionExpiredException {

        List<String> results = new ArrayList<String>();
        ResultSet rs = ConnectionController.executePreparedQuery(sessID, "SELECT DISTINCT user FROM mysql.user");
        while (rs.next()) {
            results.add(rs.getString(1));
        }

        return results.toArray(new String[0]);
    }

    @Override
    public boolean userExists(String sessID, String user) throws SQLException, SessionExpiredException {
        return ConnectionController.executePreparedQuery(sessID, "SELECT user FROM mysql.user WHERE user=?;", user).next();
    }

    /**
     * Add a new user to MedSavant.
     *
     * @param sessID the session we're logged in as
     * @param user the user to add
     * @param pass the password
     * @param level the user's level
     * @throws SQLException
     */
    @Override
    public synchronized void addUser(String sessID, String user, char[] pass, UserLevel level) throws SQLException, SessionExpiredException {
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            // TODO: Transactions aren't supported for MyISAM, so this has no effect.
            conn.setAutoCommit(false);

            conn.executePreparedUpdate("CREATE USER ?@'localhost' IDENTIFIED BY ?", user, new String(pass));
            grantPrivileges(sessID, user, level);
            conn.commit();
        } catch (SQLException sqlx) {
            conn.rollback();
            throw sqlx;
        } finally {
            for (int i = 0; i < pass.length; i++) {
                pass[i] = 0;
            }
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    @Override
    public synchronized void changePassword(String sessID, String userName, char[] oldPass, char[] newPass) throws SQLException, RemoteException, SessionExpiredException {        
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {            
            conn.setAutoCommit(true);            
            
            //Check that old password is valid.
            ConnectionController.revalidate(userName, new String(oldPass), sessID);
                         
            
            //TODO: Check the new password against the current mysql password policy.                                                
            
            //Change the password
            conn.executePreparedUpdate("SET PASSWORD FOR ?@'localhost' = PASSWORD(?)", userName, new String(newPass));                                
        } finally {                        
            for(int i = 0; i < oldPass.length; ++i){
                oldPass[i] = 0;
            }
            for(int i = 0; i < newPass.length; ++i){
                newPass[i] = 0;
            }            
            conn.close();
        }
    }
    
    

    /**
     * Grant the user the privileges appropriate to their level
     * @param name user name from <code>mysql.user</code> table
     * @param level ADMIN, USER, or GUEST
     * @throws SQLException
     */
    @Override
    public void grantPrivileges(String sessID, String name, UserLevel level) throws SQLException, SessionExpiredException {
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            String dbName = ConnectionController.getDBName(sessID);
            LOG.info("Granting " + level + " privileges to " + name + " on " + dbName + "...");
            switch (level) {
                case ADMIN:
                    conn.executePreparedUpdate("GRANT ALTER, CREATE, CREATE TEMPORARY TABLES, CREATE USER, DELETE, DROP, FILE, GRANT OPTION, INSERT, SELECT, UPDATE ON *.* TO ?@'localhost'", name);
                    conn.executePreparedUpdate(String.format("GRANT GRANT OPTION ON %s.* TO ?@'localhost'", dbName), name);
                    conn.executePreparedUpdate(String.format("GRANT ALTER, CREATE, CREATE TEMPORARY TABLES, DELETE, DROP, INSERT, SELECT, UPDATE ON %s.* TO ?@'localhost'", dbName), name);
                    conn.executePreparedUpdate("GRANT SELECT ON mysql.user TO ?@'localhost'", name);
                    conn.executePreparedUpdate("GRANT SELECT ON mysql.db TO ?@'localhost'", name);
                    break;
                case USER:
                    conn.executePreparedUpdate(String.format("GRANT CREATE TEMPORARY TABLES, SELECT ON %s.* TO ?@'localhost'", dbName), name);
                    
                    //grant read/write/delete on region sets.
                    conn.executePreparedUpdate(String.format("GRANT SELECT,INSERT,UPDATE,DELETE ON %s.region_set TO ?@'localhost'", dbName), name);
                    conn.executePreparedUpdate(String.format("GRANT SELECT,INSERT,UPDATE,DELETE ON %s.region_set_membership TO ?@'localhost'", dbName), name);
                    
                    //Grant read/write/delete on cohorts.
                    conn.executePreparedUpdate(String.format("GRANT INSERT,SELECT,UPDATE,DELETE ON %s.cohort TO ?@'localhost'", dbName), name);
                    conn.executePreparedUpdate(String.format("GRANT INSERT,SELECT,UPDATE,DELETE ON %s.cohort_membership TO ?@'localhost'", dbName), name);
                    
                    conn.executePreparedUpdate("GRANT SELECT (user, Create_user_priv) ON mysql.user TO ?@'localhost'", name);
                    conn.executePreparedUpdate("GRANT SELECT (user, Create_tmp_table_priv) ON mysql.db TO ?@'localhost'", name);                    
                    conn.executePreparedUpdate("GRANT FILE ON *.* TO ?@'localhost'", name);                    
                    break;
                case GUEST:
                    conn.executePreparedUpdate(String.format("GRANT SELECT ON %s.* TO ?@'localhost'", dbName), name);
                    conn.executePreparedUpdate("GRANT SELECT (user, Create_user_priv) ON mysql.user TO ?@'localhost'", name);
                    conn.executePreparedUpdate("GRANT SELECT (user, Create_tmp_table_priv) ON mysql.db TO ?@'localhost'", name);
                    conn.executePreparedUpdate("GRANT FILE ON *.* TO ?@'localhost'", name);                    

                    break;
            }
            LOG.info("... granted.");
        } finally {
            conn.executeQuery("FLUSH PRIVILEGES");
            conn.close();
        }
    }

    @Override
    public UserLevel getUserLevel(String sessID, String name) throws SQLException, SessionExpiredException {
        if (userExists(sessID, name)) {
            // If the user can create other users, they're assumed to be admin.
            PooledConnection conn = ConnectionController.connectPooled(sessID);
            try {
                ResultSet rs = conn.executePreparedQuery("SELECT Create_user_priv FROM mysql.user WHERE user=?", name);
                if (rs.next()) {
                    if (rs.getString(1).equals("Y")) {
                        return UserLevel.ADMIN;
                    }
                }
                rs = conn.executePreparedQuery("SELECT Create_tmp_table_priv FROM mysql.db WHERE user=?", name);
                if (rs.next()) {
                    if (rs.getString(1).equals("Y")) {
                        return UserLevel.USER;
                    }
                }
            } finally {
                conn.close();
            }
            return UserLevel.GUEST;
        }
        return UserLevel.NONE;
    }

    @Override
    public void removeUser(String sid, String name) throws SQLException, SessionExpiredException {
        PooledConnection conn = ConnectionController.connectPooled(sid);
        conn.executePreparedUpdate("DROP USER ?@'localhost'", name);
        
        conn.executeQuery("FLUSH PRIVILEGES");        
    }
}
