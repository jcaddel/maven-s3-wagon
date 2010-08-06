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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

/**
 * This task uses an SQL -> Database map in the form of a properties file to insert each SQL file listed into its
 * designated database.
 * 
 * @author <a href="mailto:jeff@custommonkey.org">Jeff Martin</a>
 * @author <a href="mailto:gholam@xtra.co.nz">Michael McCallum</A>
 * @author <a href="mailto:tim.stephenson@sybase.com">Tim Stephenson</A>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</A>
 * @author <a href="mailto:mpoeschl@marmot.at">Martin Poeschl</a>
 * @version $Id: TorqueSQLExec.java,v 1.1 2007-10-21 07:57:26 abyrne Exp $
 */
public class TorqueSQLExec extends Task {
	private int goodSql = 0;
	private int totalSql = 0;
	private Path classpath;
	private AntClassLoader loader;

	/**
     *
     */
	public static class DelimiterType extends EnumeratedAttribute {
		public static final String NORMAL = "normal";
		public static final String ROW = "row";

		public String[] getValues() {
			return new String[] { NORMAL, ROW };
		}
	}

	/** Database connection */
	private Connection conn = null;

	/** Autocommit flag. Default value is false */
	private boolean autocommit = false;

	/** SQL statement */
	private Statement statement = null;

	/** DB driver. */
	private String driver = null;

	/** DB url. */
	private String url = null;

	/** User name. */
	private String userId = null;

	/** Password */
	private String password = null;

	/** SQL Statement delimiter */
	private String delimiter = ";";

	/**
	 * The delimiter type indicating whether the delimiter will only be recognized on a line by itself
	 */
	private String delimiterType = DelimiterType.NORMAL;

	/** Print SQL results. */
	private boolean print = false;

	/** Print header columns. */
	private boolean showheaders = true;

	/** Results Output file. */
	private File output = null;

	/** RDBMS Product needed for this SQL. */
	private String rdbms = null;

	/** RDBMS Version needed for this SQL. */
	private String version = null;

	/** Action to perform if an error is found */
	private String onError = "abort";

	/** Encoding to use when reading SQL statements from a file */
	private String encoding = null;

	/** Src directory for the files listed in the sqldbmap. */
	private String srcDir;

	/** Properties file that maps an individual SQL file to a database. */
	private File sqldbmap;

	/**
	 * Set the sqldbmap properties file.
	 * 
	 * @param sqldbmap
	 *            filename for the sqldbmap
	 */
	public void setSqlDbMap(String sqldbmap) {
		this.sqldbmap = getProject().resolveFile(sqldbmap);
	}

	/**
	 * Get the sqldbmap properties file.
	 * 
	 * @return filename for the sqldbmap
	 */
	public File getSqlDbMap() {
		return sqldbmap;
	}

	/**
	 * Set the src directory for the sql files listed in the sqldbmap file.
	 * 
	 * @param srcDir
	 *            sql source directory
	 */
	public void setSrcDir(String srcDir) {
		this.srcDir = getProject().resolveFile(srcDir).toString();
	}

	/**
	 * Get the src directory for the sql files listed in the sqldbmap file.
	 * 
	 * @return sql source directory
	 */
	public String getSrcDir() {
		return srcDir;
	}

	/**
	 * Set the classpath for loading the driver.
	 * 
	 * @param classpath
	 *            the classpath
	 */
	public void setClasspath(Path classpath) {
		if (this.classpath == null) {
			this.classpath = classpath;
		} else {
			this.classpath.append(classpath);
		}
	}

	/**
	 * Create the classpath for loading the driver.
	 * 
	 * @return the classpath
	 */
	public Path createClasspath() {
		if (this.classpath == null) {
			this.classpath = new Path(getProject());
		}
		return this.classpath.createPath();
	}

	/**
	 * Set the classpath for loading the driver using the classpath reference.
	 * 
	 * @param r
	 *            reference to the classpath
	 */
	public void setClasspathRef(Reference r) {
		createClasspath().setRefid(r);
	}

	/**
	 * Set the sql command to execute
	 * 
	 * @param sql
	 *            sql command to execute
	 * @deprecated This method has no effect and will be removed in a future version.
	 */
	public void addText(String sql) {
	}

	/**
	 * Set the JDBC driver to be used.
	 * 
	 * @param driver
	 *            driver class name
	 */
	public void setDriver(String driver) {
		this.driver = driver;
	}

	/**
	 * Set the DB connection url.
	 * 
	 * @param url
	 *            connection url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Set the user name for the DB connection.
	 * 
	 * @param userId
	 *            database user
	 */
	public void setUserid(String userId) {
		this.userId = userId;
	}

	/**
	 * Set the file encoding to use on the sql files read in
	 * 
	 * @param encoding
	 *            the encoding to use on the files
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * Set the password for the DB connection.
	 * 
	 * @param password
	 *            database password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Set the autocommit flag for the DB connection.
	 * 
	 * @param autocommit
	 *            the autocommit flag
	 */
	public void setAutocommit(boolean autocommit) {
		this.autocommit = autocommit;
	}

	/**
	 * Set the statement delimiter.
	 * 
	 * <p>
	 * For example, set this to "go" and delimitertype to "ROW" for Sybase ASE or MS SQL Server.
	 * </p>
	 * 
	 * @param delimiter
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * Set the Delimiter type for this sql task. The delimiter type takes two values - normal and row. Normal means that
	 * any occurence of the delimiter terminate the SQL command whereas with row, only a line containing just the
	 * delimiter is recognized as the end of the command.
	 * 
	 * @param delimiterType
	 */
	public void setDelimiterType(DelimiterType delimiterType) {
		this.delimiterType = delimiterType.getValue();
	}

	/**
	 * Set the print flag.
	 * 
	 * @param print
	 */
	public void setPrint(boolean print) {
		this.print = print;
	}

	/**
	 * Set the showheaders flag.
	 * 
	 * @param showheaders
	 */
	public void setShowheaders(boolean showheaders) {
		this.showheaders = showheaders;
	}

	/**
	 * Set the output file.
	 * 
	 * @param output
	 */
	public void setOutput(File output) {
		this.output = output;
	}

	/**
	 * Set the rdbms required
	 * 
	 * @param vendor
	 */
	public void setRdbms(String vendor) {
		this.rdbms = vendor.toLowerCase();
	}

	/**
	 * Set the version required
	 * 
	 * @param version
	 */
	public void setVersion(String version) {
		this.version = version.toLowerCase();
	}

	/**
	 * Set the action to perform onerror
	 * 
	 * @param action
	 */
	public void setOnerror(OnError action) {
		this.onError = action.getValue();
	}

	/**
	 * Load the sql file and then execute it
	 * 
	 * @throws BuildException
	 */
	@SuppressWarnings("unchecked")
	public void execute() throws BuildException {
		if (sqldbmap == null || getSqlDbMap().exists() == false) {
			throw new BuildException("You haven't provided an sqldbmap, or " + "the one you specified doesn't exist: " + sqldbmap);
		}

		if (driver == null) {
			throw new BuildException("Driver attribute must be set!", getLocation());
		}
		if (userId == null) {
			throw new BuildException("User Id attribute must be set!", getLocation());
		}
		if (password == null) {
			throw new BuildException("Password attribute must be set!", getLocation());
		}
		if (url == null) {
			throw new BuildException("Url attribute must be set!", getLocation());
		}

		Properties map = new Properties();

		try {
			FileInputStream fis = new FileInputStream(getSqlDbMap());
			map.load(fis);
			fis.close();
		} catch (IOException ioe) {
			throw new BuildException("Cannot open and process the sqldbmap!");
		}

		Map<Object, Object> databases = new HashMap<Object, Object>();

		Iterator<?> eachFileName = map.keySet().iterator();
		while (eachFileName.hasNext()) {
			String sqlfile = (String) eachFileName.next();
			String database = map.getProperty(sqlfile);

			List<Object> files = (List<Object>) databases.get(database);

			if (files == null) {
				files = new ArrayList<Object>();
				databases.put(database, files);
			}

			// We want to make sure that the base schemas
			// are inserted first.
			if (sqlfile.indexOf("schema.sql") != -1) {
				files.add(0, sqlfile);
			} else {
				files.add(sqlfile);
			}
		}

		Iterator<?> eachDatabase = databases.keySet().iterator();
		while (eachDatabase.hasNext()) {
			String db = (String) eachDatabase.next();
			List<Object> transactions = new ArrayList<Object>();
			eachFileName = ((List<?>) databases.get(db)).iterator();
			while (eachFileName.hasNext()) {
				String fileName = (String) eachFileName.next();
				File file = new File(srcDir, fileName);

				if (file.exists()) {
					Transaction transaction = new Transaction();
					transaction.setSrc(file);
					transactions.add(transaction);
				} else {
					System.out.println("File '" + file.getAbsolutePath() + "' in sqldbmap does not exist, so skipping it.");
				}
			}

			insertDatabaseSqlFiles(url, db, transactions);
		}
	}

	/**
	 * Take the base url, the target database and insert a set of SQL files into the target database.
	 * 
	 * @param url
	 * @param database
	 * @param transactions
	 */
	private void insertDatabaseSqlFiles(String url, String database, List<?> transactions) {
		url = StringUtils.replace(url, "@DB@", database);
		System.out.println("Our new url -> " + url);

		Driver driverInstance = null;
		try {
			Class<?> dc;
			if (classpath != null) {
				log("Loading " + driver + " using AntClassLoader with classpath " + classpath, Project.MSG_VERBOSE);

				loader = new AntClassLoader(getProject(), classpath);
				dc = loader.loadClass(driver);
			} else {
				log("Loading " + driver + " using system loader.", Project.MSG_VERBOSE);
				dc = Class.forName(driver);
			}
			driverInstance = (Driver) dc.newInstance();
		} catch (ClassNotFoundException e) {
			throw new BuildException("Class Not Found: JDBC driver " + driver + " could not be loaded", getLocation());
		} catch (IllegalAccessException e) {
			throw new BuildException("Illegal Access: JDBC driver " + driver + " could not be loaded", getLocation());
		} catch (InstantiationException e) {
			throw new BuildException("Instantiation Exception: JDBC driver " + driver + " could not be loaded", getLocation());
		}

		try {
			log("connecting to " + url, Project.MSG_VERBOSE);
			Properties info = new Properties();
			info.put("user", userId);
			info.put("password", password);
			conn = driverInstance.connect(url, info);

			if (conn == null) {
				// Driver doesn't understand the URL
				throw new SQLException("No suitable Driver for " + url);
			}

			if (!isValidRdbms(conn)) {
				return;
			}

			conn.setAutoCommit(autocommit);
			statement = conn.createStatement();
			PrintStream out = System.out;
			try {
				if (output != null) {
					log("Opening PrintStream to output file " + output, Project.MSG_VERBOSE);
					out = new PrintStream(new BufferedOutputStream(new FileOutputStream(output)));
				}

				// Process all transactions
				for (Iterator<?> it = transactions.iterator(); it.hasNext();) {
					Transaction transaction = (Transaction) it.next();
					transaction.runTransaction(out);
					if (!autocommit) {
						log("Commiting transaction", Project.MSG_VERBOSE);
						conn.commit();
					}
				}
			} finally {
				if (out != null && out != System.out) {
					out.close();
				}
			}
		} catch (IOException e) {
			if (!autocommit && conn != null && onError.equals("abort")) {
				try {
					conn.rollback();
				} catch (SQLException ex) {
					// do nothing.
				}
			}
			throw new BuildException(e, getLocation());
		} catch (SQLException e) {
			if (!autocommit && conn != null && onError.equals("abort")) {
				try {
					conn.rollback();
				} catch (SQLException ex) {
					// do nothing.
				}
			}
			throw new BuildException(e, getLocation());
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
			}
		}

		System.out.println(goodSql + " of " + totalSql + " SQL statements executed successfully");
	}

	/**
	 * Read the statements from the .sql file and execute them. Lines starting with '//', '--' or 'REM ' are ignored.
	 * 
	 * @param reader
	 * @param out
	 * @throws SQLException
	 * @throws IOException
	 */
	protected void runStatements(Reader reader, PrintStream out) throws SQLException, IOException {
		String sql = "";
		String line = "";

		BufferedReader in = new BufferedReader(reader);
		PropertyHelper ph = PropertyHelper.getPropertyHelper(getProject());

		try {
			while ((line = in.readLine()) != null) {
				line = line.trim();
				line = ph.replaceProperties("", line, getProject().getProperties());
				if (line.startsWith("//") || line.startsWith("--")) {
					continue;
				}
				if (line.length() > 4 && line.substring(0, 4).equalsIgnoreCase("REM ")) {
					continue;
				}

				sql += " " + line;
				sql = sql.trim();

				// SQL defines "--" as a comment to EOL
				// and in Oracle it may contain a hint
				// so we cannot just remove it, instead we must end it
				if (line.indexOf("--") >= 0) {
					sql += "\n";
				}

				if (delimiterType.equals(DelimiterType.NORMAL) && sql.endsWith(delimiter) || delimiterType.equals(DelimiterType.ROW) && line.equals(delimiter)) {
					log("SQL: " + sql, Project.MSG_VERBOSE);
					execSQL(sql.substring(0, sql.length() - delimiter.length()), out);
					sql = "";
				}
			}

			// Catch any statements not followed by ;
			if (!sql.equals("")) {
				execSQL(sql, out);
			}
		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * Verify if connected to the correct RDBMS
	 * 
	 * @param conn
	 */
	protected boolean isValidRdbms(Connection conn) {
		if (rdbms == null && version == null) {
			return true;
		}

		try {
			DatabaseMetaData dmd = conn.getMetaData();

			if (rdbms != null) {
				String theVendor = dmd.getDatabaseProductName().toLowerCase();

				log("RDBMS = " + theVendor, Project.MSG_VERBOSE);
				if (theVendor == null || theVendor.indexOf(rdbms) < 0) {
					log("Not the required RDBMS: " + rdbms, Project.MSG_VERBOSE);
					return false;
				}
			}

			if (version != null) {
				String theVersion = dmd.getDatabaseProductVersion().toLowerCase();

				log("Version = " + theVersion, Project.MSG_VERBOSE);
				if (theVersion == null || !(theVersion.startsWith(version) || theVersion.indexOf(" " + version) >= 0)) {
					log("Not the required version: \"" + version + "\"", Project.MSG_VERBOSE);
					return false;
				}
			}
		} catch (SQLException e) {
			// Could not get the required information
			log("Failed to obtain required RDBMS information", Project.MSG_ERR);
			return false;
		}

		return true;
	}

	/**
	 * Exec the sql statement.
	 * 
	 * @param sql
	 * @param out
	 * @throws SQLException
	 */
	protected void execSQL(String sql, PrintStream out) throws SQLException {
		// Check and ignore empty statements
		if ("".equals(sql.trim())) {
			return;
		}

		try {
			totalSql++;
			if (!statement.execute(sql)) {
				log(statement.getUpdateCount() + " rows affected", Project.MSG_VERBOSE);
			} else {
				if (print) {
					printResults(out);
				}
			}

			SQLWarning warning = conn.getWarnings();
			while (warning != null) {
				log(warning + " sql warning", Project.MSG_VERBOSE);
				warning = warning.getNextWarning();
			}
			conn.clearWarnings();
			goodSql++;
		} catch (SQLException e) {
			System.out.println("Failed to execute: " + sql);
			if (!onError.equals("continue")) {
				throw e;
			}
			log(e.toString(), Project.MSG_ERR);
		}
	}

	/**
	 * print any results in the statement.
	 * 
	 * @param out
	 * @throws SQLException
	 */
	protected void printResults(PrintStream out) throws java.sql.SQLException {
		ResultSet rs = null;
		do {
			rs = statement.getResultSet();
			if (rs != null) {
				log("Processing new result set.", Project.MSG_VERBOSE);
				ResultSetMetaData md = rs.getMetaData();
				int columnCount = md.getColumnCount();
				StringBuffer line = new StringBuffer();
				if (showheaders) {
					for (int col = 1; col < columnCount; col++) {
						line.append(md.getColumnName(col));
						line.append(",");
					}
					line.append(md.getColumnName(columnCount));
					out.println(line);
					line.setLength(0);
				}
				while (rs.next()) {
					boolean first = true;
					for (int col = 1; col <= columnCount; col++) {
						String columnValue = rs.getString(col);
						if (columnValue != null) {
							columnValue = columnValue.trim();
						}

						if (first) {
							first = false;
						} else {
							line.append(",");
						}
						line.append(columnValue);
					}
					out.println(line);
					line.setLength(0);
				}
			}
		} while (statement.getMoreResults());
		out.println();
	}

	/**
	 * Enumerated attribute with the values "continue", "stop" and "abort" for the onerror attribute.
	 */
	public static class OnError extends EnumeratedAttribute {
		public static final String CONTINUE = "continue";

		public static final String STOP = "stop";

		public static final String ABORT = "abort";

		public String[] getValues() {
			return new String[] { CONTINUE, STOP, ABORT };
		}
	}

	/**
	 * Contains the definition of a new transaction element. Transactions allow several files or blocks of statements to
	 * be executed using the same JDBC connection and commit operation in between.
	 */
	public class Transaction {
		private File tSrcFile = null;
		private String tSqlCommand = "";

		public void setSrc(File src) {
			this.tSrcFile = src;
		}

		public void addText(String sql) {
			this.tSqlCommand += sql;
		}

		private void runTransaction(PrintStream out) throws IOException, SQLException {
			if (tSqlCommand.length() != 0) {
				log("Executing commands", Project.MSG_INFO);
				runStatements(new StringReader(tSqlCommand), out);
			}

			if (tSrcFile != null) {
				System.out.println("Executing file: " + tSrcFile.getAbsolutePath());
				Reader reader = (encoding == null) ? new FileReader(tSrcFile) : new InputStreamReader(new FileInputStream(tSrcFile), encoding);
				runStatements(reader, out);
				reader.close();
			}
		}
	}
}
