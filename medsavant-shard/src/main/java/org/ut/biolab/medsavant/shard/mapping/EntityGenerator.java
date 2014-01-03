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
 * Factory for entities.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public interface EntityGenerator {

    /**
     * Generates content for a .java file for the given entity.
     * 
     * @return java source
     */
    String getSource();

    /**
     * Compiles and loads a class.
     * 
     * @return class representing the entity
     */
    Class<?> getCompiled();

    /**
     * Compiles a class.
     */
    void compile();

    /**
     * Generates the name of the class to use.
     * 
     * @return class name
     */
    String getClassName();

    /**
     * Generates the package for the class.
     * 
     * @return package name
     */
    String getPackage();

    /**
     * Retrieves fields in the generated class.
     * 
     * @return list of fields
     */
    List<ClassField> getFields();

    /**
     * Changes the fields being generated for the class including get/set
     * methods.
     * 
     * @param fields
     *            fields
     */
    void setFields(List<ClassField> fields);

    /**
     * Generates a new field including get/set methods.
     * 
     * @param field
     *            field to generate
     */
    void addField(ClassField field);
}
