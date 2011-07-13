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
package info.magnolia.ui.admincentral.jcr.view;

import info.magnolia.ui.admincentral.container.JcrContainer;
import info.magnolia.ui.framework.view.View;

import javax.jcr.Item;

/**
 * UI component that displays a jcr workspace.
 *
 * @author fgrilli
 */
public interface JcrView extends View {

    /**
     * Enumeration for view types.
     * TODO: use lowercase elements and remove custom code so that C2B support "by name" could be leveraged?
     * @author fgrilli
     *
     */
    enum ViewType {
        LIST("list"),
        TREE("tree");

        private String text;

        ViewType(String text) {
            this.text = text;
        }

        public String getText() {
            return this.text;
        }

        public static ViewType fromString(String text) {
            if (text != null) {
                for (ViewType type : ViewType.values()) {
                    if (text.equalsIgnoreCase(type.text)) {
                        return type;
                    }
                }
            }
            throw new IllegalArgumentException("No view type could be found for " + text);
        }
    }

    /**
     * Presenter for the JcrView.
     *
     * @author fgrilli
     */
    public interface Presenter {

        void onItemSelection(Item item);
    }

    void setPresenter(Presenter presenter);

    // TODO should we really ask view?
    String getPathInTree(Item item);

    /**
     *
     * @throws info.magnolia.exception.RuntimeRepositoryException if the path does not exist
     */
    void select(String path);

    void refresh();

    JcrContainer getContainer();
}
