package org.apache.torque.task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This ant task will merge the schema-desc.xml with schema.xml, if
 * schema-description.xml exists
 * 
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 * 
 */
public class TorqueMergeXMLDoc extends Task {
	
	private static final String DESCRIPTION_ATTR = "description";
	private static final String JAVA_NAME_ATTR = "javaName";
	private static final String NAME_ATTR = "name";
	private static final String COLUMN_ELEMENT = "column";
	
	private File schemaWithDesc;
	private File dbSchema;
	private String schemaWithDescString;
	private String dbSchemaString;
	private Document schemaWithDescDoc;
	private Document dbSchemaDoc;
	

	public void setDbSchemaString(String dbSchemaString) {		
		this.dbSchemaString = dbSchemaString;
		dbSchema = new File(dbSchemaString);

	}

	public void setSchemaWithDescString(String schemaWithDescString) {		
		this.schemaWithDescString = schemaWithDescString;
		schemaWithDesc = new File(schemaWithDescString);

	}

	public File getDbSchema() {
		return dbSchema;
	}

	public void setSchemaWithDesc(String schemaWithDescString) {
		this.schemaWithDesc = new File(schemaWithDescString);

	}

	public void setDbSchema(String dbSchemaString) {
		this.dbSchema = new File(dbSchemaString);
	}

	/**
	 * creates a document object from an input file
	 * 
	 * @param file
	 * @return Document object
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Document setDocument(File file)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder documentBuilder;
		documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document XMLdocument = documentBuilder.parse(file);
		XMLdocument.getDocumentElement().normalize();
		return XMLdocument;
	}

	/**
	 * performs the merge operation by taking two input files as input and then
	 * merges the file with description into the existing schema.xml file
	 * 
	 * @param schemaWithDesc
	 * @param dbSchema
	 */
	public void mergeSchemas(File schemaWithDesc, File dbSchema) throws Exception {

		schemaWithDescDoc = setDocument(schemaWithDesc);
		dbSchemaDoc = setDocument(dbSchema);
		dbSchemaDoc = createNewXML(dbSchemaDoc, schemaWithDescDoc);

	}

	/**
	 * merges the two xml document. The resulting document will be same as
	 * schema.xml except it will have a description attribute for all its tables
	 * and columns.
	 */
	public Document createNewXML(Document dbSchemaDoc,
			Document schemaWithDescDoc) throws Exception {

		XPath xpath = XPathFactory.newInstance().newXPath();
		
		NodeList listOfTablesInSchema = (NodeList)xpath.evaluate("/database/table", dbSchemaDoc, XPathConstants.NODESET);

		for (int tableIndex = 0; tableIndex < listOfTablesInSchema.getLength(); tableIndex++) {
			Element tableElementInSchema = (Element)listOfTablesInSchema.item(tableIndex);
			String tableName = tableElementInSchema.getAttribute(NAME_ATTR);
			// search the schema-description.xml file for a corresponding entry that contains the description
			Element tableDescElem = (Element)xpath.evaluate("/database/table[@" + NAME_ATTR + "='" + tableName + "']", schemaWithDescDoc, XPathConstants.NODE);
			if (tableDescElem != null) {
				String tableDescription = tableDescElem.getAttribute(DESCRIPTION_ATTR);
				String tableJavaName = tableDescElem.getAttribute(JAVA_NAME_ATTR);
				tableElementInSchema.setAttribute(DESCRIPTION_ATTR, tableDescription);
				tableElementInSchema.setAttribute(JAVA_NAME_ATTR, tableJavaName);
				NodeList columnElements = tableElementInSchema.getElementsByTagName(COLUMN_ELEMENT);
				for (int columnIndex = 0; columnIndex < columnElements.getLength(); columnIndex++) {
					// look for the column in the schema description file
					Element columnElementInSchema = (Element)columnElements.item(columnIndex);
					String columnName = columnElementInSchema.getAttribute(NAME_ATTR);
					Element columnDescElement = null;
					NodeList columnDescElements = tableDescElem.getElementsByTagName(COLUMN_ELEMENT);
					for (int columnDescIndex = 0; columnDescIndex < columnDescElements.getLength(); columnDescIndex++) {
						Element element = (Element)columnDescElements.item(columnDescIndex);
						if (columnName.equals(element.getAttribute(NAME_ATTR))) {
							columnDescElement = element;
							break;
						}
					}
					if (columnDescElement != null) {
						String columnDescription = columnDescElement.getAttribute(DESCRIPTION_ATTR);
						String columnJavaName = columnDescElement.getAttribute(JAVA_NAME_ATTR);
						columnElementInSchema.setAttribute(DESCRIPTION_ATTR, columnDescription);
						columnElementInSchema.setAttribute(JAVA_NAME_ATTR, columnJavaName);
					}
				}
			}
		}
		return dbSchemaDoc;
	}

	/**
	 * writes the document object to an output file
	 * 
	 * @param newXMLDocument
	 *            output xml document
	 */
	public void writeXMLToFile(Document newXMLDocument) throws Exception {
		File dbSchema = this.getDbSchema();
		TransformerFactory tFactory = TransformerFactory.newInstance();

		Transformer transformer = tFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
			"database.dtd");
		DOMSource domSource = new DOMSource(newXMLDocument);
		StringWriter writer = new StringWriter();
		Result result = new StreamResult(writer);
		transformer.transform(domSource, result);
		FileWriter fileWriter = new FileWriter(dbSchema);

		if (dbSchema.exists()) {
			StringBuffer bufferedWriter = new StringBuffer(writer
					.toString());
			fileWriter.write(bufferedWriter.toString());
			fileWriter.close();
			System.out.println("The data has been written");
		} else {
			System.out.println("This file is not exist");
		}
	}

	@Override
	public void execute() throws BuildException {
		setDbSchema(dbSchemaString);
		setSchemaWithDesc(schemaWithDescString);
		if (!schemaWithDesc.exists()) {
			System.out.println("no schema file with description can be located");
			return;
		}
		try {
			mergeSchemas(schemaWithDesc, dbSchema);
			writeXMLToFile(dbSchemaDoc);
		} catch (Exception e) {
			throw new BuildException(e);
		}
	}
	
	

}
