package com.github.taymindis.jh;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.*;

public abstract class Dispatcher<T> extends HttpServletRequestWrapper {
    private static ThreadPoolExecutor bgExecutor = null;
    protected int httpStatus;

    public Dispatcher(HttpServletRequest request) {
        super(request);
        httpStatus = -1;
    }
    public boolean isSuccess() {
        return httpStatus >= 200 && httpStatus < 300;
    }

    public static Dispatcher newEvent(HttpServletRequest request, HttpServletResponse response) {
        return new DispatcherSync(request, response);
    }
    public static Dispatcher newBackgroundEvent(HttpServletRequest request, HttpServletResponse response) {
        return new DispatcherFuture(request, response);
    }

    public static void init(String resourcePath, int nWorkerThread) {
        if(null == bgExecutor) {
            bgExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(nWorkerThread);
        }
    }

    public static boolean isDispatchFutureEnabled() {
        return bgExecutor == null;
    }

    public static ThreadPoolExecutor getBgExecutor() {
        return bgExecutor;
    }

    public static void ResetNewThreadSize(int nThread, long nSecsToWait) {
        ThreadPoolExecutor _shutdownExecutor = bgExecutor;
        try {
            /** Hazard Pointer **/
            bgExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(nThread);
            _shutdownExecutor.shutdown();
            if (nSecsToWait == 0 || !_shutdownExecutor.awaitTermination(nSecsToWait, TimeUnit.SECONDS)){
                _shutdownExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void ShutDownBackgroundTask(long nSecsToWait) {
        try {
            bgExecutor.shutdown();
            if (nSecsToWait == 0 || !bgExecutor.awaitTermination(nSecsToWait, TimeUnit.SECONDS)){
                bgExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }




    public abstract Dispatcher addAttribute(String key, Object val);

    public abstract Dispatcher a(String key, Object val);

    public abstract Dispatcher dispatch(String jspPathAndParam) throws ServletException, IOException, Exception;

    public abstract void setResult(T rs);

    public abstract T getResult();

    public abstract T getResult(long timeout, TimeUnit unit);

    public abstract boolean isDone();

    public abstract boolean isCancelled();
}
