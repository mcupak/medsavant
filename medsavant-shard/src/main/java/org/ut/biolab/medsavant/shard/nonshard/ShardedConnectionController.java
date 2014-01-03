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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.hibernate.shards.cfg.ShardConfiguration;
import org.ut.biolab.medsavant.shard.core.ShardedSessionManager;

/**
 * Controller for connections to individual shards.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class ShardedConnectionController {

    private static final String CREATE_DB_QUERY = "CREATE DATABASE IF NOT EXISTS ";
    private static final String DROP_DB_QUERY = "DROP DATABASE IF EXISTS ";

    /**
     * Executes a query on a single shard, blocking call.
     * 
     * @param shardId
     *            id of the shard
     * @param query
     *            query to execute
     * @return result of the query (first result)
     */
    public static Object executeQueryOnShard(int shardId, String query) {
        ShardConfiguration config = ShardedSessionManager.getConfig(shardId);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Object> worker = new ShardQueryExecutor(config, query, QueryType.SELECT);
        Future<Object> future = executor.submit(worker);

        Object res = null;
        try {
            res = future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        executor.shutdown();

        return res;
    }

    /**
     * Executes a query on a single shard, not expecting any return value
     * (typically an update). Non-blocking call.
     * 
     * @param shardId
     *            id of the shard to query
     * @param query
     *            query to execute
     */
    public static void executeQueryWithoutResultOnShard(int shardId, String query) {
        ShardConfiguration config = ShardedSessionManager.getConfig(shardId);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Object> worker = new ShardQueryExecutor(config, query, QueryType.SELECT_WITHOUT_RESULT);
        executor.submit(worker);

        executor.shutdown();
    }

    /**
     * Executes an update on a single shard, not expecting any return value
     * (typically an update). Non-blocking call.
     * 
     * @param shardId
     *            id of the shard to query
     * @param query
     *            query to execute
     */
    public static void executeUpdateOnShard(int shardId, String query) {
        ShardConfiguration config = ShardedSessionManager.getConfig(shardId);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Object> worker = new ShardQueryExecutor(config, query, QueryType.UPDATE);
        executor.submit(worker);

        executor.shutdown();
    }

    private static String instantiateQuery(String template, int value) {
        return String.format(template, value);
    }

    /**
     * Execute a query on all shards. Blocking call, waits for results.
     * 
     * @param query
     *            query to execute
     * @param shardIdAsParam
     *            true if shard ID should be inserted into the query
     * @return results
     */
    public static Object executeQueryOnAllShards(String query, boolean shardIdAsParam) {
        int shardNo = ShardedSessionManager.getShardNo();
        ExecutorService executor = Executors.newFixedThreadPool(shardNo);

        Set<Future<Object>> tempResults = new HashSet<Future<Object>>();
        for (int i = 0; i < shardNo; i++) {
            ShardConfiguration config = ShardedSessionManager.getConfig(i);
            Callable<Object> worker = new ShardQueryExecutor(config, shardIdAsParam ? instantiateQuery(query, i) : query, QueryType.SELECT);
            Future<Object> future = executor.submit(worker);
            tempResults.add(future);
        }

        // collect results
        List<Object> finalResults = new ArrayList<Object>();
        for (Future<Object> f : tempResults) {
            try {
                finalResults.add(f.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        return finalResults;
    }

    /**
     * Executes a query on all shards without waiting for the results.
     * 
     * @param query
     *            query to execute
     * @param shardIdAsParam
     *            true if shard ID should be inserted into the query
     * @param block
     *            true if a blocking call should be made, false otherwise
     */
    public static void executeQueryWithoutResultOnAllShards(String query, boolean shardIdAsParam, boolean block) {
        int shardNo = ShardedSessionManager.getShardNo();
        ExecutorService executor = Executors.newFixedThreadPool(shardNo);

        for (int i = 0; i < shardNo; i++) {
            ShardConfiguration config = ShardedSessionManager.getConfig(i);
            Callable<Object> worker = new ShardQueryExecutor(config, shardIdAsParam ? instantiateQuery(query, i) : query, QueryType.SELECT_WITHOUT_RESULT);
            executor.submit(worker);
        }

        executor.shutdown();

        if (block) {
            while (!executor.isTerminated()) {
                // wait until everything is done
            }
        }
    }

    /**
     * Executes a query on all shards in sequence. Blocking call.
     * 
     * @param query
     *            query to execute
     * @param shardIdAsParam
     *            true if shard ID should be inserted into the query
     */
    public static void executeQueryWithoutResultOnAllShardsInSequence(String query, boolean shardIdAsParam) {
        int shardNo = ShardedSessionManager.getShardNo();
        for (int i = 0; i < shardNo; i++) {
            ShardConfiguration config = ShardedSessionManager.getConfig(i);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Callable<Object> worker = new ShardQueryExecutor(config, shardIdAsParam ? instantiateQuery(query, i) : query, QueryType.SELECT_WITHOUT_RESULT);
            executor.submit(worker);

            executor.shutdown();
            while (!executor.isTerminated()) {
                // wait until everything is done
            }
        }
    }

    /**
     * Executes an update on all shards without waiting for the results.
     * Non-blocking call.
     * 
     * @param query
     *            query to execute
     * @param shardIdAsParam
     *            true if shard ID should be inserted into the query
     */
    public static void executeUpdateOnAllShards(String query, boolean shardIdAsParam) {
        int shardNo = ShardedSessionManager.getShardNo();
        ExecutorService executor = Executors.newFixedThreadPool(shardNo);

        for (int i = 0; i < shardNo; i++) {
            ShardConfiguration config = ShardedSessionManager.getConfig(i);
            Callable<Object> worker = new ShardQueryExecutor(config, shardIdAsParam ? instantiateQuery(query, i) : query, QueryType.UPDATE);
            executor.submit(worker);
        }

        executor.shutdown();
    }

    /**
     * Creates databases for shards if they don't exist.
     */
    public static void createShards() {
        int shardNo = ShardedSessionManager.getShardNo();
        ExecutorService executor = Executors.newFixedThreadPool(shardNo);

        for (int i = 0; i < shardNo; i++) {
            ShardConfiguration config = ShardedSessionManager.getConfig(i);
            Callable<Object> worker = new ShardQueryExecutor(config, CREATE_DB_QUERY + ShardConfigurationUtil.getDbForShard(i), QueryType.DB);
            executor.submit(worker);
        }

        executor.shutdown();

        while (!executor.isTerminated()) {
            // wait until everything is done
        }
    }

    /**
     * Deletes databases of shards.
     */
    public static void dropShards() {
        int shardNo = ShardedSessionManager.getShardNo();
        ExecutorService executor = Executors.newFixedThreadPool(shardNo);

        for (int i = 0; i < shardNo; i++) {
            ShardConfiguration config = ShardedSessionManager.getConfig(i);
            Callable<Object> worker = new ShardQueryExecutor(config, DROP_DB_QUERY + ShardConfigurationUtil.getDbForShard(i), QueryType.DB);
            executor.submit(worker);
        }

        executor.shutdown();

        while (!executor.isTerminated()) {
            // wait until everything is done
        }
    }
}
