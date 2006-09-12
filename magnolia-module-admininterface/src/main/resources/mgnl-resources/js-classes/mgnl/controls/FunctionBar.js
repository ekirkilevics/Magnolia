/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
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
                active = (active && condition.test ());
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
