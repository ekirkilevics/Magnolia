/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.gui.misc;

/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class Icon {

    public static final String BASEPATH = "/admindocroot/icons"; //$NON-NLS-1$

    public static final int SIZE_SMALL = 16;

    public static final int SIZE_MEDIUM = 24;

    public static final int SIZE_LARGE = 32;

    public static final int SIZE_XLARGE = 48;

    public static final String PAGE = "folder_cubes"; //$NON-NLS-1$

    public static final String CONTENTNODE = "cubes"; //$NON-NLS-1$

    public static final String NODEDATA = "cube_green"; //$NON-NLS-1$

    public static final String WEBPAGE = "document_plain_earth"; //$NON-NLS-1$

    public static final String ROLE = "hat_white"; //$NON-NLS-1$

    public static final String USER = "pawn_glass_yellow"; //$NON-NLS-1$

    public String getSrc(String iconName, int size) {
        return Icon.BASEPATH + "/" + size + "/" + iconName + ".gif"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
