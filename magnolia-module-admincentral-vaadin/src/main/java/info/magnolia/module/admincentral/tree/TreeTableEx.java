/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.admincentral.tree;

import java.util.Map;

import com.vaadin.addon.treetable.Collapsible;
import com.vaadin.addon.treetable.TreeTable;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.CollapseEvent;
import com.vaadin.ui.Tree.CollapseListener;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.Tree.ExpandListener;
/**
 * An experimental TreeTable which handles node expands and collapses.
 * @author fgrilli
 *
 */
public class TreeTableEx extends TreeTable implements CollapseListener, ExpandListener {
    private static final Tree TREE = new Tree();

    public TreeTableEx() {
        super();
        addListener(ExpandEvent.class, this, ExpandListener.EXPAND_METHOD);
        addListener(CollapseEvent.class, this, CollapseListener.COLLAPSE_METHOD);
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);

        if (variables.containsKey("toggleCollapsed")) {
            String object = (String) variables.get("toggleCollapsed");
            Object itemId = itemIdMapper.get(object);
            if (isCollapsed(itemId)) {
                fireCollapseEvent(itemId);

            } else {
                fireExpandEvent(itemId);
            }
        }
    }

    public void toggleChildVisibility(Object itemId) {
        c().setCollapsed(itemId, !c().isCollapsed(itemId));
    }

    public void nodeExpand(ExpandEvent event) {
        setUriFragmentOnItemClickEvent(event);

    }

    public void nodeCollapse(CollapseEvent event) {
        setUriFragmentOnItemClickEvent(event);

    }

    private Collapsible c() {
        return (Collapsible) getContainerDataSource();
    }

    private void setUriFragmentOnItemClickEvent(ExpandEvent event) {
        setUriFragment(event.getItemId());
    }

    private void setUriFragment(Object itemId) {
        /*
         * String currentUriFragment = getUriFragmentUtility().getFragment();
         * if(StringUtils.isNotEmpty(currentUriFragment)){ String[] tokens =
         * currentUriFragment.split(";"); //we already have an item id in the
         * uri fragment, replace it if(tokens.length == 2) { currentUriFragment
         * = tokens[0]; } currentUriFragment = currentUriFragment + ";" +
         * itemId; }
         *
         * getUriFragmentUtility().setFragment(currentUriFragment, false);
         */
    }

    private void setUriFragmentOnItemClickEvent(CollapseEvent event) {
        setUriFragment(event.getItemId());
    }

    /**
     * Emits the expand event.
     *
     * @param itemId
     *            the item id.
     */
    protected void fireExpandEvent(Object itemId) {
        fireEvent(TREE.new ExpandEvent(this, itemId));
    }

    /**
     * Emits collapse event.
     *
     * @param itemId
     *            the item id.
     */
    protected void fireCollapseEvent(Object itemId) {
        fireEvent(TREE.new CollapseEvent(this, itemId));
    }

}
