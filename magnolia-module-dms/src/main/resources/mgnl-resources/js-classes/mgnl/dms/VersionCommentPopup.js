classDef("mgnl.dms.VersionCommentPopup", {

    checkEnter: false,

    show: function(){
        var txt = document.getElementById("commentPopUpTextArea");
        var div = document.getElementById("commentPopUpDiv");
        var blockInputDiv = document.getElementById("blockInputDiv");

        txt.value="";

        div.style.left = mgnlGetWindowSize().w/2-div.clientWidth/2;
        div.style.top = mgnlGetWindowSize().h/2-div.clientHeight/2;
        div.style.visibility = "visible";

        blockInputDiv.style.width = mgnlGetWindowSize().w;
        blockInputDiv.style.height = mgnlGetWindowSize().h;
        blockInputDiv.style.visibility = "visible";

        this.checkEnter=true;
    },

    save: function(){
        var txt = document.getElementById("commentPopUpTextArea");
        var hidden = document.getElementById("versionComment");
        hidden.value=txt.value;
        mgnlDialogFormSubmit();
    },

    cancel: function(){
        var div = document.getElementById("commentPopUpDiv");
        var blockInputDiv = document.getElementById("blockInputDiv");

        div.style.visibility = "hidden";
        blockInputDiv.style.visibility = "hidden";
    },

    keyDown: function(e) {
        var evt=(e)?e:(window.event)?window.event:null;
        if(evt){
            var key=(evt.charCode)?evt.charCode: ((evt.keyCode)?evt.keyCode:((evt.which)?evt.which:0));
            if(this.checkEnter && key==13) {
                this.save();
            }
        }
    }
});