/* ###################################
### general.js
### some general magnolia methods (used all over magnolia)
################################### */


var mgnlSort=false; //true as long as a page is selected


var mgnlControlSets=new Object();
//dialog: will be extended by each tab

var mgnlTreeControls=new Object();
//will be extended by each tree

var mgnlMove=false;
//inline editing: true as long as a paragraph is selected for moving


function eventHandlerOnResize(e)
	{
	//resize tabs before resize of trees (in case there are any trees on a tab)
	mgnlDialogResizeTabs();
	mgnlDialogLinkBrowserResize();
	mgnlTreeResize();
	mgnlAdminCentralResize();
	}
window.onresize = eventHandlerOnResize;




/* ###################################
### mouse events
################################### */

var xMousePos = 0; //used in site admin (place layers)
var yMousePos = 0;

function initMouseEvents()
	{
	// ns 4, check if needed for ns 6+
	if (navigator.appName=="Netscape")
		{
		document.captureEvents(Event.MOUSEMOVE);
		document.captureEvents(Event.MOUSEDOWN);
		document.captureEvents(Event.MOUSEUP);
		}
	document.onmousemove=getMousePos;
	document.onmousedown=mgnlResetDown;
	document.onmouseup=mgnlResetUp;
	}

initMouseEvents();

var mgnlDragTreeColumn_Tree;
var mgnlDragTreeColumn=false;

var mgnlTreeMoveNode_Tree;
var mgnlTreeMoveNode=false;

function getMousePos(evt)
	{
	var x,y;
	if (document.all)
		{
		x=window.event.clientX+document.body.scrollLeft;
		y=window.event.clientY+document.body.scrollTop;
		}
	else
		{
		x=evt.pageX;
		y=evt.pageY;
		}
	xMousePos = x;
	yMousePos = y;


	// moving content paragraphs
	if (mgnlMove)
		{
		var divShadow=document.getElementById('mgnlMoveDivShadow');
		divShadow.style.left=x+15;
		divShadow.style.top=y-20;

		var divDenied=document.getElementById('mgnlMoveDivDenied');
		divDenied.style.left=x+5;
		divDenied.style.top=y-15;

		var divAllowed=document.getElementById('mgnlMoveDivAllowed');
		divAllowed.style.left=x+20;
		divAllowed.style.top=y-19;
		}

	// sorting pages in site admin
	// outdated; old admin
	if (mgnlSort)
		{
		var divShadow=document.getElementById('mgnlSortDivShadow');
		divShadow.style.left=x+5;
		divShadow.style.top=y-20;

		var divDenied=document.getElementById('mgnlSortDivDenied');
		divDenied.style.left=x+5;
		divDenied.style.top=y-15;
		}

	// move/copy pages in tree
	if (mgnlTreeMoveNode)
		{
		mgnlTreeMoveNode_Tree.moveNode(x,y);
		}

	// resize tree columns
	if (mgnlDragTreeColumn)
		{
		mgnlDragTreeColumn_Tree.dragColumn(x,y);
		}
	}

function mgnlResetDown()
    {
    if (mgnlMove)
        {
        var editBarTable=document.getElementById(mgnlMoveNodeCollection+'__'+mgnlMoveNode);
		mgnlMoveNodeSetClassName(editBarTable,"NORMAL");

		var divShadow=document.getElementById('mgnlMoveDivShadow');
        divShadow.style.visibility='hidden';
		divShadow.style.left=-50;
		divShadow.style.top=-50;

		var divDenied=document.getElementById('mgnlMoveDivDenied');
		divDenied.style.visibility='hidden';
		divDenied.style.left=-50;
		divDenied.style.top=-50;

		var divAllowed=document.getElementById('mgnlMoveDivAllowed');
		divAllowed.style.visibility='hidden';
		divAllowed.style.left=-50;
		divAllowed.style.top=-50;

		mgnlMove=false;

		//trick! otherwise, by clicking a cont. of a different list, this one would be selected directly. now, the already selected will just be disabled
		mgnlMoveDont=true;
		setTimeout("mgnlMoveDont=false;",500);
        }

    if (mgnlSort)
    	{
    	if (!mgnlSortSubmit)
    		{
			var trSelected=document.getElementById('sadminTr'+mgnlSortPageId);
			trSelected.style.backgroundColor='';
			}

		var divShadow=document.getElementById('mgnlSortDivShadow');
        divShadow.style.visibility='hidden';
		divShadow.style.left=-50;
		divShadow.style.top=-50;

		var divDenied=document.getElementById('mgnlSortDivDenied');
		divDenied.style.visibility='hidden';
		divDenied.style.left=-50;
		divDenied.style.top=-50;

    	mgnlSort=false;
    	}
    }


function mgnlResetUp(evt)
	{
	if (mgnlDragTreeColumn)
		{
		mgnlDragTreeColumn_Tree.dragColumnStop();
		}
	}





/* ###################################
### open dialog window
################################### */


function mgnlOpenDialog(path,nodeCollection,node,paragraph,repository,context,dialogPage,width,height)
	{
    if (!context) context="";

	//dialog window is resized in  dialog itself (window.resize)
    if (!width) width=800;
    if (!height) height=100;

	//magnolia edit window: add browser information (needed for rich editor)
    var agent=navigator.userAgent.toLowerCase();
	//alert(agent);
	var richE="false";
	var richEPaste="";
	var richESupported=false;
	if (document.designMode)
		{
		//safari has designMode...
	    if (agent.indexOf("safari")==-1) richESupported=true;
		}
	if (richESupported)
		{
		//richedit
		richE="true";
		if (agent.indexOf("mac")!=-1) richEPaste="false";
		else if (agent.indexOf("msie")!=-1)  richEPaste="button";
		else richEPaste="textarea";
		}

	if (!dialogPage) dialogPage="/.magnolia/dialogs/standard.html";

    url=context;
    url+=dialogPage;
    url+="?mgnlPath="+path;
    url+="&mgnlNodeCollection="+nodeCollection;
	url+="&mgnlNode="+node;
	url+="&mgnlParagraph="+paragraph;
	url+="&mgnlRichE="+richE;
	url+="&mgnlRichEPaste="+richEPaste;
	//url+="&mgnlContext="+context;
	url+="&mgnlRepository="+repository;
	url+="&mgnlCK="+mgnlGetCacheKiller();

	var w=window.open(url,"mgnlDialog"+mgnlGetCacheKiller(),"width="+width+",height="+height+"scrollbars=no,status=yes,resizable=yes");
	if (w) w.focus();
	}


/* ###################################
### open tree browser
################################### */

function mgnlOpenTreeBrowser(context,controlName,pathSelected,pathOpen,repository,extension,width,height)
	{
	if (!width) width=450;
	if (!height) height=550;
	//if (!pathSelected || pathSelected=="") pathSelected="/";
	var src="";
	src+="/.magnolia/dialogs/linkBrowser.html?mgnlCK="+mgnlGetCacheKiller();
	src+="&mgnlControlName="+controlName;
	if (pathSelected) src+="&pathSelected="+pathSelected;
	if (pathOpen) src+="&pathOpen="+pathOpen;
	if (repository) src+="&repository="+repository;
	if (extension) src+="&mgnlExtension="+extension;
	var w=window.open(src,"mgnlTreeBrowser","width="+width+",height="+height+",resizable=yes,status=yes,scrollbars=no");
	if (w) w.focus();
	}



/* ###################################
### open adminCentral
################################### */

function mgnlOpenAdminCentral(path,repository)
	{
	var src="";
	src+="/.magnolia/adminCentral.html?mgnlCK="+mgnlGetCacheKiller();
	src+="&pathSelected="+path;
	if (repository) src+="&repository="+repository;
    var w=window.open(src,"mgnlAdminCentral","");
	if (w) w.focus();
	}






/* ###################################
### update mgnlCK in the extisting location string
################################### */

function mgnlUpdateCK(href)
	{
	if (!href) href=document.location.href;
	tmp=href.split("?")
	var href2=tmp[0]+"?mgnlCK="+new Date().getTime();
	if (tmp[1])
		{
		var qs=tmp[1].split("&");
		for (var elem in qs)
			{
			if (qs[elem].indexOf("mgnlCK=")!=0) href2+="&"+qs[elem];
			}
		}
	return href2;
	}


/* ###################################
### alert
################################### */
function mgnlAlert(text,title)
	{
    var line="--------------------------------------------\n";
    var alertText="";
    alertText+=line;
    if (title) alertText+=title+"\n"+line;
   	alertText+="\n"+text+"\n";
   	alertText+=line;
   	alert(alertText);
	}

/* ###################################
### confirm
################################### */
function mgnlConfirm(text,title)
	{
    var line="--------------------------------------------\n";
    var alertText="";
    alertText+=line;
    if (title) alertText+=title+"\n"+line;
   	alertText+="\n"+text+"\n";
   	alertText+=line;
   	return confirm(alertText);
   	}

