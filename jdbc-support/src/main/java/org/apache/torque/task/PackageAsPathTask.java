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

import org.apache.tools.ant.Task;
import org.apache.velocity.util.StringUtils;

/**
 * Simple task to convert packages to paths.
 *
 * @author <a href="mailto:stephenh@chase3000.com">Stephen Haberman</a>
 * @version $Id: PackageAsPathTask.java,v 1.1 2007-10-21 07:57:26 abyrne Exp $
 */
public class PackageAsPathTask extends Task
{

    /** The package to convert. */
    protected String pckg;

    /** The value to store the conversion in. */
    protected String name;

    /**
     * Executes the package to patch converstion and stores it
     * in the user property <code>value</code>.
     */
    public void execute()
    {
        super.getProject().setUserProperty(this.name,
                StringUtils.getPackageAsPath(this.pckg));
    }

    /**
     * @param pckg the package to convert
     */
    public void setPackage(String pckg)
    {
        this.pckg = pckg;
    }

    /**
     * @param name the Ant variable to store the path in
     */
    public void setName(String name)
    {
        this.name = name;
    }
}
