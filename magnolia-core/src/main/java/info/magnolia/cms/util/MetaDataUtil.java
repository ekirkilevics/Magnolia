/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.util;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.MetaData;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public class MetaDataUtil {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MetaDataUtil.class);

    private Content content;

    public MetaDataUtil(Content c) {
        this.setContent(c);
    }

    public void setContent(Content c) {
        this.content = c;
    }

    public Content getContent() {
        return this.content;
    }

    public String getPropertyValueString(String propertyName) {
        return getPropertyValueString(propertyName, null);
    }

    /**
     * <p/> Returns the representation of the value as a String:
     * </p>
     * @return String
     */
    public String getPropertyValueString(String propertyName, String dateFormat) {
        try {
            if (propertyName.equals(MetaData.CREATION_DATE)
                || propertyName.equals(MetaData.LAST_MODIFIED)
                || propertyName.equals(MetaData.LAST_ACTION)) {
                Date date = this.getContent().getMetaData().getDateProperty(propertyName).getTime();

                return DateUtil.format(date, dateFormat);
            }
            else if (propertyName.equals(MetaData.ACTIVATED)) {
                return Boolean.toString(this.getContent().getMetaData().getBooleanProperty(propertyName));
            }
            else {
                return this.getContent().getMetaData().getStringProperty(propertyName);
            }
        }
        catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
            }
        }
        return StringUtils.EMPTY;
    }
}
