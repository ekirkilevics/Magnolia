/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.module.rest.tree.config;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.*;

/**
 * Configuration for a tree view.
 */
@XmlRootElement
public class JsonTreeConfiguration {

    private String name;
    private String repository;
    private String rootPath;

    private List<JsonTreeColumn> columns = new ArrayList<JsonTreeColumn>();
    private JsonMenu contextMenu;
    private JsonMenu functionMenu;
    private boolean flatMode;

    // TODO localization, columns and menu items need to be able to do localization using the basename from here

    // list of item types to include, will appear in this order
    private List<String> itemTypes = new ArrayList<String>();
    private Set<String> strictItemTypes = new HashSet<String>();
    @XmlTransient
    private Comparator sortComparator;
    private String i18nBaseName;

    public int getColumnsWidth() {
        int n = 0;
        for (JsonTreeColumn column : columns) {
            n += column.getWidth();
        }
        return n;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public void addColumn(JsonTreeColumn column) {
        this.columns.add(column);
    }

    public List<JsonTreeColumn> getColumns() {
        return columns;
    }

    public JsonMenu getContextMenu() {
        return contextMenu;
    }

    public void setContextMenu(JsonMenu contextMenu) {
        this.contextMenu = contextMenu;
    }

    public JsonMenu getFunctionMenu() {
        return functionMenu;
    }

    public void setFunctionMenu(JsonMenu functionMenu) {
        this.functionMenu = functionMenu;
    }

    public void setFlatMode(boolean flatMode) {
        this.flatMode = flatMode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setColumns(List<JsonTreeColumn> columns) {
        this.columns = columns;
    }

    public String getName() {
        return name;
    }

    public boolean isFlatMode() {
        return flatMode;
    }

    public void setSortComparator(Comparator sortComparator) {
        this.sortComparator = sortComparator;
    }

    public void setItemTypes(List<String> itemTypes) {
        this.itemTypes = itemTypes;
    }

    public void setStrictItemTypes(Set<String> strictItemTypes) {
        this.strictItemTypes = strictItemTypes;
    }

    public Comparator getSortComparator() {
        return sortComparator;
    }

    public List<String> getItemTypes() {
        return itemTypes;
    }

    public Set<String> getStrictItemTypes() {
        return strictItemTypes;
    }

    public String getI18nBaseName() {
        return i18nBaseName;
    }

    public void setI18nBaseName(String i18nBaseName) {
        this.i18nBaseName = i18nBaseName;
    }

    public void addItemType(String itemType) {
        this.itemTypes.add(itemType);
    }

    public void addStrictItemType(String strictItemType) {
        this.strictItemTypes.add(strictItemType);
    }
}
