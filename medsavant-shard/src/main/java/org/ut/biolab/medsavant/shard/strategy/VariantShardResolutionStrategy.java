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

import java.util.ArrayList;
import java.util.List;

import org.hibernate.shards.ShardId;
import org.hibernate.shards.strategy.resolution.AllShardsShardResolutionStrategy;
import org.hibernate.shards.strategy.selection.ShardResolutionStrategyData;

/**
 * Mechanism determining the set of shards on which an object with a given id
 * might reside.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class VariantShardResolutionStrategy extends AllShardsShardResolutionStrategy {

    private ShardSelector<Long> shardSelector;

    public VariantShardResolutionStrategy(List<ShardId> shardIds) {
        super(shardIds);
    }

    public VariantShardResolutionStrategy(Long maxPos, Integer shardNo) {
        super(getShardList(shardNo));
        shardSelector = new PositionShardSelector(maxPos, shardNo);
    }

    private static List<ShardId> getShardList(int shardNo) {
        List<ShardId> res = new ArrayList<ShardId>();
        for (int i = 0; i < shardNo; i++) {
            res.add(new ShardId(i));
        }
        return res;
    }

    public List<ShardId> selectShardIdsFromShardResolutionStrategyData(ShardResolutionStrategyData srsd) {
        if (srsd.getEntityName().equals(Variant.class.getName())) {
            List<ShardId> relevantShards = new ArrayList<ShardId>();
            relevantShards.add(shardSelector.getShard((Long) srsd.getId()));
            return relevantShards;
        }
        return super.selectShardIdsFromShardResolutionStrategyData(srsd);
    }

}
