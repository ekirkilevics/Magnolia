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
 
classDef("mgnl.controls.TextField", MgnlTextField);
 
function MgnlTextField(name, value, width, imageLeft, imageLeftWidth, attributes) {
	this.attributes = attributes!=null?attributes:{};

	this.name = name;
	this.value = value;

	if (width == undefined) {
		this.width = 150;
	} else {
		this.width = width;
	}
	if (imageLeft == undefined) {
		this.imageLeft = contextPath + '/.resources/controls/textfield/left.gif';
		this.imageLeftWidth = 5;
	} else {
		this.imageLeft = imageLeft;this.imageLeftWidth = (imageLeftWidth == undefined ? '20' : imageLeftWidth);
	}
	
	this.widthSearchField = this.width - (5 + this.imageLeftWidth);
	
	// register control
	MgnlTextField.prototype.all[this.name] = this;
}

MgnlTextField.prototype.render = function() {
	// create other attributes
	var attributes="";
	for(name in this.attributes){
		attributes += " " + name + '="' + this.attributes[name] + '"';
	}

	if (window.navigator.userAgent.indexOf("Safari") == -1) {
		var html = '<table border="0" cellspacing="0" cellpadding="0" style="width: ' + this.width + 'px;"><tr>';
		html += '<td width="5"><img src="'+ this.imageLeft +'" width="'+ this.imageLeftWidth +'" border="0"></td>'
		html += '<td width="100%" class="smothTextFieldTD" background="'+contextPath+'/.resources/controls/textfield/middle.gif">'
		html += '<input class="smothTextField" style="width: ' + this.widthSearchField + 'px;" type="text" id="' + this.name + '" value="' + this.value.replace('"','&quot;') + '" ' + attributes + '></td>';
		html += '<td width="5"><img src="'+ contextPath +'/.resources/controls/textfield/right.gif" width="5" border="0"></td>';
		html += '</tr></table>'
	} else {
		var html = '<input type="search" style="width: ' + this.width + 'px;" id="' + this.name + '" value="' + this.value + '" ' + attributes + '>';
	}
	return html
}

// collection of all instantiated controls
MgnlTextField.prototype.all = new Object();
