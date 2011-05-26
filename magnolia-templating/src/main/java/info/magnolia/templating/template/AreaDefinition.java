package info.magnolia.templating.template;

import info.magnolia.templating.template.configured.ConfiguredParagraphAvailability;

import java.util.Map;


public interface AreaDefinition {

    public abstract Map<String, ConfiguredParagraphAvailability> getAvailableParagraphs();

    public abstract Boolean getEnabled();

}