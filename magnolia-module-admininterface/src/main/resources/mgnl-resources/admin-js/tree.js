/* ###################################
### tree.js
################################### */

    var globalCounter=0; //for debugging

    //#################
    //### Tree
    //### repository: the repository name
    //### name: the name of the javascript tree
    //### handlerName: the name of the tree handler
    //### browseMode: true if the tree is showed in the link browser.
    //### functionBarName: the name of the functionBar javascript object (needs call to refresh on selectNode)
    //#################

    function mgnlTree(repository, path, name, height, handlerName, browseMode, functionBarName) {
        if(handlerName == null){
            handlerName = repository;
        }

        if(browseMode == null){
            browseMode = false;
        }

        this.repository=repository;
        this.path=path;
        this.name=name;
        this.handlerName = handlerName;
        this.browseMode = browseMode;
        this.functionBarName = functionBarName;
        mgnlDebug("new mgnlTree", "tree", this);

        document.write('<div id="'+name+'_'+path+'_DivSub" style="display:none;"></div>');
        this.divMain=document.getElementById(name+"_"+path+"_DivMain");
        // this is setted afterward because a cyclic dependency for the conditions
        this.menu = null;

        this.addressBar=document.getElementById(name+"AddressBar");
        this.divMoveShadow=document.getElementById(name+"_MoveShadow");
        this.divMoveDenied=document.getElementById(name+"_MoveDenied");


        this.nodes=new Object();
        this.selectedNode=this.getNode(path);

        this.clipboardNode=null;
        this.clipboardMethod=null;

        this.lastEditedHtmlObject;
        this.lastEditedOriginalValue="";
        this.lastEditedNode;
        this.lastEditedName="";
        this.lastEditedIsMeta=false;
        this.lastEditedIsLabel=false;
        this.lastEditedIsNodeDataValue=false;
        this.lastEditedIsNodeDataType=false;

        this.columns=new Array();

        //this.columnResizerDiv=document.getElementById(name+"_ColumnResizerDiv");
        this.columnResizerLine=document.getElementById(name+"_ColumnResizerLine");
        this.columnHeaderDiv=document.getElementById(name+"_ColumnHeader");

        this.height=height;

        this.paddingLeft=0;
        this.paddingRight=0;
        this.paddingTop=0;
        this.paddingBottom=0;
        this.columnResizerGifWidthHalf=4;
        this.columnResizerGifWidth=9;
        this.columnSpacing=8;
        this.columnMinimumWidth=20;

        this.colors=new Object();
        this.colors.nodeHighlight="#EDF2FA";

        this.colors.nodeSelected="#D1E1ED";

        this.strings=new Object();
        this.strings.saving=mgnlMessages.get('tree.saving.js');
        this.strings.loading=mgnlMessages.get('tree.loading.js');
        this.strings.empty="-";

        this.moveDontReset=false;
        this.moveDenied=false;
        this.moveDeniedTimeout=200;
        this.moveLastMouseoverId=null;
    }


    mgnlTree.prototype.expandNode = function(path)
        {
        mgnlDebug('tree.expandNode','tree');

        var chunks=path.split("/");
        var id="";

        for (var i=1;i<chunks.length;i++) //i==1: first chunk is empty: /en/bla -> [], [en], [bla]
            {
            id+="/"+chunks[i];
            var node=this.nodes[id];

            if (node)
                {
                node.expand();
                }
            else
                {
                node=this.getNode(id);

                var divSub=document.getElementById(node.divSubId);
                if (divSub.innerHTML=="")
                    {
                    var params=new Object();
                    params.pathSelected=path;
                    node.expand(params);
                    break;
                    }
                }
            }
        this.selectNode(path);
        }


    mgnlTree.prototype.shifterDown = function(id)
        {
        this.moveDontReset=true;
        this.shiftNode(id);
        }

    mgnlTree.prototype.shifterOut = function()
        {
        this.moveDontReset=false;
        }


    mgnlTree.prototype.shiftNode = function(id)
        {
        var node=this.getNode(id);
        node.shift();
        }

    mgnlTree.prototype.selectNode = function(id) {
        mgnlDebug("selectNode:" + id, "tree");
        var sNode=this.getNode(id);
        var divMain=document.getElementById(sNode.divMainId);
        mgnlDebug("selectNode: divMainId:" + sNode.divMainId, "tree");

        var divMainSelected=document.getElementById(this.selectedNode.divMainId);

        if (divMain && this.selectedNode.divMainId!=sNode.divMainId) {
            if (divMainSelected) {
                divMainSelected.style.backgroundColor="";
                divMainSelected.style.textDecoration="";
            }
            divMain.style.backgroundColor=this.colors.nodeSelected;

            if(this.addressBar){
                this.addressBar.value=id;
            }
            this.selectedNode=sNode;
            // refresh functionBar
            if (window[this.functionBarName]) {
                eval (this.functionBarName).refresh ();
            }
        }
    }


    mgnlTree.prototype.getNode = function(id)
        {
        var node=this.nodes[id];
        if (!node)
            {
            node=new mgnlTreeNode(this,id);
            this.nodes[id]=node;
            }
        return node;
        }


    mgnlTree.prototype.dragColumnStart = function(resizerGif,resizerNumber)
        {
        if (!mgnlDragTreeColumn)
            {
            var line=this.columnResizerLine;
            line.style.left=mgnlGetPosX(resizerGif) + 5;
            line.style.visibility="visible";
            line.style.top=mgnlGetPosY(this.divMain);
            line.style.height=parseInt(this.divMain.style.height)+1;

            this.columnResizerGif=resizerGif;
            this.columnResizerLine=line;
            this.columnResizerNumber=resizerNumber;

            mgnlDragTreeColumn_Tree=this;
            mgnlDragTreeColumn=true;
            }
        }

    mgnlTree.prototype.dragColumnStop = function(e){
        //todo: resize edit control!

        mgnlDragTreeColumn=false;
        this.columnResizerLine.style.visibility="hidden";

        var lastLeft=0;
        // true if more than one chanaged
        var resizeAll=true;

        // loop over all columns
        // the resizer gif is already at the new position
        for (var i=0;i<this.columns.length-1;i++) {
            // get the gif div
            var colReGif=document.getElementById(this.name+"ColumnResizer"+(i+1));

            // get the position of the middle  of this resizre
            var left=parseInt(colReGif.style.left)+5;
            // if a column is smaller than the minimal width
            if (left<lastLeft+this.columnMinimumWidth) {
                left=lastLeft+this.columnMinimumWidth;
                resizeAll=true;
            }

            // clac the width of this column
            var w=left-lastLeft;
            this.columns[i].width=w;
            lastLeft=left;

            // if the last column is smaller than the the minimal width
            if (i==this.columns.length-2) {
                var w=parseInt(this.divMain.style.width)-left;
                if (w<this.columnMinimumWidth) {
                    w=this.columnMinimumWidth;
                    resizeAll=true;
                }
                this.columns[i+1].width=w;
            }
        }

        if (resizeAll)
            this.resize();
        else
            this.resize(this.columnResizerNumber);
    }


    mgnlTree.prototype.dragColumn = function(x,y)
        {
        //todo: stop at next dragger
        this.columnResizerGif.style.left=x-this.columnResizerGifWidthHalf-2;
        this.columnResizerLine.style.left=x-1;
        }

    mgnlTree.prototype.getColumnsWidth = function()
        {
        var w=0;
        for (var elem in this.columns)
            {
            w+=this.columns[elem].width;
            }
        return w;
        }

    mgnlTree.prototype.resize = function(columnNumber){
        //no columnNumber passed: resize all columns (@ resizing window)
        //columnNumber passed: resize only this column and re-clip the one before (@ resizing column)

        if (this.divMain){
            var sizeObj=mgnlGetWindowSize();

            //resize tree div
            var agent=navigator.userAgent.toLowerCase();
            //todo: to be tested!
            if (agent.indexOf("msie")!=-1)
                    this.divMain.style.width=sizeObj.w;
            else
                this.divMain.style.width=sizeObj.w-this.paddingLeft-this.paddingRight-2;

            if(this.browseMode)
                this.divMain.style.height=sizeObj.h - 60;
            else
                this.divMain.style.height=sizeObj.h - 68;

            //resize columns
            var quotient=sizeObj.w/this.getColumnsWidth();
            var sizeObjX=sizeObj;

            //todo: move to init (tree or column?)!!!

            //rules property differs in browsers
            var rulesKey;
            if (document.all){
                rulesKey="rules";
            }
            else if (document.getElementById){
                rulesKey="cssRules";
            }
            mgnlDebug("mgnlTree.resize: running with rules: " + rulesKey, "tree");

            // for each stylesheet included
            var treeColumnClasses=new Object();

            //to do: define break point!
            for (var elem0 = document.styleSheets.length-1; elem0>=0; elem0--) {
                mgnlDebug("mgnlTree.resize: styleSheets[elem0].href = " + document.styleSheets[elem0].href, "tree");

                var rules=document.styleSheets[elem0][rulesKey];
                mgnlDebug("mgnlTree.resize: rules", "tree", rules);

                //for (var elem1 in rule) //does not work in firebird 0.8, safari 1.2
                for (var elem1=0; elem1<rules.length; elem1++){

                    var cssClass=rules[elem1].selectorText;
                    // in safar 1.3 the selectorText is in lower case
                    if (cssClass && cssClass.toLowerCase().indexOf("." + this.name.toLowerCase() + "cssclasscolumn")!=-1){
                        treeColumnClasses[cssClass.toLowerCase()]=rules[elem1];
                    }
                }
            }

            mgnlDebug("mgnlTree.resize: treeColumnClasses", "tree", treeColumnClasses);

            var left=0;
            for (var elem in this.columns){
                // in safari is it lowercase
                var cssClass="."+this.name.toLowerCase()+"cssclasscolumn"+elem;
                var cssClassObj=treeColumnClasses[cssClass];
                var columnWidth=parseInt(this.columns[elem].width*quotient);
                //resize columne (by setting the left and clip attribute of its css class
                if (cssClassObj){
                    if (!columnNumber || elem==columnNumber){
                        cssClassObj.style.left=left;
                    }
                    if (!columnNumber || elem==columnNumber || elem==columnNumber-1){
                        cssClassObj.style.clip="rect(0 " + (columnWidth-8) + " 100 0)";
                    }
                    this.columns[elem].width=columnWidth;

                }

                //place the column resizer
                var columnResizer=document.getElementById(this.name+"ColumnResizer"+elem);
                var columnResizerLine=document.getElementById(this.name+"ColumnLine"+elem);

                if (columnResizer){
                    if (!columnNumber || elem==columnNumber){
                        columnResizer.style.left=left-this.columnResizerGifWidthHalf-1;
                     }
                }

                if (columnResizerLine){
                    if (!columnNumber || elem==columnNumber){
                        columnResizerLine.style.left=left;
                        columnResizerLine.style.height=this.divMain.style.height;
                     }
                }

                left+=columnWidth;
            }
        }
    }


    mgnlTree.prototype.nodeHighlight = function(htmlObject,id,permissionWrite)
        {
        var doHighlight=true;
        if (mgnlTreeMoveNode)
            {
            //only highlight first column (_Name)
            var idPost=htmlObject.id.substring(htmlObject.id.lastIndexOf("_"));
            if (idPost!="_Name" || !permissionWrite) doHighlight=false;
            }

        //if (!mgnlTreeMoveNode || permissionWrite)
        if (doHighlight)
            {
            //note: this.nodes[i] may not be initialized yet; therefor build mainDiv manualy
            var mainDivId=this.name+"_"+id+"_DivMain";
            var mainDiv=document.getElementById(mainDivId);
            if (mainDivId!=this.selectedNode.divMainId)
                {
                mainDiv.style.backgroundColor=this.colors.nodeHighlight;
                }

            if (mgnlTreeMoveNode)
                {
                this.moveLastMouseoverId=htmlObject.id;
                this.moveDenied=false;
                this.divMoveDenied.style.visibility="hidden";
                }
            }
        }

    mgnlTree.prototype.nodeReset = function (htmlObject,id)
        {
        //note: this.nodes[i] may not be initialized yet
        var mainDivId=this.name+"_"+id+"_DivMain";
        var mainDiv=document.getElementById(mainDivId);
        if (mainDivId!=this.selectedNode.divMainId)
            {
            mainDiv.style.backgroundColor="";
            }
        if (mgnlTreeMoveNode)
            {
            //to avoid flickering show the denied icon after a short timeout
            this.moveDenied=true;
            setTimeout("mgnlTreeMoveDeniedHide('"+this.name+"','"+htmlObject.id+"')",this.moveDeniedTimeout);
            //this.divMoveDenied.style.visibility="visible";
            }
        }
    function mgnlTreeMoveDeniedHide(treeName,mouseOutId)
        {
        var tree=eval(treeName);
        if (tree.moveDenied && tree.moveLastMouseoverId==mouseOutId)
            {
            tree.divMoveDenied.style.visibility="visible";
            }
        }

    mgnlTree.prototype.selectedNodeReset = function() {
        var divMain=document.getElementById(this.selectedNode.divMainId);
        divMain.style.backgroundColor="";
        this.selectedNode=this.getNode(this.path); //root

        // refresh functionBar
        if (window[this.functionBarName]) {
            eval (this.functionBarName).refresh ();
        }
    }


    mgnlTree.prototype.mainDivReset = function()
        {
        this.menuHide();
        this.moveReset();
        }

    mgnlTree.prototype.menuShow = function(event)
        {
        if (this.menu)
            {
            this.menu.show(event);
            }
        }

    mgnlTree.prototype.menuHide = function()
        {
        if (this.menu)
            {
            this.menu.hide();
            }
        }

    mgnlTree.prototype.createRootNode = function(createItemType)
        {
        this.selectedNode=this.getNode(this.path);
        this.createNode(createItemType);
        }


    mgnlTree.prototype.createNode = function(createItemType)
        {
        //todo: select root / ...
        var parentNode=this.selectedNode;

        //todo:fix
        //if (!parentNode) parentNode=this.nodes["/"];


        var params=new Object();
        params.forceReload=true;
        params.createItemType=createItemType;

        parentNode.expand(params);
        //parentNode.expand(true,'','',createItemType,'');

        var shifter=document.getElementById(parentNode.shifterId);
        if (shifter) shifter.src=shifter.src.replace("EMPTY","COLLAPSE");
        }


    mgnlTree.prototype.deleteNode = function()
        {
        var text;
        if(this.getNode(this.selectedNode.id).isActivated){
            text = mgnlMessages.get('tree.deletenode.confirm.deactivation.text.js', null, [this.selectedNode.id]);
        }
        else{
            text = mgnlMessages.get('tree.deletenode.confirm.text.js', null, [this.selectedNode.id]);
        }

        var title=mgnlMessages.get('tree.deletenode.confirm.title.js');
        if (mgnlConfirm(text,title))
            {
            var parentNode=this.getNode(this.selectedNode.parentId);
            var deleteNode=this.selectedNode.label;

            //reset to tree's root path
            this.selectedNodeReset();


            var params=new Object();
            params.forceReload=true;
            params.deleteNode=deleteNode;

            parentNode.expand(params);
            //parentNode.expand(true,'','',null,deleteNode);
            }
        }

    mgnlTree.prototype.exportNode = function(keepVersions)
        {
        var text=mgnlMessages.get('tree.exportnode.confirm.text.js');
        var title=mgnlMessages.get('tree.exportnode.confirm.title.js', null, [this.selectedNode.id]);

        if (mgnlConfirm(text,title))
            {

            keepVersions = keepVersions!=null ? keepVersions: false;
            var url= contextPath + "/.magnolia/pages/export.xml?exportxml=true&mgnlFormat=true&mgnlRepository=" + this.repository + "&mgnlPath=" + this.selectedNode.id + "&mgnlKeepVersions=" + keepVersions;

            location.href=url;
            }
        }

    mgnlTree.prototype.importNode = function(link, keepVersions, uuidBehavior)
        {
            keepVersions = keepVersions!=null ? keepVersions: false;
            // create new uuid
            uuidBehavior = uuidBehavior!=null ? uuidBehavior: 0;

            var strDiv ='<form method="post" enctype="multipart/form-data" action="' + contextPath + '/.magnolia/pages/import.html">';
            strDiv +='<input type="hidden" name="mgnlRepository" value="' + this.repository + '">';
            strDiv +='<input type="hidden" name="mgnlPath" value="' + this.selectedNode.id + '">';
            strDiv +='<input type="hidden" name="mgnlKeepVersions" id="mgnlKeepVersions" value="'+keepVersions+'"/>';
            strDiv +='<input type="hidden" name="mgnlUuidBehavior" id="mgnlUuidBehavior" value="'+uuidBehavior+'"/>';

            strDiv +='<input type="hidden" name="mgnlRedirect" value="' + contextPath + '/.magnolia/trees/' + this.handlerName + '.html?pathSelected=' + this.selectedNode.id + '&pathOpen=' + this.selectedNode.id + '">';
            strDiv +='<input type="file" name="mgnlFileImport" id="mgnlFileImport" /><br/>';

            strDiv +='<input type="submit" class="mgnlImportButton" name="importxml" value="' + mgnlMessages.get('import.button.js') + '" />';
            strDiv +='<input type="button" class="mgnlImportButton" value="' + mgnlMessages.get('import.button.close.js') + '" onclick="document.body.removeChild(this.parentNode.parentNode)" />';
            strDiv +='</form>';

            var resDiv = document.createElement('div');
            resDiv.id= "mgnlImportdiv";
            document.body.appendChild(resDiv);
            resDiv.innerHTML = strDiv;

            $('mgnlImportdiv').style.left = (mgnlGetPosX(link) + 20) + "px";
            $('mgnlImportdiv').style.top = mgnlGetPosY(link) + "px";

        }


    mgnlTree.prototype.copyNode = function()
        {
        mgnlTreeMoveNode=true;
        mgnlTreeMoveNode_Tree=this;
        this.clipboardNode=this.selectedNode;
        this.clipboardMethod=1; //Tree.ACTION_COPY
        }

    mgnlTree.prototype.cutNode = function()
        {
        mgnlTreeMoveNode=true;
        mgnlTreeMoveNode_Tree=this;
        this.clipboardNode=this.selectedNode;
        this.clipboardMethod=0; //Tree.ACTION_MOVE

        var divMain=document.getElementById(this.selectedNode.divMainId);
        divMain.style.textDecoration="line-through";
        }

    mgnlTree.prototype.pasteNode = function(id,pasteType,permissionWrite,lineDivId){
        if (mgnlTreeMoveNode && permissionWrite){
            if (this.clipboardMethod==0 && id.indexOf(this.clipboardNode.id)==0 && pasteType!=0){
                //move into itself is not possible
                mgnlAlert(mgnlMessages.get('tree.pastenode.itself.js'));
            }
            else if (this.clipboardMethod==1 && id.indexOf(this.clipboardNode.id)==0 && pasteType!=0){
                //move into itself is not possible
                mgnlAlert(mgnlMessages.get('tree.pastenode.itself.js'));
            }
            else{
                //if (!this.getNode(this.clipboardNode.id).isActivated || this.clipboardMethod==1) {
                    if (lineDivId){
                        //for last line on root level
                        var lineDiv=document.getElementById(lineDivId);
                        lineDiv.style.backgroundImage="";
                    }
                    this.moveReset();

                    this.selectedNode=this.getNode(id);
                    var parentPath=this.selectedNode.id.substring(0,this.selectedNode.id.lastIndexOf("/"));
                    if (parentPath=="") parentPath="/";

                    var pathToReload;
                    if (this.clipboardMethod==0){
                        //paste after cut
                        if (this.clipboardNode.id.indexOf(parentPath)==0){
                             //e.g. sort inside a directory or paste into sister: reload selected
                             pathToReload=parentPath;
                        }
                        else{
                            //no hokums, reload root
                            pathToReload=this.path;
                        }
                    }
                    else{
                        //paste after copy
                        if (pasteType==2){
                            //Tree.PASTETYPE_SUB: reload selected
                            pathToReload=this.selectedNode.id;

                            var shifter=document.getElementById(this.selectedNode.shifterId);
                            if (shifter){
                                var src=shifter.src;
                                src=src.replace("EMPTY","COLLAPSE");
                                src=src.replace("EXPAND","COLLAPSE");
                                shifter.src=src;
                            }
                        }
                        else {
                            //reload parent of selected
                            pathToReload=parentPath;
                        }
                    }

                    var nodeToReload=this.getNode(pathToReload);

                    var params=new Object();
                    params.forceReload=true;
                    params.treeAction=this.clipboardMethod;
                    params.pathClipboard=this.clipboardNode.id;
                    params.pathSelected=this.selectedNode.id;
                    params.pasteType=pasteType;

                    nodeToReload.expand(params);
                //}
            }
            this.clipboardNode=null;
            this.clipboardMethod=null;
            this.moveReset();
        }
    }

    mgnlTree.prototype.moveNode = function(x,y)
        {
        if (this.divMoveShadow.style.visibility=="hidden")
            {
            //initalize
            var icon=document.getElementById(this.selectedNode.iconId);
            var shadowSrc=icon.src;
            var label=document.getElementById(this.selectedNode.labelId).innerHTML;

            this.divMoveShadow.innerHTML='<img src="'+shadowSrc+'"><span class="mgnlTreeText">'+label+'</span>';
            this.divMoveShadow.style.visibility="visible";
            this.divMoveDenied.style.visibility="visible";
            }
        this.divMoveShadow.style.left=x+6;
        this.divMoveShadow.style.top=y+11;
        this.divMoveDenied.style.left=x+10;
        this.divMoveDenied.style.top=y+2;
        }

    mgnlTree.prototype.moveNodeHighlightLine = function(divId)
        {
        if (mgnlTreeMoveNode)
            {
            mgnlDebug("highlight line" + divId, "tree");
            div=document.getElementById(divId);
            div.style.backgroundImage="url(" + contextPath + "/.resources/controls/tree/line_internode.gif)";
            this.divMoveDenied.style.visibility="hidden";
            this.moveLastMouseoverId=div.id;
            this.moveDenied=false;
            }
        }

    mgnlTree.prototype.moveNodeResetLine = function(divId)
        {
        if (mgnlTreeMoveNode)
            {
            div=document.getElementById(divId);
            div.style.backgroundImage="";

            //to avoid flickering show the denied icon after a short timeout
            this.moveDenied=true;
            setTimeout("mgnlTreeMoveDeniedHide('"+this.name+"','"+div.id+"')",this.moveDeniedTimeout);
            }
        }

    mgnlTree.prototype.moveReset = function()
        {
        if (mgnlTreeMoveNode && !this.moveDontReset)
            {
            mgnlTreeMoveNode=false;
            mgnlTreeMoveNode_Tree=null;
            this.moveDenied=false;
            this.divMoveShadow.style.visibility="hidden";
            this.divMoveDenied.style.visibility="hidden";
            var divMain=document.getElementById(this.selectedNode.divMainId);
            divMain.style.textDecoration="";
            divMain.style.backgroundColor="";
            }
        }

    mgnlTree.prototype.activateNode = function(action,recursive)
        {
        var nodeToReload=this.selectedNode.getParent();

        var params=new Object();
        params.forceReload=true;
        params.treeAction=action;
        params.pathSelected=this.selectedNode.id;
        if (recursive) params.recursive=recursive;

        nodeToReload.expand(params);
        }

    mgnlTree.prototype.deactivateNode = function(action)
        {
        var nodeToReload=this.selectedNode.getParent();

        var params=new Object();
        params.forceReload=true;
        params.treeAction=action;
        params.pathSelected=this.selectedNode.id;

        nodeToReload.expand(params);
        }



    mgnlTree.prototype.editNodeData = function(span,id,columnNumber,htmlEdit)
        {
        var column=this.columns[columnNumber];
        if (htmlEdit!="")
            {
            var value=span.innerHTML;
            value = value.replace(/&lt;/g,"<");
            value = value.replace(/&gt;/g,">");
            value = value.replace(/&amp;/g,"&");

            if (value.toLowerCase().indexOf("<input")!=0 && value.toLowerCase().indexOf("<select")!=0)
            //else: already in edit mode!
                {
                span.innerHTML=htmlEdit;

                var control=document.getElementById(this.name+"_EditNodeData");
                if (value!=this.strings.saving && value!=this.strings.empty)
                    {
                    if (control.type=="select-one")
                        {
                        for (var i=0;i<control.options.length;i++)
                            {
                            var text=control.options[i].innerHTML;
                            //alert(text+"::"+value+"::"+i);
                            if (text==value)
                                {
                                control.selectedIndex=i;
                                break;
                                }
                            }
                        }
                    else
                        {
                        control.value=value;
                        }
                    }

                //resize control
                var controlWidth=this.columns[columnNumber].width-10;
                if (columnNumber==0)
                    {
                    //first column, subtract padding and 30 for switcher and icon
                    var outerSpan=document.getElementById(this.name+"_"+id+"_Column0Outer");
                    var padding=outerSpan.style.paddingLeft;
                    if (padding) padding=parseInt(padding);
                    else padding=0;
                    controlWidth=controlWidth-padding-30;
                    }
                if (controlWidth<10) controlWidth=10;
                control.style.width=controlWidth;

                control.focus();

                this.lastEditedHtmlObject=span;
                this.lastEditedOriginalValue=value;
                this.lastEditedName=column.name;
                this.lastEditedIsMeta=column.isMeta;
                this.lastEditedIsLabel=column.isLabel;
                this.lastEditedIsNodeDataValue=column.isNodeDataValue;
                this.lastEditedIsNodeDataType=column.isNodeDataType;
                this.lastEditedNode=this.nodes[id];
                }
            }
        }

    mgnlTree.prototype.resetNodeData = function()
        {
        if (this.lastEditedHtmlObject)
            {
            this.lastEditedHtmlObject.innerHTML=this.lastEditedOriginalValue;
            this.lastEditedHtmlObject=null;
            this.lastEditedOriginalValue="";
            this.lastEditedName="";
            this.lastEditedIsMeta=false;
            this.lastEditedIsLabel=false;
            this.lastEditedIsNodeDataValue=false;
            this.lastEditedIsNodeDataLabel=false;
            this.lastEditedNode=null;
            }
        }

    mgnlTree.prototype.editNodeDataKeydown = function(event,control)
        {
        if (mgnlIsKeyEscape(event))
            {
            this.resetNodeData();
            }
        else if (mgnlIsKeyEnter(event))
            {
            control.blur(); //blur event calls saveNodeData!
            }
        }


    // display is optional. use it if the column display is not the same as the saved value
    // this is not a good solution, because the system should return the label instead of passing it

    mgnlTree.prototype.saveNodeData = function(value, displayValue)
        {
        var exit=false;
        if (value==this.lastEditedOriginalValue || displayValue==this.lastEditedOriginalValue ||(value=="" && this.lastEditedOriginalValue==this.strings.empty))
            {
            exit=true;
            }

        if (exit)
            {
            this.resetNodeData();
            return;
            }
        else
            {
            //todo: create params object (see node.expand()
            var node=this.lastEditedNode;
            var htmlObject=this.lastEditedHtmlObject;
            if (htmlObject == null) {
              return;
            }
            var name=this.lastEditedName;
            var isMeta=this.lastEditedIsMeta;
            var isLabel=this.lastEditedIsLabel;
            var isNodeDataValue=this.lastEditedIsNodeDataValue;
            var isNodeDataType=this.lastEditedIsNodeDataType;
            if(displayValue==null) {
                displayValue="";
            }
            this.lastEditedHtmlObject=null;
            this.lastEditedOriginalValue="";
            this.lastEditedName="";
            this.lastEditedIsMeta=false;
            this.lastEditedIsLabel=false;
            this.lastEditedIsNodeDataType=false;
            this.lastEditedIsNodeDataValue=false;
            this.lastEditedNode=null;

            htmlObject.innerHTML=this.strings.saving;
            setTimeout("mgnlTreeSaveNodeData('"+htmlObject.id+"','"+node.id+"','"+this.name+"','"+name+"','"+escape(value)+"',"+isMeta+","+isLabel+","+isNodeDataValue+","+isNodeDataType+",'" + escape(displayValue)+"');",10);
            }
        }

    mgnlTree.prototype.refresh = function()
        {
        var rootNode=this.getNode(this.path);
        var params=new Object();
        params.forceReload=true;
        params.pathSelected=this.selectedNode.id;
        rootNode.expand(params);
        }

    mgnlTreeSaveNodeData = function (lastEditedHtmlObjectId,id,treeName,saveName,saveValue,isMeta,isLabel,isNodeDataValue,isNodeDataType, displayValue)
        {
        var tree=eval(treeName);

        var params=new Object();
        params.saveName=saveName;
        params.saveValue=saveValue;
        params.isMeta=isMeta;
        params.isLabel=isLabel;
        params.isNodeDataValue=isNodeDataValue;
        params.isNodeDataType=isNodeDataType;
        params.pathSelected='';
        params.pathOpen='';
        params.displayValue=displayValue;

        var callBackParams=new Object();
        callBackParams.id = id;
        callBackParams.treeName = treeName;
        callBackParams.lastEditedHtmlObjectId = lastEditedHtmlObjectId;
        callBackParams.isLabel = isLabel;

        // async
        tree.nodes[id].httpRequest(params, callBackParams, mgnlTreeSaveNodeDataCallback);
        }

    /**
    callback for mgnlTreeSaveNodeData
    **/
    mgnlTreeSaveNodeDataCallback = function (params, html)
        {

        var id = params.id;
        var treeName = params.treeName;
        var lastEditedHtmlObjectId = params.lastEditedHtmlObjectId;

        var tree=eval(treeName);
        var isLabel=params.isLabel;


        if (html=="") html=tree.strings.empty;

        html = mgnlAlertCallbackMessage(html);

        html = html.replace(/&/g,"&amp;");
        html = html.replace(/</g,"&lt;");
        html = html.replace(/>/g,"&gt;");

        document.getElementById(lastEditedHtmlObjectId).innerHTML=html;

        if (isLabel)
            {
            //reload parent

            //todo: highlight renamed (s. copy/move; hidden field mgnlSelectNode)
            var parentPath=id.substring(0,id.lastIndexOf("/"));
            var selectedPath=parentPath+"/"+html;

            if (!parentPath) parentPath="/";

            var parent=tree.getNode(parentPath);

            var nodeParams=new Object();
            nodeParams.forceReload=true;
            nodeParams.pathSelected=selectedPath;

            parent.expand(nodeParams);

            //reset tree nodes
            tree.nodes=new Object();
            }
        }



    function mgnlTreeResize()
        {
        for (var elem in mgnlTreeControls)
            {
            var tree=mgnlTreeControls[elem];
            mgnlTreeControls[elem].resize();
            }
        }





    //#################
    //### TreeColumn
    //#################

    function mgnlTreeColumn(width,name,isMeta,isLabel,isNodeDataValue,isNodeDataType)
        {
        this.width=width;
        this.name=name;
        this.isMeta=isMeta;
        this.isLabel=isLabel;
        this.isNodeDataValue=isNodeDataValue;
        this.isNodeDataType=isNodeDataType;
        }


    //#################
    //### TreeNode
    //#################

    function mgnlTreeNode(treeControl,id)
        {
        this.id = id;
        this.path = id;

        if (id.lastIndexOf("/")==0) this.parentId="/"; //parent is root
        else this.parentId=id.substring(0,id.lastIndexOf("/"));

        this.label=id.substring(id.lastIndexOf("/")+1);

        this.idPre=treeControl.name+"_"+id;
        this.repository=treeControl.repository;
        this.treeName=treeControl.name;
        this.tree=treeControl;

        if (document.getElementById(this.idPre+"_ItemType"))
            {
            this.itemType=document.getElementById(this.idPre+"_ItemType").value;
            }
        if (document.getElementById(this.idPre+"_IsActivated"))
            {
            if (document.getElementById(this.idPre+"_IsActivated").value=="true") this.isActivated=true;
            else this.isActivated=false;
            }
        if (document.getElementById(this.idPre+"_PermissionWrite"))
            {
            if (document.getElementById(this.idPre+"_PermissionWrite").value=="true") this.permissionWrite=true;
            else this.permissionWrite=false;
            }

        //html objects get lost, therefore use id and getElement on the float
        this.divMainId=this.idPre+"_DivMain";
        this.shifterId=this.idPre+"_Shifter";
        this.divSubId=this.idPre+"_DivSub";
        this.iconId=this.idPre+"_Icon";
        this.labelId=this.idPre+"_Column0Main";
        if(document.location.href.indexOf("?")!=-1)
                this.url=document.location.href.substring(0,document.location.href.indexOf("?"));
        else
            this.url=document.location.href;
    }

    mgnlTreeNode.prototype.getParent = function(){
        var parentPath=this.id.substring(0,this.id.lastIndexOf("/"));
        if (parentPath == ""){
            parentPath = this.tree.path; //root
        }
        return this.tree.getNode(parentPath);
    }

    mgnlTreeNode.prototype.getHttpRequest = function()
        {
        var httpReq;
        if (window.XMLHttpRequest) httpReq = new XMLHttpRequest();
        else if (window.ActiveXObject) httpReq = new ActiveXObject("Microsoft.XMLHTTP"); //IE/Windows ActiveX
        return httpReq;
        }

    mgnlTreeNode.prototype.httpRequest = function(params, callBackParams, callback)
        {
        /*
        //todo: clean up ... (e.g. isMeta, isLabel etc. to treeAction)

        * params is object of key/value pairs
        * possible keys:
        --------------------------------------------------------------------
        pathOpen: path to open
        //check! and remove? pathSelected: path to select -> does not work (js is not executed at remote scripting);
        --------------------------------------------------------------------
        * create and remove nodes
        createItemType: String; type of the item [NT_CONTENT | NT_CONTENTNODE | NT_NODEDATA]
        deleteNode: node to delete
        --------------------------------------------------------------------
        * save node data values
        * save node data types
        * save names (label) of data or nodes (rename)
        saveName: name of the item which has to be saved
        saveValue: value to be saved
        isMeta: boolean - nodeData 'saveName' is meta data
        isLabel: boolean -> move 'this.path' to 'saveValue' (rename)
        isNodeDataValue: boolean - item to save is value of node data 'saveName' ('config admin')
        isNodeDataType: boolean - item to save is type of node data 'saveName' ('config admin')
        --------------------------------------------------------------------
        * paste nodes
        treeAction: [ MOVE | COPY ]
        pathClipboard: path to move or copy //todo: mutliple selection
        pathSelected: path to move to
        pasteType: put pathOrigin [ ABOVE | BELOW | SUB ] of pathDestination
        --------------------------------------------------------------------
        * activate
        recursive: activate selected and sub nodes

        */

        var httpReq=this.getHttpRequest();
        if (httpReq)
            {
            var paramString = "treeMode=snippet";
            paramString+="&path="+this.path;
            paramString+="&browseMode="+this.tree.browseMode;
            paramString+="&mgnlCK="+mgnlGetCacheKiller();
            for (var elem in params)
                {
                if (params[elem] || params[elem]=="0") {
                    // ="0": createItemType; MAGNOLIA_NODE_DATA is 0
                    paramString+="&"+encodeURIComponent(elem)+"="+encodeURIComponent(unescape(params[elem])); //values seems to be passed escaped ...
                    }
                }
            mgnlDebug("node.httpRequest: paramString: " + paramString, "tree");
            // paramters need to be passed in body to allow utf8 encoding (query string is always ISO-88591)
                mgnlDebug("node.httpRequest: url: " + this.url, "tree");
            httpReq.open("POST",encodeURI(this.url),true);
            httpReq.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

            // register handler after the the request called after the request returned
            httpReq.onreadystatechange=function() {
               if (httpReq.readyState==4) {
                var returnText=httpReq.responseText;
                callback(callBackParams, returnText);
               }
            }

            httpReq.send(paramString);
            return;
            }

        else return;
        }

    mgnlTreeNode.prototype.shift = function()
        {
        var shifter=document.getElementById(this.shifterId);

        if (shifter && shifter.src.indexOf("EMPTY")==-1)
            {
            var divSub=document.getElementById(this.divSubId);
            if (divSub.style.display=="none") this.expand();
            else this.collapse();
            }
        }

    mgnlTreeNode.prototype.collapse = function()
        {
        mgnlDebug('node.collapse','tree');
        var divSub=document.getElementById(this.divSubId);
        if (divSub.style.display!="none")
            {
            divSub.style.display="none";
            var shifter=document.getElementById(this.shifterId);
            if (shifter) shifter.src=shifter.src.replace("COLLAPSE","EXPAND");

            if (this.tree.selectedNode.id.indexOf(this.id+"/")==0)
                {
                this.tree.selectedNodeReset();
                }
            }
        }



    mgnlTreeNode.prototype.expand = function(params)
        {
        mgnlDebug('node.expand','tree');
        /*
        * params is object of key/value pairs
        * see mgnlTreeNode.prototype.httpRequest
        */

        if (!params) params=new Object();

        var shifter=document.getElementById(this.shifterId);
        var divSub=document.getElementById(this.divSubId);

        if (!params.forceReload && shifter && shifter.src.indexOf("EMPTY")!=-1) return;
        if (params.forceReload || (divSub && divSub.style.display=="none"))
            {
            //todo: find out, why divSub is not available always ...
            if (divSub) divSub.style.display="block";
            if (shifter) shifter.src=shifter.src.replace("EXPAND","COLLAPSE");
            if (params.forceReload || divSub.innerHTML=="")
                {
                var left=0;
                if (document.getElementById(this.idPre+"_Column0Main"))
                    {
                    left=parseInt(document.getElementById(this.idPre+"_Column0Outer").style.paddingLeft);
                    if (left) left+=30;
                    else left=30;
                    }
                //todo: find out, why divSub is not available always ...
                if (divSub) divSub.innerHTML="<div class=mgnlTreeTextLoading style=padding-left:"+left+";>"+this.tree.strings.loading+"</div>";

                this.params=params;

                setTimeout("mgnlTreeDrawNodes('"+this.id+"','"+this.treeName+"');",10);
                }
            }
        }

    mgnlTreeDrawNodes = function (id,treeName)
        {
        var div=document.getElementById(treeName+"_"+id+"_DivSub");
        var tree=eval(treeName);

        var node=tree.getNode(id);
        var params=node.params;
        if (!params) params=new Object();

        var callBackParams=new Object();
        callBackParams.id = id;
        callBackParams.treeName = treeName;

        mgnlDebug('mglnTreeDrawNodes with treeName: ' + treeName, "tree");
        mgnlDebug('mglnTreeDrawNodes used params', "tree", params);
        mgnlDebug('mglnTreeDrawNodes used callBackParams', "tree", callBackParams);

        node.httpRequest(params, callBackParams, mgnlTreeDrawNodesCallback);

        }

/**
callback
**/
    mgnlTreeDrawNodesCallback = function (params, html) {

        var id = params.id;
        var treeName = params.treeName;

        var div=document.getElementById(treeName+"_"+id+"_DivSub");
        var tree=eval(treeName);

        if (div) div.innerHTML=html;

        if (id==tree.path){
            //reset tree
            tree.nodes=new Object();
            tree.selectedNode=tree.getNode(tree.path);
            tree.clipboardNode=null;
            tree.clipboardMethod=null;
        }

        html = mgnlAlertCallbackMessage(html);

        var selectPath=params.pathSelected;
        var selectNodePattern='<input type="hidden" id="mgnlSelectNode" value="';

        if (html.indexOf(selectNodePattern)==0)
            {
            mgnlDebug('mgnlTreeDrawNodesCallback', "tree");
            var tmp=html.substring(0,html.indexOf("\" />"));
            tmp=tmp.replace(selectNodePattern,"");

            if (tmp!="") selectPath=tmp;
            }

        if (selectPath)
            {
            mgnlDebug('selectPath: ' + selectPath, "tree")
            //tree.selectNode(tree.path);
            tree.selectNode(selectPath);
            }

        if (html=="")
            {
            var shifter=document.getElementById(tree.nodes[id].shifterId);
            if (shifter)
                {
                shifter.src=shifter.src.replace("COLLAPSE","EMPTY")
                }
            }

}

    function mgnlAlertCallbackMessage(html){
        var messagePattern='<input type="hidden" id="mgnlMessage" value="';

        // check if alert message was sent back
        if (html.indexOf(messagePattern) != -1){
            var start = html.indexOf(messagePattern);
            var end = html.indexOf("\" />", start);
            var msg = html.slice(start + messagePattern.length, end);
            alert(msg);
            html = html.slice(end);
        }
        return html;
    }