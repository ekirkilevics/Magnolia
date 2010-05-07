<style type="text/css">

    <#list 0..(columns?size-1) as i>
        .${tree.javascriptTree}CssClassColumn${i}{
            position:absolute;
            left:0px;
            clip:rect(0, 0, 100px, 0);
            cursor:default;
            padding-left:8px;
        }
    </#list>

</style>

<!-- background image -->
<div class="mgnlListHeaderBackground">

<!-- delimiters -->
    <!-- move icons -->
    <#list 1..(columns?size-1) as i>
        <div onmousedown="${tree.javascriptTree}.dragColumnStart(this,${i});" class="mgnlListColumnResizer" id="${tree.javascriptTree}ColumnResizer${i}"></div>
    </#list>

    <!-- column title -->
    <#list 0..(columns?size-1) as i>
        <div id="${tree.javascriptTree}ColumnHeader${i}" class="${tree.javascriptTree} mgnlListHeader ${tree.javascriptTree}CssClassColumn${i}">${tree.getColumns()[i].title}<!-- ie --></div>
    </#list>

</div>

<!-- column resizer line -->
<div id="${tree.javascriptTree}_ColumnResizerLine" style="position:absolute;top:0px;left:-100px;visibility:hidden;width:1px;height:${tree.height}px;background-color:#333333;z-index:490;">
</div>

<!-- content -->
<div id="${tree.javascriptTree}_${tree.path}_DivMain" onclick="${tree.javascriptTree}.mainDivReset();" oncontextmenu="${tree.javascriptTree}.menuShow(event);return false;" class="${treeCssClass}">

    <#if (columns?size > 1)>
        <#list 1..(columns?size-1) as i>
            <div id="${tree.javascriptTree}ColumnLine${i}" class="mgnlListColumnLine"> </div>
        </#list>
    </#if>

    <div class="mgnlDialogSpacer" style="height:8px;width:8px;" ><!-- ie --></div>

    <div id="mgnlTreeControl_${tree.path}_DivSub">
