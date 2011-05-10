/**
 * This file Copyright (c) 2008-2011 Magnolia International
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
package info.magnolia.voting.voters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.voting.Voter;

/**
 * Check a magnolia system delegatePath.
 *
 * @author had
 */
public class DelegateVoter extends AbstractBoolVoter {
    
    private static final Logger log = LoggerFactory.getLogger(DelegateVoter.class);

    private String delegatePath;

    @Override
    protected boolean boolVote(Object value) {
        Content delegate;
        try {
            // can't use normal user context as anonymous needs to be able to read this path as well (e.g. on user logout or when user is not logged in)
            delegate = MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.CONFIG).getContent(delegatePath);
            Object bean = Content2BeanUtil.toBean(delegate, true);
            Voter voter = (Voter) bean;
            return voter.vote(value) > 0;
        } catch (Exception e) {
            // this should not happen, but since we do not want to blow the instance on missconfiguration, just return false and log the error
            log.error("Failed to delegate voting to " + delegatePath + ". Returning false instead", e);
        }
        return false;
    }

    public String getDelegatePath() {
        return delegatePath;
    }

    public void setDelegatePath(String delegatePath) {
        this.delegatePath = delegatePath;
    }
}
