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
package org.ut.biolab.medsavant.shared.serverapi;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;


/**
 *
 * @author Andrew
 */
public interface SetupAdapter extends Remote {

    public void createDatabase(String dbHost, int port, String dbname, String adminName, char[] rootPassword, String versionString) throws IOException, SQLException, RemoteException, SessionExpiredException;

    public void removeDatabase(String dbHost, int port, String dbname, String adminName, char[] rootPassword) throws SQLException, RemoteException, SessionExpiredException;

    public String getServerVersion() throws RemoteException, SessionExpiredException;
}
