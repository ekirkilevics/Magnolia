/**
 * This file Copyright (c) 2007-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.mail.handlers;

import org.apache.log4j.Level;

/**
 * Defines custom logging level for forms data
 * @author tmiyar
 *
 */
public class LoggingLevel extends Level{

    private static final long serialVersionUID = 1L;

    public static final LoggingLevel MAIL_TRAIL = new LoggingLevel(98, "MAIL_TRAIL", 0);

    protected LoggingLevel(int level, String levelStr, int syslogEquivalent) {
        super(level, levelStr, syslogEquivalent);

    }

    public static Level toLevel(String sArg) {
      return MAIL_TRAIL;
    }

    public static Level toLevel(int val) {
      return MAIL_TRAIL;
    }

}
