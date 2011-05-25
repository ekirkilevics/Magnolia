package info.magnolia.templating.definition;

import java.util.Map;


public interface AreaDefinition {

    public abstract Map<String, ParagraphAvailabilityImpl> getAvailableParagraphs();

    public abstract Boolean getEnabled();

}