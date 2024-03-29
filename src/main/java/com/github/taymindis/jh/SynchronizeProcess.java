package com.github.taymindis.jh;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SynchronizeProcess extends SynchronizeRequest {
    private static Thread notificationThreads;
    private static Map<String, SynchronizeProcess> processNamesLiving = new ConcurrentHashMap<String, SynchronizeProcess>();
    private Thread processThread;
    private Long rollingTime;
    private boolean alertable;
    private Alertable Alertable;
    private long processTimeout;
    private static boolean isProcessOn = true;

    private static long globalProcessTimeout = 3600L;

    static {
        notificationThreads = newThread();
        notificationThreads.start();
    }

    public SynchronizeProcess(String name, long processTimeout, Alertable Alertable_) {
        super(name);
        int errCode = this.getErrorCode();
        this.Alertable = Alertable_;
        this.processTimeout = processTimeout;
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

    public SynchronizeProcess(String name, Alertable alertable) {
       this(name, globalProcessTimeout, alertable);
    }

    @Override
    public void release() {
        if (this.getProcessStatus() == PROCESS_IS_OK_TO_RUN
            || this.getProcessStatus() == PROCESS_HAS_ERROR) {
            processNamesLiving.remove(this.getName());
        }
        super.release();
    }

    public static void releaseAllProcess() {
        for (Map.Entry<String, SynchronizeProcess> pLiving : processNamesLiving.entrySet()) {
            SynchronizeProcess thisProcess = pLiving.getValue();
            thisProcess.release();
        }
    }

    public long getProcessTimeout() {
        return processTimeout;
    }

    public void setProcessTimeout(long processTimeout) {
        this.processTimeout = processTimeout;
    }

    public static long getGlobalProcessTimeout() {
        return globalProcessTimeout;
    }

    public static void setGlobalProcessTimeout(long globalProcessTimeout) {
        SynchronizeProcess.globalProcessTimeout = globalProcessTimeout;
    }

    /**
     Replacing by setGlobalProcessTimeout if singleProcessTimeoutNotSet
     * @param maxProcessingTime max Processing time allowed
     */
    @Deprecated
    public static void setMaxProcessingTime(long maxProcessingTime) {
        SynchronizeProcess.globalProcessTimeout = maxProcessingTime;
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

                        Set<String> keySet = processNamesLiving.keySet();

                        for(String key:keySet) {
                            SynchronizeProcess thisProcess = processNamesLiving.get(key);
                            if (thisProcess == null || !thisProcess.alertable) {
                                continue;
                            }
                            Long startedTime = thisProcess.getRollingTime();
                            Long secs = (currTime - startedTime) / 1000L;
                            if (secs > thisProcess.getProcessTimeout()) {
                                thisProcess.Alertable.triggerAlert(thisProcess.getName(), "Processing time out");
                                thisProcess.setRollingTime(currTime);
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

