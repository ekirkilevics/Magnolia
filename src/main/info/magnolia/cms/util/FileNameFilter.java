/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.util;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.commons.lang.StringUtils;


/**
 * @author Sameer Charles
 * @version 1.1
 */
public class FileNameFilter implements FilenameFilter
{

    private String searchString = "";

    public void setSearchString(String name)
    {
        this.searchString = name;
    }

    public boolean accept(File dir, String name)
    {
        return StringUtils.contains(name, this.searchString + ".");
    }

}
