package com.github.taymindis.jh;

import javax.naming.NamingException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class EventContext implements Event{
    private static ThreadPoolExecutor bgExecutor = null;
    public static String resourcePath = "";
    public static String suffix = "";
    public static String splitter = "^"; // default prevent any replacement
    protected PageContext _pageContext;

    private EventStatus evStatus; // this is event process result status
    private String statusMessage;

    public EventContext(PageContext pc) {
        this._pageContext = pc;
        evStatus = EventStatus.UNSET;
        statusMessage = null;
    }

    public static Event newEvent(PageContext pc, String jndiResource) throws NamingException {
        Event ev = new EventSync(pc);
        pc.setAttribute("$_ev_ctx", ev, PageContext.REQUEST_SCOPE);
        return ev;
    }

    public static Event newFutureEvent(PageContext pc, String jndiResource) throws NamingException {
        Event ev = new EventFuture(pc);
        pc.setAttribute("$_ev_ctx", ev, PageContext.REQUEST_SCOPE);
        return ev;
    }

    public static EventTransaction newTransactionEvent(PageContext pc, String jndiResource) throws NamingException {
        EventTransaction ev = new EventTransactionImpl(pc, jndiResource);
        pc.setAttribute("$_ev_ctx", ev, PageContext.REQUEST_SCOPE);
        return ev;
    }

    public static void init(String $resourcePath, String $suffix, String $splitter, int nWorkerThread) {
        if (null == bgExecutor && nWorkerThread > 0) {
            bgExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(nWorkerThread);
        }
        if (null != $resourcePath) {
            EventContext.resourcePath = $resourcePath;
        }
        if (null != $suffix) {
            EventContext.suffix = $suffix;
        }
        if (null != $splitter) {
            EventContext.splitter = $splitter;
        }
    }

    public static <T> T getContext(PageContext pc) {
        return (T) pc.getAttribute("$_ev_ctx", PageContext.REQUEST_SCOPE);
    }

        public <T> T getResult(Class<T> clazz) {
        return getResult();
    }

    public <T> T getResult(long timeout, TimeUnit unit, Class<T> clazz) {
        return getResult(timeout, unit);
    }

    public static Object directResult(String resourcePath, Event $ev) throws Exception {
        return $ev.dispatch(resourcePath).getResult();
    }


    public static Object directResult(String resourcePath, Event $ev,
                                      Map<String, Object> params) throws Exception {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            $ev.set(entry.getKey(), entry.getValue());
        }
        return $ev.dispatch(resourcePath).getResult();
    }


    public static Object directResult(String resourcePath, Event $ev,
                                      Object... params) throws Exception {
        for (int i = 0, sz = params.length; i < sz; i++) {
            if (i % 2 == 1) {
                $ev.set((String) params[i - 1], params[i]);
            }
        }

        return $ev.dispatch(resourcePath).getResult();
    }

    public static boolean isDispatchFutureEnabled() {
        return bgExecutor == null;
    }

    public static ThreadPoolExecutor getBgExecutor() {
        return bgExecutor;
    }

    public static void ResetNewThreadSize(int nThread, long nSecsToWait) {
        if (nThread > 0) {
            ThreadPoolExecutor _shutdownExecutor = bgExecutor;
            try {
                /** Hazard Pointer **/
                bgExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(nThread);
                if (_shutdownExecutor != null) {
                    _shutdownExecutor.shutdown();
                    if (nSecsToWait == 0 || !_shutdownExecutor.awaitTermination(nSecsToWait, TimeUnit.SECONDS)) {
                        _shutdownExecutor.shutdownNow();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void ShutDownBackgroundTask(long nSecsToWait) {
        try {
            bgExecutor.shutdown();
            if (nSecsToWait == 0 || !bgExecutor.awaitTermination(nSecsToWait, TimeUnit.SECONDS)) {
                bgExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void clearPreviousStatus() {
        setResult(null);
        setStatus(EventStatus.UNSET);
        try {
            BodyContent bodyContent = this._pageContext.pushBody();
            bodyContent.clearBody();
            bodyContent.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Boolean isStatus(EventStatus status) {
        return this.evStatus == status;
    }
    public Boolean isStatus(EventStatus ...statuses) {
        for(EventStatus s: statuses){
            if(this.evStatus == s) {
                return true;
            }
        }
        return false;
    }

    public <E> E getOrThrow(String key, String errMsg) throws NullPointerException, ClassCastException {
        Object o = get(key);
        if (o == null) {
            throw new NullPointerException(errMsg);
        }
        return (E) o;
    }

    public <E> E get(String key) {
        try {
            return (E) _pageContext.getAttribute(key, PageContext.REQUEST_SCOPE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public Object getObject(String key) {
        return get(key);
    }


    public String getString(String key) {
        try {
            return String.valueOf(get(key));
        } catch (Exception e) {
            return null;
        }
    }

    public Integer getInteger(String key) {
        Object b = get(key);
        try {
            if (b instanceof Integer) {
                return (Integer) b;
            }
            return Integer.parseInt(b.toString());
        } catch (Exception e) {
            return null;
        }
    }

    public Long getLong(String key) {
        Object b = get(key);
        try {
            if (b instanceof Long) {
                return (Long) b;
            }
            return Long.parseLong(b.toString());
        } catch (Exception e) {
            return null;
        }
    }

    public Double getDouble(String key) {
        Object b = get(key);
        try {
            if (b instanceof Double) {
                return (Double) b;
            }
            return Double.parseDouble(b.toString());
        } catch (Exception e) {
            return null;
        }
    }

    public Boolean getBoolean(String key) {
        Object b = get(key);
        try {
            if (b instanceof Boolean) {
                return (Boolean) b;
            }
            return Boolean.getBoolean(b.toString());
        } catch (Exception e) {
            return false;
        }
    }

    public void setStatus(EventStatus status) {
        this.evStatus = status;
    }

    public EventStatus getStatus() {
        return evStatus;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

}
