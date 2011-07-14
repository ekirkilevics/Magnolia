[#-------------- HowTo Paragraph --------------]


[@cms.edit /]

<h4>${content.title!content.@name}</h4>

Text: ${content.text!("No text defined yet")}

<div>
    [#if content.imageurl?has_content]
        Image: <img src="${ctx.contextPath}${content.imageurl}" />
    [#else]
        Image: No image uploaded yet.
    [/#if]
</div>

<div>
    [#if content.date?has_content]
        The date you specified: ${content.date?string("EEEE, d. MMMM yyyy")}
    [#else]
        No date specified yet.
    [/#if]
</div>

<div>
    <ul>Parameters set by filter:
        <li>using model: ${model.filterAttribute} </li>
        <li>get from request: ${ctx.sampleFilter}</li>
    </ul>
</div>


[#include "/samples/includes/inc.searchForm.ftl" ]


<div>
  <h5>Display Paragraph Sources</h5>
  <ul>
     <li><a href="${ctx.contextPath}/.sources/components/howTo.ftl">HowTo paragraph</a></li>
  </ul>
</div><br />
