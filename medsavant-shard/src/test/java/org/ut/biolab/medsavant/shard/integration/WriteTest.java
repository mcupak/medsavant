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
package org.ut.biolab.medsavant.shard.integration;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.hibernate.Session;
import org.testng.annotations.Test;
import org.ut.biolab.medsavant.shard.AbstractShardTest;
import org.ut.biolab.medsavant.shard.core.ShardedSessionManager;
import org.ut.biolab.medsavant.shard.io.FileUtils;
import org.ut.biolab.medsavant.shard.nonshard.NonShardDBUtils;
import org.ut.biolab.medsavant.shard.nonshard.ShardConfigurationUtil;
import org.ut.biolab.medsavant.shard.nonshard.ShardedConnectionController;

/**
 * Tests to verify DDL is working.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class WriteTest extends AbstractShardTest {

    @Test
    public void testShardUrl() {
        Session session = ShardedSessionManager.openSession();

        System.out.println(ShardConfigurationUtil.getConnectionUrlForShard(0));

        ShardedSessionManager.closeSession(session);
    }

    @Test
    public void testShardDb() {
        Session session = ShardedSessionManager.openSession();

        System.out.println(ShardConfigurationUtil.getDbForShard(0));

        ShardedSessionManager.closeSession(session);
    }

    @Test
    public void testShardServer() {
        Session session = ShardedSessionManager.openSession();

        System.out.println(ShardConfigurationUtil.getServerForShard(0));

        ShardedSessionManager.closeSession(session);
    }

    @Test
    public void testShardConnectionCredentials() {
        Session session = ShardedSessionManager.openSession();

        System.out.println(ShardedSessionManager.getConfig(0).getShardUser());
        System.out.println(ShardedSessionManager.getConfig(0).getShardPassword());

        ShardedSessionManager.closeSession(session);
    }

    @Test
    public void testExportTableOnOneShard() {
        final String query = "SELECT * FROM z_variant_proj1_ref3_update1 LIMIT 10 INTO OUTFILE '/tmp/test.bu' FIELDS TERMINATED BY '\t'";

        Session session = ShardedSessionManager.openSession();

        ShardedConnectionController.executeQueryWithoutResultOnShard(0, query);
        BufferedReader br = null;
        String everything = null;
        try {
            br = new BufferedReader(new FileReader("/tmp/test.bu"));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append('\n');
                line = br.readLine();
            }
            everything = sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                System.err.println("I/O error occured.");
            }
            FileUtils.deleteFile("/tmp/test.bu");
        }

        ShardedSessionManager.closeSession(session);

        System.out.println(everything);
    }

    @Test
    public void testExportTableOnAllShards() {
        final String query = "SELECT * FROM z_variant_proj1_ref3_update1 LIMIT 10 INTO OUTFILE '/tmp/test-%d.bu' FIELDS TERMINATED BY '\t'";

        Session session = ShardedSessionManager.openSession();

        ShardedConnectionController.executeQueryWithoutResultOnAllShards(query, true, true);

        // read the files
        BufferedReader br = null;
        String everything = null;
        for (int i = 0; i < ShardedSessionManager.getShardNo(); i++) {
            try {
                br = new BufferedReader(new FileReader("/tmp/test-" + i + ".bu"));
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append('\n');
                    line = br.readLine();
                }
                everything = sb.toString();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    System.err.println("I/O error occured.");
                }
                FileUtils.deleteFile("/tmp/test-" + i + ".bu");
            }
        }

        ShardedSessionManager.closeSession(session);

        System.out.println(everything);
    }

    @Test
    public void testExportTableOnAllShardsToSameFile() {
        final String fileBase = "/tmp/test.bu";
        final String query = "SELECT * FROM z_variant_proj1_ref3_update1 LIMIT 5 INTO OUTFILE '" + fileBase + "%d' FIELDS TERMINATED BY '\t'";

        Session session = ShardedSessionManager.openSession();
        int shardNo = ShardedSessionManager.getShardNo();
        ShardedConnectionController.executeQueryWithoutResultOnAllShards(query, true, true);
        ShardedSessionManager.closeSession(session);

        // merge files
        for (int i = 0; i < shardNo; i++) {
            FileUtils.mergeFiles(fileBase, fileBase + new Integer(i));
        }

        // read the file
        BufferedReader br = null;
        String everything = null;
        try {
            br = new BufferedReader(new FileReader(fileBase));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append('\n');
                line = br.readLine();
            }
            everything = sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                System.err.println("I/O error occured.");
            }
            FileUtils.deleteFile(fileBase);
        }

        System.out.println(everything);
    }

    @Test
    public void testCreateDropShards() {
        // show databases before shards
        System.out.println("Databases before creating shards:");
        showDatabases();

        // create shards
        System.out.println("Creating shards...");
        Session session = ShardedSessionManager.openSession();
        ShardedConnectionController.createShards();
        ShardedSessionManager.closeSession(session);

        // show databases after creating shards
        System.out.println("Databases after creating shards:");
        showDatabases();

        // drop shards
        System.out.println("Dropping shards...");
        session = ShardedSessionManager.openSession();
        ShardedConnectionController.dropShards();
        ShardedSessionManager.closeSession(session);

        // show databases after dropping shards
        System.out.println("Databases after dropping shards:");
        showDatabases();
    }

    @Test
    public void showDatabases() {
        Session session = ShardedSessionManager.openSession();

        List<String> dbs = NonShardDBUtils.getDatabases(ShardConfigurationUtil.getServerForShard(0), ShardedSessionManager.getConfig(0).getShardUser(), ShardedSessionManager
                .getConfig(0).getShardPassword());

        for (String s : dbs) {
            System.out.println(s);
        }

        ShardedSessionManager.closeSession(session);
    }

    @Test
    public void showTables() {
        Session session = ShardedSessionManager.openSession();

        List<String> tables = NonShardDBUtils.getTables(ShardConfigurationUtil.getConnectionUrlForShard(0), ShardedSessionManager.getConfig(0).getShardUser(),
                ShardedSessionManager.getConfig(0).getShardPassword());

        for (String s : tables) {
            System.out.println(s);
        }

        ShardedSessionManager.closeSession(session);
    }

    @Test
    public void createDropTable() {
        final String createQuery = "CREATE TABLE test (id int)";
        final String dropQuery = "DROP TABLE test";

        System.out.println("Tables before:");
        showTables();

        System.out.println("Creating tables...");
        Session session = ShardedSessionManager.openSession();
        ShardedConnectionController.executeUpdateOnShard(0, createQuery);
        ShardedSessionManager.closeSession(session);

        System.out.println("Tables after creating:");
        showTables();

        System.out.println("Dropping tables...");
        session = ShardedSessionManager.openSession();
        ShardedConnectionController.executeUpdateOnShard(0, dropQuery);
        ShardedSessionManager.closeSession(session);

        System.out.println("Tables after dropping:");
        showTables();
    }
}
