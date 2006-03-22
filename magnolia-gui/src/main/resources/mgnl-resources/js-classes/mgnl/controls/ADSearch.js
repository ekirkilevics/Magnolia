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
var	MgnlADSearch = new Object();

classDef("mgnl.controls.ADSearch", MgnlADSearch);

// the fields available -> setted in the jsp
MgnlADSearch.fields = {};

MgnlADSearch.constraints = {
	edit : {
		'contains': 'contains',
		'not': 'contains not',
		'starts': 'starts with',
		'ends': 'ends with',
		'is': 'is',
		'is not': 'is not'
	},

	date : {
		'today': 'is today',
		'before': 'is before',
		'after': 'is after',
		'is': 'is exactly'
	},

	select : {
		'is': 'is',
		'is not': 'is not'
	}
}

// create an object for a new line
MgnlADSearch.getNewObject = function(field){
	if(field == null){
		// get the next from the list
		field = this.getKeyAtIndex(this.fields, searchTable.objects.length);
		if(field == null){
			field = this.getKeyAtIndex(this.fields, 0);;
		}
	}

	var obj = new Object;
	obj.field = field;
	obj.type = this.fields[field].type;
	obj.constraint = this.getFirstConstraint(obj.type);
	obj.value = '';
	return obj;
};

// return the first key
MgnlADSearch.getFirstConstraint = function(type){
	for(name in this.constraints[type]){
		return name;
	}
}

// return the key at this index
MgnlADSearch.getKeyAtIndex = function(obj, index){
	var i=0;
	for(key in obj){
		if(i == index){
			return key;
		}
		i++;
	}
}
	
// extracts an object from the content
MgnlADSearch.getObject = function(prefix){
	var obj = new Object();
	obj.field = this.getValue(prefix + "Field");
	obj.type = this.fields[obj.field].type;
	obj.constraint = this.getValue(prefix + "Constraint");
	obj.value = this.getValue(prefix + "Value");
	
	return obj;
};

MgnlADSearch.getValue = function(name){
	var control = MgnlSearchDropdown.prototype.find(name);
	if(control == null){
		control = document.getElementById(name);
	}
	// there is not always a control
	if(control == null)
		return "";
	else
		return control.value;
};

// render a row
MgnlADSearch.renderObject = function(cell, prefix, rowNumber, obj){
	var html = "<table border='0'><tr>";
	 
	html += "<td>" + this.renderTypeSelect(prefix, rowNumber, obj) + "</td>";
	html += this.renderParameters(prefix, obj);
	
	
	// buttons
	html +="<td width='100%' align='right'><table><tr>";
	
	// erste zeile
	if(searchTable.objects.length == 1){
		html += '<td><img style="cursor:pointer;" src="' + contextPath + '/admindocroot/controls/search/dmsPlus.gif" border="0" onclick="searchTable.addNew();MgnlDMS.resizeList();"></td>';
		html += '<td><img style="cursor:pointer;" src="' + contextPath + '/admindocroot/0.gif" border="0" width="25" height="20"></td>';
	} else {
		if(searchTable.objects.length != 1){
			html += '<td><img style="cursor:pointer;" src="' + contextPath + '/admindocroot/controls/search/dmsMinus.gif" border="0" onclick="searchTable.del(' + rowNumber + ');MgnlDMS.resizeList();"></td>';
		} else {
			html += '<td><img style="cursor:pointer;" src="' + contextPath + '/admindocroot/0.gif" border="1" width="25" height="20"></td>';
		}
				
		if(searchTable.objects.length -1 == rowNumber){
			html += '<td><img style="cursor:pointer;" src="' + contextPath + '/admindocroot/controls/search/dmsPlus.gif" border="0" onclick="searchTable.addNew();MgnlDMS.resizeList();"></td>';
		} else {
			html += '<td><img src="' + contextPath + '/admindocroot/0.gif" border="0" width="25" height="20"></td>';
		}
	}

			
	html +="</tr></table></td>"
	html += "</tr></table>";
	cell.innerHTML = html;
	
	// initialize the select boxes
	MgnlSearchDropdown.prototype.initAll();
};

MgnlADSearch.validate = function(obj){
	return true;
};

MgnlADSearch.renderTypeSelect = function(prefix, rowNumber, obj){
	var options = new Object();
	for(name in this.fields){
		options[name] = this.fields[name].label;
	}
	return this.renderSelect(prefix + "Field", options, obj.field, "MgnlADSearch.changeType('"+prefix+"', " + rowNumber + ")");
};

MgnlADSearch.renderParameters = function(prefix, obj){
	var type = this.fields[obj.field].type;
	var html="";
	
	// select
	if(type == "select"){
		html = "<td>" + this.renderSelect(prefix + "Constraint", this.constraints.select, obj.constraint, "") + "</td><td>" + this.renderSelect(prefix + "Value", this.fields[obj.field].options, obj.value, "") + "</td>";
	}
	
	// date
	else if(type == "date"){
		html = "<td>" + this.renderSelect(prefix + "Constraint", this.constraints.date, obj.constraint, "MgnlADSearch.refresh()") + "</td>";
		if(obj.constraint != "today"){
			var name = 
			html += '<td><table cellpadding=0 border=0 cellspacing=0 ><tr><td>' + this.renderTextInput(prefix + 'Value', obj.value) + '</td><td class="mgnlText">&nbsp;<a href="javascript:mgnlDialogOpenCalendar(\'' +prefix + 'Value\',false)" style="text-decoration: none">...</a></td></tr></table></td>';
		}
		else{
			html += "<td></td>";
		}
	}
	
	// edit 
	else{
		html = "<td>" + this.renderSelect(prefix + "Constraint", this.constraints.edit, obj.constraint, "") + "</td><td>" + this.renderTextInput(prefix + 'Value', obj.value) + "</td>";
	}
	return html;
};

MgnlADSearch.changeType = function(prefix, rowNumber){
	var field = this.getValue(prefix + "Field");
	searchTable.set(rowNumber, this.getNewObject(field));
}

MgnlADSearch.renderSelect = function(name, options, value, onchange ){
	var dropDown = new MgnlSearchDropdown(name, options, value);
	dropDown.onchange = onchange;
	return dropDown.render();
}

MgnlADSearch.renderTextInput = function(name, value, width, imageLeft){
	var textField = new MgnlSearchTextField(name, value, width, imageLeft, {onkeypress: "if (mgnlIsKeyEnter(event)) sendADSearch();"});
	return textField.render();
}

MgnlADSearch.refresh = function(){
	searchTable.render(true);
}


// functions for passing to the dynamic table
// this. would mean the dynamic table object instead of the ADSearch Object 
MgnlADSearch_getNewObject = function(){return MgnlADSearch.getNewObject()};
MgnlADSearch_getObject = function(prefix){return MgnlADSearch.getObject(prefix)};
MgnlADSearch_renderObject = function(cell, prefix, rowNumber, obj){return MgnlADSearch.renderObject(cell, prefix, rowNumber, obj)};
MgnlADSearch_validate = function(obj){return MgnlADSearch.validate(obj)};
	