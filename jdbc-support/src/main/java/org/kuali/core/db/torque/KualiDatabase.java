package org.kuali.core.db.torque;

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

import java.util.ArrayList;
import java.util.List;

import org.apache.torque.engine.database.model.Database;
import org.xml.sax.Attributes;

/**
 * A class for holding application data structures.
 * 
 * @author <a href="mailto:leon@opticode.co.za>Leon Messerschmidt</a>
 * @author <a href="mailto:jmcnally@collab.net>John McNally</a>
 * @author <a href="mailto:mpoeschl@marmot.at>Martin Poeschl</a>
 * @author <a href="mailto:dlr@collab.net>Daniel Rall</a>
 * @author <a href="mailto:byron_foster@byron_foster@yahoo.com>Byron Foster</a>
 * @author <a href="mailto:monroe@dukece.com>Greg Monroe</a>
 * @version $Id: KualiDatabase.java,v 1.1 2007-10-21 07:57:26 abyrne Exp $
 */
public class KualiDatabase extends Database {
	/** Logging class from commons.logging */
	// private static Log log = LogFactory.getLog(KualiDatabase.class);

	private List<View> views = new ArrayList<View>();
	private List<Sequence> sequences = new ArrayList<Sequence>();

	/**
	 * Creates a new instance for the specified database type.
	 * 
	 * @param databaseType
	 *            The default type for this database.
	 */
	public KualiDatabase(String databaseType) {
		super(databaseType);
	}

	public List<View> getViews() {
		return views;
	}

	public List<Sequence> getSequences() {
		return sequences;
	}

	public View addView(Attributes attrib) {
		View view = new View();
		view.setName(attrib.getValue("name"));
		view.setDefinition(attrib.getValue("viewdefinition"));
		views.add(view);
		return view;
	}

	public Sequence addSequence(Attributes attrib) {
		Sequence sequence = new Sequence();
		sequence.setName(attrib.getValue("name"));
		sequence.setNextVal(attrib.getValue("nextval"));

		sequences.add(sequence);
		return sequence;
	}
}
