package info.magnolia.httptest;

import org.apache.commons.lang.StringUtils;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HTMLElement;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;


/**
 * A simple basic test case to see httpunit working.
 * @author fgiust
 * @version $Revision: $ ($Author: $)
 */
public class ButtonTest extends HttpUnitTestCase {

    /**
     * @see info.magnolia.httptest.HttpUnitTestCase#getJspName()
     */
    public String getJspName() {
        return "button.jsp";
    }

    /**
     * @see info.magnolia.httptest.HttpUnitTestCase#doTest(java.lang.String)
     */
    public void doTest(String jspName) throws Exception {
        WebRequest request = new GetMethodWebRequest(jspName);
        WebResponse response = runner.getResponse(request);

        String textContent = response.getText();

        if (log.isDebugEnabled()) {
            log.debug(textContent);
        }

        // this contains the button
        HTMLElement container = response.getElementWithID("button");

        // an example of checking the button label using html dom
        assertEquals("Bad or missing button label.", "value", container.getText());

        // and check nobr and nbsp using text
        assertFalse("nobr tag is not a standard html tag", StringUtils.contains(textContent, "nobr"));
        assertFalse("If a style is not set, the style attribute should not be written", StringUtils.contains(
            textContent,
            "style=\"\""));
        assertFalse("nbsp should not be used for padding", StringUtils.contains(textContent, "&nbsp;"));

    }
}
