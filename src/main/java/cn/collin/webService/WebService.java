package cn.collin.webService;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

/**
 * Created by collin on 17-5-12.
 */
public class WebService extends AbstractVerticle{
    JSONArray jsonArray = new JSONArray();

//    private JxXmlJsonReader _config;
//    private Map<NetSocket, JxReceivedDataProcessor> _dataProcessors;
    private EventBus eventBus;
    private String[] labels;
    private int[] amount;
    private SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss:SSS");
    private long start;
    private long end;
    private double interval;
    private JSONObject barData = new JSONObject();

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        // 部署发布rest服务
        vertx.deployVerticle(new WebService());
//        VertxOptions
    }

    @Override
    public void start() throws Exception {
        eventBus = vertx.eventBus();
        MessageConsumer<String> consumer = eventBus.consumer("WebService");
        consumer.handler(stringMessage -> {
            System.out.println(stringMessage);
        });

        vertx.deployVerticle(RealTimeSocket.class.getName());
        final Router router = Router.router(vertx);
        router.route().handler(CorsHandler.create("*")
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.OPTIONS)
                .allowedHeader("X-PINGARUNER")
                .allowedHeader("Content-Type"));
        router.route().handler(BodyHandler.create());
        router.post("/transData").handler(this::transData);
//        router.post("/getData").handler(this::getData);
        vertx.createHttpServer().requestHandler(router::accept).listen(8084);
    }

  /*  private void getData(RoutingContext context) {
        context.response().end("ok");
        String s = context.getBodyAsString();
        eventBus.send("RealTimeSocket", s);
        System.out.println(s);
//        System.out.println("data");


    }*/

    private void transData(RoutingContext context) {
        amount = new int[6];
        labels = new String[6];
        context.response().end("ok");
//        JsonArray jsonArray = context.getBodyAsJsonArray();
        String s = context.getBodyAsString();
        jsonArray = JSONArray.fromObject(s);
//        System.out.println("s" + s);
        if (!jsonArray.isEmpty()) {
            if (jsonArray.size() == 1) {
                amount[0] ++;
                labels[0] = getTime(jsonArray.getJSONObject(0));
            } else {
                start = (long)jsonArray.get(jsonArray.size() - 2);
                end = (long)jsonArray.get(jsonArray.size() - 1);
                interval = Math.rint((end - start) / 5);
//                System.out.println("start:" + start + "  end:" + end + "  interval:" + interval);

                long t1 = (long) (start + interval);
                long t2 = (long) (start + 2 * interval);
                long t3 = (long) (start + 3 * interval);
                long t4 = (long) (start + 4 * interval);

                labels[0] = formatter.format(start).substring(11);
                labels[1] = formatter.format(t1).substring(11);
                labels[2] = formatter.format(t2).substring(11);
                labels[3] = formatter.format(t3).substring(11);
                labels[4] = formatter.format(t4).substring(11);
                labels[5] = formatter.format(end).substring(11);

//                System.out.println("t1:" + t1 + "  t2:" + t2 + "  t3:" + t3 + "  t4:" + t4);

                for (int i = 0; i < jsonArray.size()-2; i++) {
                    long iStart = getStartTime(jsonArray.getJSONObject(i));
//                    System.out.println("istart:" + iStart);
                    if (iStart < t1) {
                        amount[1]++;
                    } else if (iStart < t2) {
                        amount[2]++;
                    } else if (iStart < t3) {
                        amount[3]++;
                    } else if (iStart < t4) {
                        amount[4]++;
                    } else {
                        amount[5]++;
                    }
                }
                barData.put("barData", JSONArray.fromObject(amount));
                barData.put("labels", JSONArray.fromObject(labels));
                System.out.println(barData);

            }
        }

        eventBus.send("RealTimeSocket", barData.toString());

    }

    public String getTime (JSONObject jsonObject) {
        String time = formatter.format(jsonObject.getLong("startTime"));
        time = time.substring(11);
        return time;
    }

    public long getStartTime (JSONObject jsonObject){
        return jsonObject.getLong("startTime");
    }


}
