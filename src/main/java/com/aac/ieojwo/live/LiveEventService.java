package com.aac.ieojwo.live;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@Service
public class LiveEventService {
 private final Map<Long,CopyOnWriteArrayList<SseEmitter>> emitters=new ConcurrentHashMap<>();
 public SseEmitter subscribe(Long userId){SseEmitter e=new SseEmitter(30L*60*1000);emitters.computeIfAbsent(userId,k->new CopyOnWriteArrayList<>()).add(e);Runnable remove=()->emitters.getOrDefault(userId,new CopyOnWriteArrayList<>()).remove(e);e.onCompletion(remove);e.onTimeout(remove);try{e.send(SseEmitter.event().name("CONNECTED").data(Map.of("aacUserId",userId)));}catch(IOException ex){remove.run();}return e;}
 public void publish(Long userId,String type,Object data){for(SseEmitter e:emitters.getOrDefault(userId,new CopyOnWriteArrayList<>()))try{e.send(SseEmitter.event().name(type).data(data));}catch(IOException ex){e.complete();}}
}
