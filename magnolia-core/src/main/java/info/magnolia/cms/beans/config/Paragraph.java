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
package info.magnolia.cms.beans.config;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * @author Sameer Charles
 */
public class Paragraph {
    private String name;
    private String title;
    private String templatePath;
    private String dialogPath;
    private String dialog;
    private String type;
    private String description;
    private String i18nBasename;

    public Paragraph() {
    }

    public String getName() {
        return this.name;
    }

    public String getTitle() {
        return this.title;
    }

    public String getTemplatePath() {
        return this.templatePath;
    }

    public String getDialogPath() {
        return this.dialogPath;
    }

    /**
     * @deprecated use getType()
     */
    public String getTemplateType() {
        return this.type;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDialogPath(String dialogPath) {
        this.dialogPath = dialogPath;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    /**
     * @deprecated use setType()
     */
    public void setTemplateType(String templateType) {
        this.type = templateType;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDialog() {
        return this.dialog;
    }

    public void setDialog(String dialog) {
        this.dialog = dialog;
    }

    public String getI18nBasename() {
        return this.i18nBasename;
    }

    public void setI18nBasename(String basename) {
        this.i18nBasename = basename;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("name", this.name) //$NON-NLS-1$
            .append("type", this.type) //$NON-NLS-1$
            .append("description", this.description) //$NON-NLS-1$
            .append("dialog", this.dialog) //$NON-NLS-1$
            .append("dialogPath", this.dialogPath) //$NON-NLS-1$
            .append("title", this.title) //$NON-NLS-1$
            .append("templatePath", this.templatePath) //$NON-NLS-1$
            .toString();
    }

}