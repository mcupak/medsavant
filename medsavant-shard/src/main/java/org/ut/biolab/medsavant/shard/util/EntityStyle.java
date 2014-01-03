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
package org.ut.biolab.medsavant.shard.util;

import java.util.Collection;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Style determining how meta information about an entity is obtained.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class EntityStyle extends ToStringStyle {

    private static final long serialVersionUID = -6907977923855540120L;
    public static final String FIELD_SEPARATOR = "\t";
    private static ToStringStyle instance;

    protected EntityStyle() {
        // singleton pattern
        setArrayContentDetail(true);
        setUseShortClassName(true);
        setUseClassName(false);
        setUseIdentityHashCode(false);
        setFieldSeparator(FIELD_SEPARATOR);
    }

    public static ToStringStyle getInstance() {
        if (instance == null) {
            instance = new EntityStyle();
        }
        return instance;
    };

    @Override
    public void appendDetail(StringBuffer buffer, String fieldName, Object value) {
        if (!value.getClass().getName().startsWith("java")) {
            buffer.append(ReflectionToStringBuilder.toString(value, instance));
        } else {
            super.appendDetail(buffer, fieldName, value);
        }
    }

    @Override
    public void appendDetail(StringBuffer buffer, String fieldName, Collection value) {
        appendDetail(buffer, fieldName, value.toArray());
    }
}