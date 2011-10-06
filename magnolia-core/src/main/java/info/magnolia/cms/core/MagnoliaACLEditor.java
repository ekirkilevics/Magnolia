/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.core;

import java.security.Principal;

import javax.jcr.AccessDeniedException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.security.AccessControlException;
import javax.jcr.security.AccessControlPolicy;

import org.apache.jackrabbit.api.security.JackrabbitAccessControlPolicy;
import org.apache.jackrabbit.core.security.authorization.AccessControlEditor;

/**
 * Delegate ACL editor. Currently just deletages to the provided implemntation. Will be used to provide special policies for principals.
 * 
 * @version $Id$
 * 
 */
public class MagnoliaACLEditor implements AccessControlEditor {

    private final AccessControlEditor editor;

    public MagnoliaACLEditor(AccessControlEditor editor) {
        this.editor = editor;
    }

    @Override
    public AccessControlPolicy[] getPolicies(String nodePath) throws AccessControlException, PathNotFoundException, RepositoryException {
        return editor.getPolicies(nodePath);
    }

    @Override
    public JackrabbitAccessControlPolicy[] getPolicies(Principal principal) throws AccessControlException, RepositoryException {
        return editor.getPolicies(principal);
    }

    @Override
    public AccessControlPolicy[] editAccessControlPolicies(String nodePath) throws AccessControlException, PathNotFoundException, RepositoryException {
        return editor.editAccessControlPolicies(nodePath);
    }

    @Override
    public JackrabbitAccessControlPolicy[] editAccessControlPolicies(Principal principal) throws AccessDeniedException, AccessControlException, RepositoryException {
        return editor.editAccessControlPolicies(principal);
    }

    @Override
    public void setPolicy(String nodePath, AccessControlPolicy policy) throws AccessControlException, PathNotFoundException, RepositoryException {
        editor.setPolicy(nodePath, policy);
    }

    @Override
    public void removePolicy(String nodePath, AccessControlPolicy policy) throws AccessControlException, PathNotFoundException, RepositoryException {
        editor.removePolicy(nodePath, policy);
    }

}
