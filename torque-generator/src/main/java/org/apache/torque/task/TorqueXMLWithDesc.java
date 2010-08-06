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
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class is task used to generate xml file with database information 
 * and its description.
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
public class TorqueXMLWithDesc extends Task {
	File outputFile;
	File inputFile;
	String inputFileString;
	String outputFileString;
	public void setOutputFileString(String outputFileString) {
		this.outputFileString = outputFileString;
		outputFile = new File(outputFileString);
	}
	public void setInputFileString(String inputFileString){
		this.inputFileString = inputFileString;
		inputFile = new File(inputFileString);
	}
	
	/**
	 * Returns a document object with table and 
	 * column name from the input file and a blank description	
	 * @return Document document object with table/column names and blank description added
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public Document createXMLWithDescription()
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory
				.newDocumentBuilder();
		Document inputDocument = documentBuilder.parse(inputFile);
		DOMImplementation domImplementation = documentBuilder
				.getDOMImplementation();
		inputDocument.getDocumentElement().normalize();
		Document outputDocument = domImplementation.createDocument(null, null,
				null);
		Element outputRootElement = outputDocument.createElement("database");
		outputDocument.appendChild(outputRootElement);
		NodeList listOfTableElementsInInput = inputDocument
				.getElementsByTagName("table");

		for (int i = 0; i < listOfTableElementsInInput.getLength(); i++) {
			Node currentTableNode = listOfTableElementsInInput.item(i);
			if ((currentTableNode.getNodeType() == Node.ELEMENT_NODE)) {
				Element currentInputTableElement = (Element) currentTableNode;
				Element currentOutputTableElement = outputDocument
						.createElement("table");
				System.out.println(currentTableNode.getAttributes().toString());
				currentOutputTableElement.setAttribute("name",
						currentInputTableElement.getAttribute("name"));
				currentOutputTableElement.setAttribute("description", "");
				currentOutputTableElement.setAttribute("javaName", "");
				NodeList listOfChildNodesInCurrentTableNode = currentTableNode
						.getChildNodes();
				for (int j = 0; j < listOfChildNodesInCurrentTableNode
						.getLength(); j++) {
					Node currentChildNode = listOfChildNodesInCurrentTableNode
							.item(j);
					if (currentChildNode.getNodeName().equals("column")) {
						if (currentChildNode.getNodeType() == Node.ELEMENT_NODE) {
							Element currentInputColumnElement = (Element) currentChildNode;
							Element currentOutputColumnElement = outputDocument
									.createElement("column");
							currentOutputColumnElement.setAttribute("name",
									currentInputColumnElement
											.getAttribute("name"));
							currentOutputColumnElement.setAttribute(
									"description", "");
							currentOutputColumnElement.setAttribute("javaName", "");
							currentOutputTableElement
									.appendChild(currentOutputColumnElement);
						}
					}
				}
				outputRootElement.appendChild(currentOutputTableElement);
			}
		}
		return outputDocument;
	}
	
	/**
	 * Writes the XMLDocument to an output file.	
	 * @param newXMLDocument
	 */
	public void writeXMLToFile(Document newXMLDocument) {
		
		TransformerFactory tFactory = TransformerFactory.newInstance();
		try {
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
					"database.dtd");
			DOMSource domSource = new DOMSource(newXMLDocument);
			StringWriter writer = new StringWriter();
			Result result = new StreamResult(writer);
			transformer.transform(domSource, result);
			FileWriter fileWriter = new FileWriter(outputFile);

			if (outputFile.exists()) {
				StringBuffer bufferedWriter = new StringBuffer(writer
						.toString());
				fileWriter.write(bufferedWriter.toString());
				fileWriter.close();
				System.out.println("The data has been written");
			} else
				System.out.println("This file is not exist");

		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * This is the exceute method of the Task called by the ant script
	 */
	public void execute() throws BuildException{
		try {
			writeXMLToFile(createXMLWithDescription());
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
