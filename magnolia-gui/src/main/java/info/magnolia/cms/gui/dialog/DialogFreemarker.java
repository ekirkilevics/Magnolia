package info.magnolia.cms.gui.dialog;

import freemarker.template.TemplateException;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.gui.control.ControlImpl;
import info.magnolia.cms.gui.control.FreemarkerControl;
import info.magnolia.cms.gui.dialog.DialogBox;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.NodeDataUtil;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Magnolia dialog that renders by a freemarker template. There are two main properties for the dialog:<br/> <table>
 * <tr>
 * <td>path (required)</td>
 * <td>Path to freemarker template: if path starts with "classpath:" the template will be searched in classpath,
 * otherwise in filesystem</td>
 * </tr>
 * <tr>
 * <td>multiple</td>
 * <td>true / false. This property gives support to multiple field values storage.</td>
 * </tr>
 * </table> The dialog passes some parameters to freemarker template: <table>
 * <tr>
 * <td>name</td>
 * <td>Dialog / field name</td>
 * </tr>
 * <tr>
 * <td>value</td>
 * <td>Field value (multiple = false)</td>
 * </tr>
 * <tr>
 * <td>values</td>
 * <td>field values (multiple = true)</td>
 * </tr>
 * <tr>
 * <td>request</td>
 * <td>current HttpServletRequest</td>
 * </tr>
 * <tr>
 * <td>configuration</td>
 * <td>Map of dialog configuration. This allows to pass to template complex dialog configuration.<br/> Eg.
 *
 * <pre>
 * -+ Dialog node
 *  |-- property 1 = value 1
 *  |-+ subnode1
 *  | |-- property 11 = value 11
 *  | |-- property 12 = value 12
 *  | |-+ subnode 11
 *  |   |-- property 111 = value 111
 *  |
 *  |-- property 2 = value 2
 *
 * The map will contain:
 * configuration = Map {
 *    "property1" = "value1",
 *    "subnode1"  = Map {
 *        "property11" = "value11",
 *        "property12" = "value12",
 *        "subnode11"  =  Map {
 *            "property111" = "value111"
 *        }
 *    },
 *    "property2" = "value2"
 * }
 * </pre>
 *
 * </td>
 * </tr>
 * </table>
 * @author Manuel Molaschi
 * @version $Id: $
 */
public class DialogFreemarker extends DialogBox {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(DialogFreemarker.class);

    private Map configuration;

    /**
     * Empty constructor should only be used by DialogFactory.
     */
    protected DialogFreemarker() {
    }

    /**
     * {@inheritDoc}
     */
    protected List readValues() {
        List values = new ArrayList();
        if (this.getWebsiteNode() != null) {
            try {
                // cycles on website content node to get multiple value
                int size = this.getWebsiteNode().getContent(this.getName()).getNodeDataCollection().size();
                for (int i = 0; i < size; i++) {
                    NodeData data = this.getWebsiteNode().getContent(this.getName()).getNodeData("" + i);
                    values.add(data.getString());
                }
            }
            catch (PathNotFoundException e) {
                // not yet existing: OK
            }
            catch (RepositoryException re) {
                log.error("can't set values", re);
            }
        }
        return values;
    }

    /**
     * Get a recursive map view of a content node
     * @param node content node
     * @return recursive map view on content node properties and children
     * @throws PathNotFoundException
     * @throws RepositoryException
     * @throws AccessDeniedException
     */
    protected Map getSubNodes(Content node) throws RepositoryException, AccessDeniedException {
        Map values = new HashMap();

        // cycles on properties and stores them in map
        Collection properties = node.getNodeDataCollection();
        if (properties != null && properties.size() > 0) {
            Iterator propertiesIt = properties.iterator();
            while (propertiesIt.hasNext()) {
                NodeData property = (NodeData) propertiesIt.next();
                values.put(property.getName(), NodeDataUtil.getValueObject(property));
            }
        }

        // cycle on children
        Collection children = node.getChildren(ItemType.CONTENT.getSystemName());
        if (children != null && children.size() > 0) {
            Iterator childrenIt = properties.iterator();
            while (childrenIt.hasNext()) {
                Content child = (Content) childrenIt.next();
                // gets sub map
                Map subValues = getSubNodes(child);
                // stores it in map
                values.put(child.getName(), subValues);
            }
        }
        return values;
    }

    /**
     * {@inheritDoc}
     */
    public void init(HttpServletRequest request, HttpServletResponse response, Content websiteNode, Content configNode)
        throws RepositoryException {
        super.init(request, response, websiteNode, configNode);
        configuration = this.getSubNodes(configNode);
    }

    /**
     * {@inheritDoc}
     */
    public void drawHtml(Writer out) throws IOException {
        Map parameters = new HashMap();
        parameters.put("name", this.getName());
        parameters.put("value", this.getValue());
        parameters.put("values", this.getValues());
        parameters.put("request", this.getRequest());
        parameters.put("configuration", this.configuration);

        String path = this.getConfigValue("path");

        this.drawHtmlPre(out);
        try {
            FreemarkerControl control = new FreemarkerControl("multiple".equals(this.getConfigValue("valueType"))
                ? ControlImpl.VALUETYPE_MULTIPLE
                : ControlImpl.VALUETYPE_SINGLE);
            control.setType(this.getConfigValue("type", PropertyType.TYPENAME_STRING));
            control.setName(this.getName());
            control.drawHtml(out, path, parameters);
        }
        catch (TemplateException ex) {
            log.error("Error processing dialog template:", ex);
            throw new NestableRuntimeException(ex);
        }
        this.drawHtmlPost(out);
    }
}