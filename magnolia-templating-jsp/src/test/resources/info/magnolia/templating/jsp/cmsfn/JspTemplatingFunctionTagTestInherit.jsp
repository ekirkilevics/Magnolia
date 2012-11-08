<%@ taglib prefix="cmsfn" uri="http://magnolia-cms.com/taglib/templating-components/cmsfn" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
 <head/>

<body>
<div id="1">
    res=${(cmsfn:inherit(content, 'paragraphs'))['@path']}
</div>
<div id="2">
    res=${(cmsfn:inherit(content, ''))['@path']}
</div>
<div id="3">
    res=${(cmsfn:inherit(content, null))['@path']}
</div>

</body>

</html>
