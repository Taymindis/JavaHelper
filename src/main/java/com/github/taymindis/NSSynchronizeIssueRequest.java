package com.github.taymindis;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class NSSynchronizeIssueRequest {
    private final String name;
    private int processStatus;

    public NSSynchronizeIssueRequest(String name) {
        this.name = name;
        if (name == null) {
            processStatus = PROCESS_NAME_IS_EMPTY;
        } else {
            if (!_$requests.add(this.name)) {
                processStatus = PROCESS_IS_ALREADY_RUNNING;
            } else {
                processStatus = PROCESS_IS_OK_TO_RUN;
            }
        }
    }

    public String getName() {
        return name;
    }

    public boolean hasError() {
        return processStatus != PROCESS_IS_OK_TO_RUN;
    }

    public int getErrorCode() {
        return processStatus;
    }

    public int getProcessStatus() {
        return processStatus;
    }

    public void release() {
        if (this.processStatus == PROCESS_IS_OK_TO_RUN) {
            _$requests.remove(this.name);
        }
    }

    /**
     * Use in Risk
     */
    public static void flushAll() {
        flush(null);
    }

    /**
     * Use in Risk
     */
    public static void flush(String nameStartWith) {
        if (nameStartWith == null) {
            _$requests.clear();
        } else {
            synchronized (_$requests) {
                Iterator<String> itr = _$requests.iterator();
                while (itr.hasNext()) {
                    String procName = itr.next();
                    if (procName.startsWith(nameStartWith)) {
                        itr.remove();
                    }
                }
            }
        }
    }

    private static final Set<String> _$requests = Collections.synchronizedSet(new HashSet<String>());
    public static final int PROCESS_IS_OK_TO_RUN = 1;
    public static final int PROCESS_IS_ALREADY_RUNNING = 2;
    public static final int PROCESS_HAS_ERROR = 3;
    public static final int PROCESS_SHOULD_NOT_RUN = 4;
    public static final int PROCESS_NAME_IS_EMPTY = 5;
}
