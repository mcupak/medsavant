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

import java.util.List;

/**
 * Provider of dynamic Hibernate mappings.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public interface MappingProvider {

    /**
     * Retrieves the mapping in place.
     * 
     * @return mapping in XML
     */
    String getMapping();

    /**
     * Returns the name of the entity the mapping is for.
     * 
     * @return entity name
     */
    String getClassName();

    /**
     * Change mapping to the given class.
     * 
     * @param clazz
     *            class name
     */
    void setClassName(String pack, String clazz);

    /**
     * Retrieves the name of table the entity is mapped to.
     * 
     * @return table name
     */
    String getTable();

    /**
     * Changes the table the entity is mapped to.
     * 
     * @param table
     *            table name
     */
    void setTable(String table);

    /**
     * Retrieves the list of active properties in the mapping.
     * 
     * @return properties
     */
    List<MappingProperty> getProperties();

    /**
     * Changes the properties in the mapping.
     * 
     * @param properties
     *            new properties
     */
    void setProperties(List<MappingProperty> properties);

    /**
     * Adds a property to the mapping.
     * 
     * @param propertyName
     *            name of the property
     * @param columnName
     *            name of the column to map to
     * @param propertyType
     *            type of the property
     */
    void addProperty(String propertyName, String columnName, String propertyType);
}
