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

import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;

/**
 * Utils for manipulation of TSV files.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class TSVUtils {
    private static final String DELIMINER1 = "\"\t\"";
    private static final String DELIMINER2 = "\t";

    private static String extractColumnFromLine(String line, int column, String deliminer) {
        String[] pieces = line.split(deliminer);
        return (column >= pieces.length) ? "" : pieces[column];
    }

    public static String getChrom(String line) {
        return extractColumnFromLine(line, BasicVariantColumns.INDEX_OF_CHROM, DELIMINER1);
    }

    public static Long getPos(String line) {
        Long pos = null;
        try {
            pos = Long.valueOf(extractColumnFromLine(line, BasicVariantColumns.INDEX_OF_POSITION, DELIMINER1));
        } catch (NumberFormatException e) {
            // NaN, ignore
            try {
                pos = Long.valueOf(extractColumnFromLine(line, BasicVariantColumns.INDEX_OF_POSITION, DELIMINER2));
            } catch (NumberFormatException e2) {
                // NaN, ignore
            }
        }

        return pos;
    }
}
