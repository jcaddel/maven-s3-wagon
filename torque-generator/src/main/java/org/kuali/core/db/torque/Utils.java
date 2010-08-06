package org.kuali.core.db.torque;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.torque.engine.database.model.Database;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class Utils {
	NumberFormat nf = NumberFormat.getInstance();
	int length = 68;
	String padding = StringUtils.repeat(".", length);

	public Utils() {
		super();
		nf.setMaximumFractionDigits(3);
		nf.setMinimumFractionDigits(3);
		nf.setGroupingUsed(false);
	}

	public String getFilename(String fileOrResource) {
		if (!isFileOrResource(fileOrResource)) {
			return null;
		}
		File f = new File(fileOrResource);
		if (f.exists()) {
			return f.getName();
		}
		ResourceLoader loader = new DefaultResourceLoader();
		Resource resource = loader.getResource(fileOrResource);
		return resource.getFilename();
	}

	public void right(PrettyPrint pp) {
		long millis = System.currentTimeMillis() - pp.getStart();
		String elapsed = getElapsed(millis);
		String padding = StringUtils.repeat(".", 79);
		String right = padding + " " + elapsed;
		System.out.println(StringUtils.right(right, 79 - pp.getMsg().length()));
	}

	public void left(PrettyPrint pp) {
		System.out.print(pp.getMsg());
		pp.setStart(System.currentTimeMillis());
	}

	public String getEncoding(String encoding) {
		if (StringUtils.isEmpty(encoding)) {
			return System.getProperty("file.encoding");
		} else {
			return encoding;
		}
	}

	public String pad(String msg, long millis) {
		String elapsed = getElapsed(millis);
		int leftWidth = msg.length() + 1;
		int rightWidth = elapsed.length() + 1;
		int chop = leftWidth + rightWidth;
		if (chop > padding.length()) {
			return msg + " " + elapsed;
		} else {
			return msg + " " + padding.substring(chop) + " " + elapsed;
		}
	}

	public String getElapsed(long millis) {
		return "[" + nf.format(millis / 1000D) + "s]";
	}

	/**
	 * Return true if this is a file on the file system OR a resource that Spring can locate
	 */
	public boolean isFileOrResource(String location) {
		if (location == null) {
			return false;
		}
		File file = new File(location);
		if (file.exists()) {
			return true;
		}
		ResourceLoader loader = new DefaultResourceLoader();
		Resource resource = loader.getResource(location);
		return resource.exists();

	}

	public void verifyExists(List<String> locations) throws FileNotFoundException {
		ResourceLoader loader = new DefaultResourceLoader();
		for (String location : locations) {
			Resource resource = loader.getResource(location);
			if (!resource.exists()) {
				throw new FileNotFoundException("Unable to locate " + location);
			}
		}
	}

	public List<Database> getDatabases(List<String> schemaXMLResources, String targetDatabase) throws IOException {
		List<Database> databases = new ArrayList<Database>();
		if (schemaXMLResources == null) {
			return databases;
		}

		verifyExists(schemaXMLResources);

		for (String location : schemaXMLResources) {
			// Get an xml parser for schema.xml
			KualiXmlToAppData xmlParser = new KualiXmlToAppData(targetDatabase, "");

			// Parse schema.xml into a database object
			try {
				Database database = xmlParser.parseResource(location);
				databases.add(database);
			} catch (Exception e) {
				throw new IOException("Error parsing: " + location, e);
			}
		}
		return databases;
	}

}
