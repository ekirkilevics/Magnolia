/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
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
