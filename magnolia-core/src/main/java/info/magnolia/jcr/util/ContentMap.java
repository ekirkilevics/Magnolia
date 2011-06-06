package info.magnolia.jcr.util;

import info.magnolia.link.LinkException;
import info.magnolia.link.LinkTransformerManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContentMap implements Map<String, Object> {

    private final static Logger log = LoggerFactory.getLogger(ContentMap.class);

    private final Node content;

    /**
     * Represents getters of the node itself.
     */
    private final List<String> attributeNames = new ArrayList<String>();

    public ContentMap(Node content) {
        if (content == null) {
            throw new NullPointerException("ContentMap doesn't accept null content");
        }

        this.content = content;

        // one might want to cache property names on instantion, however this would not work as other sessions might add/remove props at any time.

        // apache commons? there must be the code doing the same elsewhere.
        // cache method/prop names

        // FIXME don't support this
        for (Method m : content.getClass().getMethods()) {
            if (m.getParameterTypes().length > 0) {
                continue;
            }
            if (m.getName().startsWith("get")) {
                attributeNames.add(StringUtils.uncapitalize(m.getName().substring(3)));
            }

            if (m.getName().startsWith("is")) {
                attributeNames.add(StringUtils.uncapitalize(m.getName().substring(2)));
            }
        }
    }

    @Override
    public boolean containsKey(Object key) {

        String strKey = convertKey(key);

        if (!isValidKey(strKey)) {
            return false;
        }

        if (isSpecialProperty(strKey)) {
            return true;
        }

        try {
            return content.hasProperty(strKey);
        } catch (RepositoryException e) {
            // ignore, most likely invalid name
        }
        return false;
    }

    private String convertKey(Object key) {
        if (key == null) {
            return null;
        }
        if (key instanceof String) {
            return (String) key;
        }
        // TODO: should we really be so lenient? Strictly speaking this key is not valid if it has to be converted to string ... even if the toString() produces correct result.
        return key.toString();
    }

    private boolean isValidKey(String strKey) {
        return !StringUtils.isBlank(strKey);
    }

    private boolean isSpecialProperty(String strKey) {
        // TODO @nodeType @name, @path @level

        if (!strKey.startsWith("@")) {
            return false;
        }
        strKey = convertDeprecatedProps(strKey);
        return attributeNames.contains(strKey.substring(1));
    }

    private String convertDeprecatedProps(String strKey) {
        // in the past we allowed both lower and upper case notation ...
        if ("@UUID".equals(strKey) || "@uuid".equals(strKey)) {
            return "identifier";
        } else if ("@handle".equals(strKey)) {
            return "path";
        }
        return StringUtils.removeStart(strKey, "@");
    }

    @Override
    public Object get(Object key) {
        String keyStr;
        try {
            keyStr = (String) key;
        } catch (ClassCastException e) {
            throw new ClassCastException("ContentMap accepts only String as a parameters, provided object was of type " + (key == null ? "null" : key.getClass().getName()));
        }

        Object prop = getNodeProperty(keyStr);
        if (prop == null) {
            keyStr = convertDeprecatedProps(keyStr);
            return getNodeAttribute(keyStr);
        }
        return prop;
    }

    private Object getNodeAttribute(String keyStr) {

        try {
            return content.getClass().getMethod("get" + StringUtils.capitalize(keyStr), null).invoke(content, null);
        } catch (IllegalArgumentException e) {
            // ignore
        } catch (SecurityException e) {
            // ignore
        } catch (IllegalAccessException e) {
            // ignore
        } catch (InvocationTargetException e) {
            // ignore
        } catch (NoSuchMethodException e) {
            try {
                return content.getClass().getMethod("is" + StringUtils.capitalize(keyStr), null).invoke(content, null);
            } catch (IllegalArgumentException e1) {
                // ignore
            } catch (SecurityException e1) {
                // ignore
            } catch (IllegalAccessException e1) {
                // ignore
            } catch (InvocationTargetException e1) {
                // ignore
            } catch (NoSuchMethodException e1) {
                // ignore
            }
        }
        return null;
    }

    private Object getNodeProperty(String keyStr) {
        try {
            // TODO: do we care about assets or just about plain binaries?
            // philipp: not yet
            if (isAsset(keyStr)) {
                return getAssetNode(keyStr);
            }
            Property prop = content.getProperty(keyStr);
            if (prop == null) {
                // no such property exists ... shouldn't exception be thrown instead?
                return null;
            }
            int type = prop.getType();
            if (type == PropertyType.DATE) {
                return prop.getDate();
            } else if (type == PropertyType.BINARY) {
                // this will actually never happen ... plus DAM is naming binary assets differently
                // xxx = name of control ... xxxBinary or xxxDMS or xxxYYY for asset source
                // and on top of that we pbly return binaries as subnodes now
            } else if (prop.isMultiple()) {

                Value[] values = prop.getValues();

                String[] valueStrings = new String[values.length];

                for (int j = 0; j < values.length; j++) {
                    try {
                        valueStrings[j] = values[j].getString();
                    } catch (RepositoryException e) {
                        log.debug(e.getMessage());
                    }
                }

                return valueStrings;
            } else {
                try {
                    return info.magnolia.link.LinkUtil.convertLinksFromUUIDPattern(prop.getString(), LinkTransformerManager.getInstance().getBrowserLink(content.getPath()));
                } catch (LinkException e) {
                    log.warn("Failed to parse links with from " + prop.getName(), e);
                }
            }
            // don't we want to honor other types (e.g. numbers? )
            return prop.getString();

        } catch (PathNotFoundException e) {
            // ignore, property doesn't exist
        } catch (RepositoryException e) {
            log.warn("Failed to retrieve {} on {} with {}", new Object[] { keyStr, content, e.getMessage() });
        }
        return null;
    }

    private boolean isAsset(String keyStr) {
        // does the DAM know?
        return false;
    }

    private Node getAssetNode(String keyStr) throws RepositoryException {
        // get the DAM and have it handle this
        return null;
    }

    @Override
    public int size() {
        try {
            return (int) (content.getProperties().getSize() + attributeNames.size());
        } catch (RepositoryException e) {
            // ignore ... no rights to read properties.
        }
        return attributeNames.size();
    }

    @Override
    public Set<String> keySet() {
        Set<String> keys = new HashSet<String>();
        try {
            PropertyIterator props = content.getProperties();
            while (props.hasNext()) {
                keys.add(props.nextProperty().getName());
            }
        } catch (RepositoryException e) {
            // ignore - has no access
        }
        for (String name : attributeNames) {
            keys.add(name);
        }
        return keys;
    }

    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        throw new UnsupportedOperationException("Entry collections are not supported");
    }

    @Override
    public Collection<Object> values() {
        throw new UnsupportedOperationException("Value collections are not supported");
    }

    @Override
    public boolean containsValue(Object arg0) {
        throw new UnsupportedOperationException("Value checks are not supported");
    }

    @Override
    public boolean isEmpty() {
        // can never be empty because of the node props themselves (name, uuid, ...)
        return false;
    }

    @Override
    public void clear() {
        // ignore, read only
    }

    @Override
    public Object put(String arg0, Object arg1) {
        // ignore, read only
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> arg0) {
        // ignore, read only
    }

    @Override
    public Object remove(Object arg0) {
        // ignore, read only
        return null;
    }
}
