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
package info.magnolia.objectfactory.guice;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.google.inject.ProvisionException;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.ComponentProvider;


/**
 * Guice Provider that creates an object by reading it from the repository.
 *
 * @param <T> type of the instance the provider provides
 * @version $Id$
 */
public class GuiceConfiguredComponentProvider<T> implements Provider<T> {

    @Inject
    private ComponentProvider componentProvider;
    private final String workspace;
    private final String path;

    public GuiceConfiguredComponentProvider(String workspace, String path) {
        this.path = path;
        this.workspace = workspace;
    }

    @Override
    public T get() {
        final Node node;
        try {
            node = MgnlContext.getJCRSession(workspace).getNode(path);
        } catch (RepositoryException e) {
            throw new ProvisionException("Can't find the node [" + workspace + ":" + path + "] to create an instance");
        }

        try {
            return transformNode(node);
        } catch (Content2BeanException e) {
            throw new ProvisionException(e.getMessage(), e);
        } catch (RepositoryException e) {
            throw new ProvisionException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    protected T transformNode(Node node) throws Content2BeanException, RepositoryException {
        return (T) Content2BeanUtil.toBean(ContentUtil.asContent(node), true, componentProvider);
    }
}
