// this is the main switch
var mgnlDebugOn = false;

// set the contextes which you want to debug
var mgnlDebugContextes = {
	tree: true,
	dialog: true,
	debug: true	
}

function mgnlRootWindow(current){
	if(current.top != current)
		return mgnlRootWindow(current.top);
	if(current.opener != null)
		return mgnlRootWindow(current.opener);
	return current;
}

function mgnlDebug(msg, context, o, level){
	if(!mgnlDebugOn)
		return;
	if(!level)
		level = 1;
		
	// is the context in debug mode?
	if(context != null && (mgnlDebugContextes[context] == null || !mgnlDebugContextes[context]))
		return;
		
	var console = mgnlRootWindow(window).mgnlDebugConsole;
	var doc = null;
	// create new window if not allready done
	if(console == null){
		console = window.open('','mgnlDebugConsole');
		mgnlRootWindow(window).mgnlDebugConsole = console;
		doc = console.document;
		doc.write('<input type="button" value="Clear" onclick="document.getElementById(\'consoleDiv\').innerHTML=\'\';" > <p>');
		doc.write('<div id="consoleDiv" style="font-family: sans-serif; font-size: 10pt">');
		doc.write('</div>');
		doc.close();
	}
	else{
		doc = console.document;
	}
	
	if(doc == null)
		return;
	
	// get the div to write in
	var div = doc.getElementById('consoleDiv');
	
	if(o){
		msg += ":" + mgnlDebugObject(o, level, "");
	}
	
	if(context != null)
		msg = context + ": " + msg;
	div.innerHTML += msg + "<br>";
}

function mgnlDebugObject(o, level, spaces){
	var res = "";
	switch(typeof o){
	
		case "object":
			if(level<=0)
				return "object";
			res = "<br>" + spaces + "{<br>";
			for(var key in o){
				if(!res.match(/\{<br>$/))
					res += ",<br>";
				res += spaces + "&nbsp;&nbsp;&nbsp;" + key + ":";
				res += mgnlDebugObject(o[key], level-1, spaces + "&nbsp;&nbsp;&nbsp;"); 
			}
			res += "<br>" + spaces + "}";
			break;
		case "function":
			return "function"
			break;
		
		default:
			return o;
	}
	return res;
}
