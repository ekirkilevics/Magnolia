


<h3>${content.title!content.@name}</h3>
[#if content.text?has_content]
    <p>${cmsfn.decode(content).text}</p>
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

<h4>New Search</h4>
[#include "/samples/includes/searchForm.ftl" ]



<h4>Display Component Sources</h4>
<ul>
    <li><a href="${ctx.contextPath}/.sources/components/searchResult.ftl">SearchResult component</a></li>
</ul>

