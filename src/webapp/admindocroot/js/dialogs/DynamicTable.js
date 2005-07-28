/* ###################################
### DynamicTable.js
################################### */

/* ###################################
This is an example
Header:
<script src="${pageContext.request.contextPath}/admindocroot/js/json.js"></script>
<script src="${pageContext.request.contextPath}/admindocroot/js/dialogs/DynamicTable.js"></script>

Body:
<script>
	// create an object for a new line
	function getNewObject(){
		return {value:''};
	}
	
	// extracts an object from the content
	function getObject(prefix){
		// to get the cell use getElementById(prefix)
		var obj = new Object();
		obj.value = document.getElementById(prefix + "_value").value;
		return obj;
	}
	
	// render a row
	function renderObject(cell, prefix, rowNumber, obj){
		html = '<input type="text" id="' + prefix + '_value" value="' + obj.value + '">';
		html += '<input type="button" onclick="searchTable.addNew();" value="+">';
		html += '<input type="button" onclick="searchTable.del(' + rowNumber + ');" value="-">';
		cell.innerHTML = html;
	}
	
	function validate(obj){
		return true;
	}
</script>

<table id="searchTable"><tr><td></td></tr></table>

Use JSON to parse this returned value<br>
<input type="button" value="save" onclick="searchTable.persist()">
<input type="text" name="persist">

<script>
	// initialize
	var hiddenField = document.formAdvancedSearch.persist;
	var searchTable = new MgnlDynamicTable("searchTable", hiddenField, getNewObject, getObject, renderObject, validate);
	
	// use JSON format to persist
	searchTable.json=true;

	// load the table with the current value
	searchTable.add({value:'hello'});
	searchTable.addNew();
</script>
################################### */

/* ###################################
### DynamicTable Class
################################### */

/* ###################################
### Constructor
################################### */

function MgnlDynamicTable(tableName, hiddenField, getNewObjectFunction, getObjectFunction, renderObjectFunction, validateFunction){
	this.tableName = tableName;
	this.objects = new Array();
	
	// object getNewObject()
	this.getNewObject = getNewObjectFunction;

	// object getObject(prefix) returns an object with the content of the fields
	this.getObject = getObjectFunction;

	// string renderObject(cell, prefix, rowNumber, object);
	this.renderObject = renderObjectFunction;
	// boolean validate(object)
	this.validate = validateFunction;
	// used for persistence
	this.hiddenField = hiddenField;
	
	// default persistence is some thing handmade
	this.json=false;
}


/* ###################################
### Add an Object
################################### */

MgnlDynamicTable.prototype.set = function (index, object){
	this.objects = this.getObjects();
	this.objects[index] = object;
	this.render();
}

/* ###################################
### Delete an Object
################################### */

MgnlDynamicTable.prototype.del = function (index){
	var tmp = this.getObjects();
	tmp[index] = null;
	
	this.objects = new Array();
	var newIndex = 0;
	for(oldIndex= 0; oldIndex < tmp.length; oldIndex++){
		if(tmp[oldIndex] != null){
			this.objects[newIndex] = tmp[oldIndex];
			newIndex++;
		}	
	}
	
	this.render();
}

/* ###################################
### Append Object
################################### */

MgnlDynamicTable.prototype.add = function (object){
	this.set(this.objects.length, object);
}

/* ###################################
### Add New
################################### */

MgnlDynamicTable.prototype.addNew = function (){
	this.set(this.objects.length, this.getNewObject());
}

/* ###################################
### Read the objects from the Table
################################### */

MgnlDynamicTable.prototype.getObjects = function(){
	var objects = new Array();
	// get table
	var table = document.getElementById(this.tableName);
	
	// for each row
	var objectCount = 0;
	for(i=0; i < table.rows.length; i++){
		var row = table.rows[i];
		// check if this is realy a row with content
		if (row && row.cells[0] && row.cells[0].innerHTML!=""){
				objects[objectCount] = this.getObject(this.tableName + i);
				objectCount++;
		}
	}
	return objects;
}

/* ###################################
### Render the table
################################### */

MgnlDynamicTable.prototype.render = function (update){
	if(update==null){
		update = false;
	}
	
	if(update){
		this.objects = this.getObjects();
	}

	// delte all
	var table = document.getElementById(this.tableName);

	//table.innerHTML=""; does not work with safari
	for(i=table.rows.length-1; i>=0 ; i--){
		table.deleteRow(i);
	}
	
	for(i=0; i < this.objects.length; i++){
		var row = table.insertRow(table.rows.length);
		var cell = row.insertCell(0);
		cell.setAttribute("id", this.tableName + i);
		this.renderObject(cell, this.tableName + i, i, this.objects[i]);
	}
}

/* ###################################
### persist the table and save it in the hidden field
################################### */

// we should use WDDX or something similar
MgnlDynamicTable.prototype.persist = function (){
	var str = "";
	this.objects = this.getObjects();
	
	if(this.json){
		// must include the json javascript
		str = JSON.stringify(this.objects);		
	}
	// old model
	else{
		// persist all
		for(i=0; i < this.objects.length; i++){
			if(i >0)
				str += "; ";
			str += this.persistObject(this.objects[i]) ;
		}
	}
	this.hiddenField.value = str;
}

MgnlDynamicTable.prototype.persistObject = function (object){
	var str = "";
	for(key in object){
		if(str.length > 0)
			str += ", ";
		str += key + ":" + object[key];
		first = false;
	}

	return str;
}
