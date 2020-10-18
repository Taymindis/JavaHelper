package com.github.taymindis.jh;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 dispatching in between web container
 */
public class DispatcherSync<T> extends Dispatcher {
//    private HttpServletResponse response;
    private T result;

    protected DispatcherSync(HttpServletRequest request, HttpServletResponse response) {
        super(request);
        this._dispatchResponse = new DispatcherResponse(response);
        this.result = null;
    }

    public DispatcherSync addAttribute(String key, Object val) {
        super.setAttribute(key, val);
        return this;
    }

    @Override
    public DispatcherSync set(String key, Object val) {
        super.setAttribute(key, val);
        return this;
    }


    /**
     dispatching between the file via web container
     @param jspPath resource path
     @throws IOException IOException
     @throws ServletException ServletException
     @return OJHDispatcher
     */
    public DispatcherSync dispatch(String jspPath) throws ServletException, IOException {
        setResult(null);
        super.getRequestDispatcher(Dispatcher.resourcePath + jspPath.replace(Dispatcher.splitter, "/") + Dispatcher.suffix)
                .include(this, _dispatchResponse);

        return this;
    }

    public void setResult(Object rs) {
        this.result = (T) rs;
    }

    public T getResult() {
        return this.result;
    }

    @Override
    public T getResult(long timeout, TimeUnit unit) {
        return this.result;
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
