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
package info.magnolia.cms.gui.controlx.impl;

import info.magnolia.cms.gui.controlx.Control;
import info.magnolia.cms.gui.controlx.RenderKit;
import info.magnolia.cms.gui.controlx.Renderer;

import java.util.Collection;

import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;


/**
 * Default Implementation. Gets the nearest RenderKit in the controls tree.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class AbstractControl implements Control {

    /**
     * Name of the control
     */
    private String name;

    /**
     * The renderer kit used. layzy bound.
     */
    private RenderKit renderKit;

    /**
     * The name of the renderer to use.
     */
    private String renderType;

    /**
     * The renderer used. If not set the renderType is used to get the renderer from the RenderKit
     */
    private Renderer renderer;

    /**
     * Parent control
     */
    private Control parent;

    /**
     * The ordered children
     */
    private OrderedMap children = new ListOrderedMap();

    /**
     * @return Returns the parent.
     */
    public Control getParent() {
        return parent;
    }

    /**
     * @param parent The parent to set.
     */
    public void setParent(Control parent) {
        this.parent = parent;
    }

    /**
     * If no name set yet just set one.
     */
    public void addChild(Control control) {
        control.setParent(this);
        if (StringUtils.isEmpty(control.getName())) {
            control.setName(this.getName() + "_" + this.children.size());
        }
        this.children.put(control.getName(), control);
    }

    public Control getChild(String name) {
        return (Control) this.children.get(name);
    }

    public void removeChild(String name){
        this.children.remove(name);
    }
    
    public Collection getChildren() {
        return this.children.values();
    }

    public void removeAllChildren(){
        this.children.clear();
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the renderKit.
     */
    public RenderKit getRenderKit() {
        if (this.renderKit == null) {
            if (this.getParent() != null) {
                this.renderKit = this.getParent().getRenderKit();
            }
        }
        return renderKit;
    }

    /**
     * @param renderKit The renderKit to set.
     */
    public void setRenderKit(RenderKit renderKit) {
        this.renderKit = renderKit;
    }

    /**
     * Get the Renderer assigned to this renderer type and call its renderer() method.
     */
    public String render() {
        return this.getRenderer().render(this);
    }

    /**
     * @return Returns the renderType.
     */
    public String getRenderType() {
        return renderType;
    }

    /**
     * @param renderType The renderType to set.
     */
    public void setRenderType(String renderType) {
        this.renderType = renderType;
    }

    /**
     * @return Returns the renderer.
     */
    public Renderer getRenderer() {
        if (this.renderer == null) {
            this.renderer = this.getRenderKit().getRenderer(this.getRenderType());
        }

        return this.renderer;
    }

    /**
     * @param renderer The renderer to set.
     */
    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

}
