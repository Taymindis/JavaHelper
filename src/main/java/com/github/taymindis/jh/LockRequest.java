package com.github.taymindis.jh;

public class LockRequest {
    private final String reqName;
    private SynchronizeRequest nssrq = null;
    public LockRequest(String reqName) {
        this.reqName = reqName;
    }

    public boolean tryLock() {
        return tryLock(-1);
    }

    public boolean tryLock(int maxRequestAccepted) {
        if(nssrq == null) {
            nssrq = new SynchronizeRequest(this.reqName, maxRequestAccepted);
        }
        if (nssrq.getProcessStatus() != SynchronizeRequest.PROCESS_IS_OK_TO_RUN) {
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
