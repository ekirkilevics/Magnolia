classDef("mgnl.workflow.SubPagesList",{
    url:'',

    open: function(){
        var w = window.open(this.url, "subpages");
        w.focus();
    }
});