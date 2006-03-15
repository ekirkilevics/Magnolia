package info.magnolia.module.owfe;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.jcr.ValueFactory;

import openwfe.org.ApplicationContext;
import openwfe.org.ServiceException;
import openwfe.org.engine.expool.PoolException;
import openwfe.org.engine.expressions.FlowExpression;
import openwfe.org.engine.expressions.FlowExpressionId;
import openwfe.org.engine.impl.expool.AbstractExpressionStore;
import openwfe.org.engine.impl.expool.ExpoolUtils;
import openwfe.org.xml.XmlCoder;
import openwfe.org.xml.XmlUtils;

import org.jdom.Document;
import org.jdom.Element;

public class JCRExpressionStore extends AbstractExpressionStore {

	private final static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(JCRExpressionStore.class.getName());

	//
	// CONSTANTS & co
	public final static String REPO_OWFE = "owfe";

	public final static String WORKSPACEID = "Expressions";

	public final static String WORKITEM_NODENAME = "expression";

	//
	// FIELDS
	HierarchyManager hm = null;

	//
	// CONSTRUCTORS

	public void init(final String serviceName,
			final ApplicationContext context, final java.util.Map serviceParams)
			throws ServiceException {
		super.init(serviceName, context, serviceParams);
		hm = ContentRepository.getHierarchyManager(REPO_OWFE, WORKSPACEID);
		if (hm == null)
			throw new ServiceException(
					"Can't get HierarchyManager Object for workitems repository");

	}

	private String convertId(String id) {
		return id.replace("|", "").replaceAll(":", ".");

	}

	// interface
	public void storeExpression(FlowExpression fe) throws PoolException {
		try {
			Content root = hm.getRoot();

			// Content ct = root.createContent("expression",
			// ItemType.EXPRESSION);
			String id = fe.getId().toParseableString();
			log.info("store expresion: expression id = " + id);
			String nid = convertId(id);
			Content ct = root.createContent(nid, ItemType.EXPRESSION);

			// set expressionId as attribte id
			ValueFactory vf = ct.getJCRNode().getSession().getValueFactory();
			String value = fe.getId().toParseableString();
			log.debug("id_value=" + value);
			ct.createNodeData("ID", vf.createValue(value));

			// convert to xml string
			Element encoded = XmlCoder.encode(fe);
			final org.jdom.Document doc = new org.jdom.Document(encoded);
			String s = XmlUtils.toString(doc, null);

			// store it as attribute value
			ct.createNodeData("value", vf.createValue(s));
			hm.save();

		} catch (Exception e) {
			log.error("store exception faled,", e);
		}

	}

	public void unstoreExpression(FlowExpression fe) throws PoolException {
		try {
			// get root
			Content ret = findExpression(fe.getId());
			if (ret != null) {
				ret.delete();
				hm.save();
			}

		} catch (Exception e) {
			log.error("unstore exception faled,", e);
		}

	}

	/**
	 * Find expression by id
	 * 
	 * @param fei
	 *            flow expression id
	 * @return
	 * @throws Exception
	 */
private Content findExpression(FlowExpressionId fei) throws Exception{
		Content ret = null;
		String s_fei = fei.toParseableString();
		
		Content root = hm.getRoot();
		log.info("load expresion, expression id = " + s_fei);
		ret = root.getContent(convertId(s_fei));
		if (ret == null) { // if not found the id directly
			Collection c = root.getChildren(ItemType.EXPRESSION);
			Iterator it = c.iterator();
			while (it.hasNext()) {
				Content ct = (Content) it.next();
				String name = ct.getName();
				String sid = ct.getNodeData("ID").getString();
				FlowExpressionId id = null;
				// compare the expression id
				try {
					id = FlowExpressionId.fromParseableString(sid);
				} catch (Exception e) {
					log.error("parse expresion id failed", e);
					ct.delete();
					hm.save();
					continue;
				}

				if (id.equals(fei))// find the target one, just load it
				{
					ret = ct;
					break;
				}

			}
			
		}
		return ret;
}
	public FlowExpression loadExpression(FlowExpressionId fei)
			throws PoolException {
		FlowExpression ret_fe = null;
		String s_fei = "";
		try {
			Content ret = findExpression(fei);
			if (ret != null) {
				InputStream s = ret.getNodeData("value").getStream();
				final org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();

				Document doc = builder.build(s);
				ret_fe = (FlowExpression) XmlCoder.decode(doc);
				return ret_fe;
			}

		} catch (Exception e) {
			log.error("load exception faled,", e);
		}
		throw new PoolException("can not get this expression (id=" + s_fei
				+ ")");
		// return null;

	}

	public Iterator contentIterator(Class assignClass) {
		ArrayList ret = new ArrayList();
		try {
			Content root = hm.getRoot();
			Collection c = root.getChildren(ItemType.EXPRESSION);

			Iterator it = c.iterator();
			while (it.hasNext()) {
				Content ct = (Content) it.next();

				InputStream s = ct.getNodeData("value").getStream();
				final org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();

				Document doc = builder.build(s);
				FlowExpression fe = (FlowExpression) XmlCoder.decode(doc);
				if (!ExpoolUtils.isAssignableFromClass(fe, assignClass)) {
					// log.debug
					// ("contentIterator() not
					// assignClass.getName());
					continue;
				}
				ret.add(fe);
			}

			return ret.iterator();

		} catch (Exception e) {
			log.error("exception:" + e);
			return ret.iterator();
		}
	}

	public int size() {
		try {
			Content root = hm.getRoot();
			Collection c = root.getChildren(ItemType.EXPRESSION);
			// @fix it
			return c.size();
		} catch (Exception e) {
			log.error("exception:" + e);
			return 0;
		}

	}

	public boolean doTest(String s) throws Exception {
		// test write content with expresion id
		Content ct = null;
		Content root = hm.getRoot();
		// log.info("store expresion: expression id = "+"kjakf||dafa");
		// ct = root.createContent("kjakf||dafa",
		// ItemType.EXPRESSION);
		// log.info("store expresion: expression id = "+"0.0.0");
		// ct = root.createContent("0.0.0",
		// ItemType.EXPRESSION);
		// log.info("store expresion: expression id = "+"a 0 0");
		// ct = root.createContent("a 0 ",
		// ItemType.EXPRESSION);
		log.info("store expresion: expression id = " + s);
		ct = root.createContent(s, ItemType.EXPRESSION);

		hm.save();

		return true;

	}

}
