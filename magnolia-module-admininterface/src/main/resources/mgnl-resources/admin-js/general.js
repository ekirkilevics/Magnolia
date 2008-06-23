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





/* ###################################
### mouse events
################################### */

var xMousePos = 0; //used in site admin (place layers)
var yMousePos = 0;

function initMouseEvents()
    {
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

        divShadow.style.left=(x+15) + "px";
        divShadow.style.top=(y-20) + "px";


        var divDenied=document.getElementById('mgnlMoveDivDenied');
        divDenied.style.left=(x+5) + "px";
        divDenied.style.top=(y-15) + "px";

        var divAllowed=document.getElementById('mgnlMoveDivAllowed');
        divAllowed.style.left=(x+20) + "px";
        divAllowed.style.top=(y-19) + "px";
        }

    // sorting pages in site admin
    // outdated; old admin
    if (mgnlSort)
        {
        var divShadow=document.getElementById('mgnlSortDivShadow');
        divShadow.style.left=(x+5) + "px";
        divShadow.style.top=(y-20) + "px";

        var divDenied=document.getElementById('mgnlSortDivDenied');
        divDenied.style.left=(x+5) + "px";
        divDenied.style.top=(y-15) + "px";
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
        divShadow.style.left=-50 + "px";;
        divShadow.style.top=-50 + "px";

        var divDenied=document.getElementById('mgnlMoveDivDenied');
        divDenied.style.visibility='hidden';
        divDenied.style.left=-50 + "px";
        divDenied.style.top=-50 + "px";

        var divAllowed=document.getElementById('mgnlMoveDivAllowed');
        divAllowed.style.visibility='hidden';
        divAllowed.style.left=-50 + "px";
        divAllowed.style.top=-50 + "px";

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
        divShadow.style.left=-50 + "px";
        divShadow.style.top=-50 + "px";

        var divDenied=document.getElementById('mgnlSortDivDenied');
        divDenied.style.visibility='hidden';
        divDenied.style.left=-50 + "px";
        divDenied.style.top=-50 + "px";

        mgnlSort=false;
        }
    }


function mgnlResetUp(evt)
    {
    if (mgnlDragTreeColumn)
        {
        mgnlDragTreeColumn_Tree.dragColumnStop(evt);
        }
    }


/* ###################################
### open window
################################### */


function mgnlOpenWindow(url,width,height)
    {

    //dialog window is resized in  dialog itself (window.resize)
    if (!width) width=800;
    if (!height) height=100;

    if(url.charAt(0)!= "/"){
        url = "/" + url;
    }
    url = contextPath + url;

    if(url.indexOf('?')>=0){
        url+="&";
    }
    else{
        url+="?";
    }
    url+="mgnlCK="+mgnlGetCacheKiller();

    var w=window.open(url,"mgnlDialog"+mgnlGetCacheKiller(),"width="+width+",height="+height+",scrollbars=no,status=yes,resizable=yes");
    if (w) w.focus();
    }


/* ###################################
### open dialog window
################################### */
function mgnlOpenDialog(path,nodeCollection,node,paragraph,repository,dialogPage,width,height)
    {

    //dialog window is resized in  dialog itself (window.resize)
    if (!width) width=800;
    if (!height) height=100;

    //magnolia edit window: add browser information (needed for rich editor)
    var agent=navigator.userAgent.toLowerCase();

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

    if (!dialogPage){
        dialogPage = ".magnolia/dialogs/" + paragraph + ".html";
    }


    var url=contextPath;
    url+="/"+ dialogPage;
    if(path){
        url = mgnl.util.URLUtil.addParameter(url,"mgnlPath", path);
    }
    if (nodeCollection) {
        url = mgnl.util.URLUtil.addParameter(url,"mgnlNodeCollection", path);
    }
    if (node) {
        url = mgnl.util.URLUtil.addParameter(url,"mgnlNode", path);
    }
    if (paragraph) {
        url = mgnl.util.URLUtil.addParameter(url,"mgnlParagraph", path);
    }
    if(repository){
        url = mgnl.util.URLUtil.addParameter(url,"mgnlRepository", repository);
    }
    url = mgnl.util.URLUtil.addParameter(url,"mgnlRichE", richE);
    url = mgnl.util.URLUtil.addParameter(url,"mgnlRichEPaste", richEPaste);

    url = mgnl.util.URLUtil.addParameter(url,"mgnlCK", mgnlGetCacheKiller());

    var w=window.open(url,"mgnlDialog"+mgnlGetCacheKiller(),"width="+width+",height="+height+"scrollbars=no,status=yes,resizable=yes");
    if (w) w.focus();
}


/* ###################################
### open tree browser
################################### */

function mgnlOpenTreeBrowser(pathSelected, pathOpen, repository, width, height, callBackCommand){
    mgnlDebug("mgnlOpenTreeBrowser","dialog");
    if (!width) width=450;
    if (!height) height=550;
    var src =  contextPath + "/.magnolia/pages/linkBrowser.html?mgnlCK="+mgnlGetCacheKiller();
    if (pathSelected) src+="&pathSelected="+pathSelected;
    if (pathOpen) src+="&pathOpen="+pathOpen;
    if (repository) src+="&repository="+repository;
    var w=window.open(src,"mgnlTreeBrowser","width="+width+",height="+height+",resizable=yes,status=yes,scrollbars=no");

    mgnlDebug("register call back function", "dialog");
    // we can't set this on the opened window since this is lost in safari
    window.mgnlCallBackCommand = callBackCommand;
}

function mgnlOpenTreeBrowserWithControl(control,repository,pathSelected,pathOpen,extension,width,height,addcontext){
    pathSelected = pathSelected?pathSelected:control.value;
    pathOpen = pathOpen?pathOpen:control.value;
    mgnlDebug("mgnlOpenTreeBrowserWithControl","dialog");
    var callBackCommand = new MgnlTreeBrowserWithControlCallBackCommand(control,extension,addcontext);
    mgnlOpenTreeBrowser(pathSelected, pathOpen, repository, width, height, callBackCommand)
}

/* ###################################
### tree browser default callback
################################### */

function MgnlTreeBrowserWithControlCallBackCommand(control, extension, addcontext){
    this.control = control;
    this.extension = extension;
    this.addcontext = addcontext;

    this.callback = function(value){
        if (this.addcontext){
          value = contextPath + value;
        }

        if (this.extension){
            value += "." + extension;
        }

        mgnlDebug("MgnlTreeBrowserCallBackCommand: write to the control", "dialog");
        this.control.value = value;
    }
}



/* ###################################
### open adminCentral
################################### */

function mgnlOpenAdminCentral(path,repository)
    {
    var src="";
    src+="${pageContext.request.contextPath}/.magnolia/adminCentral.html?mgnlCK="+mgnlGetCacheKiller();
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
    if (!href)
       href=document.location.href;

    href = mgnlRemoveParameter(href, 'mgnlCK');
    href = mgnlAddParameter(href, 'mgnlCK', new Date().getTime());
    return href;
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


function eventHandlerOnResize(e)
    {
    //resize tabs before resize of trees (in case there are any trees on a tab)
    mgnlDialogResizeTabs();
    mgnlDialogLinkBrowserResize();
    mgnlTreeResize();
    }

