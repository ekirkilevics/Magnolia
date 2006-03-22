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
 
classDef("mgnl.controls.SearchDropdown", MgnlSearchDropdown);
 
function MgnlSearchDropdown(name, options, value){
	this.name = name;
	this.options = options;
	this.value = value;
	this.isOpen = false;
	this.closeForSure = false; // close the popup without delay
	this.onchange="";
	this.initialized = false;

	// default is the first value
	if(value== null || value=='' || options[value] == null){
		this.value = this.getFirstOption();
	}
	
	// register control
	MgnlSearchDropdown.prototype.all[this.name] = this;
}

MgnlSearchDropdown.prototype.init = function(){
	this.initialized=true;
	this.optionsDiv = document.getElementById(this.name + 'OptionsDiv');
	this.optionsTab = document.getElementById(this.name + 'OptionsTab');
	this.optionsTabBottom = document.getElementById(this.name + 'OptionsTabBottom');
	this.field = document.getElementById(this.name + 'Field');
	this.fieldValue = document.getElementById(this.name + 'FieldValue');	
	this.field.style.width = this.optionsTab.clientWidth+2;
	this.optionsTabBottom.style.width = this.optionsTab.clientWidth+2;
	this.picHeaderLeft = document.getElementById(this.name + 'PicHeaderLeft');
	this.picHeaderRight = document.getElementById(this.name + 'PicHeaderRight');
}

MgnlSearchDropdown.prototype.initAll = function(){
	for(name in MgnlSearchDropdown.prototype.all){
		var obj = MgnlSearchDropdown.prototype.all[name];
		if(!obj.initialized){
			obj.init();
		}
	}
}

MgnlSearchDropdown.prototype.getFirstOption = function(){
	for(name in this.options){
		return name;
	}
}

// collection of al instantiated controls
MgnlSearchDropdown.prototype.all = new Object();

MgnlSearchDropdown.prototype.find = function(name){
	return MgnlSearchDropdown.prototype.all[name];
}
 
MgnlSearchDropdown.prototype.toggle = function() {
	this.closeForSure = false;
	if (this.isOpen) {
		this.close();
	} else {
		this.open();
	}
}

MgnlSearchDropdown.prototype.close = function() {
	// delay closing
	this.isOpen=false;
	window.setTimeout('MgnlSearchDropdown.prototype.find("' + this.name + '")._close();', 500);
}

MgnlSearchDropdown.prototype._close = function() {
	if(!this.isOpen || this.closeForSure){
		this.optionsDiv.style.visibility = 'hidden';
		this.optionsTab.style.visibility = 'hidden';
		this.picHeaderLeft.src = contextPath+'/admindocroot/controls/search/dmsDropdownHeaderLeft.gif';
		this.picHeaderRight.src = contextPath+'/admindocroot/controls/search/dmsDropdownHeaderRight.gif';
	}
}

MgnlSearchDropdown.prototype.open = function() {
	if (!this.closeForSure) {
		this.optionsDiv.style.visibility = 'visible';
		this.optionsTab.style.visibility = 'visible';
		this.picHeaderLeft.src = contextPath+'/admindocroot/controls/search/dmsDropdownHeaderLeftOpen.gif';
		this.picHeaderRight.src = contextPath+'/admindocroot/controls/search/dmsDropdownHeaderRightOpen.gif';
		this.isOpen=true;
	}
}

MgnlSearchDropdown.prototype.hoverOut = function(div) {
	this.open();
	div.className = 'dmsDropdownEntry';
}

MgnlSearchDropdown.prototype.hover = function(div) {
	this.open();
	div.className = 'dmsDropdownEntryHover';
}

MgnlSearchDropdown.prototype.setValue = function(v) {
	this.value = v;
	this.isOpen = false;
	this.closeForSure = true;
	this.fieldValue.innerHTML = this.options[v];
	this._close();
	eval(this.onchange);
}

MgnlSearchDropdown.prototype.render = function() {
	var 	html = '<div onMouseOut="Javascript:MgnlSearchDropdown.prototype.find(\''+this.name+'\').close();">';
		html += '<div onClick="Javascript:MgnlSearchDropdown.prototype.find(\''+this.name+'\').toggle();">';
		html += '<table id="' + this.name + 'Field" border="0" cellspacing="0" cellpadding="0">';
		html += '<tr><td width="8"><img src="'+contextPath+'/admindocroot/controls/search/dmsDropdownHeaderLeft.gif" id="' + this.name + 'PicHeaderLeft" width="8" height="20" alt=""></td>';
		html += '<td width="100%" nowrap id="' + this.name + 'FieldValue" class="dmsDropdownHeaderField">' + this.options[this.value] + '</td>';
		html += '<td width="15"><img src="'+contextPath+'/admindocroot/controls/search/dmsDropdownHeaderRight.gif" id="' + this.name + 'PicHeaderRight" width="15" height="20" alt=""></td></tr>';
		html += '</table>';
		html += '</div>';
		html += '<div id="' + this.name + 'OptionsDiv" style="position: absolute; visibility: hidden" onmouseover="MgnlSearchDropdown.prototype.find(\''+this.name+'\').open();">';
		html += '<table class="dmsDropdownBox" id="' + this.name + 'OptionsTab" border="0" cellspacing="0" cellpadding="0">';
		html += '<tr><td onMouseOver="MgnlSearchDropdown.prototype.find(\''+this.name+'\').open();">';
		
		// add options
		for( value in this.options){
			var label = this.options[value];
			html += '<div class="dmsDropdownEntry" onClick="Javascript:MgnlSearchDropdown.prototype.find(\''+this.name+'\').setValue(\'' + value + '\');" onMouseOut="MgnlSearchDropdown.prototype.find(\''+this.name+'\').hoverOut(this);" onMouseOver="MgnlSearchDropdown.prototype.find(\''+this.name+'\').hover(this);"><table cellspacing="0" cellpadding="0" border="0"><tr><td nowrap class="dmsDropdownEntryText">' + label + '&nbsp;&nbsp;</td></tr></table></div>';
		
		}
				
		html += '<table border="0" cellspacing="0" cellpadding="0" id="' + this.name + 'OptionsTabBottom" width="100%">';
		html += '<tr><td width="6"><img src="'+contextPath+'/admindocroot/controls/search/dmsDropdownBottomLeft.gif" width="6" height="6" alt=""></td>';
		html += '<td><img src="'+contextPath+'/admindocroot/controls/search/dmsDropdownBottom.gif" width="100%" height="6" alt=""></td>';
		html += '<td width="6"><img src="'+contextPath+'/admindocroot/controls/search/dmsDropdownBottomRight.gif" width="6" height="6" alt=""></td></tr>';
		html += '</table>';
		html += '</td></tr>';
		html += '</table>';
		html += '</div>';	
		html += '</div>'; 
		return html;
}