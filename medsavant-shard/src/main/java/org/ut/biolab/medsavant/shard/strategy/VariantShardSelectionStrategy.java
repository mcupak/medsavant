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
package org.ut.biolab.medsavant.shard.strategy;

import org.hibernate.shards.ShardId;
import org.hibernate.shards.strategy.selection.ShardSelectionStrategy;

/**
 * Mechanism determining the shard on which a new object should be created.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class VariantShardSelectionStrategy implements ShardSelectionStrategy {

    private ShardSelector<Long> shardSelector;

    /**
     * Determine shard based on position.
     */
    public ShardId selectShardIdForNewObject(Object obj) {
        if (!(obj instanceof Long)) {
            throw new IllegalArgumentException(obj.toString());
        }
        
        return shardSelector.getShard((Long) obj);
    }

    public VariantShardSelectionStrategy(Long maxPos, Integer shardNo) {
        shardSelector = new PositionShardSelector(maxPos, shardNo);
    }
}