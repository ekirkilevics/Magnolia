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
package info.magnolia.content2bean.impl;

import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.ClasspathResourcesUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class PropertiesBasedTypeMapping extends TypeMappingImpl {

    private static Logger log = LoggerFactory.getLogger(PropertiesBasedTypeMapping.class);

    public PropertiesBasedTypeMapping() {
        String[] fileNames = ClasspathResourcesUtil.findResources(new ClasspathResourcesUtil.Filter(){
            public boolean accept(String name) {
                return name.endsWith(".content2bean");
            }
        });

        for (int i = 0; i < fileNames.length; i++) {
            String fileName = fileNames[i];
            Properties props = new Properties();
            InputStream stream = null;
            try {
                stream = ClasspathResourcesUtil.getStream(fileName);
                props.load(stream);

            }
            catch (IOException e) {
                log.error("can't read collection to bean information " + fileName,  e);
            }
            IOUtils.closeQuietly(stream);

            for (Iterator iter = props.keySet().iterator(); iter.hasNext();) {
                String key = (String) iter.next();
                try {
                    Class type = ClassUtil.classForName(StringUtils.substringBeforeLast(key, "."));
                    Class mappedType = ClassUtil.classForName(props.getProperty(key));
                    String propName = StringUtils.substringAfterLast(key, ".");
                    getPropertyTypeDescriptor(type, propName).setCollectionEntryType(getTypeDescriptor(mappedType));
                }
                catch (ClassNotFoundException e) {
                    log.error("can't read collection to bean information file: " + fileName + " key: " + key,  e);
                }
            }
        }

    }
}
