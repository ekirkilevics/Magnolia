// provides the delegate functions to the DynamicTable


// the rendering function is generated within the UserRolesEditIncludeAclDialogPage

function aclGetPermissionObject(prefix){
	var object =  new Object;
	object.accessRight=document.getElementById(prefix + "AccessRight").value;
	if(document.getElementById(prefix + "AccessType"))
		object.accessType=document.getElementById(prefix + "AccessType").value;
	object.path = document.getElementById(prefix + "Path").value;
	return object;
}

function aclGetNewPermissionObject(){
	var object =  new Object;
	object.accessRight="";
	object.accessType="";
	object.path = "";
	return object;
}

// this is filled by UserRolesEditIncludeAclDialogPage
var aclRepositories = new Array();

function aclChangeRepository(repository){
	for(i=0; i<aclRepositories.length; i++){
		var div = document.getElementById("acl"+aclRepositories[i]+"Div");
		if(aclRepositories[i]==repository)
			//div.style.left="10px";
			div.style.visibility="visible";
		else
			//div.style.left="-2000px";
			div.style.visibility="hidden";			
	}
}

function aclFormSubmit(){
	// write the stuff in all the hiddenfields
	for(var i=0; i<aclRepositories.length; i++){
		var dynamicTable = eval("acl" + aclRepositories[i] + "DynamicTable");
		dynamicTable.persist();
	}
	// check and send
	if (mgnlDialogVerifyName("name")) mgnlDialogFormSubmit();
}

function aclChoose(prefix, repository){
	var control = document.getElementById(prefix + 'Path');
	mgnlOpenTreeBrowserWithControl(control, control.value,'',repository);
}