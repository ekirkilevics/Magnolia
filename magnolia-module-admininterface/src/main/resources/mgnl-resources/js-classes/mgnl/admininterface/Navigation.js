/**
 * MgnlNavigation
 * creates a hierarchical menu
 * Usage:
 *   include this file in any html/xhtml file
 *   link the corresponding css file
 *
 *   create navigation:
 *       mgnlNavigation = new MgnlNavigation ();
 *
 *   add a node:
 *       mgnlNavigation.addNode ('node1', 'Website', 'alert (\'blau\');', './.resources/icons/24/earth.gif');
 *           Parameters for addNode:
 *             id:     the nodes id (must be unique!)
 *             title:  the txt to display
 *             action: javascript function as string
 *             icon:   path to the icon shown on the left
 *
 *   add a subnode:
 *       mgnlNavigation.getNode ('node1').addNode ('node11', 'Test', '', './.resources/icons/16/folder.gif');
 *
 *   to activate a node from wherever (without a user's click) without triggering its action:
 *       mgnlNavigation.activate ('node2346');
 *
 *   to activate a node from wherever (without a user's click) and execute its action:
 *       mgnlNavigation.activate ('node2346', true);
 *
 */
 
classDef("mgnl.admininterface.Navigation", MgnlNavigation);

/**
 * static variables
 */
// path to the images
MgnlNavigation.imagesPath = contextPath + "/.resources/controls/navigation/";

// path to the expandable-icon on the left
MgnlNavigation.openIcon = MgnlNavigation.imagesPath + 'mgnlNavigationArrow.gif';
// specify animation step speed in milliseconds
MgnlNavigation.navTimeoutOpen = 30;
MgnlNavigation.navTimeoutClose = 20;

/**
 * Parameters:
 *   id:     the nodes id (must be unique!)
 *   title:  the txt to display
 *   action: javascript function as string
 *   icon:   path to the expandable-icon on the left
 *   parent: either MgnlNavigation or MgnlNavigationNode
 *   depth:  int
 */
function MgnlNavigation (id, title, action, icon, parent, depth) {
    // holds all top-level navigation nodes
    var nodes = new Array ();
    // holds all open nodes, i.e. where the children are shown
    var openNodes = new Array ();
    // holds all nodes where mouse is moved over
    var hoverNodes = new Array ();
    // holds the currently selected/clicked node
    var activeNode = null;
    // background: the table-element in the dom-tree, this holds the background color/image that reacts on mouse-events
    var background = null;
    // whether the menu is opened or not, i.e. children visible or hidden.
    var isOpen = false;

    // obvious getters and setters
    this.getDepth = function () {
        if (depth) return depth;
        else return 0;
    }
    this.getId = function () { return id; }
    this.getTitle = function () { return title; }
    this.getIcon = function () { return icon; }
    this.hasIcon = function () { return (icon ? true : false); }
    this.getParent = function () { return parent; }

    /**
     * adds a navigation node
     * Parameters:
     *   id:     the nodes id (must be unique!)
     *   title:  the txt to display
     *   action: javascript function as string
     *   icon:   path to the icon shown on the left
     */
    this.addNode = function (id, title, action, icon) {
        // only add node if there is no such id yet
        if (!this.getNavigation ().getNode (id)) nodes[nodes.length] = new MgnlNavigation (id, title, action, icon, this, this.getDepth () + 1);
    }
    this.hasNodes = function () { return (nodes.length > 0); }
    this.getNodes = function () { return nodes; }
    this.getNode = function (id) {
        var n = this.getNodes ();
        if (this.getId () == id) return this;
        for (var i = 0; i < n.length; i++) {
            var ret = n[i].getNode (id);
            if (ret) return ret;
        }
        return null;
    }

    // open the menu tree
    this.showChildren = function () {
        // nothing to do on top-level
        if (this.getDepth () > 0) {
            this.closeOpenNodes (this);
            if (!isOpen) {
                if (this.hasNodes ()) {
                    // hide all children
                    // and set show timeout for animation
                    if (MgnlNavigation.navTimeoutOpen && 1 * MgnlNavigation.navTimeoutOpen > 0) {
                        if (c = this.background.parentNode.nextSibling.firstChild) {
                            var n = this.getNodes ();
                            var i = 0;
                            do {
                                c.style.display = 'none';
                                window.setTimeout ('document.mgnlNavigationSelf.showChild (\'' + n[i++].getId () + '\')', i * MgnlNavigation.navTimeoutOpen);
                            } while (c = c.nextSibling);
                        }
                    }
                    // display wrapper
                    this.background.parentNode.nextSibling.style.display = 'block';
                }
                this.addOpenNode (this);
                isOpen = true;
            }
        }
    }

    // close the menu tree
    this.hideChildren = function () {
        // nothing to do on top-level
        if (this.getDepth () > 0) {
            if (isOpen) {
                if (MgnlNavigation.navTimeoutClose && 1 * MgnlNavigation.navTimeoutClose > 0) {
                    var n = this.getNodes ();
                    for (var i = 0; i < n.length; i++) {
                        // pretty animation...
                        window.setTimeout ('document.mgnlNavigationSelf.hideChild (\'' + n[i].getId () + '\')', (i + 1) * MgnlNavigation.navTimeoutClose);
                    }
                }
                // display wrapper
                else this.background.parentNode.nextSibling.style.display = 'none';
            }
            isOpen = false;
        }
    }

    // open/close menus
    // these two functions are used in animation, they get called asynchronously
    this.showChild = function (idShow) {
        if (n = this.getNode (idShow)) n.background.parentNode.parentNode.style.display = 'block';
    }
    this.hideChild = function (idHide) {
        if (n = this.getNode (idHide)) {
            n.background.parentNode.parentNode.style.display = 'none';
            // if this is the last child hide the wrapper too
            if (!n.background.parentNode.parentNode.nextSibling) {
                n.background.parentNode.parentNode.parentNode.style.display = 'none';
            }
        }
    }

    // changes the css-styles of the node
    this.setStyle = function (style) {
        // nothing to do on top-level
        if (style.length > 0) {
            style = style.charAt (0).toUpperCase () + style.substring (1, style.length);
            if (this.getDepth () > 0) {
                // apply styles
                if (this.background) this.background.className = 'mgnlNavigation' + this.getDepth () + style;
        
                if (this.hasNodes ()) {
                    var n = this.getNodes ();
                    if (n[0].background) n[0].background.parentNode.parentNode.parentNode.className = 'mgnlNavigationWrapper' + (this.getDepth () + 1) + style;
                }
            }
        }
    }

    // returns the navigationobject itself
    this.getNavigation = function () {
        // only do it on top-level-parent
        if (parent) return parent.getNavigation ();
        else return this;
    }

    // adds the node to the hoverNodes array
    this.addHoverNode = function (n) {
        // only do it on top-level-parent
        if (parent) parent.addHoverNode (n);
        else {
            var exists = false
            for (var i = 0; i < hoverNodes.length; i++) if (hoverNodes[i] == n) exists = true;
            if (!exists) hoverNodes.push (n);
        }
    }

    // used to reset the hover css style
    // loops through the hoverNodes array and calls mouseout if it's not the active node
    this.removeHoverNodes = function () {
        // only do it on top-level-parent
        if (parent) parent.removeHoverNodes ();
        else {
            for (var i = 0; i < hoverNodes.length; i++) {
                if (this.getActiveNode () == null || this.activeNode != hoverNodes[i]) {
                    hoverNodes[i].mouseOut ();
                    hoverNodes.splice (i, 1);
                }
            }
        }
    }

    // adds a node to the openNodes array
    this.addOpenNode = function (n) {
        // only do it on top-level-parent
        if (parent) parent.addOpenNode (n);
        else {
            var exists = false
            for (var i = 0; i < openNodes.length; i++) if (openNodes[i] == n) exists = true;
            if (!exists) openNodes.push (n);
        }
    }

    // loops through the openNodes array and closes all nodes with greater depth expect for the given one
    this.closeOpenNodes = function (n) {
        // only do it on top-level-parent
        if (parent) parent.closeOpenNodes (n);
        else {
            for (var i = 0; i < openNodes.length; i++) {
                // close them if they are less deep or a child
                if (openNodes[i].getDepth () >= n.getDepth () || n.getNode (openNodes[i].getId ())) {
                    // only close it if it's NOT a parent of the active node
                    if (openNodes[i].getNode (n.getId ()) == null) {
                        openNodes[i].hideChildren ();
                        openNodes.splice (i);
                    }
                }
            }
        }
    }

    // the node that was clicked
    this.setActiveNode = function (n) {
        // only do it on top-level-parent
        if (parent) parent.setActiveNode (n);
        else this.activeNode = n;
    }
    this.getActiveNode = function () {
        // only do it on top-level-parent
        if (parent) return parent.activeNode;
        else return this.activeNode;
    }

    // set active style and show children
    // if it get's called with id set it's an external call
    // and we need to make sure the parents are open!
    this.activate = function (idActivate, execute) {
        if (idActivate) {
            var n = this.getNode (idActivate);
            if (n) {
                var p = n.getParent ();
                while (p && p.getDepth () > 0) {
                    if (!p.isOpen) p.showChildren ();
                    p = p.parent;
                }
            }
            this.addHoverNode (n);
            n.activate ();
            if (execute) n.execute ();
        }
        if (this.getDepth () > 0) {
            this.setActiveNode (this);
            this.removeHoverNodes ();
            if (!isOpen) this.showChildren ();
            this.setStyle ('active');
        }
    }

    // call the action that was given to the constructor
    this.execute = function () {
        if (action.length > 0) {
            var f = new Function ('a ', action + '');
            f ();
        }
    }

    this.mouseOver = function (idOver) {
        if (n = this.getNode (idOver)) {
            if (this.getActiveNode () == null || this.getActiveNode () != n) {
                this.addHoverNode (n);
                n.setStyle ('hover');
            }
        }
    }

    this.mouseOut = function (idOut) {
        if (idOut && this.getDepth () == 0) this.removeHoverNodes ();
        else {
            if (this.getActiveNode () == null || this.getActiveNode () != this) {
                this.setStyle ('inactive');
            }
        }
    }
    
    this.mouseClick = function (idClick) {
        var n = this.getNode (idClick);
        if (n) {
            n.activate ();
            n.execute ();
        }
    }
}

/**
 * static method to init the menus html -> dom
 * creates the node as dom-element and appends it to container
 *   container: the surrounding element of the parent
 */
MgnlNavigation.prototype.create = function (container) {
    // create navigation root
    if (this.getDepth () == 0) {
        // craete variable to itself
        document.mgnlNavigationSelf = this;
        var c = document.getElementById (container);
        if (c) {
            var n = this.getNodes ();
            var d = document.createElement ('div');
            d.className = 'mgnlNavigationBox';
            if (n && d) { for (var i = 0; i < n.length; i++) n[i].create (d); }
            c.appendChild (d);
        }
    }
    // create menu-entry
    else {
        // wrapper div
        var wrapper = document.createElement ('div');
        wrapper.className = 'mgnlNavigationWrapper' + this.getDepth ();
        var inner = document.createElement ('div');
        // table
        table = document.createElement ('table');
        // get the navigation object an put it into this closure to pass it in the functions
        var myNavigation = this.getNavigation();
        table.className = 'mgnlNavigation' + this.getDepth () + 'Inactive';
        table.appendChild (document.createElement ('tbody'));
        table.firstChild.appendChild (document.createElement ('tr'));

        var cell = document.createElement ('td');
        cell.className = 'mgnlNavigation' + this.getDepth () + 'Cell mgnlNavigationText';
        if (this.hasIcon ()) 
        	cell.style.backgroundImage = 'url(' + this.getIcon () + ')';

        cell.id = this.getId ();

        cell.onmouseover = function () { myNavigation.mouseOver (this.id); };
        cell.onmouseout = function () { myNavigation.mouseOut (this.id); };
        cell.onclick = function () { myNavigation.mouseClick (this.id); };
        
        cell.innerHTML = this.getTitle();

        table.firstChild.firstChild.appendChild (cell);

        inner.appendChild (table);
        wrapper.appendChild (inner);
        container.appendChild (wrapper);
        this.background = table;
        // create children
        if (this.hasNodes ()) {
            // add icon
            if (MgnlNavigation.openIcon) {
                var td = document.createElement ('td');
                td.width = '1%';
                td.appendChild (document.createElement ('img'));
                td.firstChild.src = MgnlNavigation.openIcon;
                td.firstChild.className = 'mgnlNavigation' + this.getDepth () + 'Arrow';
                table.firstChild.firstChild.appendChild (td);
            }
            // create children
            var n = this.getNodes ();
            var c = document.createElement ('div');
            c.className = 'mgnlNavigationWrapper' + (this.getDepth () + 1) + 'Inactive';
            for (var i = 0; i < n.length; i++) n[i].create (c);
            // initially hide all children
            c.style.display = 'none';
            wrapper.appendChild (c);
        }
    }
}
