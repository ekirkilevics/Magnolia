/* ###################################
### acl.js
### users and roles acl
################################### */

var globalIndex=0;

function mgnlAclChoose(context,index,repository)
	{
	mgnlOpenTreeBrowser(context,'acl'+index+'Path',document.getElementById('acl'+index+'Path').value,'',repository);
	}

function mgnlAclAdd(contextUsers,rowIndex,path,name,accessRight,accessType)
	{
	var aclTable = document.getElementById("aclTable");
	var index=globalIndex++;
	if (rowIndex>index) rowIndex=index;
	if (!path)
		{
		path="";
		name="<i>Please choose a role</i>";
		}
	else if (!name || name=="") name=path;
	var tr = aclTable.insertRow(rowIndex);
	//alert(rowIndex);
	tr.setAttribute("id","acl"+index);
	td=tr.insertCell(0);
	td.innerHTML=mgnlAclGetHtmlRow(index,path,name); // -> to be found in includeAcl.jsp resp. includeRoles.jsp
	if (!contextUsers)
		{
		document.getElementById("acl"+index+"AccessRight").value=accessRight;
		document.getElementById("acl"+index+"AccessType").value=accessType;
		}
	}

function mgnlAclDelete(index)
	{
	var row = document.getElementById("acl"+index);
	if (row.removeNode)
		{
		//ie
		row.removeNode(true);
		}
	else
		{
		//mozilla
		var rowCell=row.cells[0];
		rowCell.innerHTML=""; //workaround for safari, otherwise acl is kept in DOM
		row.innerHTML="";
		}
	}


function mgnlAclMove(contextUsers,index,up)
	{
	var aclTable = document.getElementById("aclTable");
	var moveTo=0;
	var countAll=false;
	var path,name,offset,accessRight,accessType;
	if (up) offset=0;
	else offset=1;
	var breakNext=false;
	for (var i=0;i<aclTable.rows.length;i++)
		{
		var row=aclTable.rows[i];
		var cellNotEmpty=false;
		if (row && row.cells[0] && row.cells[0].innerHTML!="") cellNotEmpty=true;

		if (row.id=="acl"+index)
			{
			path=document.getElementById("acl"+index+"Path").value;
			if (contextUsers)
				{
				//name=document.getElementById("acl"+index+"TdName").innerHTML;
				}
			else
				{
				accessRight=document.getElementById("acl"+index+"AccessRight").value;
				accessType=document.getElementById("acl"+index+"AccessType").value;
				}

			if (up) break;
			else breakNext=true;
			}
		else if (cellNotEmpty)
			{
			moveTo=i;
			if (breakNext)	break; //down
			}
		}
	moveTo+=offset;
	if (moveTo!=-1)
		{
		mgnlAclAdd(contextUsers,moveTo,path,name,accessRight,accessType);
		mgnlAclDelete(index);
		}
	}

function mgnlAclFormSubmit(contextUsers)
	{
	//loop aclTable - not correct order in mozilla if fields were looped...
	var aclTable = document.getElementById("aclTable");
	var value="";
	for (var i=0;i<aclTable.rows.length;i++)
		{
		var row=aclTable.rows[i];
		if (row && row.cells[0] && row.cells[0].innerHTML!="")
			{
			var id=row.id;
			var path=document.getElementById(id+"Path");
			if (path && path.value!="")
				{
				value+=path.value;
				if (!contextUsers)
					{
					value+=","+document.getElementById(id+"AccessRight").value;
					value+=","+document.getElementById(id+"AccessType").value;
					}
				value+=";";
				}
			}
		}
	var form=document.forms[mgnlFormMainName];
	form.aclList.value=value;
	if (mgnlDialogVerifyName("name")) mgnlDialogFormSubmit();
	}

function mgnlAclSetName(value)
	{
	var name=document.getElementById("name");
	if (name.value=='') name.value=mgnlDialogCreateName(value);
	}

