/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
