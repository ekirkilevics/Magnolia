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
 */
package info.magnolia.maven.setproperty;

import java.util.Date;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;


/**
 * Get the current date.
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public class CurrentDateValueProvider extends ValueProviderImpl {

    /**
     * Rerturn String value
     */
    private boolean asString = true;

    /**
     * Format the String value
     */
    private String format = "dd.mm.yyyy";

    public Object getValue(MavenProject project, MavenSession session) {
        Date date = new Date();
        if (this.isAsString()) {
            return DateFormatUtils.format(date, format);
        }

        return date;

    }

    public boolean isAsString() {
        return asString;
    }

    public void setAsString(boolean asString) {
        this.asString = asString;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

}
