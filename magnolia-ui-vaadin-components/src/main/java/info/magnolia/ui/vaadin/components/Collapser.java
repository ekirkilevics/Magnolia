/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.vaadin.components;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.NativeButton;


/**
 * A collapsible side bar.
 */
public class Collapser extends CssLayout {

    private NativeButton collapse;

    private Component expandedContent;

    private Component collapsedContent;

    public Collapser() {
        setStyleName("collapser");

        collapse = new NativeButton(">");
        collapse.setStyleName("collapse");
        addComponent(collapse);

        collapse.addListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                String caption = collapse.getCaption();
                if (">".equals(caption)) {
                    removeComponent(expandedContent);
                    addComponent(collapsedContent);
                    collapse.setCaption("<");
                    addStyleName("collapsed");
                }
                else if ("<".equals(caption)) {
                    removeComponent(collapsedContent);
                    addComponent(expandedContent);
                    collapse.setCaption(">");
                    removeStyleName("collapsed");
                }
            }
        });
    }

    public void setExpandedContent(Component expandedContent) {
        this.expandedContent = expandedContent;
        addComponent(expandedContent);
        expandedContent.addStyleName("expanded-content");
    }

    public void setCollapsedContent(Component collapsedContent) {
        this.collapsedContent = collapsedContent;
        collapsedContent.addStyleName("collapsed-content");
    }
}
