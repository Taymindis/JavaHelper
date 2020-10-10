package com.github.taymindis;

public interface OJHAlertable {

    boolean isApplicationRunning();

    boolean shouldAlert();

    void logInfo(String msg);

    void logError(Exception e);

    void triggerAlert(String processName, String message);

}
