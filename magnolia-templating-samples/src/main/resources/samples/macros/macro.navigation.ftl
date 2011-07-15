
[#macro renderNavigation pageNode maxDepth depth=0 ]

    [#-- Is top page of the site structure -> rendering the top page on same navigation level as its sub-pages--]
    [#assign isRootPage = (pageNode.@path==cmsfn.rootPage(content).@path)!false]
    [#if isRootPage && depth == 0]
        [#if pageNode.@path != content.@path]
            <li>
                <a href="${cmsfn.link(pageNode)}.html">${pageNode.title!pageNode.@name}</a>
            </li>
        [#else]
            <li class="selected">
                <span>${pageNode.title!pageNode.@name}</span>
            </li>
        [/#if]
    [/#if]


    [#assign childPages = cmsfn.children(pageNode, "mgnl:content")!]
    [#-- Has child pages AND is not deeper as defined in max allowed depth. --]
    [#if childPages?size!=0 && depth < maxDepth]
        [#list childPages as childPage]

            [#assign isSelected = (childPage.@path == cmsfn.page(content).@path)!false]
            [#assign isSelectedParent = (childPage.@path == cmsfn.parent(content).@path)!false]

            [#if isSelected || isSelectedParent]
                <li class="selected">
                    [#if isSelected]
                        <span>${childPage.title!childPage.@name}</span>
                    [#else]
                        <a href="${cmsfn.link(childPage)}.html"><span>${childPage.title!childPage.@name}</span></a>
                    [/#if]
                </li>
                <ul class="second">
                    [@renderNavigation childPage maxDepth depth+1 /]
                </ul>
            [#else]

                <li>
                    <a href="${cmsfn.link(childPage)}.html">${childPage.title!childPage.@name}</a>
                </li>
            [/#if]

        [/#list]
    [/#if]

[/#macro]
