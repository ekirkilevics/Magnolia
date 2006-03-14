/* ###################################
### contextmenu.js
################################### */

/* ###################################
### ContextMenu Class
################################### */

/* ###################################
### Constructor
################################### */

function mgnlContextMenu(name){
    this.divMenu=document.getElementById(name+"_DivMenu");
    this.menuItems=new Array();
    this.colors=new Object();
    this.colors.menuHighlight="#F0F2E6";

    if(!this.divMenu){
        alert('no div for the context [' + name + '] menu found');
    }
}

mgnlContextMenu.prototype.show = function(event){
    if (this.divMenu)
        {
        //todo: (hack) find out height/width of div
        var divMenuHeight=0;
        var divMenuWidth=150;
        for (var i=0;i<this.menuItems.length;i++)
            {
            divMenuHeight+=18; //17: item height; 1: possible line
            var item=this.menuItems[i];

            //reset class name: remove _DISABLED (if existing)
            item.div.className=item.div.className.replace("_DISABLED","");
            var disabled=false;
            for (var elem in item.conditions)
                {
                if (item.conditions[elem].test()==false)
                    {
                    disabled=true;
                    break;
                    }
                }

            if (disabled)
                {
                item.div.className+="_DISABLED";
                item.div.onclick="";
                }
            else
                {
                item.div.onclick=item.onclick;
                }
            }
        var pos=mgnlGetMousePos(event);

        var left=pos.x+2;
        var top=pos.y+2;
        var windowSize=mgnlGetWindowSize();
        if (windowSize.h<top+divMenuHeight+25)
            {
            top=windowSize.h-divMenuHeight-25;
            if (top<0) top=0;
            }
        if (windowSize.w<left+divMenuWidth+30)
            {
            left=windowSize.w-divMenuWidth-30;
            if (left<0) left=5;
            }

        this.divMenu.style.left=left;
        this.divMenu.style.top=top;
        this.divMenu.style.visibility="visible";
        }
    //event.returnValue=false;
}

mgnlContextMenu.prototype.hide = function()
    {
    if (this.divMenu)
        {
        this.divMenu.style.visibility="hidden";
        }
    }


mgnlContextMenu.prototype.menuItemHighlight = function(item)
    {
    if (item.className.indexOf("_DISABLED")==-1)
        {
        item.style.backgroundColor=this.colors.menuHighlight;
        }
    }

mgnlContextMenu.prototype.menuItemReset = function(item)
    {
    item.style.backgroundColor="";
    }

//#################
//### TreeMenuItem
//#################

function mgnlContextMenuItem(id){
    this.id=id;
    this.div=document.getElementById(id);
    this.onclick=this.div.onclick;
}


//##########################################
//### Conditions for coloring the menu items
//##########################################

function mgnlTreeMenuItemConditionBoolean(bool){
    this.bool = bool;

    this.test = function(){
        return bool;
    }
}

function mgnlTreeMenuItemConditionSelectedNotRoot(tree){
    this.tree = tree;

    this.test = function(){
        if (this.tree.selectedNode.id!=this.tree.path) return true;
        else return false;
    }
}


function mgnlTreeMenuItemConditionSelectedNotNodeData(tree)
    {
    this.tree = tree;

    this.test = function(){
        if (this.tree.selectedNode.itemType!="mgnl:nodeData") return true;
        else return false;
    }
}

function mgnlTreeMenuItemConditionSelectedNotContentNode(tree)
    {
    this.tree = tree;

    this.test = function(){
        if (this.tree.selectedNode.itemType!="mgnl:contentNode") return true;
        else return false;
    }
}

function mgnlTreeMenuItemConditionSelectedNotContent(tree)
    {
    this.tree = tree;

    this.test = function(){
        if (this.tree.selectedNode.itemType!="mgnl:content") return true;
        else return false;
    }
}

function mgnlTreeMenuItemConditionPermissionWrite(tree)
    {
    this.tree = tree;

    this.test = function(){
        if (this.tree.selectedNode.permissionWrite) return true;
        else return false;
    }
}

//#################
//### Common Commands
//#################

    function mgnlTreeMenuItemOpen(tree)
        {
        var url= contextPath + tree.selectedNode.path+".html";
        var w=window.open(url,"mgnlInline","");
        if (w) w.focus();
        }

    function mgnlTreeMenuOpenDialog(tree,dialogPath)
        {
        var path=tree.selectedNode.id;
        mgnlOpenDialog(path,'','','',tree.repository,dialogPath);
        }

