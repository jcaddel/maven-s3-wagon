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
 * A single token returned by SQLScanner.  This class is used internally
 * by SQLScanner and you should probably never need to create objects
 * of this class unless you are working on SQLScanner.
 *
 * @author <a href="mailto:leon@opticode.co.za">Leon Messerschmidt</a>
 * @version $Id: Token.java,v 1.1 2007-10-21 07:57:27 abyrne Exp $
 */

public class Token
{
    /** string representation */
    private String str;
    /** line number */
    private int line;
    /** column number */
    private int col;

    /**
     * Creates a new token without positioning.
     *
     * @param str string representation
     */
    public Token(String str)
    {
        this (str, 0, 0);
    }

    /**
     * Creates a new token with positioning settings.
     *
     * @param str string representation
     * @param line line number
     * @param col column number
     */
    public Token(String str, int line, int col)
    {
        this.str = str;
        this.line = line;
        this.col = col;
    }

    /**
     * Returns the string representation of this token.
     *
     * @return the string representation
     */
    public String getStr()
    {
        return str;
    }

    /**
     * Get the line number of this token.
     *
     * @return the line number
     */
    public int getLine()
    {
        return line;
    }

    /**
     * Get the column number of this token.
     *
     * @return the column number
     */
    public int getCol()
    {
        return col;
    }

    /**
     * The same as getStr()
     *
     * @return the string representation
     */
    public String toString()
    {
        return str;
    }
}
