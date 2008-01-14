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
