
[#macro renderNavigation pageNode maxDepth depth=0 ]
    [#assign childPages = cmsfn.children(pageNode, "mgnl:content")!]

    [#if depth == 0]
        <li>
            <a href="${contextPath}${pageNode.@path}">${pageNode.title!pageNode.@name}</a>
            [#-- TODO <a href="${cmsfn.link(childPage)}">${childPage!childPage.@name}</a> --]
        </li>
    [/#if]

    [#if childPages?size!=0 && depth < maxDepth]
        [#list childPages as childPage]

            [#if (childPage.@path!=content.@path) && (childPage.@path!=cmsfn.parent(content).@path)]
                <li>
                    <a href="${contextPath}${childPage.@path}">${childPage.title!childPage.@name}</a>
                </li>
            [#else]
                <li class="selected">
                    <span>${childPage.title!childPage.@name}</span>
                </li>

                <ul class="second">
                    [#-- this should work: [#assign depth = depth+1] --]
                    [@renderNavigation childPage maxDepth depth+1 /]
                </ul>
            [/#if]

        [/#list]
    [/#if]
[/#macro]
