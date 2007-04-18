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
package info.magnolia.test.model;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public final class Color {
    public static final Color PINK = new Color("pink", 255, 175, 175);
    public static final Color ORANGE = new Color("orange", 255, 200, 0);
    public static final Color RED = new Color("red", 255, 0, 0);

    private final String name;
    private final int red;
    private final int green;
    private final int blue;

    public Color(String name, int red, int green, int blue) {
        this.name = name;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public String getName() {
        return name;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }
}
