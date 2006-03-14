/* ###################################
### i18n.js
################################### */

/* ###################################
### Message Class
################################### */

var MGNL_I18N_DEFAULT_BASENAME = "info.magnolia.module.admininterface.messages";

/* ###################################
### Constructor
################################### */

function MgnlI18nMessages(){
    this.messages = new Object();
}

/* ###################################
### Add a message. Basename is optional
################################### */

MgnlI18nMessages.prototype.add = function (key, msg, basename){
    if(basename==null){
        basename = MGNL_I18N_DEFAULT_BASENAME;
    }
    
    if(this.messages[basename] == null){
        this.messages[basename] = new Object();
    }
    
    this.messages[basename][key]=msg;
}


/* ###################################
### Get a Message. Basename is optional
################################### */

MgnlI18nMessages.prototype.get = function (key, basename, args){
    var msg;
    
    // basename is optional
    if(basename == null)
        basename = MGNL_I18N_DEFAULT_BASENAME;
        
    // return ??? key ??? if you can't find the mesage
    if(this.messages[basename] == null || this.messages[basename][key] == null){
        return "???" + key + "???";
    }
    
    msg = this.messages[basename][key];
    
    // replace parameters
    if(args != null){
        for(i=0; i<args.length;i++){
            msg = msg.replace("{" + i +"}", args[i]);
        }
    }
    return msg;
}

var mgnlMessages = new MgnlI18nMessages();
