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
package info.magnolia.ckeditor.dialog.definition;

import info.magnolia.ui.model.dialog.definition.RichEditFieldDefinition;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO write javadoc.
 * Configuration utility for using the CKEditorTextField.
 * <p>
 * Adapted from http://vaadin.com/directory#addon/ckeditor-wrapper-for-vaadin.
 */
public class CKEditorFieldDefinition extends RichEditFieldDefinition implements java.io.Serializable {

    private static final Logger log = LoggerFactory.getLogger(CKEditorFieldDefinition.class);

    private static final long serialVersionUID = 4360029660001644525L;

    private CKEditorDefaultConfiguration config = new CKEditorDefaultConfiguration();

    private JsonConfig jsonConfig = new JsonConfig();

    public CKEditorFieldDefinition() {
        jsonConfig.setJsonPropertyFilter( new PropertyFilter(){
            @Override
            public boolean apply( Object source, String name, Object value ) {
                if( value == null || "source".equals(name) || "alignment".equals(name) || "tables".equals(name)
                        || "lists".equals(name) || "images".equals(name) || "spellChecker".equals(name)){
                    log.debug("excluding property {} with value {}", name, value);
                    return true;
                }
                return false;
            }
        });
    }
    public CKEditorDefaultConfiguration getConfig() {
        return config;
    }

    public void setConfig(CKEditorDefaultConfiguration config) {
        this.config = config;
    }

    public String getConfiguration() {
        if (StringUtils.isNotBlank(config.getCustomConfig())) {
            log.debug("getConfiguration() returns {}", config.getCustomConfig());
            return config.getCustomConfig();
        }

        String json = JSONSerializer.toJSON(config, jsonConfig).toString();
        log.debug("getConfiguration() returns {}", json);
        return json;
    }
}
