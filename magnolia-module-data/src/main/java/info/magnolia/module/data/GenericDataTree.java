package info.magnolia.module.data;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.Rule;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.AdminTreeMVCHandler;
import info.magnolia.module.admininterface.DialogHandlerManager;
import info.magnolia.module.admininterface.dialogs.ConfiguredDialog;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class GenericDataTree extends AdminTreeMVCHandler {

    private String dialogName;

    public GenericDataTree(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    protected void initialize() {
        super.initialize();
        setConfiguration(new DialogBasedTreeConfig((ConfiguredDialog) DialogHandlerManager
            .getInstance()
            .getDialogHandler(getDialogName(), request, response), getRepository()));
    }

    // TODO: check if this really works [cho]
    public Syndicator getActivationSyndicator(String path) {
        /*
         * Here rule defines which content types to collect, its a resposibility of the caller ro set this, it will be
         * different in every hierarchy, for instance - in website tree recursive activation : rule will allow
         * mgnl:contentNode, mgnl:content and nt:file - in website tree non-recursive activation : rule will allow
         * mgnl:contentNode and nt:file only
         */
        Rule rule = new Rule();
        rule.addAllowType(ItemType.CONTENTNODE.getSystemName());
        rule.addAllowType(ItemType.CONTENT.getSystemName());
        rule.addAllowType(ItemType.NT_METADATA);
        rule.addAllowType(ItemType.NT_RESOURCE);

        Syndicator syndicator = (Syndicator) FactoryUtil.getInstance(Syndicator.class);
        syndicator.init(MgnlContext.getUser(), this.getRepository(), ContentRepository.getDefaultWorkspace(this
            .getRepository()), rule);

        return syndicator;
    }

    public String getDialogName() {
        return dialogName;
    }

    public void setDialogName(String dialogName) {
        this.dialogName = dialogName;
    }
}
