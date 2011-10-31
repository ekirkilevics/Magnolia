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
package info.magnolia.setup;

import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractCondition;

import java.io.File;
import java.io.IOException;

/**
 * A {@link info.magnolia.module.delta.Condition} which checks the system temporary folder (as per the java.io.tmpdir property) exists and is writable.
 * It can happen that users accidentally delete the temp folder, which can cause all sorts of trouble. (JackRabbit uses it while importing files, for example)
 *
 * @version $Revision: $ ($Author: $)
 */
public class SystemTmpDirCondition extends AbstractCondition {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SystemTmpDirCondition.class);

    public SystemTmpDirCondition() {
        super("System temporary folder check", "Ensures the system temporary folder exists and is usable by the current process.");
    }

    @Override
    public boolean check(InstallContext installContext) {
        try {
            final File f = File.createTempFile("magnolia.core.check", "tmp");
            log.debug("Temp file was successfully created as {}, we can delete it and proceed.", f.getAbsolutePath());
            f.delete();
            return true;
        } catch (IOException e) {
            final String tmpDirProperty = System.getProperty("java.io.tmpdir");
            log.error("The system could not create a temporary file: {}. Please check if {} exists and is writable by the current process. {}", new Object[]{e.getMessage(), tmpDirProperty, e});
            installContext.error("The system could not create a temporary file: " + e.getMessage() + ". Please check if " + tmpDirProperty + " exists and is writable by the current process.", e);
            return false;
        }

    }
}
