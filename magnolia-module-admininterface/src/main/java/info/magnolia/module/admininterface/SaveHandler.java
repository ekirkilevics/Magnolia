/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.admininterface;

import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.ItemType;


/**
 * This class handles the saving in the dialogs.
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
