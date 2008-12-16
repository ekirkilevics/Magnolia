[#assign cms=JspTaglibs["cms-taglib"]]
[#assign cmsu=JspTaglibs["cms-util-taglib"]]
<div id="search-results" >
    <p>Query Results for <b>${ctx.parameters.query}</b></p>
    [#if ctx.parameters.query?exists]
        [@cmsu.simpleSearch query="${ctx.parameters.query}" var="results" /]
        ${ctx.getAttribute("results")}
        [#if ctx.parameters.results?size = 0]
            <p>No results</p>
        [/#if]
        [#list ctx.parameters.results as item ]

           <p>
                [@cmsu.searchResultSnippet query="${ctx.parameters.query}" page="${item}" /]
            </p>
            <a href="${ctx.contextPath}${item.handle}.html">${item.title}</a>

        [/#list]
    [/#if]
</div><!-- end search -->