/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.test.mock;

import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.security.AccessDeniedException;

import java.util.Calendar;

/**
 * TODO : this is incomplete, please complete per your needs...
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class MockMetaData extends MetaData {
    private final MockContent mockContent;

    public MockMetaData(MockContent mockContent) {
        this.mockContent = mockContent;
    }
    
    public String getHandle() {
        return mockContent.getHandle();
    }

    public String getLabel() {
        return mockContent.getName();
    }

    public boolean getBooleanProperty(String name) {
        return mockContent.getNodeData(name).getBoolean();
    }

    public String getStringProperty(String name) {
        return mockContent.getNodeData(name).getString();
    }

    public Calendar getDateProperty(String name) {
        return mockContent.getNodeData(name).getDate();
    }

    public void setProperty(String name, boolean value) throws AccessDeniedException {
        mockContent.addNodeData(new MockNodeData(name, Boolean.valueOf(value)));
    }

    public void setProperty(String name, Calendar value) throws AccessDeniedException {
        mockContent.addNodeData(new MockNodeData(name, value));
    }

    public void setProperty(String name, String value) throws AccessDeniedException {
        mockContent.addNodeData(new MockNodeData(name, value));
    }

}
