package org.apache.torque.engine.sql;

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

/**
 * An Exception for parsing SQLToAppData.  This class
 * will probably get some extra features in future.
 *
 * @author <a href="mailto:leon@opticode.co.za">Leon Messerschmidt</a>
 * @version $Id: ParseException.java,v 1.1 2007-10-21 07:57:27 abyrne Exp $
 */
public class ParseException extends Exception
{
    /**
     * Serial version
     */
    private static final long serialVersionUID = -8595262272114680431L;

    /**
     * constructor.
     *
     * @param err error message
     */
    public ParseException(String err)
    {
        super(err);
    }
}
