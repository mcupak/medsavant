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
package org.ut.biolab.medsavant.shard.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.ut.biolab.medsavant.shard.core.ShardedSessionManager;

/**
 * Utils for file manipulation.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class FileUtils {

    /**
     * Merges two files into one.
     * 
     * @param baseFilename
     *            starting file to keep
     * @param appendingFilename
     *            appending file
     */
    public static void mergeFiles(String baseFilename, String appendingFilename) {
        BufferedWriter writer = null;
        BufferedReader reader = null;
        File baseFile = new File(baseFilename);
        File appendingFile = new File(appendingFilename);
        try {
            if (!baseFile.exists()) {
                baseFile.createNewFile();
            }

            if (appendingFile.exists()) {
                writer = new BufferedWriter(new FileWriter(baseFile, true));
                reader = new BufferedReader(new FileReader(appendingFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.write("\r\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                // closing failed, ignore
            }
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
                // closing failed, ignore
            }
        }
    }

    /**
     * Deletes the given file.
     * 
     * @param fileName
     *            file to delete
     */
    public static void deleteFile(String fileName) {
        File file = new File(fileName);
        if (!(file.exists() && file.delete())) {
            System.err.println("File " + fileName + " could not be deleted.");
        }
    }

    /**
     * Generates a shard-specific file to store data in.
     * 
     * @param shardId
     *            ID of the shard to use
     * @return file name
     */
    public static String getFileForShard(Integer shardId) {
        return ShardedSessionManager.getTable() + "-" + shardId;
    }

    /**
     * Renames a file.
     * 
     * @param oldName
     *            original name
     * @param newName
     *            target name
     * @param overwrite
     *            true if the target file should be overwritten, false if
     *            exception should be thrown in case of collision
     * @return true if the file was successfully renamed, false otherwise
     * @throws IOException
     *             in case of target file name collision
     */
    public static boolean renameFile(String oldName, String newName, boolean overwrite) {
        File file = new File(oldName);
        File file2 = new File(newName);

        if (file2.exists()) {
            if (overwrite) {
                deleteFile(newName);
            } else {
                return false;
            }
        }

        return file.renameTo(file2);
    }

    /**
     * Generates a shard-specific file name.
     * 
     * @param file
     *            file to use
     * @return parametrized file name
     */
    public static String getParametrizedFilePath(File file) {
        return file.getAbsolutePath().replaceAll("\\\\", "/") + "%d";
    }
}
