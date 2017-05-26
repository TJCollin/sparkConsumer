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
import java.text.SimpleDateFormat;
/**
 * Created by collin on 17-5-12.
 */
public class WebService extends AbstractVerticle{
    JSONArray jsonArray = new JSONArray();

//    private JxXmlJsonReader _config;
//    private Map<NetSocket, JxReceivedDataProcessor> _dataProcessors;
    private EventBus eventBus;
    private String label;
    private String[] labels = new String[5];
    private int[] amout = new int[5];

    private long testTime;
    private int totalAmout;
    private long averageInvokeTime;
    private long invokeTime;
    private SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss:SSS");
    private long start;
    private long end;
    private double interval;
    private JSONObject barData = new JSONObject();

    int total = 0;

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
        context.response().end("ok");
//        JsonArray jsonArray = context.getBodyAsJsonArray();
        String s = context.getBodyAsString();
        System.out.println(s);
        jsonArray = JSONArray.fromObject(s);
//        System.out.println("s" + s);
        if (!jsonArray.isEmpty()) {
            System.out.println(jsonArray.size());
            if (jsonArray.size() == 1) {
                testTime = jsonArray.getJSONObject(0).getLong("interval");
                totalAmout = 1;
                averageInvokeTime = jsonArray.getJSONObject(0).getLong("interval");

                /*amount[0] ++;
                delay[0] = jsonArray.getJSONObject(0).getLong("interval");
                labels[0] = getTime(jsonArray.getJSONObject(0));*/
            } else {
                start = (long)jsonArray.get(jsonArray.size() - 2);
                end = (long)jsonArray.get(jsonArray.size() - 1);
                testTime = end - start;
                totalAmout = jsonArray.size();
                interval = Math.rint((end - start) / 5);
//                System.out.println("start:" + start + "  end:" + end + "  interval:" + interval);

                /*long t1 = (long) (start + interval);
                long t2 = (long) (start + 2 * interval);
                long t3 = (long) (start + 3 * interval);
                long t4 = (long) (start + 4 * interval);

                *//*labels[0] = formatter.format(start).substring(11);
                labels[1] = formatter.format(t1).substring(11);
                labels[2] = formatter.format(t2).substring(11);
                labels[3] = formatter.format(t3).substring(11);
                labels[4] = formatter.format(t4).substring(11);
                labels[5] = formatter.format(end).substring(11);*/
                label = formatter.format((Math.rint(start+end)/2)).substring(11);
                /*labels[1] = formatter.format((Math.rint(t1+t2)/2)).substring(11);
                labels[2] = formatter.format((Math.rint(t2+t3)/2)).substring(11);
                labels[3] = formatter.format((Math.rint(t3+t4)/2)).substring(11);
                labels[4] = formatter.format((Math.rint(t4+end)/2)).substring(11);*/

//                System.out.println("t1:" + t1 + "  t2:" + t2 + "  t3:" + t3 + "  t4:" + t4);

                for (int i = 0; i < jsonArray.size()-2; i++) {
                    long interval = jsonArray.getJSONObject(i).getLong("interval");

                    invokeTime += interval;
//                    System.out.println("istart:" + iStart);
                    /*if (iStart < t1) {
                        amount[0]++;
                        delay[0] += interval;
                    } else if (iStart < t2) {
                        amount[1]++;
                        delay[1] = delay[1] + interval;
                    } else if (iStart < t3) {
                        amount[2]++;
                        delay[2] = delay[2] + interval;
                    } else if (iStart < t4) {
                        amount[3]++;
                        delay[3] = delay[3] + interval;
                    } else {
                        amount[4]++;
                        delay[4] = delay[4] + interval;
                    }*/
                }
                averageInvokeTime = invokeTime/totalAmout;
            }

            System.arraycopy(labels, 0, labels, 1, 4);
            System.arraycopy(amout, 0, amout, 1, 4);
            labels[0] = label;
            amout[0] = totalAmout;
            total += totalAmout;
            System.out.println("total:" + total);
            barData.put("barData", JSONArray.fromObject(amout));
            barData.put("labels", JSONArray.fromObject(labels));
            barData.put("totalAmount", totalAmout);
            barData.put("averageInvokeTime", averageInvokeTime);
            barData.put("testTime", testTime);
//            barData.put("delay", JSONArray.fromObject(oldDelay));
            System.out.println(barData.toString());
        }

        if (!barData.isEmpty()) {
            eventBus.send("RealTimeSocket", barData.toString());
            System.out.println("barData"+barData);
        }
        barData.clear();

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
