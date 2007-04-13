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

/**
 * TODO : this is incomplete, please complete per your needs...
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class MockMetaData extends MetaData {
//    private String activatorId;
//    private String authorId;
//    private Calendar creationDate;
//    private String handle;
//    private boolean isActivated;
//    private String label;
//    private Calendar lastActionDate;
//    private Calendar modificationDate;
//    private String template;
//    private String title;

    private final MockContent mockContent;

    public MockMetaData(MockContent mockContent) {
        this.mockContent = mockContent;
    }
    
    public String getHandle() {
        return mockContent.getHandle();
    }

    public String getActivatorId() {
        return mockContent.getNodeData("activatorId").getString();
    }

    public String getAuthorId() {
        return mockContent.getNodeData("authorId").getString();
    }

    public String getLabel() {
        return mockContent.getNodeData("label").getString();
    }

    public String getTemplate() {
        return mockContent.getNodeData("template").getString();
    }

    public String getTitle() {
        return mockContent.getNodeData("title").getString();
    }
}
