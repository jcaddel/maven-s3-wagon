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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Database;
import org.apache.torque.engine.database.model.Table;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A Class that is used to parse an input xml schema file and creates and AppData java structure.
 * 
 * @author <a href="mailto:leon@opticode.co.za">Leon Messerschmidt</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:mpoeschl@marmot.at">Martin Poeschl</a>
 * @author <a href="mailto:fedor.karpelevitch@home.com">Fedor Karpelevitch</a>
 * @version $Id: XmlToData.java,v 1.1 2007-10-21 07:57:26 abyrne Exp $
 */
public class XmlToData extends DefaultHandler implements EntityResolver {
	/** Logging class from commons.logging */
	private static Log log = LogFactory.getLog(XmlToData.class);
	private Database database;
	private List<Object> data;
	private File dtdFile;

	private static SAXParserFactory saxFactory;

	static {
		saxFactory = SAXParserFactory.newInstance();
		saxFactory.setValidating(true);
	}

	/**
	 * Default custructor
	 */
	public XmlToData(Database database, String dtdFilePath) throws MalformedURLException, IOException {
		this.database = database;
		this.dtdFile = new File(dtdFilePath);
	}

	/**
     *
     */
	public List<?> parseFile(String xmlFile) throws Exception {
		data = new ArrayList<Object>();

		SAXParser parser = saxFactory.newSAXParser();

		Reader r = new BufferedReader(new FileReader(xmlFile));
		try {
			InputSource is = new InputSource(r);
			is.setSystemId(dtdFile.getAbsolutePath());
			parser.parse(is, this);
		} finally {
			r.close();
		}
		return data;
	}

	/**
	 * Handles opening elements of the xml file.
	 */
	public void startElement(String uri, String localName, String rawName, Attributes attributes) throws SAXException {
		try {
			if (rawName.equals("dataset")) {
				// ignore <dataset> for now.
			} else {
				Table table = database.getTableByJavaName(rawName);

				if (table == null) {
					throw new SAXException("Table '" + rawName + "' unknown");
				}
				List<ColumnValue> columnValues = new ArrayList<ColumnValue>();
				for (int i = 0; i < attributes.getLength(); i++) {
					Column col = table.getColumnByJavaName(attributes.getQName(i));

					if (col == null) {
						throw new SAXException("Column " + attributes.getQName(i) + " in table " + rawName + " unknown.");
					}

					String value = attributes.getValue(i);
					columnValues.add(new ColumnValue(col, value));
				}
				data.add(new DataRow(table, columnValues));
			}
		} catch (Exception e) {
			throw new SAXException(e);
		}
	}

	/**
	 * called by the XML parser
	 * 
	 * @return an InputSource for the database.dtd file
	 */
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
		try {
			File systemIdFile = new File(systemId);
			if (dtdFile.equals(systemIdFile)) {
				log.info("Resolver: used " + dtdFile.getPath());
				return new InputSource(new BufferedInputStream(new FileInputStream(dtdFile)));
			} else {
				log.info("Resolver: used " + systemId);
				return getInputSource(systemId);
			}
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	/**
	 * get an InputSource for an URL String
	 * 
	 * @param urlString
	 * @return an InputSource for the URL String
	 */
	public InputSource getInputSource(String urlString) throws IOException {
		URL url = new URL(urlString);
		InputSource src = new InputSource(url.openStream());
		return src;
	}

	/**
     *
     */
	public class DataRow {
		private Table table;
		private List<ColumnValue> columnValues;

		public DataRow(Table table, List<ColumnValue> columnValues) {
			this.table = table;
			this.columnValues = columnValues;
		}

		public Table getTable() {
			return table;
		}

		public List<?> getColumnValues() {
			return columnValues;
		}
	}

	/**
     *
     */
	public class ColumnValue {
		private Column col;
		private String val;

		public ColumnValue(Column col, String val) {
			this.col = col;
			this.val = val;
		}

		public Column getColumn() {
			return col;
		}

		public String getValue() {
			return val;
		}

		public String getEscapedValue() {
			StringBuffer sb = new StringBuffer();
			sb.append("'");
			sb.append(StringUtils.replace(val, "'", "''"));
			sb.append("'");
			return sb.toString();
		}
	}
}
