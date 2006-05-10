classDef("mgnl.controls.List", function(name){
    this.name = name;
    this.selected = -1;
    
    // css classes used
    this.css = {
        row:{
            selected: 'mgnlListRowSelected',
            hover: 'mgnlListRowHover',
            normal: 'mgnlListRow'
        }
    };
    
    this.select = function(id){
        this.unselect();
        this.selected = id;
        var row = $(this.name + 'Row' + this.selected);
        row.className = this.css.row.selected;
        
        // refresh function bar if any
        if(window["mgnlFunctionBar"]){
            window["mgnlFunctionBar"].refresh();
        }
    }
    
    this.unselect = function(){
        if(this.selected == -1)
            return;
        var row = $(this.name + 'Row' + this.selected);
        row.className= this.css.row.normal;
        this.selected = -1;
    }
    
    this.mouseover = function(id){
        if(id != this.selected){
            var row = $(this.name + 'Row' + id);
            row.className= this.css.row.hover;
        }
    }

    this.mouseout = function(id){
        if(id != this.selected){
            var row = $(this.name + 'Row' + id);
            row.className= this.css.row.normal;
        }
    }
    
    this.resize = function(){
    }
});