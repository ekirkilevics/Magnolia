package info.magnolia.module.delta;

import info.magnolia.module.InstallContext;

/**
 * @author philipp
 * @version $Id$
 *
 */
public class SamplesBootstrapTask extends IsInstallSamplesTask {

    public SamplesBootstrapTask() {
        super("Bootstrap samples", "Bootstraps samples content", new BootstrapResourcesTask("",""){
            protected boolean acceptResource(InstallContext ctx, String name) {
                final String moduleName = ctx.getCurrentModuleDefinition().getName();
                return name.startsWith("/mgnl-bootstrap-samples/" + moduleName + "/") && name.endsWith(".xml");
            }
        });
    }

}