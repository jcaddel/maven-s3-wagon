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

import java.util.List;

/**
 * Information about unique columns of a table.  This class assumes
 * that in the underlying RDBMS, unique constraints and unique indices
 * are roughly equivalent.  For example, adding a unique constraint to
 * a column also creates an index on that column (this is known to be
 * true for MySQL and Oracle).
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 * @version $Id: Unique.java,v 1.1 2007-10-21 07:57:27 abyrne Exp $
 */
public class Unique extends Index
{
    /**
     * Returns <code>true</code>.
     *
     * @return true
     */
    public final boolean isUnique()
    {
        return true;
    }

    /**
     * String representation of the index. This is an xml representation.
     *
     * @return string representation in xml
     */
    public String toString()
    {
        StringBuffer result = new StringBuffer();
        result.append(" <unique name=\"")
            .append(getName())
            .append("\">\n");

        List columns = getColumns();
        for (int i = 0; i < columns.size(); i++)
        {
            result.append("  <unique-column name=\"")
                .append(columns.get(i))
                .append("\"/>\n");
        }
        result.append(" </unique>\n");
        return result.toString();
    }
}
