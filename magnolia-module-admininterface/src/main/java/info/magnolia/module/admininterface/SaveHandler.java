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
package info.magnolia.module.admininterface;

import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.ItemType;


/**
 * This class handels the saving in the dialogs.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public interface SaveHandler {

    /**
     * Initialize this SaveHandler
     */
    public void init(MultipartForm form);

    /**
     * Save the data passed by init. Returns true if the save method succeeded
     */
    public abstract boolean save();

    /**
     * True if a not existing path should get created during the save process
     * @return boolean
     */
    public abstract boolean isCreate();

    /**
     * Set true if a not existing node should get created during the save process
     * @param create
     */
    public abstract void setCreate(boolean create);

    /**
     * Defines the node type to create if isCreate is true
     * @return the ItemType
     */
    public abstract ItemType getCreationItemType();

    /**
     * Defines the node type to create if isCreate is true
     * @param creationItemType
     */
    public abstract void setCreationItemType(ItemType creationItemType);

    /**
     * @return Returns the nodeCollectionName.
     */
    public abstract String getNodeCollectionName();

    /**
     * @param nodeCollectionName The nodeCollectionName to set.
     */
    public abstract void setNodeCollectionName(String nodeCollectionName);

    /**
     * @return Returns the nodeName.
     */
    public abstract String getNodeName();

    /**
     * @param nodeName The nodeName to set.
     */
    public abstract void setNodeName(String nodeName);

    /**
     * @return Returns the paragraph.
     */
    public abstract String getParagraph();

    /**
     * @param paragraph The paragraph to set.
     */
    public abstract void setParagraph(String paragraph);

    /**
     * @return Returns the path.
     */
    public abstract String getPath();

    /**
     * @param path The path to set.
     */
    public abstract void setPath(String path);

    /**
     * set the name of the repository saving to
     * @param repository the name of the repository
     */
    public void setRepository(String repository);

    /**
     * get the name of thre repository saving to
     * @return name
     */
    public String getRepository();

}