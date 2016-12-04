import model.User;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import session.AuthedUserListHolder;

import java.net.HttpCookie;

@WebSocket
public class ChatWebSocketHandler {


    @OnWebSocketConnect
    public void onConnect(Session session) throws Exception {
        User user = resolveUser(session);
        String username = user.getName();
        Chat.userUsernameMap.put(session, username);
        Chat.broadcastMessage("Server", (username + " joined the chat"));
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        String username = Chat.userUsernameMap.get(user);
        Chat.userUsernameMap.remove(user);
        Chat.broadcastMessage("Server",  (username + " left the chat"));
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        Chat.broadcastMessage(Chat.userUsernameMap.get(user), message);
    }

    public static User resolveUser(Session session){
        String jsessionid = getJsessionid(session);
        if (jsessionid == null){
            return null;
        }
        return AuthedUserListHolder.getByJsessionid(jsessionid);
    }

    private static String getJsessionid(Session session){
        for(HttpCookie cookie: session.getUpgradeRequest().getCookies()){
            if("JSESSIONID".equals(cookie.getName())){
                return cookie.getValue();
            }
        }
        return null;
    }

}
