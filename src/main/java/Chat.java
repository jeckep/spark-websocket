import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.websocket.api.*;
import org.json.*;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;
import utils.ViewUtils;

import java.text.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static j2html.TagCreator.*;
import static spark.Spark.*;

public class Chat {

    // this map is shared between sessions and threads, so it needs to be thread-safe (http://stackoverflow.com/a/2688817)
    static Map<Session, String> userUsernameMap = new ConcurrentHashMap<>();
    static int nextUserNumber = 1; //Assign to username for next connecting user

    public static void main(String[] args) {
        staticFiles.location("/public");
        staticFiles.expireTime(600);
        webSocket("/chat", ChatWebSocketHandler.class);
        init();


        get("/chatroom", (request, response) -> {
            if(request.session().attribute("user") == null){
                response.redirect("/login");
                return null;
            }

            Map<String, Object> model = new HashMap<>();

            // The wm files are located under the resources directory
            return new ModelAndView(model, "velocity/chatroom.html");
        }, new VelocityTemplateEngine());


        get("/login", (request, response) -> {
            if(request.session().attribute("user") != null){
                response.redirect("/chatroom");
                return null;
            }

            Map<String, Object> model = new HashMap<>();
            model.put("error", request.queryParams("error"));
            // The wm files are located under the resources directory
            return new ModelAndView(model, "velocity/login.html");
        }, ViewUtils.strictVelocityEngine());

        post("/login", ((request, response) -> {
            String name = request.queryParams("name");
            if(StringUtil.isNotBlank(name)) {
                String color = request.queryParams("color");
                request.session().attribute("user", new User(name, color));
                response.redirect("/chatroom");
            } else {
                response.redirect("/login?error=name");
            }
            return null;
        }));

        get("/logout", ((request, response) -> {
            request.session().removeAttribute("user");
            response.redirect("/login");
            return null;
        }));

        get("*", ((request, response) -> {
            if(request.session().attribute("name") != null){
                response.redirect("/chatroom");
            }else{
                response.redirect("/login");
            }
            return null;
        }));

        after((request, response) -> {
            response.header("charset", "UTF-8");
        });

    }

    //Sends a message from one user to all users, along with a list of current usernames
    public static void broadcastMessage(String sender, String message) {
        userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendString(String.valueOf(new JSONObject()
                    .put("userMessage", createHtmlMessageFromSender(sender, message))
                    .put("userlist", userUsernameMap.values())
                ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    //Builds a HTML element with a sender-name, a message, and a timestamp,
    private static String createHtmlMessageFromSender(String sender, String message) {
        return article().with(
                b(sender + " says:"),
                p(message),
                span().withClass("timestamp").withText(new SimpleDateFormat("HH:mm:ss").format(new Date()))
        ).render();
    }

}
