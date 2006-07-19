/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */

package info.magnolia.module.data;

import info.magnolia.cms.beans.config.ContentRepository;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

import org.apache.commons.lang.StringUtils;

/**
 * 
 *
 * @author Enrico Kufahl (BeagleSoft GmbH)
 * @version $Revision$ ($Author$)
 * 
 */
public class TypeEventListener implements javax.jcr.observation.EventListener{
	
	public static final int baseDepth = StringUtils.countMatches(Constants.TYPES_NODE_PATH, "/") + 1;

	protected final DataModule module;
	
	public TypeEventListener(DataModule module) {
		this.module = module;
	}
	

	public void onEvent(EventIterator events) {
		
		while(events.hasNext()){
			Event event = events.nextEvent();
			String path;
			try {
				path = event.getPath();
			} catch (RepositoryException e) {
				throw new RuntimeException(e);
			}
			int pathDepth = StringUtils.countMatches(path, "/");
			
			// only dataTypes events without subnode events
			if(pathDepth == baseDepth){
				String typeName = path.substring(path.lastIndexOf("/") + 1);
			
				if(event.getType() == Event.NODE_ADDED){
					// register node type
					try {
						ContentRepository.getRepositoryProvider(DataModule.getRepository()).registerNodeTypes(new ByteArrayInputStream(NODE_TYPE_DEF_TEMPLATE.format(new String[]{typeName}).getBytes()));
					} catch (RepositoryException e) {
						throw new RuntimeException(e);
					}
					// schedule registration of dialogs and trees
					module.scheduleDialogAndTreeRegistration(typeName);
				}
				
				if(event.getType() == Event.NODE_REMOVED){
					// dregister node type...
				}
			}
		}
	}
	
	private static final String NODE_TYPE_DEFINITION = 

		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		+ 	"<nodeTypes"
		+ 		" xmlns:rep=\"internal\""
		+ 		" xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\""
		+ 		" xmlns:mix=\"http://www.jcp.org/jcr/mix/1.0\""
		+ 		" xmlns:mgnl=\"http://www.magnolia.info/jcr/mgnl\""
		+ 		" xmlns:jcr=\"http://www.jcp.org/jcr/1.0\">"
		+		"<nodeType name=\"{0}\" isMixin=\"false\" hasOrderableChildNodes=\"true\" primaryItemName=\"\">"
		+			"<supertypes>"
		+				"<supertype>nt:hierarchyNode</supertype>"
		+			"</supertypes>"
		+			"<childNodeDefinition name=\"MetaData\" defaultPrimaryType=\"mgnl:metaData\" autoCreated=\"true\" mandatory=\"true\" onParentVersion=\"COPY\" protected=\"false\" sameNameSiblings=\"false\">"
		+				"<requiredPrimaryTypes>"
		+					"<requiredPrimaryType>mgnl:metaData</requiredPrimaryType>"
		+				"</requiredPrimaryTypes>"
		+			"</childNodeDefinition>"
		+			"<childNodeDefinition name=\"*\" defaultPrimaryType=\"\" autoCreated=\"false\" mandatory=\"false\" onParentVersion=\"COPY\" protected=\"false\" sameNameSiblings=\"true\">"
		+				"<requiredPrimaryTypes>"
		+					"<requiredPrimaryType>nt:base</requiredPrimaryType>"
		+				"</requiredPrimaryTypes>"
		+			"</childNodeDefinition>"
		+			"<propertyDefinition name=\"*\" requiredType=\"undefined\" autoCreated=\"false\" mandatory=\"false\" onParentVersion=\"COPY\" protected=\"false\" multiple=\"false\"/>"
		+		"</nodeType>"
		+	"</nodeTypes>";
	
	private static final MessageFormat NODE_TYPE_DEF_TEMPLATE = new MessageFormat(NODE_TYPE_DEFINITION);
}
