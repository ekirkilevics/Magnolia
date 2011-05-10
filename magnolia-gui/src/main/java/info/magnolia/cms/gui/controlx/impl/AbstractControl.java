/**
 * This file Copyright (c) 2003-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
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
