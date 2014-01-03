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

/**
 * Selector of shards based on position of variants.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class PositionShardSelector implements ShardSelector<Long> {

    private Long deliminer;

    private Integer divide(Long divisor, Long denominator) {
        return (int) (divisor / denominator);
    }

    public PositionShardSelector(Long maxPos, Integer shardNo) {
        deliminer = (maxPos + shardNo - 1) / shardNo;
    }

    @Override
    public ShardId getShard(Long data) {
        return new ShardId(divide(data, deliminer));
    }

}
