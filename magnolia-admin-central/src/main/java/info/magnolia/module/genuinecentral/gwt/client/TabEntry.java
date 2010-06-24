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
package info.magnolia.module.genuinecentral.gwt.client;

import com.extjs.gxt.ui.client.Style.HideMode;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;

public class TabEntry extends BaseTreeModel {

  private String name;
  private boolean fill;
  private boolean closable = true;
  private HideMode hideMode = HideMode.DISPLAY;

  public TabEntry(String name, LayoutContainer item, String image) {
    this.name = name;
    set("name", name);
    set("image", image);
    set("item", item);
  }

  public TabEntry(String name, LayoutContainer item, String image, boolean fill) {
    this(name, item, image);
    this.fill = fill;
  }

  public TabEntry(String name, LayoutContainer item, String image, boolean fill, boolean closable) {
    this(name, item, image, fill);
    this.closable = closable;
  }

  public TabEntry(String name, LayoutContainer item, String image, boolean fill, boolean closable, HideMode hideMode) {
    this(name, item, image, fill, closable);
    this.setHideMode(hideMode);
  }

  protected TabEntry() {

  }

  public LayoutContainer getItem() {
    return (LayoutContainer) get("item");
  }

  public String getId() {
    if (name.equals("% Columns")) {
      return "percentcolumns";
    }
    return name.replaceAll(" ", "").toLowerCase();
  }

  public HideMode getHideMode() {
    return hideMode;
  }

  public String getName() {
    return (String) get("name");
  }

  public boolean isClosable() {
    return closable;
  }

  public boolean isFill() {
    return fill;
  }

  public void setFill(boolean fill) {
    this.fill = fill;
  }

  public void setHideMode(HideMode hideMode) {
    this.hideMode = hideMode;
  }

  public String toString() {
    return getName();
  }

}