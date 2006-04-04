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
package info.magnolia.cms.gui.controlx;

/**
 * A general control to build a component tree. A control has a render type to tell the RenderKit how this control
 * should get rendered.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public interface Control {

    /**
     * @return Returns the parent.
     */
    public Control getParent();

    /**
     * @param parent The parent to set.
     */
    public void setParent(Control parent);

    public void addChild(Control control);

    public Control getChild(String name);

    /**
     * @return Returns the name.
     */
    public String getName();

    /**
     * @param name The name to set.
     */
    public void setName(String name);

    /**
     * @return Returns the renderKit.
     */
    public RenderKit getRenderKit();

    /**
     * @param renderKit The renderKit to set.
     */
    public void setRenderKit(RenderKit renderKit);

    public String render();

    /**
     * @return Returns the renderType.
     */
    public String getRenderType();

    /**
     * @param renderType The renderType to set.
     */
    public void setRenderType(String renderType);

}