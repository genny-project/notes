package life.genny.notes.endpoints;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.Session;

@ServerEndpoint("/v7/chat/{usercode}")         
@ApplicationScoped
public class ChatEndpoint {

    Map<String, Session> sessions = new ConcurrentHashMap<>(); 

    @OnOpen
    public void onOpen(Session session, @PathParam("usercode") String usercode) {
        sessions.put(usercode, session);
        broadcast("User " + usercode + " joined");
    }

    @OnClose
    public void onClose(Session session, @PathParam("usercode") String usercode) {
        sessions.remove(usercode);
        broadcast("User " + usercode + " left");
    }

    @OnError
    public void onError(Session session, @PathParam("usercode") String usercode, Throwable throwable) {
        sessions.remove(usercode);
        broadcast("User " + usercode + " left on error: " + throwable);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("usercode") String usercode) {
        broadcast(">> " + usercode + ": " + message);
    }

    private void broadcast(String message) {
        sessions.values().forEach(s -> {
            s.getAsyncRemote().sendObject(message, result ->  {
                if (result.getException() != null) {
                    System.out.println("Unable to send message: " + result.getException());
                }
            });
        });
    }

}