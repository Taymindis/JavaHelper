package com.github.taymindis.jh;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.concurrent.*;


/**
 * dispatching async between web container
 */
public class DispatcherFuture<T> extends Dispatcher {
    private HttpServletResponse response;
    private Future<Void> f;
    private T result;

    protected DispatcherFuture(HttpServletRequest request, HttpServletResponse response) {
        super(request);
        this.response = response;
        this.f = null;
        this.result = null;
    }

    public DispatcherFuture addAttribute(String key, Object val) {
        super.setAttribute(key, val);
        return this;
    }

    @Override
    public DispatcherFuture set(String key, Object val) {
        super.setAttribute(key, val);
        return this;
    }

    @Override
    public Object get(String key) {
        return super.getAttribute(key);
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
    public synchronized DispatcherFuture dispatch(final String jspPath) throws Exception {
        if (isDispatchFutureEnabled()) {
            throw new Exception("Background Task feature is not enabled");
        }
        if (f != null) {
            throw new Exception("Process has been executed");
        }
        final DispatcherFuture df = this;
        f = getBgExecutor().submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                getRequest().getRequestDispatcher(Dispatcher.resourcePath + jspPath.replace(Dispatcher.splitter, "/") + Dispatcher.suffix)
                        .include(df, new HttpServletResponseWrapper(response) {
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
        this.result = (T) rs;
    }

    @Override
    public T getResult() {
        if(this.result == null) {
            try {
                f.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public T getResult(long timeout, TimeUnit unit)  {
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
        return result;
    }

}
