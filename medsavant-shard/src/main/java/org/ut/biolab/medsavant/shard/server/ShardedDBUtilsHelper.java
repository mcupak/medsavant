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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.shards.criteria.ShardedCriteriaImpl;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.ut.biolab.medsavant.shard.core.ShardedSessionManager;
import org.ut.biolab.medsavant.shard.mapping.SchemaMappingUtils;
import org.ut.biolab.medsavant.shard.mapping.VariantEntityGenerator;
import org.ut.biolab.medsavant.shard.mapping.VariantMappingGenerator;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.model.Range;

/**
 * Sharded version of the helpers for DBUtils.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class ShardedDBUtilsHelper {

    /**
     * Retrieves number of variants.
     * 
     * @param table
     *            table schema for variants
     * @return variant count
     */
    public int getNumRecordsInTable(TableSchema table) {
        SchemaMappingUtils.setUpTableAndClass(table, VariantMappingGenerator.getInstance());

        Session s = ShardedSessionManager.openSession();

        Criteria c = s.createCriteria(VariantEntityGenerator.getInstance().getCompiled()).setProjection(
                Projections.count(VariantMappingGenerator.getInstance().getId().getColumn()));
        Integer res = ((BigDecimal) c.list().get(0)).intValue();

        ShardedSessionManager.closeSession(s);

        return res;
    }

    /**
     * Retrieves extreme values for a given column.
     * 
     * @param table
     *            table schema for variants
     * @param colName
     *            column name
     * @return min and max values for the given column
     */
    public Range getExtremeValuesForColumn(TableSchema table, String colName) {
        SchemaMappingUtils.setUpTableAndClass(table, VariantMappingGenerator.getInstance());

        Session s = ShardedSessionManager.openSession();

        Object oMin = s.createCriteria(VariantEntityGenerator.getInstance().getCompiled()).setProjection(Projections.min(colName)).list().get(0);
        Object oMax = s.createCriteria(VariantEntityGenerator.getInstance().getCompiled()).setProjection(Projections.max(colName)).list().get(0);

        double min;
        if (oMin instanceof Integer) {
            min = (double) (Integer) oMin;
        } else if (oMin instanceof Float) {
            min = (double) (Float) oMin;
        } else if (oMin instanceof Double) {
            min = (Double) oMin;
        } else {
            min = 0;
        }
        double max;
        if (oMax instanceof Integer) {
            max = (double) (Integer) oMax;
        } else if (oMax instanceof Float) {
            max = (double) (Float) oMax;
        } else if (oMax instanceof Double) {
            max = (Double) oMax;
        } else {
            max = 0;
        }

        ShardedSessionManager.closeSession(s);

        return new Range(min, max);
    }

    /**
     * Retrieves distinct values for a column.
     * 
     * @param table
     *            table schema for variants
     * @param colName
     *            column name
     * @param limit
     *            limit for results, <0 if no limit needed
     * @return list of distinct values
     */
    public List<Object> getDistinctValuesForColumn(TableSchema table, String colName, int limit) {
        SchemaMappingUtils.setUpTableAndClass(table, VariantMappingGenerator.getInstance());

        Session s = ShardedSessionManager.openSession();

        Criteria c = ((ShardedCriteriaImpl) s.createCriteria(VariantEntityGenerator.getInstance().getCompiled())).setProjection(Projections.sqlGroupProjection(colName
                + " as value", "value", new String[] { "value" }, new Type[] { new StringType() }));
        if (limit >= 0) {
            c.setFetchSize(limit).setMaxResults(limit);
        }
        List<Object[]> tmp = c.list();

        List<Object> res = new ArrayList<Object>();
        for (Object[] o : tmp) {
            res.add(o[0]);
        }

        ShardedSessionManager.closeSession(s);

        if (res.size() == 1 && (res.get(0) == null || ((res.get(0) instanceof Object[]) && (((Object[]) res.get(0))[0] == null)))) {
            res = new ArrayList<Object>();
        }

        return res;
    }
}
