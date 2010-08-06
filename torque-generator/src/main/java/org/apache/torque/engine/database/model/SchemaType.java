package org.apache.torque.engine.database.model;

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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.enums.Enum;

/**
 * Enum for types used in Torque schema.xml files.
 *
 * @author <a href="mailto:mpoeschl@marmot.at>Martin Poeschl</a>
 * @version $Id: SchemaType.java,v 1.1.6.1 2008-04-18 17:04:37 jkeller Exp $
 */
public class SchemaType extends Enum
{
    /**
     * Serialization support
     */
    private static final long serialVersionUID = 17588853769472381L;

    public static final SchemaType BIT = new SchemaType("BIT");
    public static final SchemaType TINYINT = new SchemaType("TINYINT");
    public static final SchemaType SMALLINT = new SchemaType("SMALLINT");
    public static final SchemaType INTEGER = new SchemaType("INTEGER");
    public static final SchemaType BIGINT = new SchemaType("BIGINT");
    public static final SchemaType FLOAT = new SchemaType("FLOAT");
    public static final SchemaType REAL = new SchemaType("REAL");
    public static final SchemaType NUMERIC = new SchemaType("NUMERIC");
    public static final SchemaType DECIMAL = new SchemaType("DECIMAL");
    public static final SchemaType CHAR = new SchemaType("CHAR");
    public static final SchemaType VARCHAR = new SchemaType("VARCHAR");
    public static final SchemaType LONGVARCHAR = new SchemaType("LONGVARCHAR");
    public static final SchemaType DATE = new SchemaType("DATE");
    public static final SchemaType TIME = new SchemaType("TIME");
    public static final SchemaType TIMESTAMP = new SchemaType("TIMESTAMP");
    public static final SchemaType BINARY = new SchemaType("BINARY");
    public static final SchemaType VARBINARY = new SchemaType("VARBINARY");
    public static final SchemaType LONGVARBINARY = new SchemaType("LONGVARBINARY");
    public static final SchemaType NULL = new SchemaType("NULL");
    public static final SchemaType OTHER = new SchemaType("OTHER");
    public static final SchemaType JAVA_OBJECT = new SchemaType("JAVA_OBJECT");
    public static final SchemaType DISTINCT = new SchemaType("DISTINCT");
    public static final SchemaType STRUCT = new SchemaType("STRUCT");
    public static final SchemaType ARRAY = new SchemaType("ARRAY");
    public static final SchemaType BLOB = new SchemaType("BLOB");
    public static final SchemaType CLOB = new SchemaType("CLOB");
    public static final SchemaType REF = new SchemaType("REF");
    public static final SchemaType BOOLEANINT = new SchemaType("BOOLEANINT");
    public static final SchemaType BOOLEANCHAR = new SchemaType("BOOLEANCHAR");
    public static final SchemaType DOUBLE = new SchemaType("DOUBLE");

    private SchemaType(String type)
    {
        super(type);
    }

    public static SchemaType getEnum(String type)
    {
        return (SchemaType) getEnum(SchemaType.class, type);
    }

    @SuppressWarnings("unchecked")
	public static Map<String,SchemaType> getEnumMap()
    {
        return getEnumMap(SchemaType.class);
    }

    @SuppressWarnings("unchecked")
    public static List<SchemaType> getEnumList()
    {
        return getEnumList(SchemaType.class);
    }

    @SuppressWarnings("unchecked")
    public static Iterator<SchemaType> iterator()
    {
        return iterator(SchemaType.class);
    }

}
