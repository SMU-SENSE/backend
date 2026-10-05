package com.aac.ieojwo.live;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LiveEventService {
 private final Map<Long,CopyOnWriteArrayList<SseEmitter>> emitters=new ConcurrentHashMap<>();
 private final MeterRegistry meters;
 private final AtomicInteger activeConnections=new AtomicInteger();
 private final Counter openedConnections;
 private final Counter closedConnections;
 public LiveEventService(MeterRegistry meters){this.meters=meters;this.openedConnections=Counter.builder("malmoa.sse.connections.opened").description("Opened SSE connections").register(meters);this.closedConnections=Counter.builder("malmoa.sse.connections.closed").description("Closed SSE connections").register(meters);meters.gauge("malmoa.sse.connections.active",activeConnections);}
 public SseEmitter subscribe(Long userId){SseEmitter e=new SseEmitter(30L*60*1000);emitters.computeIfAbsent(userId,k->new CopyOnWriteArrayList<>()).add(e);openedConnections.increment();activeConnections.incrementAndGet();AtomicBoolean removed=new AtomicBoolean();Runnable remove=()->{if(removed.compareAndSet(false,true)){emitters.getOrDefault(userId,new CopyOnWriteArrayList<>()).remove(e);activeConnections.decrementAndGet();closedConnections.increment();}};e.onCompletion(remove);e.onTimeout(remove);e.onError(error->remove.run());try{e.send(SseEmitter.event().name("CONNECTED").data(Map.of("aacUserId",userId)));meters.counter("malmoa.sse.events.published","type","CONNECTED").increment();}catch(IOException ex){remove.run();e.completeWithError(ex);}return e;}
 public void publish(Long userId,String type,Object data){meters.counter("malmoa.sse.events.published","type",type).increment();for(SseEmitter e:emitters.getOrDefault(userId,new CopyOnWriteArrayList<>()))try{e.send(SseEmitter.event().name(type).data(data));}catch(IOException ex){e.completeWithError(ex);}}
}
