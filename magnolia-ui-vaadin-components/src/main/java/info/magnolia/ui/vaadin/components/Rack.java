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

import java.util.HashMap;
import java.util.Map;

import org.vaadin.jouni.animator.AnimatorProxy;
import org.vaadin.jouni.animator.client.ui.VAnimatorProxy.AnimType;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.themes.BaseTheme;


/**
 * An enclosure for mounting multiple components.
 * @author mrichert
 */
public class Rack extends CssLayout {

    private Layout container;

    private ComboBox selector;

    private Map<String, Unit> units = new HashMap<String, Unit>();

    public Rack() {
        setSizeUndefined();
        setStyleName("rack");

        container = new CssLayout();
        addComponent(container);

        selector = new ComboBox();
        selector.setNullSelectionAllowed(true);
        selector.setImmediate(true);
        selector.setInputPrompt("More...");
        selector.addStyleName("select-button");
        selector.setSizeUndefined();
        addComponent(selector);

        selector.addListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                Object value = event.getProperty().getValue();
                if (value != null) {
                    units.get("" + value).setVisible(true);
                }
            }
        });
    }

    public Unit addUnit(Button title) {
        Unit unit = new Unit(title);
        container.addComponent(unit);
        unit.setVisible(false);
        return unit;
    }

    /**
     * A number of UI controls logically and semantically belonging together.
     *
     * @author mrichert
     */
    @SuppressWarnings("serial")
    public class Unit extends CssLayout {

        public static final String STYLE = "v-disclosure";

        public static final String STYLE_CAPTION = STYLE + "-caption";

        public static final String STYLE_CAPTION_OPEN = STYLE_CAPTION + "-open";

        protected Component content;

        protected Button title = new Button();

        protected boolean expanded = true;

        private Label close;

        private AnimatorProxy animator = new AnimatorProxy();

        private Unit(Button title) {
            super.addComponent(animator);
            setStyleName(STYLE);
            setSizeUndefined();

            close = new Label("x");
            close.setStyleName("close");
            close.addStyleName("borderless");
            close.setSizeUndefined();
            super.addComponent(close);

            addListener(new LayoutClickListener() {

                @Override
                public void layoutClick(LayoutClickEvent event) {
                    if (close.equals(event.getClickedComponent())) {
                        setVisible(false);
                    }
                }
            });

            this.title = title;
            this.title.addStyleName(BaseTheme.BUTTON_LINK);
            this.title.addStyleName(STYLE_CAPTION);
            this.title.addStyleName(STYLE_CAPTION_OPEN);
            super.addComponent(title);

            this.title.addListener(new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    if (expanded) {
                        collapse();
                    }
                    else {
                        expand();
                    }
                }
            });
        }

        public void setClosable(boolean closable) {
            close.setVisible(closable);
            if (!closable) {
                setVisible(true);
            }
        }

        @Override
        public void setVisible(boolean visible) {
            super.setVisible(visible);
            String caption = title.getCaption();
            if (visible) {
                units.remove(caption);
                selector.removeItem(caption);
                expand();
            }
            else {
                units.put(caption, this);
                selector.addItem(caption);
            }
            selector.setEnabled(!units.isEmpty());
        }

        public String getDisclosureCaption() {
            return title.getCaption();
        }

        public void setDisclosureCaption(String caption) {
            title.setCaption(caption);
        }

        public void setContent(Component newContent) {
            if (content != newContent) {
                if (content != null && content.getParent() != null) {
                    removeComponent(content);
                }
                if (expanded && newContent != null) {
                    super.addComponent(newContent);
                }
                content = newContent;
            }
        }

        public Component getContent() {
            return content;
        }

        public void expand() {
            if (!isExpanded()) {
                if (content != null) {
                    if (content.getParent() != this) {
                        super.addComponent(content);
                    }
                    animator.animate(content, AnimType.ROLL_DOWN_OPEN_POP);
                    title.addStyleName(STYLE_CAPTION_OPEN);
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
                title.removeStyleName(STYLE_CAPTION_OPEN);
                expanded = false;
            }
        }

        @Override
        public void addComponent(Component c) {
            if (content == null) {
                setContent(c);
            }
            else {
                throw new UnsupportedOperationException(
                        "You can only add one component to the Disclosure. Use Disclosure.setContent() method instead.");
            }
        }

        @Override
        public void removeAllComponents() {
            setContent(null);
        }
    }
}
