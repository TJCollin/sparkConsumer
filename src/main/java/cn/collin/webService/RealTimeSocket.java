package cn.collin.webService;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Created by collin on 17-5-21.
 */
public class RealTimeSocket extends AbstractVerticle{
    private EventBus eventBus;
    private HttpServer httpServer;
    static ServerWebSocket webSocket;
    @Override
    public void start() throws Exception {
        eventBus = vertx.eventBus();
        MessageConsumer<String> consumer = eventBus.consumer("RealTimeSocket");
        consumer.handler(this::sendMessage);
        /*setConnectHandler();
        _server.listen(8082);
        _eb = vertx.eventBus();*/
        System.out.println("start");
        httpServer = vertx.createHttpServer();

        httpServer.websocketHandler(serverWebSocket -> {

            this.webSocket = serverWebSocket;
            System.out.println("connected");
            /*String message = "hello";

            serverWebSocket.writeTextMessage(message);
            webSocket.textMessageHandler(s -> {
                System.out.println(s);
            });*/
//            test();

        }).listen(8086);

    }

    private void sendMessage(Message<String> stringMessage) {
        System.out.println("message:"+stringMessage.body());
        String result = stringMessage.body();
        /*JSONArray data = JSONObject.fromObject(stringMessage.body()).getJSONArray("barData");
        JSONArray labels = JSONObject.fromObject(stringMessage.body()).getJSONArray("labels");
        JSONObject barData = new JSONObject();
        barData.put("barData", data);
        barData.put("labels", labels);
        System.out.println(barData.toString());*/
        test(result);
    }

    public void test (String message) {
        System.out.println("message1:"+message);
        webSocket.writeTextMessage(message);
    }
}
