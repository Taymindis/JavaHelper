package com.github.taymindis;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NSSSLIssueHttp {

    private int statusCode;
    private String response = null;
    private BufferedReader in = null;
    private String errMessage = null;
    private int connTimeout = 0; // means default
    private int readTimeout = 0; // means default
    private String charset = "UTF-8";
    private static final String USER_AGENT = "Mozilla/5.0";
    private boolean tls1_2 = true;

    public NSSSLIssueHttp() {

    }

    public NSSSLIssueHttp(boolean tls1_2) {
        this.tls1_2 = tls1_2;
    }

    public NSSSLIssueHttp(int connTimeout, int readTimeout, boolean tls1_2) {
        this.connTimeout = connTimeout;
        this.readTimeout = readTimeout;
        this.tls1_2 = tls1_2;
    }

    private void setSSLTLS1_2Context(HttpsURLConnection con) {
        try {
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(null, null, null);
            con.setSSLSocketFactory(context.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            // Globals.traceException(e);
        } catch (KeyManagementException e) {
            // Globals.traceException(e);
        } finally {

        }
    }

    private void _request(HttpURLConnection con,
                          Map<String, String> requestHeader,
                          String requestBody) {
        boolean okToRead = true;
        if (tls1_2 && "https".equalsIgnoreCase(con.getURL().getProtocol())) {
            setSSLTLS1_2Context((HttpsURLConnection) con);
        }
        try {
            con.setRequestProperty("User-Agent", USER_AGENT);
            // accept response
            con.setUseCaches(false);
            con.setDoInput(true);

            if (requestHeader != null) {
                Iterator i = requestHeader.keySet().iterator();
                while (i.hasNext()) {
                    String key = (String) i.next();
                    String value = requestHeader.get(key);
                    con.setRequestProperty(key, value);
                }
            }

            if (connTimeout > 0) {
                con.setConnectTimeout(connTimeout);
            }
            if (readTimeout > 0) {
                con.setReadTimeout(readTimeout);
            }

            // For POST only - START
            if (requestBody != null) {
                byte[] postData = requestBody.getBytes(StandardCharsets.UTF_8);
                int postDataLength = postData.length;
                con.setRequestProperty( "charset", "utf-8");
                con.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
                con.setDoOutput(true);
                OutputStream os = null;
                try {
                    os = con.getOutputStream();
                    os.write(postData);
                } catch (IOException e) {
                    errMessage = e.getMessage();
                    setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
                    okToRead = false;
                    // Globals.traceException(e);
                } finally {
                    if (os != null) {
                        os.flush();
                        os.close();
                    }
                }
            }
            // For POST only - END

            if (!okToRead) {
                return;
            }

            int responseCode = con.getResponseCode();

            if (responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_MULT_CHOICE) { //success
                in = new BufferedReader(new InputStreamReader(
                        con.getInputStream(), charset));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                setStatusCode(responseCode);
                setResponse(response.toString());
            } else {
                setStatusCode(responseCode);
                setResponse(null);
            }
        } catch (IOException e) {
            setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
            errMessage = e.getMessage();
            // Globals.traceException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Globals.traceException(e);
                }
                in = null;
            }

            /**
             *
             * So in order to reuse the Socket,
             * just call InputStream.close().
             * Do not call HttpURLConnection.disconnect()
             *
             */
//			con.disconnect();
        }
    }

    public static Map<String, String> useJsonRequestResponseContentType() {
        return new HashMap<String, String>() {{
            put("Content-Type", "application/json; charset=UTF-8");
            put("Accept", "application/json");
        }};
    }

    public static Map<String, String> useXFormUrlEncoded() {
        return new HashMap<String, String>() {{
            put("Content-Type", "application/x-www-form-urlencoded");
        }};
    }

    public static Map<String, String> useMultiPartFormData() {
        final String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
        return new HashMap<String, String>() {{
            put("Content-Type", "multipart/form-data; boundary=" + boundary);
        }};
    }

    public void getRequest(String url,
                           Map<String, String> requestHeader) throws IOException {

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        _request(con, requestHeader, null);
    }

    public void postRequest(String url, Map<String, String> requestHeader,
                            String requestBody) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        _request(con, requestHeader, requestBody);
    }

    public void postRequest(String url, Map<String, String> requestHeader,
                           Map<String,Object> params) throws IOException {
        postRequest(url, requestHeader, paramToStringBody(params));
    }

    public void putRequest(String url, Map<String, String> requestHeader,
                           String requestBody) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("PUT");
        _request(con, requestHeader, requestBody);
    }

    public void putRequest(String url, Map<String, String> requestHeader,
                           Map<String,Object> params) throws IOException {
        putRequest(url, requestHeader, paramToStringBody(params));
    }


    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }

    public int getConnTimeout() {
        return connTimeout;
    }

    public void setConnTimeout(int connTimeout) {
        this.connTimeout = connTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void release() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                errMessage = e.getMessage();
                // Globals.traceException(e);
                // TODO error
            }
            in = null;
        }
    }

    private static String paramToStringBody(Map<String, Object> params) {
        if(params!= null && !params.isEmpty()) {
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                postData.append('&');
                postData.append(param.getKey());
                postData.append('=');
                postData.append(param.getValue());
            }
            return postData.deleteCharAt(0).toString();
        } else {
            return null;
        }
    }
}