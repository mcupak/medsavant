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
package org.ut.biolab.medsavant.shard.server;

import org.hibernate.Session;
import org.ut.biolab.medsavant.shard.core.ShardedSessionManager;
import org.ut.biolab.medsavant.shard.nonshard.ShardedConnectionController;

/**
 * Helper class for setup of a sharded database.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class ShardedDatabaseSetupHelper {

    /**
     * Creates database shards.
     */
    public void createDatabase() {
        Session session = ShardedSessionManager.openSession();

        ShardedConnectionController.createShards();

        ShardedSessionManager.closeSession(session);
    }

    /**
     * Removes database shards.
     */
    public void removeDatabase() {
        Session session = ShardedSessionManager.openSession();

        ShardedConnectionController.dropShards();

        ShardedSessionManager.closeSession(session);
    }

    /**
     * Creates variant tables on shards.
     * 
     * @param query
     */
    public void createVariantTables(String query) {
        Session session = ShardedSessionManager.openSession();

        ShardedConnectionController.executeUpdateOnAllShards(query, false);

        ShardedSessionManager.closeSession(session);
    }

    /**
     * Removes variant tables from shards.
     * 
     * @param query
     *            drop table query
     */
    public void dropVariantTables(String tableName) {
        final String query = "DROP TABLE IF EXISTS " + tableName;

        Session session = ShardedSessionManager.openSession();

        ShardedConnectionController.executeUpdateOnAllShards(query, true);

        ShardedSessionManager.closeSession(session);
    }
}
