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
package info.magnolia.setup;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.security.Digester;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AllChildrenNodesOperation;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.repository.RepositoryConstants;

import java.io.UnsupportedEncodingException;

import javax.jcr.RepositoryException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task to encrypt base64 encoded passwords. Will skip all non-base64 entries.
 *
 * @version $Id$
 *
 */
public final class HashUsersPasswords extends AllChildrenNodesOperation {

    private static final Logger log = LoggerFactory.getLogger(HashUsersPasswords.class);

    private static final Content.ContentFilter filter = new Content.ContentFilter() {

        @Override
        public boolean accept(Content content) {
            String type;
            try {
                type = content.getNodeTypeName();
            } catch (RepositoryException e) {
                return false;
            }
            return MgnlNodeType.NT_FOLDER.equals(type) || MgnlNodeType.USER.equals(type);
        }
    };

    public HashUsersPasswords(String name, String description, String repositoryName, String parentNodePath) {
        super(name, description, repositoryName, parentNodePath, filter);
    }

    public HashUsersPasswords() {
        this("/");
    }

    public HashUsersPasswords(String path) {
        this("Hash Passwords", "Hash all user passwords", RepositoryConstants.USERS, path);
    }

    @Override
    protected void operateOnChildNode(Content node, InstallContext ctx) throws RepositoryException, TaskExecutionException {
        if (MgnlNodeType.USER.equals(node.getNodeTypeName())) {
            String encodedPassword = node.getNodeData("pswd").getString();

            if (StringUtils.isNotBlank(encodedPassword)) {
                byte[] pwdBytes;
                try {
                    pwdBytes = encodedPassword.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    String message = node.getName() + " password could not be hashed. User might need to reset the password before logging again.";
                    log.warn(message);
                    ctx.warn(message);
                    pwdBytes = encodedPassword.getBytes();
                }
                if (Base64.isArrayByteBase64(pwdBytes)) {
                    String pwd = new String(Base64.decodeBase64(pwdBytes));
                    String hashedPwd = Digester.getBCrypt(pwd);
                    node.setNodeData("pswd", hashedPwd);
                }
            }
        } else {
            // AllChildrennodeOp is not recursive!
            for (Content child : node.getChildren(filter)) {
                operateOnChildNode(child, ctx);
            }
        }
    }
}