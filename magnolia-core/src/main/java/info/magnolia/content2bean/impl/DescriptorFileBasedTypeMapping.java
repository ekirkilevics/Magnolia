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
import info.magnolia.content2bean.TypeDescriptor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
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
public class DescriptorFileBasedTypeMapping extends TypeMappingImpl {

    private static Logger log = LoggerFactory.getLogger(DescriptorFileBasedTypeMapping.class);

    public DescriptorFileBasedTypeMapping() {
        String[] fileNames = ClasspathResourcesUtil.findResources(new ClasspathResourcesUtil.Filter(){
            public boolean accept(String name) {
                return name.endsWith(".content2bean");
            }
        });

        for (int i = 0; i < fileNames.length; i++) {
            String fileName = fileNames[i];
            processFile(fileName);
        }
    }

    protected void processFile(String fileName) {
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

        String className = StringUtils.replaceChars(fileName, File.separatorChar, '.');
        className = StringUtils.removeStart(className, ".");
        className = StringUtils.removeEnd(className, ".content2bean");
        try {
            Class typeClass = ClassUtil.classForName(className);

            TypeDescriptor typeDescriptor = processProperties(typeClass, props);
            addTypeDescriptor(typeClass, typeDescriptor);
        } catch (Exception e) {
            log.error("can't instantiate type descriptor for " + className, e);
        }
    }

    protected TypeDescriptor processProperties(Class className, Properties props) throws Exception {
            String descriptorClassName = StringUtils.defaultIfEmpty(props.getProperty("descriptorClass"), PropertiesBasedTypeDescriptor.class.getName());
            Class descriptorClass = ClassUtil.classForName(descriptorClassName);
            Constructor constructor = descriptorClass.getConstructor(new Class[] {Properties.class});
            return (TypeDescriptor) constructor.newInstance(new Object[]{props});
    }
}
