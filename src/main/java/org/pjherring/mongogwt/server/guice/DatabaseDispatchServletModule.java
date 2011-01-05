/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.guice;


import com.google.inject.servlet.ServletModule;
import java.util.logging.Logger;
import org.pjherring.mongogwt.server.domain.rpc.DatabaseServiceImpl;

/**
 *
 * @author pjherring
 */
public class DatabaseDispatchServletModule extends ServletModule {

    private final static Logger LOG = Logger.getLogger(DatabaseDispatchServletModule.class.getName());

    @Override
    public void configureServlets() {
        serve("*/data").with(DatabaseServiceImpl.class);
    }

}
