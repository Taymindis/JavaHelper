package com.github.taymindis;

public class OJHLockProcess {
    private final String reqName;
    private OJHSynchronizeProcess nonSuckIssueSynchronizeProcess = null;
    private OJHAlertable OJHAlertable;

    public OJHLockProcess(String reqName, OJHAlertable OJHAlertable_) {
        this.reqName = reqName;
        if(OJHAlertable_ == null) {
            OJHAlertable = new OJHAlertable() {
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
            OJHAlertable = OJHAlertable_;
        }
    }

    public boolean tryLock() {
        try {
            if (nonSuckIssueSynchronizeProcess == null) {
                nonSuckIssueSynchronizeProcess = new OJHSynchronizeProcess(this.reqName, this.OJHAlertable);
            }
            if (nonSuckIssueSynchronizeProcess.getProcessStatus() != OJHSynchronizeRequest.PROCESS_IS_OK_TO_RUN) {
                nonSuckIssueSynchronizeProcess = null;
                OJHAlertable.logError(new Exception("Process has been locked "));
                return false;
            }
        } catch (Exception e) {
            OJHAlertable.logError(e);
        }
        return true;
    }

    public void unlock() {
        if(nonSuckIssueSynchronizeProcess != null) {
            nonSuckIssueSynchronizeProcess.release();
            nonSuckIssueSynchronizeProcess = null;
        }
    }

}
