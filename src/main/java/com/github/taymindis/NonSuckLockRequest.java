package com.github.taymindis;

public class NonSuckLockRequest {
    private final String reqName;
    private NonSuckSynchronizeRequest nssrq = null;
    public NonSuckLockRequest(String reqName) {
        this.reqName = reqName;
    }

    public boolean tryLock() {
        if(nssrq == null) {
            nssrq = new NonSuckSynchronizeRequest(this.reqName);
        }
        if (nssrq.getProcessStatus() != NonSuckSynchronizeRequest.PROCESS_IS_OK_TO_RUN) {
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
