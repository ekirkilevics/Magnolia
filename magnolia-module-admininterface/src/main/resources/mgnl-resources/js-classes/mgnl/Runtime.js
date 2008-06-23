/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */

// we can not use package command yet: make sure that the var mgnl exists
var mgnl = mgnl ? mgnl : new Object();

// used to set the browser flags
var agent = navigator.userAgent.toLowerCase() ;

/**
 * This is the main class which must be loaded to use the importClass method.
 */
MgnlRuntime = mgnl.Runtime = {

    /**
     * This flag is false if all classes are loaded in one file in a final version.
     */
    loadingOn: true,

    /**
     * If one script is loading synchronized. The others mast load synchronized too.
     */
    loadSynchronized: false,

    /**
     * Remember allready loaded scripts (do not load twice)
     */
    loaded: new Object(),

    /**
     * Check the browser
     */
    isIE       : ( agent.indexOf("msie") != -1 ),
    isGecko    : !( agent.indexOf("msie") != -1 ),     // Firefox and others
    isSafari   : ( agent.indexOf("safari") != -1 ),
    isNetscape : ( agent.indexOf("netscape") != -1 ),

    /**
     * Creates the package object structure if not yet existing and register the class.
     * classDef(name, object)
     * classDef(name, constructor, [members], [static])
     * classDef(name, superClass, constructor, [members], [static])
     * @param name the full name (including the class name)
     * @param superKlass (optional) the supper class or an object used as prototype
     * @param klass a constructor or an object defining the class
     * @param members (optional) an object containing the members (methods/properties)
     * @param statics (optional) an object containing the static members
     */
    classDef: function(name, superKlass, klass, members, statics ){
        var names = name.split(".");
        var current = window;

        // if super was not passed change the parameters
        if(arguments.length <= 2 || typeof klass != "function"){
            statics = members;
            members = klass;
            klass = superKlass;
            superKlass = null;
        }

        if(superKlass){
            // if this is a constructor
            if(typeof superKlass == "function"){
                klass.superKlass = superKlass;

                // clone prototype of the supper
                for (var property in klass.superKlass.prototype) {
                    klass.prototype[property] = klass.superKlass.prototype[property];
                }

                // add nice shortcut to call the super method(s)
                klass.prototype.parentConstructor = function(){
                    // remember latest constructor to follow the chain
                    var superKlass = klass.superKlass;
                    if(this.nextSuperKlass){
                        superKlass = this.nextSuperKlass;
                    }
                    this.nextSuperKlass = superKlass.superKlass;
                    superKlass.apply(this, arguments);
                }
            }
            // in case this is an object
            else{
                klass.prototype = superKlass;
            }
        }

        // create package structure
        for(i=0; i<names.length; i++){
            var varName = names[i];
            if(i == names.length -1){
                current[varName] = klass;
            }
            else if(current[varName] == null){
                current[varName] = new Object;
            }
            current = current[varName];
        }

        // add the the class name
        if(klass.prototype){
            klass.prototype._class = name;
        }
        else{
            klass._class = name;
        }

        // add members (prototype)
        if(members){
            for (var property in members) {
                klass.prototype[property] = members[property];
            }
        }

        // add statics
        if(statics){
            for (var property in statics) {
                klass[property] = statics[property];
            }
        }

        return klass;
    },

    /**
     * Loads a script if needed and make the class avaiable under it's name. A
     * shortcut with a Mgnl prefix is created. Set sync = true if you load
     * a script after the page was loaded.<b>
     * Pest practise
     * <ul>
     *  <li>load scripts in the header
     *  <li>start using them in the onload method
     * </ul>
     * @see the load functions documentation
     */
    importClass: function(name, sync){
        var names = name.split(".");
        var className = names[names.length-1];

        if(!this.existVariable(name)){
            if(this.loadingOn){
                var url = name.replace(/\./g,"/");
                // load the script and set the shortcut after the script is loaded
                this.load(contextPath + "/admindocroot/js/classes/" + url + ".js", sync,
                    function(){
                        window["Mgnl" + className] = eval(name);
                    }
                 );
             }
             else{
                alert('not imported class [' + name + ']');
             }
        }
        // make sure that the shortcut exists
        else{
            window["Mgnl" + className] = eval(name);
        }
    },

    /**
     * Checks savely if this variable exists or not.
     */
    existVariable: function(name){
        try{
            var test = eval(name);
            if(test != null){
                return true;
            }
        }
        catch(e){
        }
        return false;
    },

    /**
     * This method loads a script dynamically. Set sync only to true if you load
     * a script after the page was loaded. If you load scripts in the header you
     * and use them after the onload event is fired (in the onload method for example)
     * you can savely load in the faster async mode.
     *
     * @param url the url to load (must be a javascript)
     * @param sync true if the method should wait until the script was loaded (optional)
     * @param callback the method called after async call (optional)
     */
    load: function(url, sync, callback){
        if(this.loaded[url] == null){
            // do not load twice, but is not yet loaded
            this.loaded[url]=false;

            // if this is a synchronized call we must synchronize until this is finished
            var oldSynchronization;
            if(sync){
                oldSynchronization = this.loadSynchronized;
                this.loadSynchronized = true;
            }
            else{
                sync = this.loadSynchronized;
            }

            this.debug("load " + url + "(snc: " + sync + ")");

            // save this for the follwing function
            var myRuntime = this;

            // create the function called after the script is loaded
            var onLoad =  function(){
                myRuntime.loaded[url] = true;

                // set to the original mode again
                if(oldSynchronization != null){
                    myRuntime.loadSynchronized = oldSynchronization;
                }

                // call callback function
                if(callback){
                    callback();
                }
            }

           // in safari we must make a sync AJAX call
           // ie does not give a onload event if the page is already loaded
           // therfore we make a AJAX call if the page was loaded
           if(sync || this.isSafari || (this.isIE && document.readyState == "complete")){

                var xmlRequest;
                if(this.isIE){
                    try{
                        xmlRequest = new ActiveXObject('Msxml2.XMLHTTP');
                    }
                    catch(e){
                            xmlRequest = new ActiveXObject('Microsoft.XMLHTTP');
                    }
                }
                else{
                    xmlRequest = new XMLHttpRequest() ;
                }

                // Load the script synchronously.
                xmlRequest.open( "GET", url, false ) ;
                xmlRequest.send( null ) ;

                // Evaluate the script.
                if ( xmlRequest.status == 200 ){
                    try{
                        eval( xmlRequest.responseText ) ;
                        onLoad();

                    }
                    catch ( e ){
                        alert( 'Error parsing ' + url + ': ' + e.message ) ;
                    }
                }
                else{
                    alert( 'Error loading ' + url ) ;
                }
           }

           // async load
           else{
               var e = document.createElement("script");
               e.src = url;
               e.type="text/javascript";

               e.onload = e.onreadystatechange = function (){
                    // Gecko doesn't have a "readyState" property
                    if ( !this.readyState || this.readyState == 'loaded' ){
                        onLoad();
                    }
               }
               document.getElementsByTagName("head")[0].appendChild(e);
           }
       }
    },

    /**
     * Cache for not yet printed messages
     */
    notYetDebuged: new Array(),

    /**
     * Debug the message. Check if the debug class is already loaded. If not cache.
     */
    debug: function(msg, o ,level){
        if(window.MgnlDebug){
            if(notYetDebugged.length>0){
                for(i=0; i < notYetDebugged.length; i++){
                    MgnlDebug.debug(notYetDebuged[i].msg, this, notYetDebuged[i].o, notYetDebuged[i].level);
                }
                notYetDebugged = new Array();
            }
            MgnlDebug.debug(msg, this, o, level);
        }
        else{
            notYetDebuged.append({msg: msg, o:o, level:level});
        }
    }

};

// define shortcuts: the wrapping function is used to save the this variable
classDef = function(name, superKlass, klass, members, statics){mgnl.Runtime.classDef(name, superKlass, klass, members, statics)};
importClass = function(name, sync){mgnl.Runtime.importClass(name,sync)};

// define this class properly
classDef("mgnl.Runtime", MgnlRuntime);