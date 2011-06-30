[@cms.edit /]

[#if content.title?has_content]
    <div style="font-weight:bold;font-size:16px;">${content.title}</div>
[/#if]

${content.text!}