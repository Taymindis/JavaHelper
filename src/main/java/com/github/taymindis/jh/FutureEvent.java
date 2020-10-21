package com.github.taymindis.jh;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.concurrent.*;


/**
 * dispatching async between web container
 */
public class FutureEvent extends Dispatcher implements Event{
    private Future<Void> f;
    private Object result;

    protected FutureEvent(HttpServletRequest request, HttpServletResponse response) {
        super(request);
        this._dispatchResponse = new DispatcherResponse(response);
        this.f = null;
        this.result = null;
    }

    public FutureEvent addAttribute(String key, Object val) {
        super.setAttribute(key, val);
        return this;
    }

    @Override
    public FutureEvent set(String key, Object val) {
        super.setAttribute(key, val);
        return this;
    }


    /**
     * dispatching first between the file via web container, get the result at the end of request
     *
     * @param jspPath resource path
     * @return DispatchFuture
     * @throws IOException      IOException
     * @throws ServletException ServletException
     */
    @Override
    public synchronized FutureEvent dispatch(final String jspPath) throws Exception {
        if (isDispatchFutureEnabled()) {
            throw new Exception("Background Task feature is not enabled");
        }
        if (f != null) {
            throw new Exception("Process has been executed");
        }
        clearPreviousStatus();
        final FutureEvent df = this;
        f = getBgExecutor().submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                getRequest().getRequestDispatcher(Dispatcher.resourcePath + jspPath.replace(Dispatcher.splitter, "/") + Dispatcher.suffix)
                        .include(df, _dispatchResponse);
                return null;
            }
        });


        return this;
    }

    @Override
    public boolean isDone() {
        return this.f.isDone();
    }

    @Override
    public boolean isCancelled() {
        return this.f.isCancelled();
    }



    @Override
    public void setResult(Object rs) {
        this.result = rs;
    }

    @Override
    public <T> T getResult() {
        if(this.result == null) {
            try {
                f.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return (T) result;
    }

    @Override
    public <T> T getResult(long timeout, TimeUnit unit)  {
        if(this.result == null) {
            try {
                f.get(timeout, unit);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }
        return (T) result;
    }

}
