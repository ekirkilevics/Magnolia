/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.templating.freemarker;

import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.gui.misc.FileProperties;
import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Chris Miner
 * @version $Revision: $ ($Author: $)
 */
public class BinaryNodeData implements TemplateHashModelEx, TemplateScalarModel {
    private final NodeData data;
    private final MagnoliaContentWrapper wrapper;

    BinaryNodeData(NodeData data, MagnoliaContentWrapper wrapper) {
        this.data = data;
        this.wrapper = wrapper;
    }

    public int size() throws TemplateModelException {
        int result = 0;

        try {
            result = data.getAttributeNames().size();
        } catch (RepositoryException e) {
            // don't care
        }

        return result;
    }

    public TemplateCollectionModel keys() throws TemplateModelException {
        Iterator result = null;
        try {
            result = data.getAttributeNames().iterator();
        } catch (RepositoryException e) {
            // don't care
        }
        return (TemplateCollectionModel) wrapper.wrap(result);
    }

    public TemplateCollectionModel values() throws TemplateModelException {
        ArrayList result = new ArrayList();
        try {
            Iterator iter = data.getAttributeNames().iterator();
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
                result = data.getHandle();
            }
        } else if (key.equals(FileProperties.CONTENT_TYPE)) {
            result = data.getAttribute(FileProperties.PROPERTY_CONTENTTYPE);
        } else if (key.equals(FileProperties.NAME)) {
            String filename = data.getAttribute(FileProperties.PROPERTY_FILENAME);
            String ext = data.getAttribute(FileProperties.PROPERTY_EXTENSION);
            result = filename + ((StringUtils.isEmpty(ext)) ? "" : "." + ext);
        } else if (key.equals(FileProperties.PROPERTY_FILENAME)) {
            result = data.getAttribute(FileProperties.PROPERTY_FILENAME);
        } else if (key.equals(FileProperties.PROPERTY_EXTENSION)) {
            result = data.getAttribute(FileProperties.PROPERTY_EXTENSION);
        } else if (key.equals(FileProperties.PROPERTY_LASTMODIFIED)) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                result = format.parse(data.getAttribute(FileProperties.PROPERTY_LASTMODIFIED));
            } catch (ParseException e) {
                // do nothing.
            }
        } else {
            result = data.getAttribute(key);
        }
        return wrapper.wrap(result);
    }

    public boolean isEmpty() throws TemplateModelException {
        return (size() == 0);
    }

    // this reproduces the logic found in the cms out tag.
    public String getAsString() throws TemplateModelException {
        String handle = data.getHandle();
        String filename = data.getAttribute(FileProperties.PROPERTY_FILENAME);
        String ext = data.getAttribute(FileProperties.PROPERTY_EXTENSION);
        return handle + "/" + filename + ((StringUtils.isEmpty(ext)) ? "" : "." + ext);
    }

}
