/* ###################################
### generic.js
### generic, magnolia independant methods
################################### */




/* ###################################
### get position of an object
################################### */

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


function mgnlIsKeyEnter(event)
	{
	return mgnlIsKey(event,13);
	}

function mgnlIsKeyEscape(event)
	{
	return mgnlIsKey(event,27);
	}

function mgnlIsKey(event,keyCode)
	{
	if (window.event && window.event.keyCode == keyCode) return true;
	else if (navigator.appName=="Netscape" && event.which==keyCode) return true;
	else return false;
	}

function mgnlWhichKey(event)
	{
	if (window.event && window.event.keyCode) alert(window.event.keyCode);
	else if (navigator.appName=="Netscape" && event.which==keyCode) alert(event.which);
	}



function mgnlGetWindowSize()
	{
	var obj=new Object();
	if( typeof (window.innerWidth) == 'number' )
		{
		//Non-IE
		obj.w=window.innerWidth;
		obj.h=window.innerHeight;
		}
	else if (document.documentElement && (document.documentElement.clientWidth || document.documentElement.clientHeight ) )
		{
		//IE 6+ in 'standards compliant mode'
		obj.w=document.documentElement.clientWidth;
		obj.h=document.documentElement.clientHeight;
		}
	else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) )
		{
		//IE 4 compatible
		obj.w=document.body.clientWidth;
		obj.h=document.body.clientHeight;
		}
	//alert(h+'//'+w);
	return obj;
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

function mgnlAddParameter(href,name,value)
	{
	var delimiter;
	if (href.indexOf("?")==-1) delimiter="?";
	else delimiter="&";
	return href+delimiter+name+"="+value;
	}


function mgnlRemoveParameter(href,name)
	{
	//works only for a single paramter
	/*
	var nameExtended;
	var delimiter
	var tmp;
	if (href.indexOf("?"+name+"=")!=-1) delimiter="?";
	else delimiter="&";

	var tmp=href.split(delimiter+name+"=");
	var href2=tmp[0];
	if (tmp[1])
		{
		if (tmp[1].indexOf("&")!=-1) href2+=delimiter+tmp[1].substring(tmp[1].indexOf("&")+1);
		}
	return href2;
	*/

	var tmp=href.split("?");

	var newHref=tmp[0];
	if (tmp[1])
		{
		var paramObj=tmp[1].split("&");

		for (var i=0;i<paramObj.length;i++)
			{
			if (paramObj[i].indexOf(name+"=")!=0)
				{
				if (i==0) newHref+="?";
				else newHref+="&";
				newHref+=paramObj[i];
				}
			}
		}
	return newHref;
	}

