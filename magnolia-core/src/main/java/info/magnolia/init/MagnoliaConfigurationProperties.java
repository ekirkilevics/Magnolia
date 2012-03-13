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
package info.magnolia.init;

/**
 * MagnoliaConfigurationProperties instances provides access to system-wide configuration properties. They are
 * essentially a wrapper around all the <tt>magnolia.properties</tt> files that are used in the system, as well as
 * properties configured in module descriptors, etc.
 *
 * <p>Implementations of this interface are responsible for locating relevant {@link PropertySource}, and provide them in a sensible order.</p>
 *
 * The following properties are required:
 * <ul>
 * <li><b>magnolia.cache.startdir</b>:<br/>
 * directory used for cached pages</li>
 * <li><b>magnolia.upload.tmpdir</b>:<br/>
 * tmp directory for uploaded files</li>
 * <li><b>magnolia.exchange.history</b>:<br/>
 * history directory used for activation</li>
 * <li><b>magnolia.repositories.config</b>:<br/>
 * repositories configuration</li>
 * <li><b>log4j.config</b>:<br/>
 * Name of a log4j config file. Can be a .properties or .xml file. The value can be:
 * <ul>
 * <li>a full path</li>
 * <li>a path relative to the webapp root</li>
 * <li>a file name which will be loaded from the classpath</li>
 * </ul>
 * </li>
 * <li><b>magnolia.root.sysproperty</b>:<br/>
 * Name of a system variable which will be set to the webapp root. You can use this property in log4j configuration
 * files to handle relative paths, such as <code>${magnolia.root}logs/magnolia-debug.log</code>.
 * <strong>Important</strong>: if you drop multiple magnolia wars in a container which doesn't isolate system properties
 * (e.g. tomcat) you will need to change the name of the <code>magnolia.root.sysproperty</code> variable in web.xml and
 * in log4j configuration files.</li>
 * <li><b>magnolia.bootstrap.dir</b>:<br/>
 * Directory containing xml files for initialization of a blank magnolia instance. If no content is found in any of
 * the repository, they are initialized importing xml files found in this folder. If you don't want to let magnolia
 * automatically initialize repositories simply remove this parameter.</li>
 * </ul>
 *
 * @version $Id$
 * @since 4.5
 */
public interface MagnoliaConfigurationProperties extends PropertySource {

    PropertySource getPropertySource(String key);

    void init() throws Exception;
}
