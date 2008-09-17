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

import java.util.ArrayList;

import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.NodeDataUtil;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;

public class MultiValueSaveHandler extends SaveHandlerImpl implements CustomSaveHandler {

    protected void processMultiple(Content node, String name, int type, String[] values) throws RepositoryException,
        PathNotFoundException, AccessDeniedException {

        ArrayList l = new ArrayList();

        if (values != null && values.length != 0) {

            for (int j = 0; j < values.length; j++) {
                String valueStr = values[j];
                if (StringUtils.isNotEmpty(valueStr)) {
                    Value value = getValue(valueStr, type);
                    if (value != null) {

                        l.add(value);
                    }
                }
            }
            NodeData data = NodeDataUtil.getOrCreateAndSet(node, name, (Value[])l.toArray(new Value[l.size()]));
        } else {
            NodeData data = NodeDataUtil.getOrCreateAndSet(node, name, new Value[]{null});
        }
    }

    public void save(Content parentNode, Content configNode, String name,
            MultipartForm form, int type, int valueType, int isRichEditValue,
            int encoding) throws RepositoryException, AccessDeniedException {
        processMultiple(parentNode, name, type, form.getParameterValues(name));

    }


}

