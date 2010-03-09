/* ###################################
### inline.js
################################### */


/* ###################################
### preview
################################### */

function mgnlPreview(prev)
    {
    var href=mgnlUpdateCK(document.location.href)


    href=mgnlRemoveParameter(href,"mgnlIntercept");
    href=mgnlAddParameter(href,"mgnlIntercept","PREVIEW");

    href=mgnlRemoveParameter(href,"mgnlPreview");
    if (prev) href=mgnlAddParameter(href,"mgnlPreview","true");
    else href=mgnlAddParameter(href,"mgnlPreview","false");

    document.location.href=href;
    }



/* ###################################
### delete paragraph
################################### */

function mgnlDeleteNode(path,paragraphName,nodeName)
    {
    var alertText = mgnlMessages.get("inline.delete.text.js");
    if (mgnlConfirm(alertText,mgnlMessages.get("inline.delete.title.js")))
        {
        var href=mgnlUpdateCK(document.location.href)

        href=mgnlRemoveParameter(href,"mgnlIntercept");
        href=mgnlAddParameter(href,"mgnlIntercept","NODE_DELETE");

        href=mgnlRemoveParameter(href,"mgnlPath");
        if (typeof paragraphName == 'undefined'&& typeof nodeName == 'undefined') {
            href=mgnlAddParameter(href,"mgnlPath",path);
        } else if (paragraphName == "") {
            href=mgnlAddParameter(href,"mgnlPath",path+"/"+nodeName);
        } else {
            href=mgnlAddParameter(href,"mgnlPath",path+"/"+paragraphName+"/"+nodeName);
        }

        window.location.href=href;
        }
    else return;
    }


/* ###################################
### move paragraphs
################################### */

var mgnlMoveNode; //selected container name
var mgnlMoveNodeCollection; //selected container list name

//divs which will be placed onMouseMove
//shadow: half transparent and scaled down editBar
function loadMoveDivs() {
    var div = document.createElement("div");
    div.id = "mgnlMoveDivShadow";
    document.body.appendChild(div);

    div = document.createElement("div");
    div.id = "mgnlMoveDivDenied";
    document.body.appendChild(div);

    div = document.createElement("div");
    div.id = "mgnlMoveDivAllowed";
    div.setAttribute("style", "visibility:hidden");
    div.innerHTML = mgnlMessages.get('inline.move.aboveThisOne.js');
    document.body.appendChild(div);
}

MgnlDHTMLUtil.addOnLoad(loadMoveDivs);

var mgnlMoveDont=false;
//move will not start as long as mgnlMoveDont is true
//set true when clicking a button on a editBar ((and on timeout at mgnlMoveReset()))

//var mgnlMove=false; //true as long as a container is selected
// -> moved to general.js

function mgnlMoveNodeStart(containerList,container,barId)
    {
    if (!mgnlMoveDont && !mgnlMove)
        {
        var bar=document.getElementById(barId);
        mgnlMoveNodeSetClassName(bar,"PUSHED");

        //var tmp=bar.id.split('__');
        mgnlMoveNodeCollection=containerList;
        mgnlMoveNode=container;

        var divShadow=document.getElementById('mgnlMoveDivShadow');
        divShadow.style.visibility='visible';

        var divDenied=document.getElementById('mgnlMoveDivDenied');
        var divAllowed=document.getElementById('mgnlMoveDivAllowed');
        if (divAllowed.style.visibility=='hidden') divDenied.style.visibility='visible'; // only if new selection

        mgnlMove=true;
        }
    }


function mgnlMoveNodeSetClassName(bar,state)
    {
    if (!state) state="NORMAL";
    var base=bar.className;
    if (base.indexOf("_")!=-1) base=base.substring(0,base.indexOf("_"));
    if (state=="NORMAL") bar.className=base;
    else if (state=="PUSHED") bar.className=base+"_PUSHED";
    else if (state=="MOUSEOVER") bar.className=base+"_MOUSEOVER";
    }

 function mgnlMoveNodeHigh(bar)
    {
    var tmp=bar.id.split('__');
    if (mgnlMove)
        {
        if (tmp[0]==mgnlMoveNodeCollection && tmp[1]!=mgnlMoveNode)
            {
            //same container list and not the container to move: highlight this bar
            mgnlMoveNodeSetClassName(bar,"MOUSEOVER");
            var divDenied=document.getElementById('mgnlMoveDivDenied');
            divDenied.style.visibility='hidden';
            var divAllowed=document.getElementById('mgnlMoveDivAllowed');
            divAllowed.style.visibility='visible';
            }
        }
    }

function mgnlMoveNodeReset(bar)
    {
    tmp=bar.id.split('__');
    if (mgnlMove)
        {
        if (tmp[0]==mgnlMoveNodeCollection)
            {
            //same containre list
            if (tmp[1]!=mgnlMoveNode)
                {
                //not the container to move: reset this bar
                mgnlMoveNodeSetClassName(bar,"NORMAL");
                }
            var divDenied=document.getElementById('mgnlMoveDivDenied');
            divDenied.style.visibility='visible';
            var divAllowed=document.getElementById('mgnlMoveDivAllowed');
            divAllowed.style.visibility='hidden';
            }
        }
    }

 function mgnlMoveNodeEnd(bar,path)
    {
    if (mgnlMove)
        {
        var tmp=bar.id.split('__');
        if (mgnlMoveNodeCollection==tmp[0] && mgnlMoveNode!=tmp[1])
            {
            //same container list and not container to move
            mgnlMoveNodeSetClassName(bar,"PUSHED");
            var href=document.location.href;
            var pathSelected=path+"/"+mgnlMoveNodeCollection+"/"+mgnlMoveNode;
            var pathSortAbove=path+"/"+mgnlMoveNodeCollection+"/"+tmp[1];


            //'MoveNodeCollection='+mgnlMoveNodeCollection+'&MoveNode='+mgnlMoveNode+'&MoveNodeAbove='+tmp[1]+'&path='+path+'&actions=moveNode';
            //var href='/.CMSadmin/tagRequestHandler.html?MoveNodeCollection='+mgnlMoveNodeCollection+'&MoveNode='+mgnlMoveNode+'&MoveNodeAbove='+tmp[1]+'&path='+path+'&actions=moveNode';
            href=mgnlUpdateCK(href)

            href=mgnlRemoveParameter(href,"mgnlIntercept");
            href=mgnlAddParameter(href,"mgnlIntercept","NODE_SORT");

            href=mgnlRemoveParameter(href,"mgnlPathSelected");
            href=mgnlAddParameter(href,"mgnlPathSelected",pathSelected);

            href=mgnlRemoveParameter(href,"mgnlPathSortAbove");
            href=mgnlAddParameter(href,"mgnlPathSortAbove",pathSortAbove);

            document.location.href=href;
            }
        }
    }




