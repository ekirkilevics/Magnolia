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
package info.magnolia.ui.admincentral.navigation;

import java.util.Collection;
import java.util.HashSet;

import org.vaadin.jouni.animator.AnimatorProxy;
import org.vaadin.jouni.animator.client.ui.VAnimatorProxy.AnimType;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;


/**
 * A custom accordion, loosely based on com.vaadin.addon.chameleon.SidebarMenu and
 * org.vaadin.jouni.animator.Disclosure.
 *
 * @author mrichert
 */
public class Melodion extends CssLayout {

    private Collection<NativeButton> buttons = new HashSet<NativeButton>();

    private Collection<Tab> tabs = new HashSet<Tab>();

    public Melodion() {
        addStyleName("sidebar-menu");
    }

    public void setSelected(Component c) {
        updateStyles();
        if (c instanceof Tab) {
            Tab t = (Tab) c;
            t.caption.addStyleName("selected");
            t.expand();
        }
        else {
            c.addStyleName("selected");
        }
    }

    private void updateStyles() {
        for (NativeButton b : buttons) {
            b.removeStyleName("selected");
        }
        for (Tab t : tabs) {
            t.caption.removeStyleName("selected");
        }
    }

    public void collapseOthers(Tab tab) {
        for (Tab t : tabs) {
            if (t != tab) {
                t.collapse();
            }
        }
    }

    public Tab addTab(Label caption) {
        Tab tab = new Tab(caption);
        tabs.add(tab);
        addComponent(tab);
        return tab;
    }

    public Component addSpacer() {
        Component spacer = new Label();
        spacer.setStyleName("spacer");
        addComponent(spacer);
        return spacer;
    }

    /**
     * Top-level menu item in the Melodion.
     * 
     * @author mrichert
     */
    public class Tab extends CssLayout {

        private AnimatorProxy animator = new AnimatorProxy();

        private Label caption;

        private CssLayout content = null;

        private boolean expanded = false;

        private Tab(Label caption) {
            this.caption = caption;
            addComponent(animator);
            addComponent(caption);

            addListener(new LayoutClickListener() {

                @Override
                public void layoutClick(LayoutClickEvent event) {
                    if (event.getChildComponent() == Tab.this.caption) {
                        setSelected(Tab.this);
                        if (!expanded) {
                            expand();
                        }
                    }
                }
            });
        }

        public void addButton(NativeButton b) {
            if (content == null) {
                content = new CssLayout();
            }
            buttons.add(b);
            content.addComponent(b);

            b.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    updateStyles();
                    event.getButton().addStyleName("selected");
                }
            });
        }

        public void expand() {
            if (!isExpanded()) {
                collapseOthers(this);
                if (content != null) {
                    if (content.getParent() != this) {
                        addComponent(content);
                    }
                    animator.animate(content, AnimType.ROLL_DOWN_OPEN_POP);
                    expanded = true;
                }
            }
        }

        public boolean isExpanded() {
            return expanded;
        }

        public void collapse() {
            if (content != null) {
                animator.animate(content, AnimType.ROLL_UP_CLOSE_REMOVE);
                expanded = false;
            }
        }
    }
}
