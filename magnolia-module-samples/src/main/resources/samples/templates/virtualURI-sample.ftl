<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
[#assign cms=JspTaglibs["cms-taglib"]]
[#assign cmsu=JspTaglibs["cms-util-taglib"]]
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
[#include "include/header.ftl"]
</head>

<body >

    <div >
    <p>
    Select a product:

        <a href="${ctx.contextPath}${content.@handle}/product1">Product 1</a>
        <a href="${ctx.contextPath}${content.@handle}/product2">Product 2</a>
        <a href="${ctx.contextPath}${content.@handle}/product3">Product 3</a>
    </p>
    </div><!-- end div -->

    You selected: ${ctx.parameters.product!"none yet"}
</body>
</html>