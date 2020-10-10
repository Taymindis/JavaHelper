package com.github.taymindis;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OJHSynchronizeProcess extends OJHSynchronizeRequest {
    private static Thread notificationThreads;
    private static Map<String, OJHSynchronizeProcess> processNamesLiving = new HashMap<String, OJHSynchronizeProcess>();
    private Thread processThread;
    private Long rollingTime;
    private boolean alertable;
    private OJHAlertable OJHAlertable;
    private static boolean isProcessOn = true;

    static {
        notificationThreads = newThread();
        notificationThreads.start();
    }

    public OJHSynchronizeProcess(String name, OJHAlertable OJHAlertable_) {
        super(name);
        int errCode = this.getErrorCode();
        this.OJHAlertable = OJHAlertable_;
        this.alertable = this.OJHAlertable.shouldAlert();
        if (errCode != OJHSynchronizeRequest.PROCESS_IS_OK_TO_RUN) {
            String log = name + " process is still running or invalid process ";
            OJHAlertable.logInfo(log);
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
        for (Map.Entry<String, OJHSynchronizeProcess> pLiving : processNamesLiving.entrySet()) {
            OJHSynchronizeProcess thisProcess = pLiving.getValue();
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
     * @param processName process name
     */
    public static void kill(String processName) {
        OJHSynchronizeProcess syncTrackableProcess = processNamesLiving.get(processName);
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
                        for (Map.Entry<String, OJHSynchronizeProcess> pLiving : processNamesLiving.entrySet()) {
                            OJHSynchronizeProcess thisProcess = pLiving.getValue();
                            if(!thisProcess.alertable) {
                                continue;
                            }
                            Long startedTime = thisProcess.getRollingTime();
                            Long secs = (currTime - startedTime) / 1000L;
                            if (secs > 300) {
                                thisProcess.OJHAlertable.triggerAlert(thisProcess.getName(), "Processing time out");
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
        OJHSynchronizeProcess.isProcessOn = isProcessOn;
    }
}

