/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
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
