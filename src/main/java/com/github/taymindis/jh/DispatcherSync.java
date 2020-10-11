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
public class DispatcherSync extends Dispatcher {
    private HttpServletResponse response;
    private Object result;

    protected DispatcherSync(HttpServletRequest request, HttpServletResponse response) {
        super(request);
        this.response = response;
        this.result = null;
    }

    public DispatcherSync addAttribute(String key, Object val) {
        super.setAttribute(key, val);
        return this;
    }

    public DispatcherSync a(String key, Object val) {
        super.setAttribute(key, val);
        return this;
    }

    /**
     dispatching between the file via web container
     @param jspPathAndParam resource path
     @throws IOException IOException
     @throws ServletException ServletException
     @return OJHDispatcher
     */
    public DispatcherSync dispatch(String jspPathAndParam) throws ServletException, IOException {

        super.getRequestDispatcher(jspPathAndParam)
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
        this.result = rs;
    }

    public Object getResult() {
        return this.result;
    }

    @Override
    public Object getResult(long timeout, TimeUnit unit) {
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
