package com.github.taymindis.jh;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 dispatching in between web container
 */
public class DispatcherSync<T> extends Dispatcher {
    private HttpServletResponse response;
    private T result;

    protected DispatcherSync(HttpServletRequest request, HttpServletResponse response) {
        super(request);
        this.response = response;
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

    @Override
    public Object get(String key) {
        return super.getAttribute(key);
    }

    /**
     dispatching between the file via web container
     @param jspPath resource path
     @throws IOException IOException
     @throws ServletException ServletException
     @return OJHDispatcher
     */
    public DispatcherSync dispatch(String jspPath) throws ServletException, IOException {

        super.getRequestDispatcher(Dispatcher.resourcePath + jspPath.replace(Dispatcher.splitter, "/") + Dispatcher.suffix)
                .include(this, new HttpServletResponseWrapper(response) {
                    @Override
                    public void sendError(int sc) throws IOException {
                        httpStatus = sc;
                        super.sendError(sc);
                    }

                    @Override
                    public void sendError(int sc, String msg) throws IOException {
                        httpStatus = sc;
                        super.sendError(sc, msg);
                    }


                    @Override
                    public void setStatus(int sc) {
                        httpStatus = sc;
                        super.setStatus(sc);
                    }
                });

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
        return httpStatus != -1;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

}
