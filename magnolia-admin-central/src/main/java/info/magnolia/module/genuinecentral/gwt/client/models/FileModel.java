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
package info.magnolia.module.genuinecentral.gwt.client.models;

import java.util.Map;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class FileModel extends BaseModelData {

  protected FileModel() {

  }

  public FileModel(Map<String, Object> properties) {
      super();
      setProperties(properties);
  }

  public FileModel(String name, String path) {
    setName(name);
    setPath(path);
  }

  public String getId() {
      String uuid = get("uuid");
      return uuid == null ? ("" + hashCode()) : uuid;
  }

  public void setName(String name) {
    set("name", name);
  }

  public void setPath(String path) {
    set("path", path);
  }

  public String getPath() {
    return get("path");
  }

  public String getName() {
    return get("name");
  }

  public boolean isLeaf() {
      // used by tree
      Object hasChildren = get("hasChildren");
      return hasChildren == null || !Boolean.parseBoolean(hasChildren.toString());
  }

  @Override
    public int hashCode() {
        return getPath() == null ? 17 : getPath().hashCode();
    }

  @Override
  public boolean equals(Object obj) {
      if (this == obj) {
          return true;
      }

      if (obj == null || !(obj instanceof FileModel)) {
          return false;
      }

      FileModel that = (FileModel) obj;
      return this.getName() == null ? that.getName() == null : this.getName().equals(that.getName())
              && this.getPath() == null ? that.getPath() == null : this.getPath().equals(that.getPath());
  }

  @Override
    public String toString() {
        return getPath();
    }

    public String getUuid() {
        return get("uuid");
    }

    public void setHasChildren(boolean b) {
        set("hasChildren", "true");
    }
}