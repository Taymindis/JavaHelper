package com.github.taymindis.jh;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * dispatching in between web container
 */
public class SyncEvent extends Dispatcher implements Event {
    //    private HttpServletResponse response;
    private Object result;

    protected SyncEvent(HttpServletRequest request, HttpServletResponse response) {
        super(request);
        this._dispatchResponse = new DispatcherResponse(response);
        this.result = null;
    }

    public SyncEvent addAttribute(String key, Object val) {
        super.setAttribute(key, val);
        return this;
    }

    @Override
    public SyncEvent set(String key, Object val) {
        super.setAttribute(key, val);
        return this;
    }


    /**
     * dispatching between the file via web container
     *
     * @param jspPath resource path
     * @return OJHDispatcher
     * @throws IOException      IOException
     * @throws ServletException ServletException
     */
    @Override
    public SyncEvent dispatch(String jspPath) throws ServletException, IOException {
        clearPreviousStatus();
        super.getRequestDispatcher(Dispatcher.resourcePath + jspPath.replace(Dispatcher.splitter, "/") + Dispatcher.suffix)
                .include(this, _dispatchResponse);

        return this;
    }

    public void setResult(Object rs) {
        this.result = rs;
    }

    public <T> T getResult() {
        return (T) this.result;
    }

    @Override
    public <T> T getResult(long timeout, TimeUnit unit) {
        return (T) this.result;
    }

    @Override
    public boolean isDone() {
        return _dispatchResponse.getStatus() != -1;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }


}
