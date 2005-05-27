// this is the main switch
var mgnlDebugOn = false;

// set the contextes which you want to debug
var mgnlDebugContextes = {
	tree: true
}

function mgnlDebug(str, context){
	if(!mgnlDebugOn)
		return;
		
	// is the context in debug mode?
	if(context != null && (mgnlDebugContextes[context] == null || !mgnlDebugContextes[context]))
		return;
		
	var console = window.top.mgnlDebugConsole;
	var doc = null;
	// create new window if not allready done
	if(console == null){
		console = window.open('','mgnlDebugConsole');
		window.top.mgnlDebugConsole = console;
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