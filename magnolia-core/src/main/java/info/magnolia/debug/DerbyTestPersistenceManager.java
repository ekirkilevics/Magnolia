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
package info.magnolia.debug;

import javax.jcr.PropertyType;
import org.apache.jackrabbit.core.persistence.db.DerbyPersistenceManager;
import org.apache.jackrabbit.core.state.ChangeLog;
import org.apache.jackrabbit.core.state.ItemStateException;
import org.apache.jackrabbit.core.state.PropertyState;


/**
 * A PM which measures the time spent in saving. You must enable the PerformanceTestFilter.
 * 
 * @author philipp
 * @version $Id$
 */
public class DerbyTestPersistenceManager extends DerbyPersistenceManager {

    /**
     * Configure that with a parameter in the jackrabbit configuration file. Make sure that you change that value for
     * the versions PM so that you see where the time is spent.
     */
    private String testPrefix = "pm";

    public void store(ChangeLog changeLog) throws ItemStateException {
        PerformanceTestStatus.getInstance().start(testPrefix + "-store");
        super.store(changeLog);
        PerformanceTestStatus.getInstance().stop(testPrefix + "-store");

    }

    public void store(PropertyState state) throws ItemStateException {
        if (state.getType() == PropertyType.BINARY) {
            PerformanceTestStatus.getInstance().start(testPrefix + "-blob");
        }
        super.store(state);
        if (state.getType() == PropertyType.BINARY) {
            PerformanceTestStatus.getInstance().stop(testPrefix + "-blob");
        }
    }

    public String getTestPrefix() {
        return this.testPrefix;
    }

    public void setTestPrefix(String testPrefix) {
        this.testPrefix = testPrefix;
    }

}
