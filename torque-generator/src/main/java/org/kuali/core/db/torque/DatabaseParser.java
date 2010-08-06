package org.kuali.core.db.torque;

import org.apache.torque.engine.EngineException;
import org.apache.torque.engine.database.model.Database;

/**
 * This lets KualiTorqueSQLTask and KualiXmlToAppData play nicely with Torque 3.3 Maven Plugin Mojo's
 */
public interface DatabaseParser {

	public Database parseResource(String location) throws EngineException;

}
