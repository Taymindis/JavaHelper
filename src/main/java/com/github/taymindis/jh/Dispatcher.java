package com.github.taymindis.jh;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

public abstract class Dispatcher<T> extends HttpServletRequestWrapper {
    private static ThreadPoolExecutor bgExecutor = null;
    public static String resourcePath = "";
    public static String suffix = "";
    public static String splitter = "^"; // default prevent any replacement
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

    public static void init(String $resourcePath, String $suffix, String $splitter, int nWorkerThread) {
        if (null == bgExecutor && nWorkerThread > 0) {
            bgExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(nWorkerThread);
        }
        if (null != $resourcePath) {
            Dispatcher.resourcePath = $resourcePath;
        }
        if (null != $suffix) {
            Dispatcher.suffix = $suffix;
        }
        if (null != $splitter) {
            Dispatcher.splitter = $splitter;
        }
    }

    @Deprecated
    public static Object DirectResult(String resourcePath,
                                      HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        Dispatcher $ev = Dispatcher.newEvent(request, response);
        return $ev.dispatch(resourcePath).getResult();
    }

    @Deprecated
    public static Object DirectResult(String resourcePath,
                                      HttpServletRequest request,
                                      HttpServletResponse response,
                                      Map<String, Object> params) throws Exception {
        Dispatcher $ev = Dispatcher.newEvent(request, response);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            $ev.set(entry.getKey(), entry.getValue());
        }
        return $ev.dispatch(resourcePath).getResult();
    }

    @Deprecated
    public static Object DirectResult(String resourcePath,
                                      HttpServletRequest request,
                                      HttpServletResponse response,
                                      Object... params) throws Exception {
        Dispatcher $ev = Dispatcher.newEvent(request, response);
        for (int i=0,sz = params.length; i < sz; i++) {
            if(i%2 == 1) {
                $ev.set((String) params[i-1], params[i]);
            }
        }

        return $ev.dispatch(resourcePath).getResult();
    }

    @Deprecated
    public static Object DirectResult(String resourcePath, Dispatcher $ev) throws Exception {
        return $ev.dispatch(resourcePath).getResult();
    }

    @Deprecated
    public static Object DirectResult(String resourcePath, Dispatcher $ev,
                                      Map<String, Object> params) throws Exception {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            $ev.set(entry.getKey(), entry.getValue());
        }
        return $ev.dispatch(resourcePath).getResult();
    }

    @Deprecated
    public static Object DirectResult(String resourcePath, Dispatcher $ev,
                                      Object... params) throws Exception {
        for (int i=0,sz = params.length; i < sz; i++) {
            if(i%2 == 1) {
                $ev.set((String) params[i-1], params[i]);
            }
        }

        return $ev.dispatch(resourcePath).getResult();
    }




    public static Object directResult(String resourcePath,
                                      HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        Dispatcher $ev = Dispatcher.newEvent(request, response);
        return $ev.dispatch(resourcePath).getResult();
    }


    public static Object directResult(String resourcePath,
                                      HttpServletRequest request,
                                      HttpServletResponse response,
                                      Map<String, Object> params) throws Exception {
        Dispatcher $ev = Dispatcher.newEvent(request, response);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            $ev.set(entry.getKey(), entry.getValue());
        }
        return $ev.dispatch(resourcePath).getResult();
    }


    public static Object directResult(String resourcePath,
                                      HttpServletRequest request,
                                      HttpServletResponse response,
                                      Object... params) throws Exception {
        Dispatcher $ev = Dispatcher.newEvent(request, response);
        for (int i=0,sz = params.length; i < sz; i++) {
            if(i%2 == 1) {
                $ev.set((String) params[i-1], params[i]);
            }
        }

        return $ev.dispatch(resourcePath).getResult();
    }


    public static Object directResult(String resourcePath, Dispatcher $ev) throws Exception {
        return $ev.dispatch(resourcePath).getResult();
    }


    public static Object directResult(String resourcePath, Dispatcher $ev,
                                      Map<String, Object> params) throws Exception {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            $ev.set(entry.getKey(), entry.getValue());
        }
        return $ev.dispatch(resourcePath).getResult();
    }


    public static Object directResult(String resourcePath, Dispatcher $ev,
                                      Object... params) throws Exception {
        for (int i=0,sz = params.length; i < sz; i++) {
            if(i%2 == 1) {
                $ev.set((String) params[i-1], params[i]);
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


    public abstract Dispatcher addAttribute(String key, Object val);

    public abstract Dispatcher set(String key, Object val);

    public abstract Object get(String key);

    public abstract String getString(String key);
    public abstract Integer getInteger(String key);
    public abstract Long getLong(String key);
    public abstract Double getDouble(String key);

    public abstract Dispatcher dispatch(String jspPathAndParam) throws ServletException, IOException, Exception;

    public abstract void setResult(T rs);

    public abstract T getResult();

    public abstract T getResult(long timeout, TimeUnit unit);

    public abstract boolean isDone();

    public abstract boolean isCancelled();
}
