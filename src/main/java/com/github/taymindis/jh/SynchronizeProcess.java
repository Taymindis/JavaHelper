package com.github.taymindis.jh;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SynchronizeProcess extends SynchronizeRequest {
    private static Thread notificationThreads;
    private static Map<String, SynchronizeProcess> processNamesLiving = new HashMap<String, SynchronizeProcess>();
    private Thread processThread;
    private Long rollingTime;
    private boolean alertable;
    private Alertable Alertable;
    private static boolean isProcessOn = true;
    private static long maxProcessingTime = 300L;

    static {
        notificationThreads = newThread();
        notificationThreads.start();
    }

    public SynchronizeProcess(String name, Alertable Alertable_) {
        super(name);
        int errCode = this.getErrorCode();
        this.Alertable = Alertable_;
        this.alertable = this.Alertable.shouldAlert();
        if (errCode != SynchronizeRequest.PROCESS_IS_OK_TO_RUN) {
            String log = name + " process is still running or invalid process ";
            Alertable.logInfo(log);
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
//        if (this.getProcessStatus() == PROCESS_IS_OK_TO_RUN) {
        processNamesLiving.remove(this.getName());
//        }
        super.release();
    }

    public static void releaseAllProcess() {
        for (Map.Entry<String, SynchronizeProcess> pLiving : processNamesLiving.entrySet()) {
            SynchronizeProcess thisProcess = pLiving.getValue();
            thisProcess.release();
        }
    }

    public static long getMaxProcessingTime() {
        return maxProcessingTime;
    }

    public static void setMaxProcessingTime(long maxProcessingTime) {
        SynchronizeProcess.maxProcessingTime = maxProcessingTime;
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
     *
     * @param processName process name
     */
    public static void kill(String processName) {
        SynchronizeProcess syncTrackableProcess = processNamesLiving.get(processName);
        if (syncTrackableProcess != null) {
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
                        for (Map.Entry<String, SynchronizeProcess> pLiving : processNamesLiving.entrySet()) {
                            SynchronizeProcess thisProcess = pLiving.getValue();
                            if (!thisProcess.alertable) {
                                continue;
                            }
                            Long startedTime = thisProcess.getRollingTime();
                            Long secs = (currTime - startedTime) / 1000L;
                            if (secs > maxProcessingTime) {
                                thisProcess.Alertable.triggerAlert(thisProcess.getName(), "Processing time out");
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
        SynchronizeProcess.isProcessOn = isProcessOn;
    }
}

