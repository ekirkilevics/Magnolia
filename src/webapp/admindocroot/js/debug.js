// this is the main switch
var mgnlDebugOn = false;

// set the contextes which you want to debug
var mgnlDebugContextes = {
	tree: true,
	dialog: true
}

function mgnlRootWindow(current){
	if(current.top != current)
		return mgnlRootWindow(current.top);
	if(current.opener != null)
		return mgnlRootWindow(current.opener);
	return current;
}

function mgnlDebug(str, context){
	if(!mgnlDebugOn)
		return;
		
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
	if(context != null)
		str = context + ": " + str;
	div.innerHTML += str + "<br>";
}