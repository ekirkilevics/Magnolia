/**
 * This file Copyright (c) 2003-2007 Magnolia International
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
package info.magnolia.cms.security.auth;

import java.io.Serializable;
import java.security.Principal;
import java.util.List;


/**
 * @author Sameer Charles $Id$
 */
public interface ACL extends Principal, Serializable {

    /**
     * Get name given to this principal
     * @return name
     */
    public String getName();

    /**
     * Set this principal name
     */
    public void setName(String name);

    /**
     * Get repository ID for which this ACL has been constructed
     * @return repository ID
     */
    public String getRepository();

    /**
     * Set repository ID for which this ACL will be constructed
     * @param repository
     */
    public void setRepository(String repository);

    /**
     * Get workspace ID for which this ACL has been contructed
     * @return workspace ID
     */
    public String getWorkspace();

    /**
     * Set workspace ID for which this ACL will be constructed
     * @param workspace
     */
    public void setWorkspace(String workspace);

    /**
     * add permission in to an existing list
     * @param permission
     */
    public void addPermission(Object permission);

    /**
     * Initialize access control list with provided permissions it will overwrite any existing permissions set before.
     * @param list
     */
    public void setList(List list);

    /**
     * Returns list of permissions for this principal
     */
    public List getList();

}
