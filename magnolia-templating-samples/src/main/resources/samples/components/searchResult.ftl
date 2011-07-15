
[@cms.edit /]


<h4>${content.title!content.@name}</h4>
[#if content.text?has_content]
    <p>${content.text}</p>
[/#if]


[#if model.getSearchResult()?has_content]
    <div id="search-results" >
        <h4>Query Results for: "${model.query!"No query set yet"}"</h4>

        <ul>
            [#list model.getSearchResult() as resultItem]
                [#assign foundOnPage = cmsfn.page(resultItem)]
                <li><a href="${ctx.contextPath}${foundOnPage.@path}">${foundOnPage.title!foundOnPage.@name}</a><span>(found in Node: ${resultItem.@path})</span></li>
            [/#list]
        </ul>
    </div><!-- end search-results -->
[/#if]


[#include "/samples/includes/inc.searchForm.ftl" ]


<div>
  <h5>Display Paragraph Sources</h5>
  <ul>
     <li><a href="${ctx.contextPath}/.sources/components/searchResult.ftl">SearchResult paragraph</a></li>
  </ul>
</div><br />
