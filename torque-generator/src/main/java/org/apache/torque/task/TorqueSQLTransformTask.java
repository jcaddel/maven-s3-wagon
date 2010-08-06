package org.apache.torque.task;

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

import java.io.BufferedWriter;
import java.io.FileWriter;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import org.apache.torque.engine.database.model.Database;
import org.apache.torque.engine.database.transform.SQLToAppData;

/**
 * An ant task for creating an xml schema from an sql schema
 *
 * @author <a href="mailto:leon@opticode.co.za">Leon Messerschmidt</a>
 * @author <a href="mailto:jvanzyl@zenplex.com">Jason van Zyl</a>
 * @version $Id: TorqueSQLTransformTask.java,v 1.1 2007-10-21 07:57:26 abyrne Exp $
 */
public class TorqueSQLTransformTask extends Task
{
    /** SQL input file. */
    private String inputFile;

    /** XML descriptor output file. */
    private String outputFile;

    /**
     * Get the current input file
     *
     * @return the input file
     */
    public String getInputFile()
    {
        return inputFile;
    }

    /**
     * Set the sql input file.  This file must exist
     *
     * @param v the input file
     */
    public void setInputFile(String v)
    {
        inputFile = v;
    }

    /**
     * Get the current output file.
     *
     * @return the output file
     */
    public String getOutputFile()
    {
        return outputFile;
    }

    /**
     * Set the current output file.  If the file does not
     * exist it will be created.  If the file exists all
     * it's contents will be replaced.
     *
     * @param v the output file
     */
    public void setOutputFile (String v)
    {
        outputFile = v;
    }

    /**
     * Execute the task.
     *
     * @throws BuildException Any exceptions caught during procssing will be
     *         rethrown wrapped into a BuildException
     */
    public void execute() throws BuildException
    {
        try
        {
            log("Parsing SQL Schema", Project.MSG_INFO);

            SQLToAppData sqlParser = new SQLToAppData(inputFile);
            Database app = sqlParser.execute();

            log("Preparing to write xml schema", Project.MSG_INFO);
            FileWriter fr = new FileWriter(outputFile);
            BufferedWriter br = new BufferedWriter (fr);

            br.write(app.toString());

            log("Writing xml schema", Project.MSG_INFO);

            br.flush();
            br.close();
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }
    }
}
