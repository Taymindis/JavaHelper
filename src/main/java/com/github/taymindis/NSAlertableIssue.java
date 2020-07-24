package com.github.taymindis;

public interface NSAlertableIssue {

    boolean isApplicationRunning();

    boolean shouldAlert();

    void logInfo(String msg);

    void logError(Exception e);

    void triggerAlert(String processName, String message);

}
