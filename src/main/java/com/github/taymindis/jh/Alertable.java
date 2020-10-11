package com.github.taymindis.jh;

public interface Alertable {

    boolean isApplicationRunning();

    boolean shouldAlert();

    void logInfo(String msg);

    void logError(Exception e);

    void triggerAlert(String processName, String message);

}
