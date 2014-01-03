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

import org.hibernate.shards.cfg.ShardConfiguration;
import org.ut.biolab.medsavant.shard.core.ShardedSessionManager;

/**
 * Helpers for manipulation of shard configuration.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class ShardConfigurationUtil {

    /**
     * Retrieves connection URL to a certain shard.
     * 
     * @param shardId
     *            ID of a shard
     * @return connection URL
     */
    public static String getConnectionUrlForShard(int shardId) {
        ShardConfiguration s = ShardedSessionManager.getConfig(shardId);

        return (s == null) ? null : s.getShardUrl();
    }

    /**
     * Retrieves the database name for a shard.
     * 
     * @param shardId
     *            ID of the shard
     * @return database name
     */
    public static String getDbForShard(int shardId) {
        String url = getConnectionUrlForShard(shardId);

        String db = null;
        if (url != null) {
            db = url.substring(url.lastIndexOf("/".charAt(0)) + 1);
        }

        return db;
    }

    /**
     * Retrieves the server URL for a shard.
     * 
     * @param shardId
     *            ID of the shard
     * @return server URL
     */
    public static String getServerForShard(int shardId) {
        String url = getConnectionUrlForShard(shardId);

        String server = null;
        if (url != null) {
            server = url.substring(0, url.lastIndexOf("/".charAt(0)) + 1);
        }

        return server;
    }
}
