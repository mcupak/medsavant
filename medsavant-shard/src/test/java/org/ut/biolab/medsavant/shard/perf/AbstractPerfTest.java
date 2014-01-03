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
package org.ut.biolab.medsavant.shard.perf;

import org.testng.annotations.BeforeClass;
import org.ut.biolab.medsavant.shard.AbstractShardTest;
import org.ut.biolab.medsavant.shard.core.ShardedSessionManager;

/**
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class AbstractPerfTest extends AbstractShardTest{
    private long start;
    private long stop;

    public void recordStart() {
        start = System.nanoTime();
    }

    public void recordStop() {
        stop = System.nanoTime();
    }

    public double getTimeInMilis() {
        return ((double) (stop - start)) / 1000000;
    }

    @BeforeClass
    public void init() {
        System.out.println("Number of shards: " + ShardedSessionManager.getShardNo());
    }

}
