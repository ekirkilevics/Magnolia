/**
 * This file Copyright (c) 1993-2009 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */

classDef("mgnl.controls.FunctionBar", MgnlFunctionBar);

MgnlFunctionBar.classActive = 'mgnlFunctionBarButton';
MgnlFunctionBar.classInactive = 'mgnlFunctionBarButtonInactive';

function MgnlFunctionBar (name, active, iconActive, iconInactive, onClick) {
    var parent = null;
    var nodes = new Array ();
    var img = null;
    var td = null;
    var div = null;
    var conditions = new Array ();

    this.addNode = function (name, active, iconActive, iconInactive, onClick) {
        var n = new MgnlFunctionBar (name, active, iconActive, iconInactive, onClick);
        n.setParent (this);
        nodes.push (n);
    }

    this.refresh = function () {
        if (parent != null) {
            // div must be present, img not necesseraly
            if (div == null) {
                td = document.getElementById (this.getFullName ());
                div = document.getElementById (this.getFullName () + '_div');
                img = document.getElementById (this.getFullName () + '_img');
            }
            // determine the items state using javascript conditions
            active = true;
            for (var i = 0; i < conditions.length; i++) {
                //var condition = eval (conditions[i]);
                condition = conditions[i];

                var testResult;
                if(typeof condition == "function"){
                    testResult = condition();
                }
                else{
                    testResult = condition.test();
                }

                active = (active && testResult);
            }
            // set icon and css-class corresponding to the nodes state
            if (active) {
                if (img) img.src = iconActive;
                td.className = MgnlFunctionBar.classActive;
            }
            else {
                if (img) img.src = iconInactive;
                td.className = MgnlFunctionBar.classInactive;
            }
        }
        for (var i = 0; i < nodes.length; i++) {
            nodes[i].refresh ();
        }
    }

    this.getName = function () {
        return name;
    }

    this.getParent = function () { return parent; }

    this.setParent = function (p) { parent = p; }

    this.getFullName = function () {
        if (parent != null) {
            return (parent.getFullName () + '_' + this.getName ());
        }
        else return this.getName ();
    }

    this.getNode = function (nodeName) {
        var n = this.getNodes ();
        if (this.getName () == nodeName) return this;
        for (var i = 0; i < n.length; i++) {
            var ret = n[i].getNode (nodeName);
            if (ret) return ret;
        }
        return null;
    }

    this.getNodes = function () {
        return nodes;
    }

    this.setActive = function (nodeName, state) {
        if (this.getName () == nodeName) {
            active = state;
        }
        else {
            var n = this.getNode (nodeName);
            if (n) n.setActive (nodeName, state);
        }
    }

    this.isActive = function () { return active; }

    this.getOnClick = function () { return onClick; }

    this.addCondition = function (condition) {
        conditions.push (condition);
    }

    this.clicked = function (nodeName) {
        var n = this.getNode (nodeName);
        // only execute if node is found and node is active
        if (n && n.isActive ()) {
            var c = n.getOnClick ();
            if (c) {
                var f = new Function ('a ', c + '');
                f();
            }
        }
    }
}
