/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.cms.link;

import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.i18n.I18nContentSupportFactory;
import info.magnolia.context.MgnlContext;

import org.apache.commons.lang.StringUtils;

public class AbsolutePathTransformer implements PathToLinkTransformer{
    boolean addContextPath = true;

    boolean useURI2RepositoryMapping = true;

    boolean useI18N = true;

    public AbsolutePathTransformer(boolean addContextPath, boolean useURI2RepositoryMapping, boolean useI18N) {
        this.addContextPath = addContextPath;
        this.useURI2RepositoryMapping = useURI2RepositoryMapping;
        this.useI18N = useI18N;
    }

    public String transform(UUIDLink uuidLink) {
        String linkStr = uuidLink.getHandle();
        if(useURI2RepositoryMapping){
            linkStr = URI2RepositoryManager.getInstance().getURI(uuidLink.getRepository(), uuidLink.getHandle());
        }
        linkStr += getURISuffix(uuidLink);
        if(useI18N){
            linkStr = I18nContentSupportFactory.getI18nSupport().toI18NURI(linkStr);
        }
        if(addContextPath){
            linkStr = prefixLink(linkStr);
        }
        return linkStr;
    }

    protected String prefixLink(String linkStr) {
        return MgnlContext.getContextPath() + linkStr;
    }

    /**
     * URI after the path
     */
    public String getURISuffix(UUIDLink uuidLink) {
        return (StringUtils.isNotEmpty(uuidLink.getNodeDataName())? "/" + uuidLink.getNodeDataName() : "") +
        (StringUtils.isNotEmpty(uuidLink.getFileName())? "/" + uuidLink.getFileName() : "") +
        (StringUtils.isNotEmpty(uuidLink.getExtension())? "." + uuidLink.getExtension() : "") +
        (StringUtils.isNotEmpty(uuidLink.getAnchor())? "#" + uuidLink.getAnchor() : "") +
        (StringUtils.isNotEmpty(uuidLink.getParameters())? "?" + uuidLink.getParameters() : "");

    }


    public boolean isAddContextPath() {
        return this.addContextPath;
    }


    public void setAddContextPath(boolean addContextPath) {
        this.addContextPath = addContextPath;
    }


    public boolean isUseI18N() {
        return this.useI18N;
    }


    public void setUseI18N(boolean useI18N) {
        this.useI18N = useI18N;
    }


    public boolean isUseURI2RepositoryMapping() {
        return this.useURI2RepositoryMapping;
    }


    public void setUseURI2RepositoryMapping(boolean useURI2RepositoryMapping) {
        this.useURI2RepositoryMapping = useURI2RepositoryMapping;
    }

}