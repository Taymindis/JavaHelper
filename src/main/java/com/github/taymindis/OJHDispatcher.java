package com.github.taymindis;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;


/**
 dispatching in between web container
 */
public class OJHDispatcher {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private int httpStatus;
    private static final String _RESP_KEY = "@#D_";
//    private static final String _F_RESP_KEY = "@#FD_";

//    private static ThreadPoolExecutor bgExecutor = null;


    public OJHDispatcher(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

//    public OJHDispatcher(HttpServletRequest request, HttpServletResponse response, int nThread) {
//        if(null == bgExecutor) {
//            bgExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(nThread);
//        }
//        this.request = request;
//        this.response = response;
//    }

    public OJHDispatcher addAttribute(String key, Object val) {
        request.setAttribute(key, val);
        return this;
    }

    public OJHDispatcher a(String key, Object val) {
        request.setAttribute(key, val);
        return this;
    }

    /**
     dispatching between the file via web container
     @param jspPathAndParam resource path
     @throws IOException IOException
     @throws ServletException ServletException
     @return OJHDispatcher
     */
    public OJHDispatcher dispatch(String jspPathAndParam) throws ServletException, IOException {

        request.getRequestDispatcher(jspPathAndParam)
                .include(request, new HttpServletResponseWrapper(response) {
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
//    /**
//     dispatching first between the file via web container, get the result at the end of request
//     @throws IOException IOException
//     @throws ServletException ServletException
//      * @param jspPathAndParam resource path
//     * @return
//     */
//    public OJHDispatcher dispatchFuture(String jspPathAndParam) throws Exception {
//        if(null == bgExecutor) {
//            throw new Exception("Background Task feature is not enabled");
//        }
//        Future<Void> $f = bgExecutor.submit(new Callable<Void>() {
//            @Override
//            public Void call() throws Exception {
//                OJHDispatcher.SetResponse(request, "This is long output");
//                Thread.sleep(5000);
//                return null;
//            }
//        });
//
//        request.setAttribute(_F_RESP_KEY + jspPathAndParam, $f);
//
//        return this;
//    }

    public boolean isSuccess() {
        return httpStatus >= 200 && httpStatus < 300;
    }

    public Object getResult() {
        return request.getAttribute(_RESP_KEY + request.getRequestURI());
    }

//    public Object getFutureResult() {
//        try {
//            Future<Void> $f = (Future<Void>) request.getAttribute(_F_RESP_KEY + request.getRequestURI());
//            $f.get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
//        return getResult();
//    }

    public static void SetResponse(HttpServletRequest request, Object output) {
        request.setAttribute(_RESP_KEY + request.getRequestURI(), output);
    }

//    public static void ResetNewThreadSize(int nThread, long nSecsToWait) {
//        ThreadPoolExecutor _shutdownExecutor = bgExecutor;
//        try {
//            /** Hazard Pointer **/
//            bgExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(nThread);
//            _shutdownExecutor.shutdown();
//            if (nSecsToWait == 0 || !_shutdownExecutor.awaitTermination(nSecsToWait, TimeUnit.SECONDS)){
//                _shutdownExecutor.shutdownNow();
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void ShutDownBackgroundTask(long nSecsToWait) {
//        try {
//            bgExecutor.shutdown();
//            if (nSecsToWait == 0 || !bgExecutor.awaitTermination(nSecsToWait, TimeUnit.SECONDS)){
//                bgExecutor.shutdownNow();
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

}
