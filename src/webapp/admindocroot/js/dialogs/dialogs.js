/* ###################################
### dialogs.js
################################### */


/* ###################################
### global initialisations and event handlers
################################### */

var kupuEditors=new Array();
//will be extended at each richEdit control

var mgnlFormMainName="mgnlFormMain";


var mgnlTabLeft=8;
var mgnlTabTop=55;
var mgnlTabBottom=44;



function mgnlDialogInit()
	{
	var hasRichE=mgnlDialogRichEStart();
	if (!hasRichE) mgnlDialogFocusFirstEdit();
	}

function mgnlDialogFocusFirstEdit()
	{
	var form=document.forms[mgnlFormMainName];
	if (form)
		{
		/*
		for (var elem in form)
			{
			if (form[elem] && form[elem].type)
				{
				var type=form[elem].type;
				if (type=="text" || type=="textarea")
					{
					form[elem].focus();
					break;
					}
				}
			}
		*/
		}
	}


/* ###################################
### submit
################################### */


function mgnlDialogFormSubmit()
	{
	//richText: get data from iFrame
	for (var elem in mgnlRichEditors)
		{
		var richFrameDoc=null;
		var richFrameName=mgnlRichEditors[elem]+"-kupu-editor";

		if (document.frames && document.frames[richFrameName]) richFrameDoc=document.frames[richFrameName].document;
		else if (document.getElementById(richFrameName)) richFrameDoc=document.getElementById(richFrameName).contentDocument;

		if (richFrameDoc)
			{
			var body=richFrameDoc.getElementsByTagName("body");
			var value=body[0].innerHTML;
			//alert("["+value+"]");
			var hiddenTextarea=document.getElementById(mgnlRichEditors[elem]);
			if (hiddenTextarea) hiddenTextarea.value=value;
			}
		}
    document.forms[mgnlFormMainName].submit();
	}


/* ###################################
### reload opener
################################### */


function mgnlDialogReloadOpener()
	{
	mgnlDialogReload(opener.document);
	}

function mgnlDialogReload(doc)
	{
	if (!doc) doc=document;

	href=doc.location.href;

	href=mgnlRemoveParameter(href,"mgnlIntercept");
	href=mgnlRemoveParameter(href,"mgnlPath");

	doc.location.href=mgnlUpdateCK(href);
	}




/* ###################################
### dialog tabs
################################### */

function mgnlDialogShiftTab(setId,tabId,tabIndex)
	{
	var items=mgnlControlSets[setId].items;
	for (var elem in items)
		{
		var left="";
		var top="";
		if ((tabId && items[elem]==tabId) || (!tabId && elem==tabIndex))
			{
            left=mgnlTabLeft;
	        }
        var tab=document.getElementById(items[elem]+"_div");
        if (tab) tab.style.left=left;
		}
	}

function mgnlDialogResizeTabs()
	{
    var sizeObj=mgnlGetWindowSize();
  	for (var setElem in mgnlControlSets)
		{
		if (mgnlControlSets[setElem].resize==true)
			{
			var hTab=sizeObj.h-mgnlTabTop-mgnlTabBottom;
			for (var tabElem in mgnlControlSets[setElem].items)
				{
				var tab=document.getElementById(mgnlControlSets[setElem].items[tabElem]+"_div");
				if (tab)
					{
					tab.style.width=sizeObj.w-2*mgnlTabLeft-2; // -2: border
					tab.style.height=hTab;
					}
				}
			}
		}
	}



/* ###################################
### file control
################################### */


function mgnlDialogFileRemove(id) {

	var contentDiv=document.getElementById(id+"_contentDiv");
	var contentEmpty=document.getElementById(id+"_contentEmpty");
	if (contentDiv && contentEmpty)
		{
		contentDiv.innerHTML=contentEmpty.value;
		mgnlControlFileSetFileName(id,true);
		}

	}


function mgnlControlFileRemove(id)
	{
	var removeElem=document.getElementById(id+'_remove');
	if (removeElem) removeElem.value='true';
	}


function mgnlControlFileSetFileName(id,clear)
	{
	var nameElem=document.getElementById(id+"_fileName");
	var extElem=document.getElementById(id+"_fileNameExtension");

	var ext="";
	var name="";

	if (!clear)
		{
		var fileElem=document.getElementById(id);
		var value=fileElem.value;

		var slash="\\";
		if (value.indexOf("/")!=-1) slash="/";

		if (value.indexOf(".")!=-1)
			{
			name=value.substring(value.lastIndexOf(slash)+1,value.lastIndexOf("."));
			ext=value.substring(value.lastIndexOf("."));
			}
		else name=value.substring(value.lastIndexOf(slash)+1);
		}
	else ext=".";

	if (nameElem) nameElem.value=name;
	if (extElem) extElem.innerHTML=ext;

	}

/* ###################################
### control link: open link browser
################################### */

function mgnlDialogLinkOpenBrowser(context,controlName,repository,extension)
	{
	var pathSelected=document.getElementById(controlName).value;

	var pathOpen=""; //without selection: open path of editing page
	var pathElem=document.getElementById("mgnlPath");
	if (pathElem) pathOpen=pathElem.value;

	if (extension) pathSelected=pathSelected.replace("."+extension,"");

	mgnlOpenTreeBrowser(context,controlName,pathSelected,pathOpen,repository,extension)
	}


function mgnlDialogLinkBrowserResize()
	{
	var divTree=document.getElementById("mgnlTreeDiv");

	if (divTree)
		{
		var sizeObj=mgnlGetWindowSize();
		var h=sizeObj.h-50;
		var w=sizeObj.w-20;

		divTree.style.width=w;
		divTree.style.height=h;
		var iFrameTree=document.getElementById("mgnlDialogLinkBrowserIFrame");
		iFrameTree.style.height=h;
		}
	}

function mgnlDialogLinkBrowserWriteBack(controlName,extension,context)
	{
	var iFrameDoc=mgnlGetIFrameDocument('mgnlDialogLinkBrowserIFrame');
	var addressBar=iFrameDoc.getElementById("mgnlTree_AddressBar");
	var control=opener.document.getElementById(controlName);

	if (extension) extension="."+extension;
	if (!context) context = "";

	control.value=context + addressBar.value+extension;
	window.close();
	}



/* ###################################
### control checkboxSwitch: set value of hidden control to state of checkbox
################################### */

function mgnlDialogShiftCheckboxSwitch(id)
	{
	var hidden=document.getElementById(id);
	var button=document.getElementById(id+"_SWITCH_SETBUTTON_0");
	if (hidden && button)
		{
		hidden.value=button.checked;
		}
	}


/* ###################################
### control date
################################### */


function mgnlDialogOpenCalendar(controlName,showTime)
	{
	var y,M,d,H,m,s;
	var controlElem=document.getElementById(controlName);
	var value="";
	if (controlElem) value=controlElem.value;

	if (value!="")
		{
		value=value.split('T');
		var date=value[0].split('-'); // year-month-day
		y=date[0];
		M=date[1]-1;
		d=date[2];
		if (value[1])
			{
			var time=value[1].split(':'); // hours:minutes:seconds
			H=time[0];
			m=time[1];
			s=time[2];
			}
		}
	show_calendar(showTime,controlName,y,M,d,H,m,s);
	}


function mgnlDialogDatePatternCheck(control,pattern)
	{
	var value=control.value;
	for (var i=0; i<10; i++)
		{
		value=mgnlDialogDatePatternCheckDetail(value,i);
		}
	if (value!=pattern)
		{
		if (pattern=="XXXX-XX-XX") mgnlAlert("Date must be of format\nyyyy-MM-dd\n\nexample:\n2004-06-14");
		else mgnlAlert("Date must be of format\nyyyy-MM-dd'T'HH:mm:ss\n\nexample:\n2004-06-14T09:28:00");
		control.focus();
		}
	}

function mgnlDialogDatePatternCheckDetail(value,i)
	{
	while(value.indexOf(i)!=-1)
		{
		value=value.replace(i,"X");
		}
	return value;
	}



/* ###################################
### rich text editor
################################### */
function mgnlDialogRichEStart()
	{
	var hasRichE=false;
	for (var elem in mgnlRichEditors)
		{
		var nodeDataName=mgnlRichEditors[elem];
		var richFrameName=nodeDataName+"-kupu-editor";
		var richFrameDoc=mgnlGetIFrameDocument(richFrameName);

		if (richFrameDoc)
			{
			var frame = document.getElementById(richFrameName);
			kupu = initKupu(frame,nodeDataName);
			kupuui = kupu.getTool('ui');
			kupu.initialize();
			hasRichE=true;
			}
		}
	return hasRichE;
	}


function mgnlDialogRichEPasteClean()
	{
	if (!window.clipboardData) alert("Your browser does not support clean paste");
	else kupuui.pasteButtonHandler(window.clipboardData.getData('Text'));
	}
function mgnlDialogRichEPasteCleanHelp()
	{
	var str="";
	str+="Text copied in some applications (e.g. Word, Excel) contains additional information which might mess up your web content.\n\n";
	str+="Pasting text by Ctrl-V would insert this additional information.";
	alert(str);
	}


function mgnlDialogRichEPasteTextarea(nodeDataName,append)
	{
	var form=document.forms[mgnlFormMainName];
	var valueArea=form.elements[nodeDataName+"-paste"];
	var value=valueArea.value;

	if (append)
		{
		//append value at end of text
		var richFrameDoc=null;
		var richFrameName=nodeDataName+"-kupu-editor";

		if (document.frames && document.frames[richFrameName]) richFrameDoc=document.frames[richFrameName].document;
		else if (document.getElementById(richFrameName)) richFrameDoc=document.getElementById(richFrameName).contentDocument;

		form.elements[nodeDataName+"-paste"].value="";
		richFrameDoc.insertText(value);
		valueArea.value="";
		}
	else
		{
		//insert value at cursor position; not supported by default(changes in security props needed)
		//http://www.mozilla.org/editor/midasdemo/securityprefs.html
		if (kupuui.pasteButtonHandler(value)!="failed") valueArea.value="";
		}
	}



/* ###################################
### webDAV browser
################################### */

function mgnlDialogDAVSelect(nodeDataName,name,i,size,lastModified)
	{
	var value=document.getElementById(nodeDataName+"_"+i+"_hidden").value;
	parent.document.getElementById(nodeDataName).value=value;
	parent.document.getElementById(nodeDataName+"_size").value=size;
	parent.document.getElementById(nodeDataName+"_lastModified").value=decodeURI(lastModified);
	parent.document.getElementById(nodeDataName+"_showName").innerHTML=decodeURI(name);
	parent.document.getElementById(nodeDataName+"_showPath").innerHTML="<a href=\"javascript:mgnlDialogDAVBrowse('"+nodeDataName+"_iFrame','selectedValue');\">"+mgnlDialogDAVGetPath(value)+"</a>";
	parent.document.getElementById(nodeDataName+"_showIcon").src=document.getElementById(nodeDataName+"_"+i+"_icon").src;
	document.getElementById("selectedValue").value=value;
	}

function mgnlDialogDAVGetPath(value)
	{
	var path=value;
	if (path.lastIndexOf("/")==path.length-1) path=path.substring(0,path.length-1);
	if (path.indexOf("/")!=-1) path=path.substring(0,path.lastIndexOf("/")+1);
	else path="";
	path="/"+path;
	return decodeURI(path);
	}

function mgnlDialogDAVBrowse(iFrameName,idHidden)
	{
	var doc,value;
	if (iFrameName) doc=mgnlGetIFrameDocument(iFrameName);
	else doc=document;
    var form=doc.forms.mgnlDialogDAVBrowseForm;
    value=doc.getElementById(idHidden).value;
	if (idHidden=="selectedValue")
    	{
    	if (value.lastIndexOf("/")==value.length-1) value=value.substring(0,value.lastIndexOf("/"));
    	value=value.substring(0,value.lastIndexOf("/")+1);
    	}
	form.subDirectory.value=value;
	form.submit();
	}

function mgnlDialogDAVShow(id,vis)
	{
	if (vis) vis="visible";
	else vis="hidden";
	parent.document.getElementById(id).style.visibility=vis;
	}



/* ###################################
### password verification
################################### */

function mgnlDialogPasswordVerify(id)
	{
	var c1=document.getElementById(id);
	var c2=document.getElementById(id+"_verification");
	if (c1.value!=c2.value)
		{
		var form=document.forms[mgnlFormMainName];
		mgnlAlert("Password verification failed!");
		form[id+"_verification"].value="";
		//seems not to work...
		form[id+"_verification"].focus();
		}
	}


/* ###################################
### create/check names (label)
################################### */

function mgnlDialogCreateName(s)
	{
	// allowed characters: a-z, 0-9, -, _
	// first and second words only are taken, connected by _
	// if first word length <3 characters (a, one, der, die, das etc.) and second word existing, second and third words is taken
	// finally converted to lower case
	if (!s) return "untitled";

	var name = "";
	var nameRaw;
	var obj=s.split(" ");

	// get rid of the, a, one, der, die, das, la, il etc.
	var start=0;
	if (obj[0].length<=3 && obj[1]) start=1;
	nameRaw=obj[start];
	if (obj[start+1]) nameRaw+="_"+obj[start+1];

	for (var i=0;i<nameRaw.length;i++)
		{
		var charCode = nameRaw.charCodeAt(i);
		// charCodes: 48-57: 0-9; 65-90: A-Z; 97-122: a-z; 45: -; 95: _
		if (((charCode >= 48) && (charCode <= 57)) || ((charCode >= 97) && (charCode <= 122)) || charCode==45 || charCode==95)
			{
			name+=nameRaw.charAt(i);
			}
		else if ((charCode >= 65) && (charCode <= 90))
			{
			name+=nameRaw.charAt(i).toLowerCase();
			}
		else
			{
			//replace illegal characters with -
			name+="-";
			}
		}
	return name;
	}


function mgnlDialogVerifyName(nameId)
	{
	var name=document.getElementById(nameId);
	if (name)
		{
		if (name.value!=mgnlDialogCreateName(name.value))
			{
			mgnlAlert("Illegal characters in name!");
			//name.value=mgnlDialogCreateName(name.value);
			name.focus();
			return false;
			}
		else return true;
		}
	else return "element "+nameId+" not found";
	}



