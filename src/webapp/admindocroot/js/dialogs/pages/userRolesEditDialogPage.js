// provides the delegate functions to the DynamicTable


// the rendering function is generated within the UserRolesEditIncludeAclDialogPage

function aclGetPermissionObject(prefix){
	var object =  new Object;
	object.accessRight=document.getElementById(prefix + "AccessRight").value;
	if(document.getElementById(prefix + "AccessType"))
		object.accessType=document.getElementById(prefix + "AccessType").value;
	object.path = document.getElementById(prefix + "Path").value;
	mgnlDebug("aclGetPermissionObject: prefix = " + prefix, "acl", object);
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
		if(aclRepositories[i]==repository){
			div.style.visibility="visible";
			div.style.zIndex=1000;
		}
		else{
			div.style.zIndex=-1000;
			div.style.visibility="hidden";			
		}
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