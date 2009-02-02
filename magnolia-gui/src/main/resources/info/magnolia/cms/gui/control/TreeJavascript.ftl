<script type="text/javascript">
    // <![CDATA[
    // create tree instance
    var ${tree.javascriptTree}=new mgnlTree(
        '${tree.repository}',
        '${tree.path}',
        '${tree.javascriptTree}',
        ${tree.height},
        '${tree.name}',
        ${tree.browseMode?string},
        '${tree.functionBar.javascriptName}'
    );

    // register the control
    mgnlTreeControls['${tree.javascriptTree}']=${tree.javascriptTree};

    // add the columns
    <#list columns as tc>
        ${tree.javascriptTree}.columns[${columns?seq_index_of(tc)}]=new mgnlTreeColumn(
            ${tc.width},
            '${tc.name?default('column'+ columns?seq_index_of(tc))}',
            ${tc.isMeta?string},
            ${tc.isLabel?string},
            ${tc.isNodeDataValue?string},
            ${tc.isNodeDataType?string}
        );
    </#list>


    ${tree.javascriptTree}.selectNode('${tree.pathSelected?default("")}');

    <#if menu?exists && (menu.menuItems?size >0)>
        ${menu.javascript}
    </#if>

    <#if !tree.browseMode && tree.functionBar?exists>
        ${tree.functionBar.javascript}
    </#if>

    // make the menu avaiable for the tree control
    ${tree.javascriptTree}.menu = ${menu.name};

    <#if message?exists>
        MgnlDHTMLUtil.addOnLoad(function(){alert('${message}')});
    </#if>

    MgnlDHTMLUtil.addOnResize(function(){${tree.javascriptTree}.resize()});
    MgnlDHTMLUtil.addOnLoad(function(){${tree.javascriptTree}.resize()});

    // ]]>
</script>