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

    protected Paragraph() {
    }

    /**
     * @return String, name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return String, title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * @return String, templatePath
     */
    public String getTemplatePath() {
        return this.templatePath;
    }

    /**
     * @return String, dialogPath
     */
    public String getDialogPath() {
        return this.dialogPath;
    }

    /**
     * @return String, template type (jsp / servlet)
     * @deprecated use getType()
     */
    public String getTemplateType() {
        return this.type;
    }

    public String getType() {
        return type;
    }

    /**
     * @return String, description
     */
    public String getDescription() {
        return this.description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    void setDialogPath(String dialogPath) {
        this.dialogPath = dialogPath;
    }

    void setName(String name) {
        this.name = name;
    }

    void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    /**
     * @deprecated use setType()
     */
    void setTemplateType(String templateType) {
        this.type = templateType;
    }

    void setType(String type) {
        this.type = type;
    }

    void setTitle(String title) {
        this.title = title;
    }

    /**
     * Getter for <code>dialog</code>.
     * @return Returns the dialog.
     */
    public String getDialog() {
        return this.dialog;
    }

    /**
     * Setter for <code>dialog</code>.
     * @param dialog The dialog to set.
     */
    public void setDialog(String dialog) {
        this.dialog = dialog;
    }

    /**
     * @see java.lang.Object#toString()
     */
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

    /**
     * @return Returns the i18nBasename.
     */
    public String getI18nBasename() {
        return this.i18nBasename;
    }

    /**
     * @param basename The i18nBasename to set.
     */
    public void setI18nBasename(String basename) {
        this.i18nBasename = basename;
    }

}