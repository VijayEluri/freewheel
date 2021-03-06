package org.freewheelschedule.freewheel.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freewheelschedule.freewheel.controlserver.FreewheelAbstractRunnable;
import org.springframework.beans.factory.annotation.Autowired;

public class RestServices {

    private final static Log log = LogFactory.getLog(RestServices.class);


    @Autowired
    private Runnable webService;

    private Thread webServiceThread;

    public void runRestService() {
        log.info("Running REST Services ...");

        log.info("Registering shutdown hook");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                log.info("Shutting down the ControlServer ...");
                ((FreewheelAbstractRunnable) webService).setContinueWaiting(false);
            }
        });

        webServiceThread = new Thread(webService);
        webServiceThread.start();
    }

    public void joinWebService() {
        try {
            webServiceThread.join();
        } catch (InterruptedException e) {
            log.error("RestService interrupted waiting for jobs", e);
        }
    }


}
