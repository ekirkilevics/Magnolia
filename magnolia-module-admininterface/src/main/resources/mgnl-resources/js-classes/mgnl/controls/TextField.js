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
