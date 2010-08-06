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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.torque.engine.EngineException;

/**
 * A <code>NameGenerator</code> implementation for table-specific
 * constraints.  Conforms to the maximum column name length for the
 * type of database in use.
 *
 * @author <a href="mailto:dlr@finemaltcoding.com>Daniel Rall</a>
 * @version $Id: ConstraintNameGenerator.java,v 1.1 2007-10-21 07:57:27 abyrne Exp $
 */
public class ConstraintNameGenerator implements NameGenerator
{
    /** Logging class from commons.logging */
    private static Log log = LogFactory.getLog(ConstraintNameGenerator.class);

    /**
     * First element of <code>inputs</code> should be of type {@link
     * org.apache.torque.engine.database.model.Database}, second
     * should be a table name, third is the type identifier (spared if
     * trimming is necessary due to database type length constraints),
     * and the fourth is a <code>Integer</code> indicating the number
     * of this contraint.
     *
     * @see org.apache.torque.engine.database.model.NameGenerator
     */
    public String generateName(List inputs)
        throws EngineException
    {
        StringBuffer name = new StringBuffer();
        Database db = (Database) inputs.get(0);
        name.append((String) inputs.get(1));
        String namePostfix = (String) inputs.get(2);
        String constraintNbr = inputs.get(3).toString();

        // Calculate maximum RDBMS-specific column character limit.
        int maxBodyLength = -1;
        try
        {
            int maxColumnNameLength = db.getPlatform().getMaxColumnNameLength();
            maxBodyLength = (maxColumnNameLength - namePostfix.length()
                    - constraintNbr.length() - 2);

            if (log.isDebugEnabled())
            {
                log.debug("maxColumnNameLength=" + maxColumnNameLength
                        + " maxBodyLength=" + maxBodyLength);
            }
        }
        catch (NumberFormatException maxLengthUnknown)
        {
        }

        // Do any necessary trimming.
        if (maxBodyLength != -1 && name.length() > maxBodyLength)
        {
            name.setLength(maxBodyLength);
        }

        name.append(STD_SEPARATOR_CHAR).append(namePostfix)
            .append(STD_SEPARATOR_CHAR).append(constraintNbr);

        return name.toString();
    }
}
