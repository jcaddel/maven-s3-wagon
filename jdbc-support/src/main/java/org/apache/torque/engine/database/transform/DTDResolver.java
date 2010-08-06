package org.apache.torque.engine.database.transform;

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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A resolver to get the database.dtd file for the XML parser from the jar.
 *
 * @author <a href="mailto:mpoeschl@marmot.at">Martin Poeschl</a>
 * @author <a href="mailto:kschrader@karmalab.org">Kurt Schrader</a>
 * @author <a href="mailto:quintonm@bellsouth.net">Quinton McCombs</a>
 * @version $Id: DTDResolver.java,v 1.1 2007-10-21 07:57:26 abyrne Exp $
 */
public class DTDResolver implements EntityResolver
{
    /** Where the DTD is located on the web. */
    public static final String WEB_SITE_DTD
            = "http://db.apache.org/torque/dtd/database_3_3.dtd";

    /** Where the 3.2 DTD is located on the web. */
    public static final String WEB_SITE_DTD_3_2
            = "http://db.apache.org/torque/dtd/database_3_2.dtd";

    /** Logging class from commons.logging */
    private static Log log = LogFactory.getLog(DTDResolver.class);

    /**
     * constructor
     */
    public DTDResolver()
            throws SAXException
    {
    }

    /**
     * An implementation of the SAX <code>EntityResolver</code>
     * interface to be called by the XML parser.  If the dtd is the
     * current Torque DTD, the DTD is read from the generator jar.
     * In all other cases, null is returned to indicate that the parser
     * should open a regular connection to the systemId URI.
     *
     * @param publicId The public identifier of the external entity
     * @param systemId The system identifier of the external entity
     * @return An <code>InputSource</code> for the
     * <code>database.dtd</code> file in the generator jar, or null.
     */
    public InputSource resolveEntity(String publicId, String systemId)
            throws IOException, SAXException
    {
        if (WEB_SITE_DTD.equals(systemId))
        {
            return readFromClasspath("database.dtd");
        }
        else if (WEB_SITE_DTD_3_2.equals(systemId))
        {
            return readFromClasspath("database_3_2.dtd");
        }
        else
        {
            log.debug("Resolver: used default behaviour");
            return null;
        }
    }

    /**
     * Reads the resource with the given name from the classpath.
     *
     * @param resourceName the name of the resource to read
     * @return an Inputsource witht the content of the resource.
     *
     * @throws SAXException if the resource cannot be read.
     */
    private InputSource readFromClasspath(String resourceName)
        throws SAXException
    {
        try
        {
            InputStream dtdStream
                    = getClass().getResourceAsStream(resourceName);

            // getResource was buggy on many systems including Linux,
            // OSX, and some versions of windows in jdk1.3.
            // getResourceAsStream works on linux, maybe others?
            if (dtdStream != null)
            {
                String pkg = getClass().getName().substring(0,
                        getClass().getName().lastIndexOf('.'));
                log.debug("Resolver: used " + resourceName + " from '"
                         + pkg + "' package");
                return new InputSource(dtdStream);
            }
            else
            {
                log.warn("Could not locate database.dtd");
                return null;
            }
        }
        catch (Exception ex)
        {
            throw new SAXException(
                    "Could not get stream for " + resourceName,
                    ex);
        }
    }
}
