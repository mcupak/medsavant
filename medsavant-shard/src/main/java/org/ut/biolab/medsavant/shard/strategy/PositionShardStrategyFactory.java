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

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.hibernate.shards.ShardId;
import org.hibernate.shards.strategy.ShardStrategy;
import org.hibernate.shards.strategy.ShardStrategyFactory;
import org.hibernate.shards.strategy.ShardStrategyImpl;
import org.hibernate.shards.strategy.access.ParallelShardAccessStrategy;
import org.hibernate.shards.strategy.access.ShardAccessStrategy;
import org.hibernate.shards.strategy.resolution.ShardResolutionStrategy;
import org.hibernate.shards.strategy.selection.ShardSelectionStrategy;

/**
 * Position-based shard strategy factory.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class PositionShardStrategyFactory implements ShardStrategyFactory {

    private Long maxPos;
    private Integer shardNo;
    private ThreadPoolExecutor exec;

    public PositionShardStrategyFactory(Long maxPos, Integer shardNo, ThreadPoolExecutor exec) {
        this.maxPos = maxPos;
        this.shardNo = shardNo;
        this.exec = exec;
    }

    @Override
    public ShardStrategy newShardStrategy(List<ShardId> shardIds) {
        ShardSelectionStrategy pss = new VariantShardSelectionStrategy(maxPos, shardNo);
        ShardResolutionStrategy prs = new VariantShardResolutionStrategy(maxPos, shardNo);
        ShardAccessStrategy pas = new ParallelShardAccessStrategy(exec);

        return new ShardStrategyImpl(pss, prs, pas);
    }
}
