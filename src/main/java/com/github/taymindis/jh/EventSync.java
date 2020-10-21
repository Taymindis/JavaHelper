package com.github.taymindis.jh;

import javax.servlet.ServletException;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * dispatching in between web container
 */
public class EventSync extends EventContext implements Event {
    //    private HttpServletResponse response;
    private Object result;

    protected EventSync(PageContext pc) {
        super(pc);
        this.result = null;
    }

    public EventSync addAttribute(String key, Object val) {
        this._pageContext.setAttribute(key, val, PageContext.REQUEST_SCOPE);
        return this;
    }

    @Override
    public EventSync set(String key, Object val) {
        addAttribute(key, val);
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
    public EventSync dispatch(String jspPath) throws ServletException, IOException {
        clearPreviousStatus();
        this._pageContext.include(resourcePath + jspPath.replace(splitter, "/") + suffix);
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
        return this.result != null;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }


}
