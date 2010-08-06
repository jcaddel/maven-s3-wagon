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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.torque.engine.database.model.Domain;
import org.apache.torque.engine.database.model.SchemaType;

/**
 * MySql Platform implementation.
 *
 * @author <a href="mailto:mpoeschl@marmot.at">Martin Poeschl</a>
 * @version $Id: PlatformMysqlImpl.java,v 1.1.6.1 2008-04-18 17:04:37 jkeller Exp $
 */
public class PlatformMysqlImpl extends PlatformDefaultImpl
{
    /**
     * Default constructor.
     */
    public PlatformMysqlImpl()
    {
        super();
        initialize();
    }

    /**
     * Initializes db specific domain mapping.
     */
    private void initialize()
    {
        setSchemaDomainMapping(new Domain(SchemaType.NUMERIC, "DECIMAL"));
        setSchemaDomainMapping(new Domain(SchemaType.LONGVARCHAR, "MEDIUMTEXT"));
        setSchemaDomainMapping(new Domain(SchemaType.DATE, "DATETIME"));
        setSchemaDomainMapping(new Domain(SchemaType.BINARY, "BLOB"));
        setSchemaDomainMapping(new Domain(SchemaType.VARBINARY, "MEDIUMBLOB"));
        setSchemaDomainMapping(new Domain(SchemaType.LONGVARBINARY, "LONGBLOB"));
        setSchemaDomainMapping(new Domain(SchemaType.BLOB, "LONGBLOB"));
        setSchemaDomainMapping(new Domain(SchemaType.CLOB, "LONGTEXT"));
    }

    /**
     * @see Platform#getAutoIncrement()
     */
    public String getAutoIncrement()
    {
        return "AUTO_INCREMENT";
    }

    /**
     * @see Platform#hasSize(String)
     */
    public boolean hasSize(String sqlType)
    {
        return !("MEDIUMTEXT".equals(sqlType) || "LONGTEXT".equals(sqlType)
                || "BLOB".equals(sqlType) || "MEDIUMBLOB".equals(sqlType)
                || "LONGBLOB".equals(sqlType));
    }

	@Override
	public String filterInvalidDefaultValues(String defaultValue) {
		if ( defaultValue != null ) {
			defaultValue = defaultValue.replace( "SYS_GUID()", "" );
			defaultValue = defaultValue.replace( "SYSDATE", "" );
			defaultValue = defaultValue.replace( "USERENV(\'SESSIONID\')", "" );
			defaultValue = defaultValue.trim();
		}
		
		return defaultValue;
	}

	@Override
	public Long getSequenceNextVal(Connection con, String schema, String sequenceName) {
		try {
			PreparedStatement ps = con.prepareStatement( "SELECT auto_increment FROM information_schema.tables WHERE table_schema = ? AND table_name = ?" );
			Long nextVal = 0L;
			ps.setString( 1, schema );
			ps.setString( 2, sequenceName );
			ResultSet rs = ps.executeQuery();
			if ( rs.next() ) {
				nextVal = rs.getLong( 1 );
			}
			rs.close();
			ps.close();
			System.out.println( "Next Val for " + schema + "." + sequenceName + "=" + nextVal);
			return nextVal;
		} catch ( SQLException ex ) {
			System.err.println( "Unable to extract sequence definition: " + schema + "." + sequenceName );
			ex.printStackTrace();
			return 0L;
		}
	}

	@Override
	public String getViewDefinition( Connection con, String schema, String viewName) {
		try {
			PreparedStatement ps = con.prepareStatement( "SELECT view_definition FROM information_schema.views WHERE table_schema = ? AND table_name = ?" );
			String definition = "";
			ps.setString( 1, schema );
			ps.setString( 2, viewName );
			ResultSet rs = ps.executeQuery();
			if ( rs.next() ) {
				definition = rs.getString( 1 );
			}
			rs.close();
			ps.close();
			return definition;
		} catch ( SQLException ex ) {
			System.err.println( "Unable to extract view definition: " + schema + "." + viewName );
			ex.printStackTrace();
			return "";
		}
	}
	
}
