package com.github.taymindis;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NSSynchronizeIssueProcess extends NSSynchronizeIssueRequest {
    private static Thread notificationThreads;
    private static Map<String, NSSynchronizeIssueProcess> processNamesLiving = new HashMap<String, NSSynchronizeIssueProcess>();
    private Thread processThread;
    private Long rollingTime;
    private boolean alertable;
    private NSAlertableIssue nsAlertableIssue;
    private static boolean isProcessOn = true;

    static {
        notificationThreads = newThread();
        notificationThreads.start();
    }

    public NSSynchronizeIssueProcess(String name, NSAlertableIssue nsAlertableIssue_) {
        super(name);
        int errCode = this.getErrorCode();
        this.nsAlertableIssue = nsAlertableIssue_;
        this.alertable = this.nsAlertableIssue.shouldAlert();
        if (errCode != NSSynchronizeIssueRequest.PROCESS_IS_OK_TO_RUN) {
            String log = name + " process is still running or invalid process ";
            nsAlertableIssue.logInfo(log);
        } else {
            if (notificationThreads == null || notificationThreads.isInterrupted() || !notificationThreads.isAlive()) {
                notificationThreads = newThread();
                notificationThreads.start();
            }
            processThread = Thread.currentThread();
            rollingTime = new Date().getTime();
            processNamesLiving.put(name, this); // put started date
        }
    }

    @Override
    public void release() {
        if (this.getProcessStatus() == PROCESS_IS_OK_TO_RUN) {
            processNamesLiving.remove(this.getName());
        }
        super.release();
    }

    public static void releaseAllProcess() {
        for (Map.Entry<String, NSSynchronizeIssueProcess> pLiving : processNamesLiving.entrySet()) {
            NSSynchronizeIssueProcess thisProcess = pLiving.getValue();
            thisProcess.release();
        }
    }

    public Thread getProcessThread() {
        return processThread;
    }

    public Long getRollingTime() {
        return rollingTime;
    }

    public void setRollingTime(Long rollingTime) {
        this.rollingTime = rollingTime;
    }

    /**
     * Use in Risk
     * @param processName
     */
    public static void kill(String processName) {
        NSSynchronizeIssueProcess syncTrackableProcess = processNamesLiving.get(processName);
        if(syncTrackableProcess != null) {
            syncTrackableProcess.getProcessThread().interrupt();
            syncTrackableProcess.release();
        }
    }

    private static Thread newThread() {
        return new Thread() {
            public void run() {
                try {
                    while (isProcessOn) {
                        Long currTime = new Date().getTime();
                        for (Map.Entry<String, NSSynchronizeIssueProcess> pLiving : processNamesLiving.entrySet()) {
                            NSSynchronizeIssueProcess thisProcess = pLiving.getValue();
                            if(!thisProcess.alertable) {
                                continue;
                            }
                            Long startedTime = thisProcess.getRollingTime();
                            Long secs = (currTime - startedTime) / 1000L;
                            if (secs > 300) {
                                thisProcess.nsAlertableIssue.triggerAlert(thisProcess.getName(), "Processing time out");
                                thisProcess.setRollingTime(currTime);
                                pLiving.setValue(thisProcess);
                            }
                        }
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public static void setIsProcessOn(boolean isProcessOn) {
        NSSynchronizeIssueProcess.isProcessOn = isProcessOn;
    }
}

