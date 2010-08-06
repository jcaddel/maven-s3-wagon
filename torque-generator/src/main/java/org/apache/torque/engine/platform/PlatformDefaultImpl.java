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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.torque.engine.database.model.Domain;
import org.apache.torque.engine.database.model.SchemaType;

/**
 * Default implementation for the Platform interface.
 * 
 * @author <a href="mailto:mpoeschl@marmot.at">Martin Poeschl</a>
 * @version $Id: PlatformDefaultImpl.java,v 1.1.6.2 2008-04-18 17:04:37 jkeller Exp $
 */
public class PlatformDefaultImpl implements Platform {
	private Map<SchemaType, Domain> schemaDomainMap;

	/**
	 * Default constructor.
	 */
	public PlatformDefaultImpl() {
		initialize();
	}

	private void initialize() {
		schemaDomainMap = new HashMap<SchemaType, Domain>(30);
		Iterator<SchemaType> iter = SchemaType.iterator();
		while (iter.hasNext()) {
			SchemaType type = iter.next();
			schemaDomainMap.put(type, new Domain(type));
		}
		schemaDomainMap.put(SchemaType.BOOLEANCHAR, new Domain(SchemaType.BOOLEANCHAR, "CHAR"));
		schemaDomainMap.put(SchemaType.BOOLEANINT, new Domain(SchemaType.BOOLEANINT, "INTEGER"));
	}

	protected void setSchemaDomainMapping(Domain domain) {
		schemaDomainMap.put(domain.getType(), domain);
	}

	/**
	 * @see Platform#getMaxColumnNameLength()
	 */
	public int getMaxColumnNameLength() {
		return 64;
	}

	/**
	 * @see Platform#getNativeIdMethod()
	 */
	public String getNativeIdMethod() {
		return Platform.IDENTITY;
	}

	/**
	 * @see Platform#getDomainForSchemaType(SchemaType)
	 */
	public Domain getDomainForSchemaType(SchemaType jdbcType) {
		return schemaDomainMap.get(jdbcType);
	}

	/**
	 * @return Only produces a SQL fragment if null values are disallowed.
	 * @see Platform#getNullString(boolean)
	 */
	public String getNullString(boolean notNull) {
		// TODO: Check whether this is true for all DBs. Also verify
		// the old Sybase templates.
		return (notNull ? "NOT NULL" : "");
	}

	/**
	 * @see Platform#getAutoIncrement()
	 */
	public String getAutoIncrement() {
		return "IDENTITY";
	}

	/**
	 * @see Platform#hasScale(String) TODO collect info for all platforms
	 */
	public boolean hasScale(String sqlType) {
		return true;
	}

	/**
	 * @see Platform#hasSize(String) TODO collect info for all platforms
	 */
	public boolean hasSize(String sqlType) {
		return true;
	}

	/**
	 * @see Platform#createNotNullBeforeAutoincrement()
	 */
	public boolean createNotNullBeforeAutoincrement() {
		return true;
	}

	public String filterInvalidDefaultValues(String defaultValue) {
		return defaultValue;
	}

	public boolean isSpecialDefault(String defaultValue) {
		return false;
	}

	public Long getSequenceNextVal(Connection con, String schema, String sequenceName) {
		throw new UnsupportedOperationException("getSequenceDefinition");
	}

	public String getViewDefinition(Connection con, String schema, String viewName) {
		throw new UnsupportedOperationException("getViewDefinition");
	}

	/**
	 * Retrieves a list of the columns composing the primary key for a given table.
	 * 
	 * @param dbMeta
	 *            JDBC metadata.
	 * @param tableName
	 *            Table from which to retrieve PK information.
	 * @return A list of the primary key parts for <code>tableName</code>.
	 * @throws SQLException
	 */
	public List<String> getPrimaryKeys(DatabaseMetaData dbMeta, String dbSchema, String tableName) throws SQLException {
		List<String> pk = new ArrayList<String>();
		ResultSet parts = null;
		try {
			parts = dbMeta.getPrimaryKeys(null, dbSchema, tableName);
			while (parts.next()) {
				pk.add(parts.getString(4));
			}
		} finally {
			if (parts != null) {
				parts.close();
			}
		}
		return pk;
	}

	/**
	 * Get all the table names in the current database that are not system tables.
	 * 
	 * @param dbMeta
	 *            JDBC database metadata.
	 * @return The list of all the tables in a database.
	 * @throws SQLException
	 */
	public List<String> getTableNames(DatabaseMetaData dbMeta, String databaseSchema) throws SQLException {
		// System.out.println("Getting table list...");
		List<String> tables = new ArrayList<String>();
		ResultSet tableNames = null;
		// these are the entity types we want from the database
		String[] types = { "TABLE" }; // JHK: removed views from list
		try {
			tableNames = dbMeta.getTables(null, databaseSchema, null, types);
			while (tableNames.next()) {
				String name = tableNames.getString(3);
				tables.add(name);
			}
		} finally {
			if (tableNames != null) {
				tableNames.close();
			}
		}
		// System.out.println("Found " + tables.size() + " tables.");
		return tables;
	}
}
