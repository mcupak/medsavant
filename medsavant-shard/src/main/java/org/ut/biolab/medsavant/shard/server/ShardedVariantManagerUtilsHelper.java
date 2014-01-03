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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.hibernate.Session;
import org.ut.biolab.medsavant.shard.core.ShardedSessionManager;
import org.ut.biolab.medsavant.shard.io.FileUtils;
import org.ut.biolab.medsavant.shard.io.TSVUtils;
import org.ut.biolab.medsavant.shard.nonshard.ShardedConnectionController;
import org.ut.biolab.medsavant.shard.strategy.PositionShardSelector;
import org.ut.biolab.medsavant.shared.util.MiscUtils;

/**
 * Sharded helper class for VariantManagerUtils.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class ShardedVariantManagerUtilsHelper {

    /**
     * Exports variant table on each shard to a local file.
     * 
     * @param exportQuery
     *            parametrized query to run
     */
    public void exportVariantTablesToFiles(String exportQuery) {
        Session session = ShardedSessionManager.openSession();

        ShardedConnectionController.executeQueryWithoutResultOnAllShards(exportQuery, true, true);

        ShardedSessionManager.closeSession(session);
    }

    /**
     * Exports variant table on each shard to a single shared file.
     * 
     * @param file
     *            file
     * @param exportQuery
     *            query to run
     */
    public void exportVariantTablesToSingleFile(File file, String exportQuery) {
        Session session = ShardedSessionManager.openSession();

        int shardNo = ShardedSessionManager.getShardNo();
        ShardedConnectionController.executeQueryWithoutResultOnAllShards(exportQuery, true, true);

        // merge files
        for (int i = 0; i < shardNo; i++) {
            FileUtils.mergeFiles(file.getAbsolutePath().replaceAll("\\\\", "/"), String.format(FileUtils.getParametrizedFilePath(file), i));
            // FileUtils.deleteFile(fileBase + FileUtils.getFileForShard(i));
        }

        ShardedSessionManager.closeSession(session);
    }

    /**
     * Loads TSV file to a table.
     * 
     * @param file
     *            file to load
     * @param tableName
     *            table to fill in
     * @param fieldDeliminer
     *            field deliminer
     * @param enclosedBy
     *            enclosed by string
     * @param escapeSequence
     *            escape string
     * @throws IOException
     */
    public static void uploadTSVFileToVariantTable(File file, String tableName, String fieldDeliminer, String enclosedBy, String escapeSequence) throws IOException {
        Session session = ShardedSessionManager.openSession();
        int shardNo = ShardedSessionManager.getShardNo();
        PositionShardSelector sel = new PositionShardSelector(ShardedSessionManager.MAX_VARIANT_POSITION, shardNo);

        int chunkSize = 100000; // number of lines per chunk (100K lines =
        // ~50MB for a standard VCF file)

        // parse the file and split it to separate files per-shard
        BufferedReader br = new BufferedReader(new FileReader(file));
        String parentDirectory = file.getParentFile().getAbsolutePath();
        List<BufferedWriter> bws = new ArrayList<BufferedWriter>();
        List<Integer> lineNumbers = new ArrayList<Integer>();
        List<String> currentOutputPaths = new ArrayList<String>();
        List<Boolean> stateOpens = new ArrayList<Boolean>();
        for (int i = 0; i < shardNo; i++) {
            bws.add(null);
            lineNumbers.add(0);
            currentOutputPaths.add(null);
            stateOpens.add(false);
        }

        String line;
        int currentShard;
        while ((line = br.readLine()) != null) {
            // determine shard
            currentShard = sel.getShard(TSVUtils.getPos(line)).getId();

            lineNumbers.set(currentShard, lineNumbers.get(currentShard) + 1);

            // start a new output file
            if (lineNumbers.get(currentShard) % chunkSize == 1) {
                currentOutputPaths.set(currentShard,
                        parentDirectory + "/" + MiscUtils.extractFileName(file.getAbsolutePath()) + "_" + currentShard + "_" + (lineNumbers.get(currentShard) / chunkSize));
                bws.set(currentShard, new BufferedWriter(new FileWriter(currentOutputPaths.get(currentShard))));
                stateOpens.set(currentShard, true);
            }

            // write line to chunk file
            bws.get(currentShard).write(line + "\n");

            // close and upload this output file
            if (lineNumbers.get(currentShard) % chunkSize == 0) {
                try {
                    bws.get(currentShard).close();
                } catch (IOException e) {
                    System.err.println("Failed to close writer to: " + currentOutputPaths.get(currentShard));
                }

                String query = "LOAD DATA LOCAL INFILE '" + currentOutputPaths.get(currentShard).replaceAll("\\\\", "/") + "' " + "INTO TABLE " + tableName + " "
                        + "FIELDS TERMINATED BY '" + StringEscapeUtils.escapeJava(fieldDeliminer) + "' ENCLOSED BY '" + enclosedBy + "' " + "ESCAPED BY '"
                        + StringEscapeUtils.escapeJava(escapeSequence) + "' "
                        // + " LINES TERMINATED BY '\\r\\n'";
                        + ";";

                ShardedConnectionController.executeQueryWithoutResultOnShard(currentShard, query);

                stateOpens.set(currentShard, false);
            }
        }

        // write the remaining open files
        for (int i = 0; i < shardNo; i++) {
            if (bws.get(i) != null && stateOpens.get(i)) {
                try {
                    bws.get(i).close();
                } catch (IOException e) {
                    System.err.println("Failed to close writer to: " + currentOutputPaths.get(i));
                }

                String query = "LOAD DATA LOCAL INFILE '" + currentOutputPaths.get(i).replaceAll("\\\\", "/") + "' " + "INTO TABLE " + tableName + " " + "FIELDS TERMINATED BY '"
                        + StringEscapeUtils.escapeJava(fieldDeliminer) + "' ENCLOSED BY '" + enclosedBy + "' " + "ESCAPED BY '" + StringEscapeUtils.escapeJava(escapeSequence)
                        + "'"
                        // + " LINES TERMINATED BY '\\r\\n'"
                        + ";";

                ShardedConnectionController.executeQueryWithoutResultOnShard(i, query);
            }
        }

        ShardedSessionManager.closeSession(session);
    }
}
