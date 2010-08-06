package org.kuali.core.db.torque;

import java.io.IOException;

import org.apache.torque.engine.database.transform.DTDResolver;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Kuali customized Torque's database.dtd by adding sequence and view. Thus we need to use our custom dtd instead of the
 * latest one from Torque. At the moment the Kuali database.dtd is stored in many different spots inside Subversion.
 * This is because the impex was not set up such that XML parsing/generation was able to read Kuali's customized
 * database.dtd off of the classpath. Thus, database.dtd has to be present on the file system in the same directory as
 * any schema.xml files that are being parsed.
 * 
 * This class allows a single copy of database.dtd to be bundled into a .jar and shared by everyone at Kuali.
 */
public class ImpexDTDResolver extends DTDResolver {
	public static final String DTD_NAME = "database.dtd";
	public static final String DTD_LOCATION = "http://www.kuali.org/dtd/" + DTD_NAME;

	public ImpexDTDResolver() throws SAXException {
		super();
	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
		if (DTD_LOCATION.equals(systemId)) {
			ResourceLoader loader = new DefaultResourceLoader();
			Resource resource = loader.getResource("classpath:" + DTD_NAME);
			return new InputSource(resource.getInputStream());
		} else {
			return super.resolveEntity(publicId, systemId);
		}
	}

}
