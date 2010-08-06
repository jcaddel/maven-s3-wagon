package org.kuali.core.db.torque;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.EngineException;
import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Domain;
import org.apache.torque.engine.database.model.ForeignKey;
import org.apache.torque.engine.database.model.Index;
import org.apache.torque.engine.database.model.Table;
import org.apache.torque.engine.database.model.Unique;
import org.apache.torque.engine.database.transform.DTDResolver;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class KualiXmlToAppData extends DefaultHandler implements DatabaseParser {
	/** Logging class from commons.logging */
	private static Log log = LogFactory.getLog(KualiXmlToAppData.class);

	private KualiDatabase database;
	private Table currTable;
	// private View currView;
	// private Sequence currSequence;
	private Column currColumn;
	private ForeignKey currFK;
	private Index currIndex;
	private Unique currUnique;

	private boolean firstPass;
	private boolean isExternalSchema;
	private String currentPackage;
	private String currentXmlFile;
	private String defaultPackage;

	private static SAXParserFactory saxFactory;

	/** remember all files we have already parsed to detect looping. */
	private Vector<String> alreadyReadFiles;

	/** this is the stack to store parsing data */
	private Stack<Object> parsingStack = new Stack<Object>();

	static {
		saxFactory = SAXParserFactory.newInstance();
		saxFactory.setValidating(true);
	}

	/**
	 * Creates a new instance for the specified database type.
	 * 
	 * @param databaseType
	 *            The type of database for the application.
	 */
	public KualiXmlToAppData(String databaseType) {
		database = new KualiDatabase(databaseType);
		firstPass = true;
	}

	/**
	 * Creates a new instance for the specified database type.
	 * 
	 * @param databaseType
	 *            The type of database for the application.
	 * @param defaultPackage
	 *            the default java package used for the om
	 */
	public KualiXmlToAppData(String databaseType, String defaultPackage) {
		database = new KualiDatabase(databaseType);
		this.defaultPackage = defaultPackage;
		firstPass = true;
	}

	protected InputStream getInputStream(String xmlFile) throws IOException {
		File file = new File(xmlFile);
		if (file.exists()) {
			return new BufferedInputStream(new FileInputStream(file));
		}
		DefaultResourceLoader loader = new DefaultResourceLoader();
		Resource resource = loader.getResource(xmlFile);
		if (resource.exists()) {
			return resource.getInputStream();
		}
		throw new IOException("Unable to locate " + xmlFile);
	}

	/**
	 * Parses a XML input file and returns a newly created and populated Database structure.
	 * 
	 * @param xmlFile
	 *            The input file to parse.
	 * @return Database populated by <code>xmlFile</code>.
	 */
	public KualiDatabase parseResource(String location) throws EngineException {
		try {
			// in case I am missing something, make it obvious
			if (!firstPass) {
				throw new Error("No more double pass");
			}
			// check to see if we alread have parsed the file
			if ((alreadyReadFiles != null) && alreadyReadFiles.contains(location)) {
				return database;
			} else if (alreadyReadFiles == null) {
				alreadyReadFiles = new Vector<String>(3, 1);
			}

			// remember the file to avoid looping
			alreadyReadFiles.add(location);

			currentXmlFile = location;

			saxFactory.setValidating(false);
			SAXParser parser = saxFactory.newSAXParser();

			InputStream in = null;
			try {
				in = getInputStream(location);
				InputSource is = new InputSource(in);
				is.setSystemId(location);
				parser.parse(is, this);
			} finally {
				in.close();
			}
		} catch (SAXParseException e) {
			throw new EngineException("Sax error on line " + e.getLineNumber() + " column " + e.getColumnNumber() + " : " + e.getMessage(), e);
		} catch (Exception e) {
			throw new EngineException(e);
		}
		if (!isExternalSchema) {
			firstPass = false;
		}
		database.doFinalInitialization();
		return database;
	}

	/**
	 * EntityResolver implementation. Called by the XML parser
	 * 
	 * @param publicId
	 *            The public identifier of the external entity
	 * @param systemId
	 *            The system identifier of the external entity
	 * @return an InputSource for the database.dtd file
	 * @see DTDResolver#resolveEntity(String, String)
	 */
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
		try {
			return new ImpexDTDResolver().resolveEntity(publicId, systemId);
		} catch (Exception e) {
			throw new SAXException(e);
		}
	}

	/**
	 * Handles opening elements of the xml file.
	 * 
	 * @param uri
	 * @param localName
	 *            The local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param rawName
	 *            The qualified name (with prefix), or the empty string if qualified names are not available.
	 * @param attributes
	 *            The specified or defaulted attributes
	 */
	public void startElement(String uri, String localName, String rawName, Attributes attributes) throws SAXException {
		try {
			if (rawName.equals("database")) {
				if (isExternalSchema) {
					currentPackage = attributes.getValue("package");
					if (currentPackage == null) {
						currentPackage = defaultPackage;
					}
				} else {
					database.loadFromXML(attributes);
					if (database.getPackage() == null) {
						database.setPackage(defaultPackage);
					}
				}
			} else if (rawName.equals("external-schema")) {
				String xmlFile = attributes.getValue("filename");
				if (xmlFile.charAt(0) != '/') {
					File f = new File(currentXmlFile);
					xmlFile = new File(f.getParent(), xmlFile).getPath();
				}

				// put current state onto the stack
				ParseStackElement.pushState(this);

				isExternalSchema = true;

				parseResource(xmlFile);
				// get the last state from the stack
				ParseStackElement.popState(this);
			} else if (rawName.equals("domain")) {
				Domain domain = new Domain();
				domain.loadFromXML(attributes, database.getPlatform());
				database.addDomain(domain);
			} else if (rawName.equals("table")) {
				currTable = database.addTable(attributes);
				if (isExternalSchema) {
					currTable.setForReferenceOnly(true);
					currTable.setPackage(currentPackage);
				}
			} else if (rawName.equals("view")) {
				// currView = database.addView(attributes);
			} else if (rawName.equals("sequence")) {
				// currSequence = database.addSequence(attributes);
			} else if (rawName.equals("column")) {
				currColumn = currTable.addColumn(attributes);
			} else if (rawName.equals("inheritance")) {
				currColumn.addInheritance(attributes);
			} else if (rawName.equals("foreign-key")) {
				currFK = currTable.addForeignKey(attributes);
			} else if (rawName.equals("reference")) {
				currFK.addReference(attributes);
			} else if (rawName.equals("index")) {
				currIndex = currTable.addIndex(attributes);
			} else if (rawName.equals("index-column")) {
				currIndex.addColumn(attributes);
			} else if (rawName.equals("unique")) {
				currUnique = currTable.addUnique(attributes);
			} else if (rawName.equals("unique-column")) {
				currUnique.addColumn(attributes);
			} else if (rawName.equals("id-method-parameter")) {
				currTable.addIdMethodParameter(attributes);
			} else if (rawName.equals("option")) {
				setOption(attributes);
			}
		} catch (Exception e) {
			throw new SAXException(e);
		}
	}

	/**
	 * Handles closing elements of the xml file.
	 * 
	 * @param uri
	 * @param localName
	 *            The local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param rawName
	 *            The qualified name (with prefix), or the empty string if qualified names are not available.
	 */
	public void endElement(String uri, String localName, String rawName) throws SAXException {
		if (log.isDebugEnabled()) {
			log.debug("endElement(" + uri + ", " + localName + ", " + rawName + ") called");
		}
		try {
			// Reset working objects to null to allow option to know
			// which element it is associated with.
			if (rawName.equals("table")) {
				currTable = null;
			} else if (rawName.equals("view")) {
				// currView = null;
			} else if (rawName.equals("sequence")) {
				// currSequence = null;
			} else if (rawName.equals("column")) {
				currColumn = null;
			} else if (rawName.equals("foreign-key")) {
				currFK = null;
			} else if (rawName.equals("index")) {
				currIndex = null;
			} else if (rawName.equals("unique")) {
				currUnique = null;
			}
		} catch (Exception e) {
			throw new SAXException(e);
		}
	}

	public void setOption(Attributes attributes) {
		// Look thru supported model elements in reverse order to
		// find one that this option statement applies to.

		String key = attributes.getValue("key");
		String value = attributes.getValue("value");
		if (currUnique != null) {
			currUnique.addOption(key, value);
		} else if (currIndex != null) {
			currIndex.addOption(key, value);
		} else if (currFK != null) {
			currFK.addOption(key, value);
		} else if (currColumn != null) {
			currColumn.addOption(key, value);
		} else if (currTable != null) {
			currTable.addOption(key, value);
		} else { // Must be a db level option.
			database.addOption(key, value);
		}
	}

	/**
	 * Handles exception which occur when the xml file is parsed
	 * 
	 * @param e
	 *            the exception which occured while parsing
	 * @throws SAXException
	 *             always
	 */
	public void error(SAXParseException e) throws SAXException {
		log.error("Sax parser threw an Exception", e);
		throw new SAXException("Error while parsing " + currentXmlFile + " at line " + e.getLineNumber() + " column " + e.getColumnNumber() + " : " + e.getMessage());
	}

	/**
	 * When parsing multiple files that use nested <external-schema> tags we need to use a stack to remember some
	 * values.
	 */
	private static class ParseStackElement {
		private boolean isExternalSchema;
		private String currentPackage;
		private String currentXmlFile;
		private boolean firstPass;

		/**
		 * 
		 * @param parser
		 */
		public ParseStackElement(KualiXmlToAppData parser) {
			// remember current state of parent object
			isExternalSchema = parser.isExternalSchema;
			currentPackage = parser.currentPackage;
			currentXmlFile = parser.currentXmlFile;
			firstPass = parser.firstPass;

			// push the state onto the stack
			parser.parsingStack.push(this);
		}

		/**
		 * Removes the top element from the stack and activates the stored state
		 * 
		 * @param parser
		 */
		public static void popState(KualiXmlToAppData parser) {
			if (!parser.parsingStack.isEmpty()) {
				ParseStackElement elem = (ParseStackElement) parser.parsingStack.pop();

				// activate stored state
				parser.isExternalSchema = elem.isExternalSchema;
				parser.currentPackage = elem.currentPackage;
				parser.currentXmlFile = elem.currentXmlFile;
				parser.firstPass = elem.firstPass;
			}
		}

		/**
		 * Stores the current state on the top of the stack.
		 * 
		 * @param parser
		 */
		public static void pushState(KualiXmlToAppData parser) {
			new ParseStackElement(parser);
		}
	}

}
