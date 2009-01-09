[#assign cms=JspTaglibs["cms-taglib"]]
[#assign cmsu=JspTaglibs["cms-util-taglib"]]
<div id="search-results" >
    <p>Query Results for <b>${ctx.query!}</b></p>
    [#if ctx.query?exists]
        [@cmsu.simpleSearch query="${ctx.query}" var="results" scope="request" /]

        [#if ctx.results?size = 0]
            <p>No results</p>
        [/#if]
        [#list ctx.results as item ]
            <div class="searchresult">
                <h4>${item.title}</h4>
                <a href="${ctx.contextPath}${item.@handle}.html">${item.title}</a>
            </div>

        [/#list]
    [/#if]
</div><!-- end search -->