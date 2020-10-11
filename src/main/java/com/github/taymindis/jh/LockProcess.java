package com.github.taymindis.jh;

public class LockProcess {
    private final String reqName;
    private SynchronizeProcess synchronizeProcess = null;
    private Alertable Alertable;

    public LockProcess(String reqName, Alertable Alertable_) {
        this.reqName = reqName;
        if(Alertable_ == null) {
            Alertable = new Alertable() {
                @Override
                public boolean isApplicationRunning() {
                    return false;
                }

                @Override
                public boolean shouldAlert() {
                    return false;
                }

                @Override
                public void logInfo(String msg) {

                }

                @Override
                public void logError(Exception e) {

                }

                @Override
                public void triggerAlert(String processName, String message) {

                }
            };
        } else {
            Alertable = Alertable_;
        }
    }

    public boolean tryLock() {
        try {
            if (synchronizeProcess == null) {
                synchronizeProcess = new SynchronizeProcess(this.reqName, this.Alertable);
            }
            if (synchronizeProcess.getProcessStatus() != SynchronizeRequest.PROCESS_IS_OK_TO_RUN) {
                synchronizeProcess = null;
                Alertable.logError(new Exception("Process has been locked "));
                return false;
            }
        } catch (Exception e) {
            Alertable.logError(e);
        }
        return true;
    }

    public void unlock() {
        if(synchronizeProcess != null) {
            synchronizeProcess.release();
            synchronizeProcess = null;
        }
    }

}
