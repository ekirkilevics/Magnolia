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
package info.magnolia.freemarker;

import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.beans.runtime.FileProperties;
import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * TODO : review this !
 * 
 * @author Chris Miner
 * @version $Revision: $ ($Author: $)
 */
public class BinaryNodeData implements TemplateHashModelEx, TemplateScalarModel {
    private final NodeData binaryNodeData;
    private final MagnoliaContentWrapper wrapper;

    BinaryNodeData(NodeData binaryNodeData, MagnoliaContentWrapper wrapper) {
        this.binaryNodeData = binaryNodeData;
        this.wrapper = wrapper;
    }

    public int size() throws TemplateModelException {
        int result = 0;

        try {
            result = binaryNodeData.getAttributeNames().size();
        } catch (RepositoryException e) {
            // don't care
        }

        return result;
    }

    public TemplateCollectionModel keys() throws TemplateModelException {
        Iterator result = null;
        try {
            result = binaryNodeData.getAttributeNames().iterator();
        } catch (RepositoryException e) {
            // don't care
        }
        return (TemplateCollectionModel) wrapper.wrap(result);
    }

    public TemplateCollectionModel values() throws TemplateModelException {
        ArrayList result = new ArrayList();
        try {
            Iterator iter = binaryNodeData.getAttributeNames().iterator();
            while (iter.hasNext()) {
                result.add(iter.next());
            }
        } catch (RepositoryException e) {
            // don't care
        }
        return (TemplateCollectionModel) wrapper.wrap(result.iterator());
    }

    public TemplateModel get(String key) throws TemplateModelException {
        Object result = null;

        if (key.startsWith("@")) {
            if (key.equals("@handle")) {
                result = binaryNodeData.getHandle();
            }
        } else if (key.equals(FileProperties.CONTENT_TYPE)) {
            result = binaryNodeData.getAttribute(FileProperties.PROPERTY_CONTENTTYPE);
        } else if (key.equals(FileProperties.NAME)) {
            String filename = binaryNodeData.getAttribute(FileProperties.PROPERTY_FILENAME);
            String ext = binaryNodeData.getAttribute(FileProperties.PROPERTY_EXTENSION);
            result = filename + ((StringUtils.isEmpty(ext)) ? "" : "." + ext);
        } else if (key.equals(FileProperties.PROPERTY_FILENAME)) {
            result = binaryNodeData.getAttribute(FileProperties.PROPERTY_FILENAME);
        } else if (key.equals(FileProperties.PROPERTY_EXTENSION)) {
            result = binaryNodeData.getAttribute(FileProperties.PROPERTY_EXTENSION);
        } else if (key.equals(FileProperties.PROPERTY_LASTMODIFIED)) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                result = format.parse(binaryNodeData.getAttribute(FileProperties.PROPERTY_LASTMODIFIED));
            } catch (ParseException e) {
                // do nothing.
            }
        } else {
            result = binaryNodeData.getAttribute(key);
        }
        return wrapper.wrap(result);
    }

    public boolean isEmpty() throws TemplateModelException {
        return (size() == 0);
    }

    // this reproduces the logic found in the cms out tag.
    public String getAsString() throws TemplateModelException {
        String handle = binaryNodeData.getHandle();
        String filename = binaryNodeData.getAttribute(FileProperties.PROPERTY_FILENAME);
        String ext = binaryNodeData.getAttribute(FileProperties.PROPERTY_EXTENSION);
        return handle + "/" + filename + ((StringUtils.isEmpty(ext)) ? "" : "." + ext);
    }

}
