package com.github.taymindis.jh;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public interface Event {

   Event addAttribute(String key, Object val);

   Event set(String key, Object val);

   Event dispatch(String jspPathAndParam) throws ServletException, IOException, Exception;

   <T> void setResult(T rs);

   <T> T getResult();

   <T> T getResult(long timeout, TimeUnit unit);

   <T> T getResult(Class<T> clazz);

   <T> T getResult(long timeout, TimeUnit unit, Class<T> clazz);

   boolean isDone();

   boolean isCancelled();


   Boolean isStatus(EventStatus status);
   Boolean isStatus(EventStatus ...statuses);

   <E> E getOrThrow(String key, String errMsg) throws NullPointerException, ClassCastException;

   <E> E get(String key);

   Object getObject(String key);

   String getString(String key);

   Integer getInteger(String key);

   Long getLong(String key);

   Double getDouble(String key);

   Boolean getBoolean(String key);

   void setStatus(EventStatus status);

   EventStatus getStatus();

   String getStatusMessage();

   void setStatusMessage(String statusMessage);
}
