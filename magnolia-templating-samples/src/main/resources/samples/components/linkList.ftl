[@cms.edit /]

[#if content.title?has_content]
<h3>${content.title}</h3>
[/#if]
<div class="linkList">
    <ul>
    [@cms.area name="links" /]
    </ul>
</div>