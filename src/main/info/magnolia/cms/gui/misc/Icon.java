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

    public final static String BASEPATH = "/admindocroot/icons";

    public final static int SIZE_SMALL = 16;

    public final static int SIZE_MEDIUM = 24;

    public final static int SIZE_LARGE = 32;

    public final static int SIZE_XLARGE = 48;

    public final static String PAGE = "folder_cubes";

    public final static String CONTENTNODE = "cubes";

    public final static String NODEDATA = "cube_green";

    public final static String WEBPAGE = "document_plain_earth";

    public final static String ROLE = "hat_white";

    public final static String USER = "pawn_glass_yellow";

    public String getSrc(String iconName, int size) {
        return Icon.BASEPATH + "/" + size + "/" + iconName + ".gif";
    }
}
