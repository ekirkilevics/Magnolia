/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.taglibs.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;


/**
 * Class to generate PNG images from TrueType font strings. Originally by Philip McCarthy - http://chimpen.com
 * http://chimpen.com/things/archives/001139.php
 * @author Philip McCarthy
 * @author Patrick Janssen
 */
public class Text2PngFactory {

    /** Font name */
    private String fontname;

    /** Font size */
    private int fontsize;

    /** Text to render */
    private String text = "";

    /** Text color */
    private int r = 0;

    private int g = 0;

    private int b = 0;

    /** Background colour */
    private int br = 0xff;

    private int bg = 0xff;

    private int bb = 0xff;

    /** Used to obtain fontmetrics for given fontname */
    private Graphics2D g2;

    /** Cached Font object */
    private Font cachedFont;

    /**
     * Construct factory without setting font
     */
    public Text2PngFactory() {
        // Create a single-pixel buffered image to do font stuff with
        this.g2 = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR).createGraphics();
        // Set same hints as used for final render
        setOptimalRenderQuality(this.g2);
    }

    /**
     * Construct factory with given font face and size
     * @param fontname Name of TrueType font
     * @param fontsize Point size of font
     * @throws IOException if font can't be loaded
     * @throws FontFormatException if font is not a valid TTF
     */
    public Text2PngFactory(String fontname, int fontsize) throws IOException, FontFormatException {
        this(fontname, fontsize, "");
    }

    /**
     * Construct factory with given font face and size
     * @param fontname Name of TrueType font
     * @param fontsize Point size of font
     * @param text The text to render
     * @throws IOException if font can't be loaded
     * @throws FontFormatException if font is not a valid TTF
     */
    public Text2PngFactory(String fontname, int fontsize, String text) throws IOException, FontFormatException {

        // Create a single-pixel buffered image to get font sizes etc from.
        this.g2 = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR).createGraphics();
        // Set same hints as used for final render
        setOptimalRenderQuality(this.g2);
        this.setFontFace(fontname);
        this.setFontSize(fontsize);
        this.setText(text);
    }

    /**
     * Renders the current text to a .png file
     * @param location Location to write the file out to
     * @throws IOException if file cannot be created
     */
    public void createPngFile(String location) throws IOException {
        createPngFile(new File(location));
    }

    /**
     * Renders the current text to a .png file
     * @param location Location to write the file out to
     * @throws IOException if file cannot be created
     */
    public void createPngFile(File location) throws IOException {
        ImageIO.write(createImage(), "png", location);
    }

    /**
     * Renders the current text in the current font fontname, fontsize and color
     * @return Image containing rendered text
     * @throws IOException if no font name has been specified yet
     */
    public RenderedImage createImage() throws IOException {

        if (this.fontname == null) {
            throw new IOException("No font name given!");
        }

        // Get the bounds needed to render the text
        FontRenderContext frc = this.g2.getFontRenderContext();
        TextLayout layout = new TextLayout(this.text, this.cachedFont, frc);
        Rectangle2D bounds = layout.getBounds();

        // Get the width needed to render this piece of text
        // 2 pixels were added here due to problem with cutting of end of text
        int stringWidth = (int) (Math.ceil(bounds.getWidth())) + 2;

        // Get the height from generic font info
        // This way, all strings in this font will have same height
        // and vertical alignment
        FontMetrics fm = this.g2.getFontMetrics();
        int stringHeight = fm.getHeight();

        // Make an image to contain string
        BufferedImage im = new BufferedImage(stringWidth, stringHeight, BufferedImage.TYPE_3BYTE_BGR);

        // Set the font and colours on the image
        Graphics2D graphics = im.createGraphics();

        // Setup best-quality rendering
        setOptimalRenderQuality(graphics);

        // Set colours and clear rectangle
        graphics.setBackground(new Color(this.br, this.bg, this.bb));
        graphics.setColor(new Color(this.r, this.g, this.b));
        graphics.clearRect(0, 0, stringWidth, stringHeight);

        // Set the font to use
        graphics.setFont(getFont());

        // Position text on baseline, with first character exactly against
        // left margin
        layout.draw(graphics, -(float) Math.floor(bounds.getX()), fm.getMaxAscent());

        // Return the image
        return im;
    }

    /**
     * Set the text to be rendered by the Txt2PngFactory
     * @param text The text to render
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Set 8-bit RGB values for text colour
     * @param r Red component (0-255)
     * @param g Green component (0-255)
     * @param b Blue component (0-255)
     */
    public void setTextRGB(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    /**
     * Set 8-bit RGB values for background colour
     * @param r Red component (0-255)
     * @param g Green component (0-255)
     * @param b Blue component (0-255)
     */
    public void setBackgroundRGB(int r, int g, int b) {
        this.br = r;
        this.bg = g;
        this.bb = b;
    }

    /**
     * Set the TrueType font to render with
     * @param fontname The name of the font to use
     */
    public void setFontFace(String fontname) throws IOException, FontFormatException {

        if (!fontname.equals(this.fontname)) {
            this.fontname = fontname;
            updateFace();
        }
    }

    /**
     * Set the point size of the font
     * @param fontsize The point size of the font
     */
    public void setFontSize(int fontsize) {
        if (fontsize != this.fontsize) {
            this.fontsize = fontsize;
            updateSize();
        }
    }

    /**
     * Updates the cached font object
     * @throws IOException if the font can't be loaded
     * @throws FontFormatException if font is not a valid TTF
     */
    private void updateFace() throws IOException, FontFormatException {
        Font createdFont = null;

        // Attempt to load font from /fonts under classloader
        String fontpath = "fonts/" + this.fontname + ".ttf";

        InputStream fontStream = this.getClass().getClassLoader().getResourceAsStream(fontpath);
        if (fontStream != null) {
            createdFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            fontStream.close();
        }
        // Next try to get it from fontpath
        if (createdFont == null) {
            Font tempFont = new Font(this.fontname, Font.PLAIN, 1);

            // Check we got correct font, not a fallback
            if (tempFont.getFamily().equals(this.fontname)) {
                // It's the correct font, set it
                createdFont = tempFont;
            }
        }
        // Last resort, treat as a path to font
        if (createdFont == null) {
            fontStream = new FileInputStream(this.fontname);

            if (fontStream != null) {
                createdFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                fontStream.close();
            }
        }
        // If we still don't have a font, throw exception
        if (createdFont == null) {
            throw new IOException("Can't locate font: " + this.fontname);
        }

        // Derive font of correct fontsize
        this.cachedFont = createdFont.deriveFont((float) this.fontsize);

        // Set on prototype image
        this.g2.setFont(this.cachedFont);
    }

    /**
     * Updates the cached font to new font derived with new size
     */
    private void updateSize() {

        if (this.cachedFont == null) {
            return;
        }

        // Derive font of correct fontsize
        this.cachedFont = this.cachedFont.deriveFont((float) this.fontsize);

        // Set on Graphics object so we can get FontMetrics
        this.g2.setFont(this.cachedFont);
    }

    /**
     * Get the FontMetrics object for the current font
     * @return FontMetrics object for current font
     */
    private FontMetrics getFontMetrics() {

        return this.g2.getFontMetrics();
    }

    /**
     * Get a Font object for the current fontname and fontsize
     * @return Font object for current name and size
     */
    private Font getFont() {

        return this.cachedFont;
    }

    /**
     * Sets rendering hints for optimal rendering quality
     * @param graphics Graphics2D object to set rendering options on
     */
    private void setOptimalRenderQuality(Graphics2D graphics) {

        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    public String toString() {
        return this.fontname + ", " + this.fontsize + "pt: " + this.text;
    }
}
