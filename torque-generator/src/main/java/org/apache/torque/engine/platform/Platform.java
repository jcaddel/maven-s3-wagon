package org.apache.torque.engine.platform;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

import org.apache.torque.engine.database.model.Domain;
import org.apache.torque.engine.database.model.SchemaType;

/**
 * Interface for RDBMS platform specific behaviour.
 *
 * @author <a href="mailto:mpoeschl@marmot.at">Martin Poeschl</a>
 * @version $Id: Platform.java,v 1.1.6.2 2008-04-18 17:04:37 jkeller Exp $
 */
public interface Platform
{
    /** constant for native id method */
    String IDENTITY = "identity";
    /** constant for native id method */
    String SEQUENCE = "sequence";

    /**
     * Returns the native IdMethod (sequence|identity)
     *
     * @return the native IdMethod
     */
    String getNativeIdMethod();

    /**
     * Returns the max column length supported by the db.
     *
     * @return the max column length
     */
    int getMaxColumnNameLength();

    /**
     * Returns the db specific domain for a jdbcType.
     *
     * @param jdbcType the jdbcType name
     * @return the db specific domain
     */
    Domain getDomainForSchemaType(SchemaType jdbcType);

    /**
     * @return The RDBMS-specific SQL fragment for <code>NULL</code>
     * or <code>NOT NULL</code>.
     */
    String getNullString(boolean notNull);

    /**
     * @return The RDBMS-specific SQL fragment for autoincrement.
     */
    String getAutoIncrement();

    /**
     * Returns if the RDBMS-specific SQL type has a size attribute.
     *
     * @param sqlType the SQL type
     * @return true if the type has a size attribute
     */
    boolean hasSize(String sqlType);

    /**
     * Returns if the RDBMS-specific SQL type has a scale attribute.
     *
     * @param sqlType the SQL type
     * @return true if the type has a scale attribute
     */
    boolean hasScale(String sqlType);

    /**
     * Returns whether the "not null part" of the definition of a column
     * should be generated before the "autoincrement part" in a "create table"
     * statement.
     *
     * @return true if the "not null part" should be first,
     *         false if the "autoincrement part" should be first in a
     *         "create table" statement.
     */
    boolean createNotNullBeforeAutoincrement();
    
    String filterInvalidDefaultValues( String defaultValue );
    
	public boolean isSpecialDefault( String defaultValue );
    
	String getViewDefinition( Connection con, String schema, String viewName);
	
	Long getSequenceNextVal( Connection con, String schema, String sequenceName );

	public List<String> getPrimaryKeys(DatabaseMetaData dbMeta, String dbSchema, String tableName) throws SQLException;
	
	public List<String> getTableNames(DatabaseMetaData dbMeta, String databaseSchema) throws SQLException;
}
