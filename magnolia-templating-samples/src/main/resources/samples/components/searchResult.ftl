
[@cms.edit /]


<h3>${content.title!content.@name}</h3>
[#if content.text?has_content]
    <p>${content.text}</p>
[/#if]


[#if model.searchResult?has_content]
    <div id="search-results" >
        <h3>Query Results for: "${model.query!"No query set yet"}"</h3>

        <ul>
            [#list model.searchResult as resultItem]
                [#assign foundOnPage = cmsfn.page(resultItem)]
                <li><a href="${ctx.contextPath}${foundOnPage.@path}">${foundOnPage.title!foundOnPage.@name}</a><span>(found in Node: ${resultItem.@path})</span></li>
            [/#list]
        </ul>
    </div><!-- end search-results -->
[/#if]


[#include "/samples/includes/inc.searchForm.ftl" ]



<h4>Display Paragraph Sources</h4>
<ul>
    <li><a href="${ctx.contextPath}/.sources/components/searchResult.ftl">SearchResult paragraph</a></li>
</ul>

