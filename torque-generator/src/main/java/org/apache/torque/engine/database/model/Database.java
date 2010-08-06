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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.EngineException;
import org.apache.torque.engine.database.transform.DTDResolver;
import org.apache.torque.engine.platform.Platform;
import org.apache.torque.engine.platform.PlatformFactory;
import org.xml.sax.Attributes;


/**
 * A class for holding application data structures.
 *
 * @author <a href="mailto:leon@opticode.co.za>Leon Messerschmidt</a>
 * @author <a href="mailto:jmcnally@collab.net>John McNally</a>
 * @author <a href="mailto:mpoeschl@marmot.at>Martin Poeschl</a>
 * @author <a href="mailto:dlr@collab.net>Daniel Rall</a>
 * @author <a href="mailto:byron_foster@byron_foster@yahoo.com>Byron Foster</a>
 * @author <a href="mailto:monroe@dukece.com>Greg Monroe</a>
 * @version $Id: Database.java,v 1.1.6.1 2008-04-01 04:07:48 jkeller Exp $
 */
public class Database
{
    /** Logging class from commons.logging */
    private static Log log = LogFactory.getLog(Database.class);

    private String databaseType = null;
    private List tableList = new ArrayList(100);
    private Map domainMap = new HashMap();
    private String name;
    private String javaName;
    private String pkg;
    private String baseClass;
    private String basePeer;
    private String defaultIdMethod;
    private String defaultJavaType;
    private String defaultJavaNamingMethod;
    private Hashtable tablesByName = new Hashtable();
    private Hashtable tablesByJavaName = new Hashtable();
    private boolean heavyIndexing;
    /** the name of the definition file */
    private String fileName;
    private Map options = Collections.synchronizedMap(new ListOrderedMap());


    /**
     * Creates a new instance for the specified database type.
     *
     * @param databaseType The default type for this database.
     */
    public Database(String databaseType)
    {
        this.databaseType = databaseType;
    }

    /**
     * Load the database object from an xml tag.
     *
     * @param attrib the xml attributes
     */
    public void loadFromXML(Attributes attrib)
    {
        setName(attrib.getValue("name"));
        pkg = attrib.getValue("package");
        baseClass = attrib.getValue("baseClass");
        basePeer = attrib.getValue("basePeer");
        defaultJavaType = attrib.getValue("defaultJavaType");
        defaultIdMethod = attrib.getValue("defaultIdMethod");
        defaultJavaNamingMethod = attrib.getValue("defaultJavaNamingMethod");
        if (defaultJavaNamingMethod == null)
        {
            defaultJavaNamingMethod = NameGenerator.CONV_METHOD_UNDERSCORE;
        }
        heavyIndexing = "true".equals(attrib.getValue("heavyIndexing"));
    }

    /**
     * Get the name of the Database
     *
     * @return name of the Database
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the name of the Database
     *
     * @param name name of the Database
     */
    public void setName(String name)
    {
        /** @task check this */
//        this.name = (name == null ? Torque.getDefaultDB() : name);
        this.name = (name == null ? "default" : name);
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String name)
    {
        this.fileName = name;
    }

    /**
     * Get the value of package.
     * @return value of package.
     */
    public String getPackage()
    {
        return pkg;
    }

    /**
     * Set the value of package.
     * @param v  Value to assign to package.
     */
    public void setPackage(String v)
    {
        this.pkg = v;
    }

    /**
     * Get the value of baseClass.
     * @return value of baseClass.
     */
    public String getBaseClass()
    {
        if (baseClass == null)
        {
            return "BaseObject";
        }
        return baseClass;
    }

    /**
     * Set the value of baseClass.
     * @param v  Value to assign to baseClass.
     */
    public void setBaseClass(String v)
    {
        this.baseClass = v;
    }

    /**
     * Get the value of basePeer.
     * @return value of basePeer.
     */
    public String getBasePeer()
    {
        if (basePeer == null)
        {
            return "BasePeer";
        }
        return basePeer;
    }

    /**
     * Set the value of basePeer.
     * @param v Value to assign to basePeer.
     */
    public void setBasePeer(String v)
    {
        this.basePeer = v;
    }

    /**
     * Get the value of defaultIdMethod.
     * @return value of defaultIdMethod.
     */
    public String getDefaultIdMethod()
    {
        return defaultIdMethod;
    }

    /**
     * Set the value of defaultIdMethod.
     * @param v Value to assign to defaultIdMethod.
     */
    public void setDefaultIdMethod(String v)
    {
        this.defaultIdMethod = v;
    }

    /**
     * Get type to use in Java sources (primitive || object)
     *
     * @return the type to use
     */
    public String getDefaultJavaType()
    {
        return defaultJavaType;
    }

    /**
     * Get the value of defaultJavaNamingMethod which specifies the
     * method for converting schema names for table and column to Java names.
     *
     * @return The default naming conversion used by this database.
     */
    public String getDefaultJavaNamingMethod()
    {
        return defaultJavaNamingMethod;
    }

    /**
     * Set the value of defaultJavaNamingMethod.
     * @param v The default naming conversion for this database to use.
     */
    public void setDefaultJavaNamingMethod(String v)
    {
        this.defaultJavaNamingMethod = v;
    }

    /**
     * Get the value of heavyIndexing.
     * @return value of heavyIndexing.
     */
    public boolean isHeavyIndexing()
    {
        return heavyIndexing;
    }

    /**
     * Set the value of heavyIndexing.
     * @param v  Value to assign to heavyIndexing.
     */
    public void setHeavyIndexing(boolean v)
    {
        this.heavyIndexing = v;
    }

    /**
     * Return an List of all tables
     *
     * @return List of all tables
     */
    public List getTables()
    {
        return tableList;
    }

    /**
     * Return the table with the specified name.
     *
     * @param name table name
     * @return A Table object.  If it does not exist it returns null
     */
    public Table getTable(String name)
    {
        return (Table) tablesByName.get(name);
    }

    /**
     * Return the table with the specified javaName.
     *
     * @param javaName name of the java object representing the table
     * @return A Table object.  If it does not exist it returns null
     */
    public Table getTableByJavaName(String javaName)
    {
        return (Table) tablesByJavaName.get(javaName);
    }

    /**
     * An utility method to add a new table from an xml attribute.
     *
     * @param attrib the xml attributes
     * @return the created Table
     */
    public Table addTable(Attributes attrib)
    {
        Table tbl = new Table();
        tbl.setDatabase(this);
        tbl.loadFromXML(attrib, this.getDefaultIdMethod());
        addTable(tbl);
        return tbl;
    }

    /**
     * Add a table to the list and sets the Database property to this Database
     *
     * @param tbl the table to add
     */
    public void addTable(Table tbl)
    {
        tbl.setDatabase(this);
        tableList.add(tbl);
        tablesByName.put(tbl.getName(), tbl);
        tablesByName.put(tbl.getName().toLowerCase(), tbl);
        tablesByName.put(tbl.getName().toUpperCase(), tbl);
        tablesByJavaName.put(tbl.getJavaName(), tbl);
        tbl.setPackage(getPackage());
    }

    public void addDomain(Domain domain)
    {
        domainMap.put(domain.getName(), domain);
    }

    public Domain getDomain(String domainName)
    {
        return (Domain) domainMap.get(domainName);
    }

    protected String getDatabaseType()
    {
        return databaseType;
    }

    public void setDatabaseType(String databaseType)
    {
        this.databaseType = databaseType;
    }

    /**
     * Returns the Platform implementation for this database.
     *
     * @return a Platform implementation
     */
    public Platform getPlatform()
    {
        return PlatformFactory.getPlatformFor(databaseType);
    }

    /**
     * Determines if this database will be using the
     * <code>IDMethod.ID_BROKER</code> to create ids for torque OM
     * objects.
     * @return true if there is at least one table in this database that
     * uses the <code>IDMethod.ID_BROKER</code> method of generating
     * ids. returns false otherwise.
     */
    public boolean requiresIdTable()
    {
        Iterator iter = getTables().iterator();
        while (iter.hasNext())
        {
            Table table = (Table) iter.next();
            if (table.getIdMethod().equals(IDMethod.ID_BROKER))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Initializes the model.
     *
     * @throws EngineException
     */
    public void doFinalInitialization() throws EngineException
    {
        Iterator iter = getTables().iterator();
        while (iter.hasNext())
        {
            Table currTable = (Table) iter.next();

            // check schema integrity
            // if idMethod="autoincrement", make sure a column is
            // specified as autoIncrement="true"
            // FIXME: Handle idMethod="native" via DB adapter.
            // TODO autoincrement is no longer supported!!!
            if (currTable.getIdMethod().equals("autoincrement"))
            {
                boolean foundOne = false;
                Iterator colIter = currTable.getColumns().iterator();
                while (colIter.hasNext() && !foundOne)
                {
                    foundOne = ((Column) colIter.next()).isAutoIncrement();
                }

                if (!foundOne)
                {
                    String errorMessage = "Table '" + currTable.getName()
                            + "' is marked as autoincrement, but it does not "
                            + "have a column which declared as the one to "
                            + "auto increment (i.e. autoIncrement=\"true\")\n";
                    throw new EngineException("Error in XML schema: " + errorMessage);
                }
            }

            currTable.doFinalInitialization();

            // setup reverse fk relations
            Iterator fks = currTable.getForeignKeys().iterator();
            while (fks.hasNext())
            {
                ForeignKey currFK = (ForeignKey) fks.next();
                Table foreignTable = getTable(currFK.getForeignTableName());
                if (foreignTable == null)
                {
                    throw new EngineException("Attempt to set foreign"
                            + " key to nonexistent table, "
                            + currFK.getForeignTableName());
                }
                else
                {
                    // TODO check type and size
                    List referrers = foreignTable.getReferrers();
                    if ((referrers == null || !referrers.contains(currFK)))
                    {
                        foreignTable.addReferrer(currFK);
                    }

                    // local column references
                    Iterator localColumnNames = currFK.getLocalColumns().iterator();
                    while (localColumnNames.hasNext())
                    {
                        Column local = currTable
                                .getColumn((String) localColumnNames.next());
                        // give notice of a schema inconsistency.
                        // note we do not prevent the npe as there is nothing
                        // that we can do, if it is to occur.
                        if (local == null)
                        {
                            throw new EngineException("Attempt to define foreign"
                                    + " key with nonexistent column in table, "
                                    + currTable.getName());
                        }
                        else
                        {
                            //check for foreign pk's
                            if (local.isPrimaryKey())
                            {
                                currTable.setContainsForeignPK(true);
                            }
                        }
                    }

                    // foreign column references
                    Iterator foreignColumnNames
                            = currFK.getForeignColumns().iterator();
                    while (foreignColumnNames.hasNext())
                    {
                        String foreignColumnName = (String) foreignColumnNames.next();
                        Column foreign = foreignTable.getColumn(foreignColumnName);
                        // if the foreign column does not exist, we may have an
                        // external reference or a misspelling
                        if (foreign == null)
                        {
                            throw new EngineException("Attempt to set foreign"
                                    + " key to nonexistent column: table="
                                    +  currTable.getName() + ", foreign column="
                                    +  foreignColumnName);
                        }
                        else
                        {
                            foreign.addReferrer(currFK);
                        }
                    }
                }
            }
        }
    }

    /**
     * Get the base name to use when creating related Java Classes.
     *
     * @return A Java syntax capatible version of the dbName using the method
     *         defined by the defaultJavaNamingMethod XML value.
     */
    public String getJavaName()
    {
        if (javaName == null)
        {
            List inputs = new ArrayList(2);
            inputs.add(name);
            inputs.add(defaultJavaNamingMethod);
            try
            {
                javaName = NameFactory.generateName(NameFactory.JAVA_GENERATOR,
                                                    inputs);
            }
            catch (EngineException e)
            {
                log.error(e, e);
            }
        }
        return javaName;
    }

    /**
     * Convert dbName to a Java compatible name by the JavaName method only
     * (ignores the defaultJavaNamingMethod).
     *
     * @return The current dbName converted to a standard format that can
     *          be used as part of a Java Object name.
     */
    public String getStandardJavaName()
    {
        if (javaName == null)
        {
            List inputs = new ArrayList(2);
            inputs.add(name);
            inputs.add(NameGenerator.CONV_METHOD_JAVANAME);
            try
            {
                javaName = NameFactory.generateName(NameFactory.JAVA_GENERATOR,
                                                    inputs);
            }
            catch (EngineException e)
            {
                log.error(e, e);
            }
        }
        return javaName;
    }

    /**
     * Creats a string representation of this Database.
     * The representation is given in xml format.
     *
     * @return string representation in xml
     */
    public String toString()
    {
        StringBuffer result = new StringBuffer();

        result.append ("<?xml version=\"1.0\"?>\n");
        result.append ("<!DOCTYPE database SYSTEM \""
                + DTDResolver.WEB_SITE_DTD + "\">\n");
        result.append("<!-- Autogenerated by SQLToXMLSchema! -->\n");
        result.append("<database name=\"").append(getName()).append('"')
            .append(" package=\"").append(getPackage()).append('"')
            .append(" defaultIdMethod=\"").append(getDefaultIdMethod())
            .append('"')
            .append(" baseClass=\"").append(getBaseClass()).append('"')
            .append(" basePeer=\"").append(getBasePeer()).append('"')
            .append(">\n");

        for (Iterator i = tableList.iterator(); i.hasNext();)
        {
            result.append(i.next());
        }

        result.append("</database>");
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
