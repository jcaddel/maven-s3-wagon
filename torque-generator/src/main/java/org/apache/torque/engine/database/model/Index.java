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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.torque.engine.EngineException;

import org.xml.sax.Attributes;

/**
 * Information about indices of a table.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:dlr@finemaltcoding.com>Daniel Rall</a>
 * @author <a href="mailto:monroe@dukece.com>Greg Monroe</a>
 * @version $Id: Index.java,v 1.1 2007-10-21 07:57:27 abyrne Exp $
 */
public class Index
{
    /** Logging class from commons.logging */
    private static Log log = LogFactory.getLog(Index.class);
    /** name of the index */
    private String indexName;
    /** table */
    private Table parentTable;
    /** columns */
    private List indexColumns;
    /** The XML Options specified for this index */
    private Map options;


    /**
     * Creates a new instance with default characteristics (no name or
     * parent table, small column list size allocation, non-unique).
     */
    public Index()
    {
        indexColumns = new ArrayList(3);
        options = Collections.synchronizedMap(new ListOrderedMap());
    }

    /**
     * Creates a new instance for the list of columns composing an
     * index.  Otherwise performs as {@link #Index()}.
     *
     * @param table The table this index is associated with.
     * @param indexColumns The list of {@link
     * org.apache.torque.engine.database.model.Column} objects which
     * make up this index.  Cannot be empty.
     * @exception EngineException Error generating name.
     * @see #Index()
     */
    protected Index(Table table, List indexColumns)
        throws EngineException
    {
        this();
        setTable(table);
        if (!indexColumns.isEmpty())
        {
            this.indexColumns = indexColumns;

            if (log.isDebugEnabled())
            {
                log.debug("Created Index named " + getName()
                        + " with " + indexColumns.size() + " columns");
            }
        }
        else
        {
            throw new EngineException("Cannot create a new Index using an "
                    + "empty list Column object");
        }
    }

    /**
     * Imports index from an XML specification
     *
     * @param attrib the xml attributes
     */
    public void loadFromXML(Attributes attrib)
    {
        indexName = attrib.getValue("name");
    }

    /**
     * Returns the uniqueness of this index.
     *
     * @return the uniqueness of this index
     */
    public boolean isUnique()
    {
        return false;
    }

    /**
     * Gets the name of this index.
     *
     * @return the name of this index
     */
    public String getName()
    {
        return indexName;
    }

    /**
     * Set the name of this index.
     *
     * @param name the name of this index
     */
    public void setName(String name)
    {
        this.indexName = name;
    }

    /**
     * Set the parent Table of the index
     *
     * @param parent the table
     */
    public void setTable(Table parent)
    {
        parentTable = parent;
    }

    /**
     * Get the parent Table of the index
     *
     * @return the table
     */
    public Table getTable()
    {
        return parentTable;
    }

    /**
     * Returns the Name of the table the index is in
     *
     * @return the name of the table
     */
    public String getTableName()
    {
        return parentTable.getName();
    }

    /**
     * Adds a new column to an index.
     *
     * @param attrib xml attributes for the column
     */
    public void addColumn(Attributes attrib)
    {
        indexColumns.add(attrib.getValue("name"));
    }

    /**
     * Return a comma delimited string of the columns which compose this index.
     *
     * @return a list of column names
     */
    public String getColumnList()
    {
        return Column.makeList(getColumns());
    }

    /**
     * Return the list of local columns. You should not edit this list.
     *
     * @return a list of columns
     */
    public List getColumns()
    {
        return indexColumns;
    }

    /**
     * Returns the list of names of the columns referenced by this
     * index.  Slightly over-allocates the list's buffer (just in case
     * more elements are going to be added, such as when a name is
     * being generated).  Feel free to modify this list.
     *
     * @return a list of column names
     */
    protected List getColumnNames()
    {
        List names = new ArrayList(indexColumns.size() + 2);
        Iterator i = getColumns().iterator();
        while (i.hasNext())
        {
            Column c = (Column) i.next();
            names.add(c.getName());
        }
        return names;
    }

    /**
     * String representation of the index. This is an xml representation.
     *
     * @return a xml representation
     */
    public String toString()
    {
        StringBuffer result = new StringBuffer();
        result.append(" <index name=\"")
              .append(getName())
              .append("\"");

        result.append(">\n");

        for (int i = 0; i < indexColumns.size(); i++)
        {
            result.append("  <index-column name=\"")
                .append(indexColumns.get(i))
                .append("\"/>\n");
        }
        result.append(" </index>\n");
        return result.toString();
    }

    /**
     * Add an XML Specified option key/value pair to this element's option set.
     *
     * @param key the key of the option.
     * @param value the value of the option.
     */
    public void addOption(String key, String value)
    {
        options.put(key, value);
    }

    /**
     * Get the value that was associated with this key in an XML option
     * element.
     *
     * @param key the key of the option.
     * @return The value for the key or a null.
     */
    public String getOption(String key)
    {
        return (String) options.get(key);
    }

    /**
     * Gets the full ordered hashtable array of items specified by XML option
     * statements under this element.<p>
     *
     * Note, this is not thread save but since it's only used for
     * generation which is single threaded, there should be minimum
     * danger using this in Velocity.
     *
     * @return An Map of all options. Will not be null but may be empty.
     */
    public Map getOptions()
    {
        return options;
    }
}
