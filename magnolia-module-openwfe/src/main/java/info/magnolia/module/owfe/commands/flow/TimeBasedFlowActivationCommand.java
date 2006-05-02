package info.magnolia.module.owfe.commands.flow;

import info.magnolia.cms.util.AlertUtil;
import info.magnolia.module.owfe.MgnlConstants;
import info.magnolia.module.owfe.jcr.JCRFlowDefinition;
import info.magnolia.module.owfe.jcr.JCRPersistedEngine;

import java.util.HashMap;


import openwfe.org.engine.workitem.LaunchItem;
import openwfe.org.engine.workitem.StringAttribute;

import org.apache.commons.chain.Context;
import org.apache.log4j.Logger;

/**
 * The activation command which will launch a flow to do scheduled activation by "sleep" functionality of owfe
 * @author jackie
 *
 */
public class TimeBasedFlowActivationCommand extends AbstractFlowCommand {
    private static final String WEB_SCHEDULED_ACTIVATION = "webScheduledActivation";
    private static Logger log = Logger.getLogger(TimeBasedFlowActivationCommand.class);
    static final String[] parameters = {MgnlConstants.P_RECURSIVE, MgnlConstants.P_START_DATE, MgnlConstants.P_END_DATE, MgnlConstants.P_PATH};

    static final String XSLT = "<xsl:stylesheet version = '1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform' ><xsl:param name=\"p_startDate\" select=\"no\"/><xsl:param name=\"p_endDate\" select=\"no\"/><!-- Identity transformation --><xsl:template match=\"/ | @*|node()\">  <xsl:copy>    <xsl:apply-templates select=\"@*|node()\" />  </xsl:copy> </xsl:template>  <!--  <xsl:template match=\"set[@field='mailTo']\">  <xsl:copy>    <xsl:attribute name=\"a\">aaa</xsl:attribute>    <xsl:attribute name=\"util\"><xsl:value-of select=\"$p_startDate\"/></xsl:attribute>  </xsl:copy> </xsl:template> -->  <xsl:template match=\"sleep[@util='${startDate}']\">  <xsl:copy>    <xsl:attribute name=\"util\"><xsl:value-of select=\"$p_startDate\"/></xsl:attribute>  </xsl:copy> </xsl:template>  <xsl:template match=\"sleep[@util='${endDate}']\">  <xsl:copy>    <xsl:attribute name=\"util\"><xsl:value-of select=\"$p_endDate\"/></xsl:attribute>  </xsl:copy> </xsl:template>  </xsl:stylesheet> ";
    
    /**
     * List of the parameters that this command needs to run
     *
     * @return a list of string describing the parameters needed. The parameters should have a  mapping in this class.
     */
    public String[] getExpectedParameters() {
        return parameters;
    }


    public String getFlowName() {
        return WEB_SCHEDULED_ACTIVATION;
    }

    public void preLaunchFlow(Context context, HashMap params, JCRPersistedEngine engine, LaunchItem li) {
    	
        try {
        	
            // Retrieve parameters from caller
            String pathSelected = (String) params.get(MgnlConstants.P_PATH);
            String startDate = null;
            String endDate = null;
            String s = (String)params.get(MgnlConstants.P_START_DATE);
            if (s!=null)
             startDate = params.get(MgnlConstants.P_START_DATE).toString();
            
            s = (String)params.get(MgnlConstants.P_END_DATE);
            if (s!=null)
             endDate = params.get(MgnlConstants.P_END_DATE).toString();
            
            log.info("start date = " + startDate);
            log.info("end date = " + endDate);

            // set parameters for lanuching the flow
            li.setWorkflowDefinitionUrl(MgnlConstants.P_WORKFLOW_DEFINITION_URL);
            li.addAttribute(MgnlConstants.P_PATH, new StringAttribute(pathSelected));
            li.addAttribute(MgnlConstants.P_OK, MgnlConstants.ATT_FALSE);
            li.addAttribute(MgnlConstants.P_RECURSIVE, new StringAttribute( (params.get(MgnlConstants.P_RECURSIVE)).toString() ) );


//            li.addAttribute(MgnlConstants.P_START_DATE, new StringAttribute(params.get(MgnlConstants.P_START_DATE)));
//            li.addAttribute(MgnlConstants.P_END_DATE, new StringAttribute(params.get(MgnlConstants.P_END_DATE)));

            if (startDate!=null)
            	li.getAttributes().puts(MgnlConstants.P_START_DATE, startDate);
            if (endDate!=null)
            	li.getAttributes().puts(MgnlConstants.P_END_DATE, endDate);
            
            // Retrieve and add the flow definition to the LaunchItem
            String flowDef = new JCRFlowDefinition().getflowDefAsString(MgnlConstants.P_DEFAULT_SCHEDULEDACTIVATION_FLOW);
          //  flowDef = transform(flowDef, startDate, endDate);
            //log.info(flowDef);
            li.getAttributes().puts(MgnlConstants.P_DEFINITION, flowDef);

        } catch (Exception e) {
            log.error("can't launch flow", e);
            AlertUtil.setMessage(AlertUtil.getExceptionMessage(e));
        }
    }
    /*
    String transform(String source, String startDate, String endDate) throws Exception{
		StringWriter result = new StringWriter();
		StreamResult xml_result = new StreamResult(result);
		
		StreamSource xml_src = new StreamSource(new StringReader(source));
		StreamSource xslt_src = new StreamSource(new StringReader(XSLT));

		try {
			TransformerFactory tf = TransformerFactory.newInstance();
		Templates template = tf.newTemplates(xslt_src);
		Transformer t = template.newTransformer();

		t.setParameter("p_startDate", startDate);
		t.setParameter("p_endDate", endDate);

		t.transform(xml_src, xml_result);
		
		return xml_result.toString();

	} catch (Exception e) {
		//te.printStackTrace();
		log.error("can not do transform", e);
		throw e;
	} finally {
		// sr.getWriter().close();
	}
	
    }*/

}
