/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.logging;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import info.magnolia.objectfactory.Components;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for auditory logging, it's optional to define it, configured in mgnl-beans.properties
 * @author tmiyar
 *
 */
public class AuditLoggingManager {

    private List logConfigurations = new ArrayList();

    private String defaultSeparator = ", ";

    private static Logger applog = LoggerFactory.getLogger(AuditLoggingManager.class);

    public AuditLoggingManager() {

    }

    public static AuditLoggingManager getInstance() {
        try {
            return Components.getSingleton(AuditLoggingManager.class);
        } catch (Exception e) {
            // if not defined skip and return null
            applog.info("Class AuditLoggingManager not defined");
            return null;
        }
    }

    public void addLogConfigurations(LogConfiguration action) {
        this.logConfigurations.add(action);
    }

    public List getLogConfigurations() {
        return logConfigurations;
    }

    public void setLogConfigurations(List logConfigurations) {
        this.logConfigurations = logConfigurations;
    }

    public LogConfiguration getLogConfiguration(String action) {
        Iterator iterator = this.logConfigurations.iterator();
        while (iterator.hasNext()) {
            final LogConfiguration trail = (LogConfiguration)iterator.next();
            if(StringUtils.equals(trail.getName(), action) ) {
                return trail;
            }
        }
        return null;
    }

    public void log(String action, String[] data) {
        String message = "";
        try {
            LogConfiguration trail = this.getLogConfiguration(action);
            String separator = defaultSeparator;
            if(!StringUtils.isEmpty(trail.getSeparator())) {
                separator = trail.getSeparator();
            }
            message += separator + action;
            if (trail != null && trail.isActive()) {
                for (int i = 0; i < data.length; i++) {
                    if (StringUtils.isNotEmpty(data[i])) {
                      message += separator + data[i];
                    }

                }

                org.apache.log4j.Logger.getLogger(trail.getLogName()).log(
                        LoggingLevel.AUDIT_TRAIL, message);
            }

        } catch (Exception e) {
            applog.trace("Can't get log configuration");
        }
    }

    public String getDefaultSeparator() {
        return defaultSeparator;
    }

    public void setDefaultSeparator(String defaultSeparator) {
        this.defaultSeparator = defaultSeparator;
    }

}
