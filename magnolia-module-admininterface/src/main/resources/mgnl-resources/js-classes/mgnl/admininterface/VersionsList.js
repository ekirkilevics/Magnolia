/**
 * This file Copyright (c) 1993-2009 Magnolia International
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

 /**
  *
  */
 classDef("mgnl.admininterface.VersionsList", function(onShowItem, onDiffItem){

    /**
     * The current selected items version label
     */
    this.currentVersionLabel = null;

    /**
     * The function called to show the lists item
     */
    this.onShowItem = onShowItem;

    /**
     * The function called to show the lists item
     */
    this.onDiffItem = onDiffItem;

    this.restore = function(versionLabel){
        versionLabel = versionLabel==null ? this.currentVersionLabel : versionLabel;
        document.mgnlForm.command.value="restore";
        document.mgnlForm.versionLabel.value=versionLabel;
        document.mgnlForm.submit();
    };

    this.showItem = function(versionLabel){
        versionLabel = versionLabel==null ? this.currentVersionLabel : versionLabel;
        // on show must be set by the user of this class
        onShowItem(versionLabel);
    };

    this.diffItem = function(versionLabel){
        versionLabel = versionLabel==null ? this.currentVersionLabel : versionLabel;
        // on show must be set by the user of this class
        onDiffItem(versionLabel);
    };
 });

/**
 * Show versions of a page
 */
mgnl.admininterface.VersionsList.show = function(repository, path){
    url = "/.magnolia/pages/" + repository + "VersionsList.html";
    url = MgnlURLUtil.addParameter(url, "repository", repository);
    url = MgnlURLUtil.addParameter(url, "path", path);

    mgnlOpenWindow(url, 1000, 600);
};