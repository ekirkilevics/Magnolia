package info.magnolia.templating.template;

import info.magnolia.templating.template.configured.ConfiguredParagraphAvailability;

import java.util.Map;


/**
 * Definition for a Area.
 *
 * @version $Id$
 */

public interface AreaDefinition extends TemplateDefinition {

    public abstract Map<String, ConfiguredParagraphAvailability> getAvailableParagraphs();

    public abstract Boolean getEnabled();

}