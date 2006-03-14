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
package info.magnolia.cms.module;


/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public class RepositoryDefinition {
    
    /**
     * The name of the repository to register
     */
    String name;
    
    /**
     * The file containing the nodeTypes
     */
    String nodeTypeFile;
    
    /**
     * @return Returns the name.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return Returns the nodeTypeFile.
     */
    public String getNodeTypeFile() {
        return this.nodeTypeFile;
    }
    
    /**
     * @param nodeTypeFile The nodeTypeFile to set.
     */
    public void setNodeTypeFile(String nodeTypeFile) {
        this.nodeTypeFile = nodeTypeFile;
    }
        

}
