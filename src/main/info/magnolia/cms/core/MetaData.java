/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */






package info.magnolia.cms.core;



import org.apache.log4j.Logger;

import javax.jcr.*;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.Date;

import info.magnolia.cms.beans.config.ItemType;


/**
 * Date: Apr 28, 2003
 * Time: 11:20:59 AM
 * @author Sameer Charles
 * @author Marcel Salathe
 * @version 1.1
 */



public class MetaData {



    private static Logger log = Logger.getLogger(MetaData.class);

    /**
     * Top level atoms viewed as metadata of the specified content
     * these must be set by the authoring system itself, but could be
     * changed via custom templates if neccessary.
     * */
    public static final String TITLE = "title";
    public static final String CREATION_DATE = "creationdate";
    public static final String LAST_MODIFIED = "lastmodified";
    public static final String LAST_ACTION = "lastaction";
    public static final String AUTHOR_ID = "authorid";
    public static final String ACTIVATOR_ID = "activatorid";
    public static final String START_TIME = "starttime";
    public static final String END_TIME = "endtime";
    public static final String TEMPLATE = "template";
    public static final String TEMPLATE_TYPE = "templatetype";
    public static final String ACTIVATED = "activated";
	public static final String SEQUENCE_POS = "sequenceposition";

    public static final String ACTIVATION_INFO = ".activationInfo";
    public static final String DEFAULT_META_NODE = "MetaData";

	public static final long SEQUENCE_POS_COEFFICIENT=1000;

    /* meta data node */
    private Node node;


    /**
     * Package private constructor
     *
     * @param workingNode current <code>Node</code> on which <code>MetaData</code> is requested
     */
    MetaData (Node workingNode) {
        this.setMetaNode(workingNode, DEFAULT_META_NODE);
    }



    /**
     * constructor
     *
     * @param workingNode current <code>Node</code> on which <code>MetaData</code> is requested
     * @param nodeName under which this data is saved
     */
    MetaData (Node workingNode, String nodeName) {
        this.setMetaNode(workingNode, nodeName);
    }



    private void setMetaNode(Node workingNode, String name) {
        try {
            this.node = workingNode.getNode(name).getNode("jcr:content");
        } catch (PathNotFoundException e) {
            try {
                this.node = workingNode.addNode(name,ItemType.getSystemName(ItemType.NT_FILE));
                this.node = this.node.addNode("jcr:content","nt:unstructured");
                this.node.setProperty("Data",name);
            } catch (RepositoryException re) {
                log.error("Failed to create meta data node - "+name);
                log.error(re.getMessage(), re);
            }
        } catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
    }



    public PropertyIterator getProperties() {
        try {
            return this.node.getProperties();
        } catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        return null;
    }



    /**
     * <p>part of metadata, same as name of actual storage node<br>
     * this value is unique at the hierarchy level context</p>
     *
     * @return String value of the requested metadata
     */
    public String getLabel() {
        try {
            return this.node.getName();
        } catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        return "";
    }



    /**
     * <p>part of metadata , could be used as html header</p>
     *
     * @return String value of the requested metadata
     */
    public String getTitle() {
        try {
            return this.node.getProperty(TITLE).getString();
        } catch (PathNotFoundException ee) {
            return "";
        }
        catch (RepositoryException re) {return ""; }
    }



    /**
     * <p>part of metadata, could be used as html header</p>
     *
     * @param value
     */
    public void setTitle(String value) {
        try {
            this.node.getProperty(TITLE).setValue(value);
	    } catch (PathNotFoundException ee) {
            try {
                this.node.setProperty(TITLE,value);
			} catch (RepositoryException e) {log.error("Failed to set title - "+value);}
        } catch (RepositoryException re) {log.error("Failed to set title - "+value);}
    }



    /**
     * <p>part of metadata, adds creation date of the current node</p>
     *
     */
    public void setCreationDate() {
        GregorianCalendar creationDate = new GregorianCalendar(TimeZone.getDefault());
        try {
            this.node.getProperty(CREATION_DATE).setValue(creationDate);
        } catch (PathNotFoundException ee) {
            try {
                this.node.setProperty(CREATION_DATE, creationDate);
            } catch (RepositoryException e ) {log.warn("Failed to set date");}
        } catch (RepositoryException re) {log.warn("Failed to set date");}
    }



    /**
     * <p>part of metadata, get creation date of the current node</p>
     *
     * @return Calendar
     */
    public Calendar getCreationDate() {
        try {
            return this.node.getProperty(CREATION_DATE).getDate();
        } catch (PathNotFoundException ee) {return null; }
        catch (RepositoryException re) {return null; }
    }




	/**
	 * <p>part of metadata, adds sequence number of the current node</p>
	 *
	 */
	public void setSequencePosition(long seqPos) {
		if (seqPos==0) {
			Date now = new Date();
			seqPos=now.getTime()*SEQUENCE_POS_COEFFICIENT;
		}
		try {
            this.node.getProperty(SEQUENCE_POS).setValue(seqPos);
		} catch (PathNotFoundException ee) {
			try {
                this.node.setProperty(SEQUENCE_POS, seqPos);
			} catch (RepositoryException e ) {}
		} catch (RepositoryException re) {}
	}


	/**
	 * <p>part of metadata, adds sequence number of the current node</p>
	 *
	 */
	public void setSequencePosition() {
		setSequencePosition(0);
	}


	/**
	 * <p>part of metadata, get sequence position of the current node</p>
	 *
	 * @return long
	 */
	public long getSequencePosition() {
		try {
            return this.node.getProperty(SEQUENCE_POS).getLong();
		} catch (PathNotFoundException ee) {
			Calendar cd=getCreationDate();
			return cd.getTimeInMillis()*SEQUENCE_POS_COEFFICIENT;
		}
		catch (Exception e) { return 0; }
	}






    /**
     * <p>part of metadata, adds activated status of the current node</p>
     *
     */
    public void setActivated() {
        try {
            this.node.getProperty(ACTIVATED).setValue(true);
        } catch (PathNotFoundException ee) {
            try {
                this.node.setProperty(ACTIVATED, true);
            } catch (RepositoryException e ) {
                log.warn("Failed to set [ Activated ] flag");
                log.error(e.getMessage(), e);
            }
        }
        catch (RepositoryException e) {
            log.warn("Failed to set [ Activated ] flag");
            log.error(e.getMessage(), e);
        }
    }



    /**
     * <p>part of metadata, adds activated status of the current node</p>
     *
     */
    public void setUnActivated() {
        try {
            this.node.getProperty(ACTIVATED).setValue(false);
        } catch (PathNotFoundException ee) {
            try {
                this.node.setProperty(ACTIVATED, false);
            } catch (RepositoryException e ) {
                log.warn("Failed to set [ UnActivated ] flag");
                log.error(e.getMessage(), e);
            }
        }
        catch (RepositoryException re) {
            log.warn("Failed to set [ UnActivated ] flag");
            log.error(re.getMessage(), re);
        }
    }



    /**
     * <p>part of metadata, get last activated status of the current node</p>
     *
     * @return Calendar
     */
    public boolean getIsActivated() {
        try {
            return this.node.getProperty(ACTIVATED).getBoolean();
        } catch (RepositoryException re) {return false;}
    }



    /**
     * <p>part of metadata, adds activated date of the current node</p>
     *
     */
    public void setLastActivationActionDate() {
        GregorianCalendar currentDate = new GregorianCalendar(TimeZone.getDefault());
        try {
            this.node.getProperty(LAST_ACTION).setValue(currentDate);
        } catch (PathNotFoundException ee) {
            try {
                this.node.setProperty(LAST_ACTION, currentDate);
            } catch (RepositoryException e ) {}
        }
        catch (RepositoryException re) {}
    }



    /**
     * <p>part of metadata, get last activated/de- date of the current node</p>
     *
     * @return Calendar
     */
    public Calendar getLastActionDate() {
        try {
            return this.node.getProperty(LAST_ACTION).getDate();
        } catch (PathNotFoundException ee) {return null; }
        catch (RepositoryException re) {return null; }
    }



    /**
     * <p>part of metadata, adds modification date of the current node</p>
     *
     */
    public void setModificationDate() {
        GregorianCalendar currentDate = new GregorianCalendar(TimeZone.getDefault());
        try {
            this.node.getProperty(LAST_MODIFIED).setValue(currentDate);
        } catch (PathNotFoundException ee) {
            try {
                this.node.setProperty(LAST_MODIFIED, currentDate);
            } catch (RepositoryException e ) {}
        }
        catch (RepositoryException re) {}
    }



    /**
     * <p>part of metadata, get last modified date of the current node</p>
     *
     * @return Calendar
     */
    public Calendar getModificationDate() {
        try {
            return this.node.getProperty(LAST_MODIFIED).getDate();
        } catch (PathNotFoundException ee) {return null; }
        catch (RepositoryException re) {return null; }
    }



   /**
     * <p>part of metadata , last known author of this node</p>
     *
     * @return String value of the requested metadata
     */
    public String getAuthorId() {
        try {
            return this.node.getProperty(AUTHOR_ID).getString();
        } catch (PathNotFoundException ee) {return ""; }
        catch (RepositoryException re) {return ""; }
    }



    /**
     * <p>part of metadata, current logged-in author who did some action on this page</p>
     *
     * @param value
     */
    public void setAuthorId(String value) {
        try {
            this.node.getProperty(AUTHOR_ID).setValue(value);
        } catch (PathNotFoundException ee) {
            try {
                this.node.setProperty(AUTHOR_ID, value);
            } catch (RepositoryException e ) {}
        }
        catch (RepositoryException re) {}
    }



    /**
      * <p>part of metadata , last known activator of this node</p>
      *
      * @return String value of the requested metadata
      */
     public String getActivatorId() {
         try {
             return this.node.getProperty(ACTIVATOR_ID).getString();
         } catch (PathNotFoundException ee) {return ""; }
         catch (RepositoryException re) {return ""; }
     }



    /**
     * <p>part of metadata, current logged-in author who last activated this page</p>
     *
     * @param value
     */
    public void setActivatorId(String value) {
        try {
            this.node.getProperty(ACTIVATOR_ID).setValue(value);
        } catch (PathNotFoundException ee) {
            try {
                this.node.setProperty(ACTIVATOR_ID, value);
            } catch (RepositoryException e ) {}
        }
        catch (RepositoryException re) {}
    }




    /**
     * <p>part of metadata, node activation time</p>
     *
     */
    public void setStartTime(Calendar startTime) {
        try {
            this.node.getProperty(START_TIME).setValue(startTime);
        } catch (PathNotFoundException ee) {
            try {
                this.node.setProperty(START_TIME, startTime);
            } catch (RepositoryException e) {}
        }
        catch (RepositoryException re) {}
    }



    /**
     * <p>part of metadata, node activation time</p>
     *
     * @return Calendar
     */
    public Calendar getStartTime() {
        try {
            return this.node.getProperty(START_TIME).getDate();
        } catch (PathNotFoundException ee) {return null; }
        catch (RepositoryException re) {return null; }
    }



    /**
     * <p>part of metadata, node de-activation time</p>
     *
     */
    public void setEndTime(Calendar endTime) {
        try {
            this.node.getProperty(END_TIME).setValue(endTime);
        } catch (PathNotFoundException ee) {
            try {
                this.node.setProperty(END_TIME, endTime);
            } catch (RepositoryException e) {}
        }
        catch (RepositoryException re) {}
    }



    /**
     * <p>part of metadata, node de-activation time</p>
     *
     * @return Calendar
     */
    public Calendar getEndTime() {
        try {
            return this.node.getProperty(END_TIME).getDate();
        } catch (PathNotFoundException ee) {return null; }
        catch (RepositoryException re) {return null; }
    }



    /**
     * <p>part of metadata , template which will be used to render content of this node</p>
     *
     * @return String value of the requested metadata
     */
    public String getTemplate() {
        try {
            return this.node.getProperty(TEMPLATE).getString();
        } catch (PathNotFoundException ee) {return ""; }
        catch (RepositoryException re) {return ""; }
    }



    /**
     * <p>part of metadata, template which will be used to render content of this node</p>
     *
     * @param value
     */
    public void setTemplate(String value) {
        try {
            this.node.getProperty(TEMPLATE).setValue(value);
        } catch (PathNotFoundException ee) {
            try {
                this.node.setProperty(TEMPLATE, value);
            } catch (RepositoryException e) {}
        }
        catch (RepositoryException re) {}
    }



    /**
     * <p>part of metadata, template type : JSP - Servlet - _xxx_</p>
     *
     * @param value
     */
    public void setTemplateType(String value) {
        try {
            this.node.getProperty(TEMPLATE_TYPE).setValue(value);
        } catch (PathNotFoundException ee) {
            try {
                this.node.setProperty(TEMPLATE_TYPE, value);
            } catch (RepositoryException e) {}
        }
        catch (RepositoryException re) {}
    }


    /**
     *
     * @param name
     * @param value
     * */
    public void setProperty(String name, String value) {
        try {
            this.node.getProperty(name).setValue(value);
        } catch (PathNotFoundException e) {
            try {
                this.node.setProperty(name,value);
            } catch(RepositoryException re) {
                log.error(re);
            }
        } catch (RepositoryException re) {
            log.error(re);
        }
    }


    /**
     *
     * @param name
     * @param value
     * */
    public void setProperty(String name, long value) {
        try {
            this.node.getProperty(name).setValue(value);
        } catch (PathNotFoundException e) {
            try {
                this.node.setProperty(name,value);
            } catch(RepositoryException re) {
                log.error(re);
            }
        } catch (RepositoryException re) {
            log.error(re);
        }
    }



    /**
     *
     * @param name
     * @param value
     * */
    public void setProperty(String name, double value) {
        try {
            this.node.getProperty(name).setValue(value);
        } catch (PathNotFoundException e) {
            try {
                this.node.setProperty(name,value);
            } catch(RepositoryException re) {
                log.error(re);
            }
        } catch (RepositoryException re) {
            log.error(re);
        }
    }



    /**
     *
     * @param name
     * @param value
     * */
    public void setProperty(String name, boolean value) {
        try {
            this.node.getProperty(name).setValue(value);
        } catch (PathNotFoundException e) {
            try {
                this.node.setProperty(name,value);
            } catch(RepositoryException re) {
                log.error(re);
            }
        } catch (RepositoryException re) {
            log.error(re);
        }
    }




    /**
     *
     * @param name
     * @param value
     * */
    public void setProperty(String name, Calendar value) {
        try {
            this.node.getProperty(name).setValue(value);
        } catch (PathNotFoundException e) {
            try {
                this.node.setProperty(name,value);
            } catch(RepositoryException re) {
                log.error(re);
            }
        } catch (RepositoryException re) {
            log.error(re);
        }
    }




    /**
     *
     * @param propertyName
     * */
    public Calendar getDateProperty(String propertyName) {
        try {
            return this.node.getProperty(propertyName).getDate();
        } catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        return null;
    }



    /**
     *
     * @param propertyName
     * */
    public boolean getBooleanProperty(String propertyName) {
        try {
            return this.node.getProperty(propertyName).getBoolean();
        } catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        return false;
    }



    /**
     *
     * @param propertyName
     * */
    public double getDoubleProperty(String propertyName) {
        try {
            return this.node.getProperty(propertyName).getDouble();
        } catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        return 0d;
    }



    /**
     *
     * @param propertyName
     * */
    public long getLongProperty(String propertyName) {
        try {
            return this.node.getProperty(propertyName).getLong();
        } catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        return 0l;
    }



    /**
     *
     * @param propertyName
     * */
    public String getStringProperty(String propertyName) {
        try {
            return this.node.getProperty(propertyName).getString();
        } catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        return "";
    }



}
