package com.github.taymindis;

public class OJHLockRequest {
    private final String reqName;
    private OJHSynchronizeRequest nssrq = null;
    public OJHLockRequest(String reqName) {
        this.reqName = reqName;
    }

    public boolean tryLock() {
        if(nssrq == null) {
            nssrq = new OJHSynchronizeRequest(this.reqName);
        }
        if (nssrq.getProcessStatus() != OJHSynchronizeRequest.PROCESS_IS_OK_TO_RUN) {
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
