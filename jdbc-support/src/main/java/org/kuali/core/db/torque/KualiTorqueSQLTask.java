package org.kuali.core.db.torque;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.torque.task.TorqueDataModelTask;
import org.apache.velocity.context.Context;

/**
 * Generate schema.sql and schema-constraints.sql from schema.xml files
 */
public class KualiTorqueSQLTask extends TorqueDataModelTask {
	Utils utils = new Utils();
	Map<Thread, PrettyPrint> currentThread = new HashMap<Thread, PrettyPrint>();

	private String database;

	private String suffix = "";

	/**
	 * Sets the name of the database to generate sql for.
	 * 
	 * @param database
	 *            the name of the database to generate sql for.
	 */
	public void setDatabase(String database) {
		this.database = database;
	}

	/**
	 * Returns the name of the database to generate sql for.
	 * 
	 * @return the name of the database to generate sql for.
	 */
	public String getDatabase() {
		return database;
	}

	/**
	 * Sets the suffix of the generated sql files.
	 * 
	 * @param suffix
	 *            the suffix of the generated sql files.
	 */
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	/**
	 * Returns the suffix of the generated sql files.
	 * 
	 * @return the suffix of the generated sql files.
	 */
	public String getSuffix() {
		return suffix;
	}

	protected DatabaseParser getDatabaseParser() {
		return new KualiXmlToAppData(getTargetDatabase(), getTargetPackage());
	}

	/**
	 * create the sql -> database map.
	 * 
	 * @throws Exception
	 */
	protected void createSqlDbMap() throws Exception {
		if (getSqlDbMap() == null) {
			return;
		}

		// Produce the sql -> database map
		Properties sqldbmap = new Properties();
		Properties sqldbmap_c = new Properties();

		// Check to see if the sqldbmap has already been created.
		File file = new File(getSqlDbMap());

		if (file.exists()) {
			FileInputStream fis = new FileInputStream(file);
			sqldbmap.load(fis);
			fis.close();
		}

		Iterator<String> i = getDataModelDbMap().keySet().iterator();

		while (i.hasNext()) {
			String dataModelName = (String) i.next();

			String databaseName;

			if (getDatabase() == null) {
				databaseName = (String) getDataModelDbMap().get(dataModelName);
			} else {
				databaseName = getDatabase();
			}

			String sqlFile = dataModelName + getSuffix() + ".sql";
			sqldbmap.setProperty(sqlFile, databaseName);
			sqlFile = dataModelName + getSuffix() + "-constraints.sql";
			sqldbmap_c.setProperty(sqlFile, databaseName);
		}

		sqldbmap.store(new FileOutputStream(getSqlDbMap()), "Sqlfile -> Database map");
		sqldbmap_c.store(new FileOutputStream(getSqlDbMap() + "-constraints"), "Sqlfile -> Database map");
	}

	public void onBeforeGenerate() {
		PrettyPrint pp = new PrettyPrint("[INFO] Generating schema SQL ");
		currentThread.put(Thread.currentThread(), pp);
		utils.left(pp);
	}

	public void onAfterGenerate() {
		utils.right(currentThread.remove(Thread.currentThread()));
	}

	/**
	 * Set up the initial context for generating the SQL from the XML schema.
	 * 
	 * @return the context
	 * @throws Exception
	 */
	public Context initControlContext() throws Exception {
		super.initControlContext();

		createSqlDbMap();

		return context;
	}
}
