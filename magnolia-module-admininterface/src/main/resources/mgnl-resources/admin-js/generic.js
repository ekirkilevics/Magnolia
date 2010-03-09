/* ###################################
### generic.js
### generic, magnolia independant methods
################################### */

/**
 * Simple shortcut for document.getElementById()
 */
if (typeof $ != 'function') {
    $ = function(element)
    {
        return document.getElementById(element);
    }
}

/**
 * get position of an object
 */
function mgnlGetPosX(obj)
    {
    if (!obj) return 0;

    if (navigator.appName.indexOf("Microsoft")==-1)
        {
        if (navigator.vendor!=("Netscape6") && navigator.product!=("Gecko")) return obj.x; //ns4
        else //ns6; don't laugh...
            {
            for (var elem in obj)
                {
                var tmp=obj[elem];
                }
            return obj.offsetLeft;
            }
        }
    var x=document.body.scrollLeft;
    while (obj.offsetParent)
        {
        x+=obj.offsetLeft;
        obj=obj.offsetParent;
        }
    return x; //ie
    }


function mgnlGetPosY(obj)
    {
    if (!obj) return 0;

    return obj.offsetTop;
    if (navigator.appName.indexOf("Microsoft")==-1)
        {
        if (navigator.vendor!=("Netscape6") && navigator.product!=("Gecko")) return obj.y; //ns4
        else //ns6; don't laugh...
            {
            for (var elem in obj)
                {
                var tmp=obj[elem];
                }
            return obj.offsetTop;
            }
        }
    var y=document.body.scrollTop;
    while (obj.offsetParent)
        {
        y+=obj.offsetLeft;
        obj=obj.offsetParent;
        }
    return y; //ie
    }


function mgnlGetMousePos(event)
    {
    var pos=new Object();
    if (document.all)
        {
        pos.x=window.event.clientX+document.body.scrollLeft;
        pos.y=window.event.clientY+document.body.scrollTop;
        }
    else
        {
        pos.x=event.pageX;
        pos.y=event.pageY;
        }
    return pos;
    }


/* ###################################
### miscellaneous
################################### */


function mgnlIsKeyEnter(event){
    return mgnlIsKey(event,13);
}

function mgnlIsKeyEscape(event){
    return mgnlIsKey(event,27);
}

function mgnlIsKey(event,keyCode){
    if (window.event && window.event.keyCode == keyCode)
        return true;
    else if
        (navigator.appName=="Netscape" && event.which==keyCode) return true;
    else
        return false;
}

function mgnlGetWindowSize() {
    return mgnl.util.DHTMLUtil.getWindowSize();
}


function mgnlGetIFrameDocument(iFrameName)
    {
    if (document.frames && document.frames[iFrameName]) return document.frames[iFrameName].document;
    else if (document.getElementById(iFrameName)) return document.getElementById(iFrameName).contentDocument;
    else return null;
    }

function mgnlGetCacheKiller() {
    var now = new Date();
    return now.getTime();
}



/* ###################################
### add/remove parameter to query string
################################### */

function mgnlAddParameter(href, name, value)
{
    var anchorSplit = href.split("#");
    var anchor = (anchorSplit.length == 2) ? "#"+anchorSplit[1] : "";
    href=anchorSplit[0];

    var delimiter;
    if (href.indexOf("?") == -1)
        delimiter = "?";
    else
        delimiter = "&";

    return href + delimiter + name + "=" + value + anchor;
}


function mgnlRemoveParameter(href, name)
    {
    var anchorSplit = href.split("#");
    var anchor = (anchorSplit.length == 2) ? "#"+anchorSplit[1] : "";

    href=anchorSplit[0];
    var tmp=href.split("?");

    var newHref=tmp[0];
    var query= new Array();
    if (tmp[1]) {
        var paramObj=tmp[1].split("&");

        for (var i=0;i<paramObj.length;i++) {
            if (paramObj[i].indexOf(name+"=")!=0) {
                    query.push(paramObj[i]);
            }
        }
    }

    if(query.length > 0) {
        newHref += "?";

        for(var i=0; i < query.length; i++) {
            newHref += query[i];

            if(i + 1 < query.length) {
                newHref += "&";
            }
        }
    }

    return newHref + anchor;
}

