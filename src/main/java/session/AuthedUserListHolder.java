package session;


import lombok.extern.slf4j.Slf4j;
import model.User;
import spark.Request;

import javax.servlet.http.Cookie;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class AuthedUserListHolder {
    private static Map<String, User> jsessionidToUser = new ConcurrentHashMap<>();
    private static Map<User, String> userToJsessionid = new ConcurrentHashMap<>();

    public static void put(Request request, User user){
        String jsessionid = getJsessionid(request);
        put(jsessionid, user);
    }

    public static void put(String jsessionid, User user){
        if(jsessionid != null){
            jsessionidToUser.put(jsessionid, user);
            userToJsessionid.put(user, jsessionid);
        }else{
            log.warn("JSESSIONID not found in cookies");
        }
        log.info("User " + user.getName() + " added.");
    }

    public static void remove(User user){
        String jsessionid = userToJsessionid.get(user);
        if(jsessionid != null){
            jsessionidToUser.remove(jsessionid);
            userToJsessionid.remove(user);
        }else{
            log.warn("User " + user.getName() + " already removed");
        }
        log.info("User " + user.getName() + " removed.");
    }

    public static User getByJsessionid(String jsessionid){
        return jsessionidToUser.get(jsessionid);
    }


    public static String getJsessionid(Request request){
        for(Cookie cookie: request.raw().getCookies()){
            if("JSESSIONID".equals(cookie.getName())){
                return cookie.getValue();
            }
        }
        return null;
    }


}
