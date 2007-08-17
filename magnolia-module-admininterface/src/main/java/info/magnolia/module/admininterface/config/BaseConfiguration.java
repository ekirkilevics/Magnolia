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
package info.magnolia.module.admininterface.config;

import info.magnolia.cms.i18n.MessagesUtil;

import org.apache.commons.lang.StringUtils;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class BaseConfiguration {

    private String name;
    private String label;
    private String i18nBasename;

    public BaseConfiguration() {
        super();
    }

    public String getI18nBasename() {
        return this.i18nBasename;
    }

    public String getI18nLabel() {
        return MessagesUtil.getWithDefault(this.getLabel(), this.getLabel(), this.getI18nBasename());
    }

    public void setI18nBasename(String basename) {
        this.i18nBasename = basename;
    }

    public String getLabel() {
        return StringUtils.defaultIfEmpty(this.label, StringUtils.capitalize(this.getName()));
    }

    public void setLabel(String lable) {
        this.label = lable;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

}