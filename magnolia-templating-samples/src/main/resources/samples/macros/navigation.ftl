

[#macro renderNavigation pageNode maxDepth=2 ]

    [#assign currentPage = cmsfn.page(content)]
    [#assign isSelected = (pageNode.@path == currentPage.@path)!false]

    [#if isSelected ]
        <li class="selected">
            <span>${pageNode.title!pageNode.@name}</span>
        </li>
    [#else]
        <li>
            <a href="${cmsfn.link(pageNode)}"><span>${pageNode.title!pageNode.@name}</span></a>
        </li>
    [/#if]

    [#if pageNode.@depth <  maxDepth]
        [#list cmsfn.children(pageNode, "mgnl:page") as childPage]
            [@renderNavigation childPage /]
        [/#list]
    [/#if]
    
[/#macro]
