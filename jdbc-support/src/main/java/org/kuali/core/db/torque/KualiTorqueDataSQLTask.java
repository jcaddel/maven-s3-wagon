package org.kuali.core.db.torque;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.torque.engine.database.model.Database;
import org.apache.torque.engine.database.model.Table;
import org.apache.torque.engine.database.transform.XmlToData;
import org.apache.torque.task.TorqueDataModelTask;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.springframework.util.StringUtils;

/**
 * This task converts data XML files into SQL files. It uses schema.xml to analyze which tables have a corresponding
 * data XML file
 */
public class KualiTorqueDataSQLTask extends TorqueDataModelTask {
	Utils utils = new Utils();
	Map<Thread, PrettyPrint> currentThread = new HashMap<Thread, PrettyPrint>();

	/**
	 * The directory for the DTD
	 */
	protected File dataDTDDir;

	/**
	 * The DTD itself.
	 */
	protected File dataDTD;

	/**
	 * Database object representing the information from schema.xml
	 */
	protected Database database;

	/**
	 * Get a list of File objects from the directory and filenames passed in
	 */
	protected List<File> getFiles(File srcDir, String[] filenames) {
		List<File> files = new ArrayList<File>();
		for (int i = 0; i < filenames.length; i++) {
			files.add(new File(srcDir, filenames[i]));
		}
		return files;
	}

	/**
	 * Cycle through the filesets to generate a list of file objects
	 */
	protected List<File> getFiles(List<FileSet> fileSets, Project project) {
		List<File> files = new ArrayList<File>();
		for (FileSet fileSet : fileSets) {
			DirectoryScanner ds = fileSet.getDirectoryScanner(project);
			File srcDir = fileSet.getDir(project);
			files.addAll(getFiles(srcDir, ds.getIncludedFiles()));
		}
		return files;
	}

	/**
	 * Extract table names from schema.xml
	 */
	protected Set<String> getTableNamesFromSchemaXML(Database database) {
		List<?> tables = database.getTables();
		Set<String> tablenames = new TreeSet<String>();
		for (Object object : tables) {
			Table table = (Table) object;
			tablenames.add(table.getName());
		}
		return tablenames;
	}

	/**
	 * Extract table names from files on the file system
	 */
	protected Set<String> getTableNamesFromFiles(List<File> files) {
		Set<String> tablenames = new TreeSet<String>();
		for (File file : files) {
			String filename = file.getName();
			int pos = filename.indexOf(".xml");
			String tablename = filename.substring(0, pos);
			tablenames.add(tablename);
		}
		return tablenames;
	}

	/**
	 * Initialize a Velocity context that can generate SQL from XML data files
	 */
	public Context initControlContext() throws Exception {
		if (getFilesets().isEmpty()) {
			throw new BuildException("You must specify a fileset of XML data files!");
		}

		// Get an xml parser for schema.xml
		KualiXmlToAppData xmlParser = new KualiXmlToAppData(getTargetDatabase(), "");

		// Parse schema.xml into a database object
		Database database = xmlParser.parseResource(getXmlFile());
		setDatabase(database);

		// Locate the DTD
		String dtdFile = getDataDTDDir().getAbsolutePath() + "/" + database.getName() + "-data.dtd";
		setDataDTD(new File(dtdFile));
		if (!getDataDTD().exists()) {
			throw new BuildException("Could not find the DTD for " + database.getName());
		}

		// These are the XML data files
		List<File> files = getFiles(getFilesets(), getProject());

		// Resolve table information from the data XML files with table information from schema.xml
		Set<String> schemaTables = getTableNamesFromSchemaXML(database);
		Set<String> fileTables = getTableNamesFromFiles(files);
		Set<String> missingFiles = SetUtils.difference(schemaTables, fileTables);
		Set<String> intersection = SetUtils.intersection(schemaTables, fileTables);
		Set<String> extraFiles = SetUtils.difference(fileTables, schemaTables);

		log("Total tables: " + database.getTables().size());
		log("Tables with data XML files: " + intersection.size());
		log("Tables without data XML files: " + missingFiles.size());
		if (extraFiles.size() > 0) {
			log("There are files that have no corresponding entry in schema.xml: " + extraFiles.size(), Project.MSG_WARN);
		}

		// Setup the Velocity context
		VelocityContext context = new VelocityContext();
		context.put("xmlfiles", files);
		context.put("task", this);
		context.put("targetDatabase", getTargetDatabase());
		return context;
	}

	public void onBeforeGenerate(File file) {
		PrettyPrint pp = new PrettyPrint("[INFO] Generating: " + getTargetDatabase() + "/" + StringUtils.replace(file.getName(), ".xml", ".sql"));
		utils.left(pp);
		currentThread.put(Thread.currentThread(), pp);
	}

	public void onAfterGenerate(File file) {
		utils.right(currentThread.remove(Thread.currentThread()));
	}

	/**
	 * Parse a data XML file. This method gets invoked from the Velocity template - sql/load/Control.vm
	 */
	public List<?> getData(File file) {
		try {
			XmlToData dataXmlParser = new XmlToData(getDatabase(), getDataDTD().getAbsolutePath());
			List<?> newData = dataXmlParser.parseFile(file.getAbsolutePath());
			return newData;
		} catch (Exception e) {
			throw new BuildException(e);
		}
	}

	public Database getDatabase() {
		return database;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}

	public File getDataDTDDir() {
		return dataDTDDir;
	}

	public void setDataDTDDir(File dataDTDDir) {
		this.dataDTDDir = dataDTDDir;
	}

	public File getDataDTD() {
		return dataDTD;
	}

	public void setDataDTD(File dataDTD) {
		this.dataDTD = dataDTD;
	}

}
