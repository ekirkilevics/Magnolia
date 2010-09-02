/**
 * This file Copyright (c) 2008-2010 Magnolia International
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
package info.magnolia.cms.util;

import info.magnolia.cms.core.Path;

import java.util.ArrayList;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Util to register MBeans.
 * @version $Id$
 *
 */
public class MBeanUtil {

    private static Logger log = LoggerFactory.getLogger(MBeanUtil.class);

    public static void registerMBean(String name, Object mbean) {
        String appName = Path.getAppRootDir().getName();
        final String id = "Magnolia:type=" + name + ",domain=" + appName;
        try {
            final ObjectName mbeanName = new ObjectName(id);
            final MBeanServer mbeanServer = getMBeanServer();
            if (!mbeanServer.isRegistered(mbeanName)) {
                mbeanServer.registerMBean(mbean, mbeanName);
            }
        }
        catch (InstanceAlreadyExistsException e) {
            log.info("MBean '{}' already exists", id);
        }
        catch (Throwable t) {
            log.error("Could not register JMX MBean '" + id + "'", t);
        }
    }

    public static MBeanServer getMBeanServer() {
        final ArrayList list = MBeanServerFactory.findMBeanServer(null);
        final MBeanServer mbeanServer;
        if (list != null && list.size() > 0) {
            mbeanServer = (MBeanServer) list.get(0);
        } else {
            mbeanServer = MBeanServerFactory.createMBeanServer();
        }
        return mbeanServer;
    }
}
