/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.util;


import java.util.logging.Logger;

/**
 *
 * @author pjherring
 */
public class StringUtil {

    private final static Logger LOG = Logger.getLogger(StringUtil.class.getName());

    public StringUtil() {}

    public String upperCaseFirst(String str) {
        return Character.toUpperCase(str.charAt(0))
            + str.substring(1);
    }

}
