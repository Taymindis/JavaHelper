package com.github.taymindis;

public class NSLockIssueRequest {
    private final String reqName;
    private NSSynchronizeIssueRequest nssrq = null;
    public NSLockIssueRequest(String reqName) {
        this.reqName = reqName;
    }

    public boolean tryLock() {
        if(nssrq == null) {
            nssrq = new NSSynchronizeIssueRequest(this.reqName);
        }
        if (nssrq.getProcessStatus() != NSSynchronizeIssueRequest.PROCESS_IS_OK_TO_RUN) {
            nssrq = null;
            return false;
        }
        return true;
    }


    public void unlock() {
        if (nssrq != null) {
            nssrq.release();
            nssrq = null;
        }
    }
}
