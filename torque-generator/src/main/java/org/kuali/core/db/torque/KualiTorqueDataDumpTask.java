package org.kuali.core.db.torque;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Database;
import org.apache.torque.engine.database.model.Table;
import org.apache.torque.engine.platform.Platform;
import org.apache.torque.engine.platform.PlatformFactory;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.DocumentTypeImpl;
import org.apache.xerces.util.XMLChar;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Element;

import static java.sql.Types.*;

/**
 * This task exports the tables specified by the schema XML resource to the file system. One table per XML file. If the
 * schema XML resource cannot be located, all tables from the schema specified will be exported.
 */
public class KualiTorqueDataDumpTask extends Task {
	Utils utils = new Utils();
	private static final String FS = System.getProperty("file.separator");

	/**
	 * Database we are exporting
	 */
	Database database;

	/**
	 * Encoding to use
	 */
	private String encoding;

	/**
	 * JDBC URL
	 */
	private String url;

	/**
	 * JDBC driver
	 */
	private String driver;

	/**
	 * Database username
	 */
	private String username;

	/**
	 * Database schema
	 */
	private String schema;

	/**
	 * Database password
	 */
	private String password;

	/**
	 * XML file describing the schema
	 */
	private String schemaXMLFile;

	/**
	 * Oracle, mysql, postgres, etc
	 */
	private String databaseType;

	/**
	 * The database connection used to retrieve the data to export
	 */
	private Connection connection;

	/**
	 * The directory where XML files will be written
	 */
	private File outputDirectory;

	/**
	 * The format to use for dates/timestamps
	 */
	private String dateFormat = "yyyyMMddHHmmss";

	/**
	 * Dump the data to XML files
	 */
	public void execute() throws BuildException {

		log("Impex - Starting Data Export");
		log("Driver: " + getDriver());
		log("URL: " + getUrl());
		log("Username: " + getUsername());
		log("Schema: " + getSchema());
		log("Encoding: " + utils.getEncoding(getEncoding()));

		try {

			// See if we can locate a schema XML
			boolean exists = new Utils().isFileOrResource(getSchemaXMLFile());
			if (exists) {
				// Get an xml parser for the schema XML
				KualiXmlToAppData xmlParser = new KualiXmlToAppData(getDatabaseType(), "");
				// Parse schema XML into a database object
				Database database = xmlParser.parseResource(getSchemaXMLFile());
				setDatabase(database);
				log("Schema XML: " + utils.getFilename(getSchemaXMLFile()));
			} else {
				log("Unable to locate " + getSchemaXMLFile(), Project.MSG_WARN);
				log("Exporting ALL tables");
			}

			// Initialize JDBC and establish a connection to the db
			Class.forName(getDriver());
			connection = DriverManager.getConnection(getUrl(), getUsername(), getPassword());
			log("DB connection established", Project.MSG_DEBUG);

			// Generate the XML
			generateXML(connection);
		} catch (Exception e) {
			throw new BuildException(e);
		} finally {
			closeQuietly(connection);
		}
	}

	/**
	 * Generate a SQL statement that selects all data from the table
	 */
	protected String getDataSelectStatement(Platform platform, DatabaseMetaData dbMetaData, String tableName) throws SQLException {
		StringBuffer sb = new StringBuffer("SELECT * FROM ");
		sb.append(tableName);
		sb.append(" ORDER BY 'x'");
		List<String> pkFields = platform.getPrimaryKeys(dbMetaData, getSchema(), tableName);
		for (String field : pkFields) {
			sb.append(", ").append(field);
		}
		return sb.toString();
	}

	/**
	 * Generate an array of Column objects from the result set metadata
	 */
	protected Column[] getColumns(ResultSetMetaData md) throws SQLException {
		Column[] columns = new Column[md.getColumnCount() + 1];
		for (int i = 1; i <= md.getColumnCount(); i++) {
			Column column = new Column();
			column.setName(md.getColumnName(i));
			column.setJdbcType(md.getColumnType(i));
			columns[i] = column;
		}
		return columns;
	}

	/**
	 * Extract a column value from the result set, converting as needed
	 */
	protected Object getColumnValue(ResultSet rs, int index, Column column) throws SQLException {
		// Extract a raw object
		Object columnValue = rs.getObject(index);
		// If it is null we're done
		if (columnValue == null) {
			return null;
		}
		// Handle special types
		switch (column.getJdbcType()) {
		case (CLOB):
			// Extract a CLOB
			return getClob((Clob) columnValue);
		case (DATE):
		case (TIMESTAMP):
			// Extract dates and timestamps
			return getDate(rs, index);
		default:
			// Otherwise return the raw object
			return columnValue;
		}
	}

	/**
	 * Convert a JDBC Timestamp into a java.util.Date using the format they specified
	 */
	protected String getDate(ResultSet rs, int index) throws SQLException {
		SimpleDateFormat df = new SimpleDateFormat(getDateFormat());
		Timestamp date = rs.getTimestamp(index);
		return df.format(date);
	}

	/**
	 * Convert a CLOB to a String
	 */
	protected String getClob(Clob clob) throws SQLException {
		Reader r = null;
		StringBuffer sb = new StringBuffer();
		try {
			r = clob.getCharacterStream();
			char[] buffer = new char[4096];
			int len;
			while ((len = r.read(buffer)) != -1) {
				sb.append(buffer, 0, len);
			}
		} catch (IOException e) {
			throw new SQLException(e);
		} finally {
			IOUtils.closeQuietly(r);
		}
		return sb.toString();
	}

	/**
	 * Convert a row from the result set into an Element
	 */
	protected Element getRow(DocumentImpl doc, String tableName, ResultSetMetaData md, ResultSet rs, Column[] columns) throws SQLException {
		// Generate a row object
		Element row = doc.createElement(tableName);
		// Cycle through the result set columns
		int colCount = md.getColumnCount();
		for (int i = 1; i <= colCount; i++) {
			// Extract a column value
			Object columnValue = getColumnValue(rs, i, columns[i]);
			// Null values can be omitted from the XML
			if (columnValue == null) {
				continue;
			}
			// Otherwise, escape the String and add it to the row Element
			row.setAttribute(columns[i].getName(), xmlEscape(columnValue.toString()));
		}
		// Return our Element that represents one row of JDBC data from the ResultSet
		return row;
	}

	/**
	 * Generate and return the dataset Element
	 */
	protected Element getDatasetNode(DocumentImpl document, Platform platform, DatabaseMetaData dbMetaData, String tableName) throws SQLException {
		Element datasetNode = document.createElement("dataset");
		Statement stmt = null;
		ResultSet rs = null;
		try {
			// This query selects everything from the table
			String query = getDataSelectStatement(platform, dbMetaData, tableName);
			stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(query);
			ResultSetMetaData md = rs.getMetaData();
			Column[] columns = getColumns(md);
			int count = 0;
			// Process the ResultSet
			while (rs.next()) {
				count++;
				log("processing row of " + tableName, Project.MSG_DEBUG);
				Element row = getRow(document, tableName, md, rs, columns);
				datasetNode.appendChild(row);
			}
			// Keep track of how many rows we found
			if (count == 0) {
				log("No data found in table " + tableName, Project.MSG_DEBUG);
				return null;
			}
		} catch (Exception e) {
			throw new SQLException(e);
		} finally {
			// Clean up
			closeQuietly(rs);
			closeQuietly(stmt);
		}
		return datasetNode;
	}

	/**
	 * Return the systemId to use
	 */
	protected String getSystemId() {
		if (getDatabase() != null && getDatabase().getName() != null) {
			return getDatabase().getName() + "-data.dtd";
		} else if (getSchema() != null) {
			return getSchema().toLowerCase() + "-data.dtd";
		} else {
			return "data.dtd";
		}

	}

	/**
	 * Return the XML Document object that we will serialize to disk
	 */
	protected DocumentImpl getDocument(String tableName, Platform platform, DatabaseMetaData dbMetaData) throws SQLException {
		// Generate the document type
		DocumentTypeImpl docType = new DocumentTypeImpl(null, "dataset", null, getSystemId());
		// Generate an empty document
		DocumentImpl doc = new DocumentImpl(docType);
		// Extract the data from the table
		Element datasetNode = getDatasetNode(doc, platform, dbMetaData, tableName);
		if (datasetNode == null) {
			// There was no data (zero rows), we are done
			return null;
		}
		// Add the dataset to the document
		doc.appendChild(datasetNode);
		// Return what we found
		return doc;
	}

	/**
	 * <code>
	 * Convert a List<Table> into a List<String> of table names
	 * </code>
	 */
	protected List<String> getTableNamesFromTableObjects(List<?> list) {
		List<String> names = new ArrayList<String>();
		for (Object object : list) {
			Table table = (Table) object;
			names.add(table.getName());
		}
		return names;
	}

	/**
	 * Convert a List to a Set
	 * 
	 * @param list
	 * @return
	 */
	protected Set<String> getSet(List<String> list) {
		Set<String> set = new TreeSet<String>();
		set.addAll(list);
		return set;
	}

	/**
	 * Generate XML from the data in the tables in the database
	 */
	protected void generateXML(Connection con) throws Exception {
		// Get metadata about the database
		DatabaseMetaData dbMetaData = con.getMetaData();
		// Get the correct platform (oracle, mysql etc)
		Platform platform = PlatformFactory.getPlatformFor(getDatabaseType());
		// Get ALL the table names
		Set<String> jdbcTableNames = getSet(getJDBCTableNames(dbMetaData));
		log("JDBC Table Count: " + jdbcTableNames.size());
		// Do we have a valid schema XML file?
		boolean exists = new Utils().isFileOrResource(getSchemaXMLFile());
		if (exists) {
			// If so, only export data for tables that are listed in the schema XML
			Set<String> schemaXMLNames = getSet(getTableNamesFromTableObjects(getDatabase().getTables()));
			// These are tables that are in JDBC but not in schema XML
			Set<String> extraTables = SetUtils.difference(jdbcTableNames, schemaXMLNames);
			// These are tables that are in schema XML but not in JDBC (should always be zero)
			Set<String> missingTables = SetUtils.difference(schemaXMLNames, jdbcTableNames);
			// These are tables that are in both JDBC and the schema XML
			Set<String> intersection = SetUtils.intersection(jdbcTableNames, schemaXMLNames);
			// Log what we are up to
			log("Schema XML Table Count: " + schemaXMLNames.size());
			log("Tables present in both: " + intersection.size());
			log("Tables in JDBC that will not be exported: " + extraTables.size());
			if (missingTables.size() > 0) {
				throw new BuildException("There are " + missingTables.size() + " tables defined in " + getSchemaXMLFile() + " that are not being returned by JDBC [" + missingTables + "]");
			}
			// Process only those tables that are in both
			processTables(intersection, platform, dbMetaData);
		} else {
			// Process all the tables
			processTables(jdbcTableNames, platform, dbMetaData);
		}

	}

	/**
	 * Process the tables, keeping track of which tables had at least one row of data
	 */
	protected void processTables(Set<String> tableNames, Platform platform, DatabaseMetaData dbMetaData) throws IOException, SQLException {
		long start = System.currentTimeMillis();
		int exportCount = 0;
		int skipCount = 0;
		for (String tableName : tableNames) {
			boolean exported = processTable(tableName, platform, dbMetaData);
			if (exported) {
				exportCount++;
			} else {
				skipCount++;
			}
		}
		long elapsed = System.currentTimeMillis() - start;
		log(utils.pad("Processed " + tableNames.size() + " tables", elapsed));
		log("Exported data from " + exportCount + " tables to XML");
		log("Skipped " + skipCount + " tables that had zero rows");
	}

	/**
	 * Process one table. Only create an XML file if there is at least one row of data
	 */
	protected boolean processTable(String tableName, Platform platform, DatabaseMetaData dbMetaData) throws SQLException, IOException {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);
		nf.setMinimumFractionDigits(1);
		log("Processing: " + tableName, Project.MSG_DEBUG);
		long ts1 = System.currentTimeMillis();
		DocumentImpl doc = getDocument(tableName, platform, dbMetaData);
		long ts2 = System.currentTimeMillis();
		log(utils.pad("Extracting: " + tableName + " ", ts2 - ts1), Project.MSG_DEBUG);
		boolean exported = false;
		if (doc != null) {
			serialize(tableName, doc);
			exported = true;
		}
		long ts3 = System.currentTimeMillis();
		log(utils.pad("Serializing: " + tableName + " ", ts3 - ts2), Project.MSG_DEBUG);
		log(utils.pad(tableName, (ts3 - ts1)));
		return exported;
	}

	/**
	 * This is where the XML will be written to
	 */
	protected Writer getWriter(String tableName) throws FileNotFoundException {
		String filename = getOutputDirectory() + FS + tableName + ".xml";
		log("filename:" + filename, Project.MSG_DEBUG);
		return new PrintWriter(new FileOutputStream(filename));
	}

	/**
	 * This is the XMLSerializer responsible for outputting the XML document
	 */
	protected XMLSerializer getSerializer(Writer out) {
		return new XMLSerializer(out, new OutputFormat(Method.XML, getEncoding(), true));
	}

	/**
	 * Serialize the document
	 */
	protected void serialize(String tableName, DocumentImpl doc) throws IOException {
		Writer out = null;
		try {
			out = getWriter(tableName);
			XMLSerializer serializer = getSerializer(out);
			serializer.serialize(doc);
			out.flush();
		} catch (IOException e) {
			throw e;
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * Escape characters that would cause issues for XML parsers
	 */
	protected String xmlEscape(String st) {
		StringBuffer buff = new StringBuffer();
		char[] block = st.toCharArray();
		String stEntity = null;
		int i, last;

		for (i = 0, last = 0; i < block.length; i++) {
			if (XMLChar.isInvalid(block[i])) {
				stEntity = " ";
			}
			if (stEntity != null) {
				buff.append(block, last, i - last);
				buff.append(stEntity);
				stEntity = null;
				last = i + 1;
			}
		}
		if (last < block.length) {
			buff.append(block, last, i - last);
		}
		return buff.toString();
	}

	/**
	 * Get the names of all the tables in our schema
	 */
	public List<String> getJDBCTableNames(DatabaseMetaData dbMeta) throws SQLException {
		// these are the entity types we want from the database
		String[] types = { "TABLE" }; // JHK: removed views from list
		List<String> tables = new ArrayList<String>();
		ResultSet tableNames = null;
		try {
			// JHK: upper-cased schema name (required by Oracle)
			tableNames = dbMeta.getTables(null, getSchema().toUpperCase(), null, types);
			while (tableNames.next()) {
				String name = tableNames.getString(3);
				tables.add(name);
			}
		} finally {
			closeQuietly(tableNames);
		}
		log("Found " + tables.size() + " tables.");
		return tables;
	}

	/**
	 * Close both, ignoring exceptions
	 */
	protected void closeQuietly(ResultSet rs, Connection c) {
		closeQuietly(rs);
		closeQuietly(c);
	}

	/**
	 * Close, ignoring exceptions
	 */
	protected void closeQuietly(Statement stmt) {
		if (stmt == null) {
			return;
		}
		try {
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Close, ignoring exceptions
	 */
	protected void closeQuietly(Connection c) {
		if (c == null) {
			return;
		}
		try {
			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Close, ignoring exceptions
	 */
	protected void closeQuietly(ResultSet rs) {
		if (rs == null) {
			return;
		}
		try {
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDatabaseType() {
		return databaseType;
	}

	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}

	public String getSchemaXMLFile() {
		return schemaXMLFile;
	}

	public void setSchemaXMLFile(String schemaXMLFile) {
		this.schemaXMLFile = schemaXMLFile;
	}

	public File getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public Database getDatabase() {
		return database;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}
}
