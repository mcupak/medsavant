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
package org.ut.biolab.medsavant.shard.mapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ut.biolab.medsavant.shard.core.ShardedSessionManager;
import org.ut.biolab.medsavant.shared.db.TableSchema;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;

/**
 * Utils for determining whether the TableSchema in use matches the mapping.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class SchemaMappingUtils {

    /**
     * Determines if the columns in the given schema match the columns in the
     * given mapping.
     * 
     * @param schema
     *            schema
     * @param e
     *            variant mapping generator
     * @return true if the columns match, false otherwise
     */
    public static boolean matches(TableSchema schema, VariantMappingGenerator e) {
        Set<String> s1 = getColumnsInMapping(e);
        if (schema.getNumFields() == s1.size()) {
            List<DbColumn> s2 = schema.getColumns();
            for (DbColumn d : s2) {
                if (!s1.contains(d.getColumnNameSQL())) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Retrieves column names in the given mapping.
     * 
     * @param e
     *            variant mapping generator
     * @return list of columns
     */
    public static Set<String> getColumnsInMapping(VariantMappingGenerator e) {
        Set<String> res = new HashSet<String>();
        res.add(e.getId().getColumn());
        for (MappingProperty c : e.getProperties()) {
            res.add(c.getColumn());
        }

        return res;

    }

    /**
     * Retrieves column names in the given table schema.
     * 
     * @param schema
     *            schema to use
     * @return list of columns
     */
    public static Set<String> getColumnsInSchema(TableSchema schema) {
        Set<String> res = new HashSet<String>();
        for (DbColumn c : schema.getColumns()) {
            res.add(c.getColumnNameSQL());
        }

        return res;
    }

    private static ClassField getClassFieldFromDbColumn(DbColumn d) {
        String type = d.getTypeNameSQL().toLowerCase();
        String value;
        if (type.contains("char")) {
            type = "String";
            value = "\"\"";
        } else if (type.contains("bigint")) {
            type = "Long";
            value = "new Long(0)";
        } else if (type.contains("numeric") || type.contains("decimal")) {
            type = "BigDecimal";
            value = "new BigDecimal(0)";
        } else if (type.contains("int")) {
            type = "Integer";
            value = "0";
        } else if (type.contains("real")) {
            type = "Float";
            value = "new Float(0)";
        } else if (type.contains("float") || type.contains("double")) {
            type = "Double";
            value = "new Double(0)";
        } else {
            type = "Object";
            value = "null";
        }

        return new ClassField("private", type, d.getColumnNameSQL(), value);
    }

    /**
     * Sets up hibernate mapping, table schema and variant class so that they
     * are in a consistent state determined by table schema.
     * 
     * @param table
     *            talbe schema
     * @param e
     *            variant mapping generator instance
     */
    public static synchronized void setUpTableAndClass(TableSchema table, VariantMappingGenerator e) {
        boolean rebuild = ShardedSessionManager.setTable(table.getTableName());

        if (!SchemaMappingUtils.matches(table, e)) {
            // set fields in class based on columns in table schema
            List<DbColumn> tableColumns = table.getColumns();
            VariantEntityGenerator.getInstance().setFields(new ArrayList<ClassField>());
            for (DbColumn d : tableColumns) {
                VariantEntityGenerator.getInstance().addField(getClassFieldFromDbColumn(d));
            }
            VariantEntityGenerator.getInstance().compile();

            if (ShardedSessionManager.setClassInMapping()) {
                rebuild = true;
            }
        }

        if (rebuild) {
            ShardedSessionManager.buildConfig();
        }
    }
}
