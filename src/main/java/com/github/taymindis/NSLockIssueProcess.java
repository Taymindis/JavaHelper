package com.github.taymindis;

public class NSLockIssueProcess {
    private final String reqName;
    private NSSynchronizeIssueProcess nonSuckIssueSynchronizeProcess = null;
    private NSAlertableIssue nsAlertableIssue;

    public NSLockIssueProcess(String reqName, NSAlertableIssue nsAlertableIssue_) {
        this.reqName = reqName;
        if(nsAlertableIssue_ == null) {
            nsAlertableIssue = new NSAlertableIssue() {
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
            nsAlertableIssue = nsAlertableIssue_;
        }
    }

    public boolean tryLock() {
        try {
            if (nonSuckIssueSynchronizeProcess == null) {
                nonSuckIssueSynchronizeProcess = new NSSynchronizeIssueProcess(this.reqName, this.nsAlertableIssue);
            }
            if (nonSuckIssueSynchronizeProcess.getProcessStatus() != NSSynchronizeIssueRequest.PROCESS_IS_OK_TO_RUN) {
                nonSuckIssueSynchronizeProcess = null;
                nsAlertableIssue.logError(new Exception("Process has been locked "));
                return false;
            }
        } catch (Exception e) {
            nsAlertableIssue.logError(e);
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
