package com.example.study;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.text.BreakIterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.app.ProgressDialog.show;

public class MainActivity extends AppCompatActivity {
   private Button btn_1;
   private ImageView image_1 ;
   private TextView text_1;
   private TextView text_2;
   private MqttClient client;
    private MqttConnectOptions options;

    private String host = "tcp://39.106.161.132:1883";
    private String userName = "android";
    private String passWord = "android";
    private String mqtt_id = "client-0002";

    private Handler handler;
    private ScheduledExecutorService scheduler;
    String T_Val  = "1";



    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ui_init();
        Mqtt_init();
        startReconnect();

        //按钮事件
//        btn_1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //调试信息
//                System.out.println("hello");
//                Toast.makeText( MainActivity.this, "hello",Toast.LENGTH_SHORT).show();
//            }
//        });
        //图片事件
        image_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("imgae");
                //Toast.makeText( MainActivity.this, "imgae",Toast.LENGTH_SHORT).show();
                //text_1.setText("我是新的内容");
                if(T_Val.equals("1")){
                    publishmessageplus("inTopic","1");
                    text_1.setText("开灯");
                    T_Val = "0";
                }
                else{
                    publishmessageplus("inTopic","0");
                    text_1.setText("关灯");
                    T_Val = "1";
                }



            }
        });

        handler = new Handler() {

            @SuppressLint({"SetTextI18n", "HandlerLeak"})
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1: //开机校验更新回传
                        break;
                    case 2:  // 反馈回传

                        break;
                    case 3:  //MQTT 收到消息回传   UTF8Buffer msg=new UTF8Buffer(object.toString());
                        //Toast.makeText(MainActivity.this,msg.obj.toString() ,Toast.LENGTH_SHORT).show();
                        text_2.setText(msg.obj.toString());
                        break;
                    case 30:  //连接失败
                        Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                        break;
                    case 31:   //连接成功
                        Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                        try {
                            //订阅温度数据
                            client.subscribe("outTopic", 1);
                            //订阅湿度数据
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }
        };


    }

    private void ui_init() {
        image_1 = findViewById(R.id.image_1);
        text_1 = findViewById(R.id.text_1);
        text_2  =  findViewById(R.id.text_2);

    }
    //Mqtt初始化函数
    private void Mqtt_init()
    {
        try {
            //host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            client = new MqttClient(host, mqtt_id,
                    new MemoryPersistence());
            //MQTT的连接设置
            options = new MqttConnectOptions();
            //设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(false);
            //设置连接的用户名
            options.setUserName(userName);
            //设置连接的密码
            options.setPassword(passWord.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(10);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(20);
            //设置回调
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    //连接丢失后，一般在这里面进行重连
                    System.out.println("connectionLost----------");
                    //startReconnect();
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //publish后会执行到这里
                    System.out.println("deliveryComplete---------"
                            + token.isComplete());
                }
                @Override
                public void messageArrived(String topicName, MqttMessage message)
                        throws Exception {
                    //subscribe后得到的消息会执行到这里面
                    System.out.println("messageArrived----------");
                    Message msg = new Message();
                    if(topicName.equals("outTopic"))
                    {
                        //收到气体数据
                        msg.what = 3;   //收到消息标志位
                        msg.obj = message.toString() + "摄氏度";
                        handler.sendMessage(msg);    // hander 回传
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //MQTT连接EMQ-X函数
    private void Mqtt_connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(!(client.isConnected()) )  //如果还未连接
                    {
                        client.connect(options);
                        Message msg = new Message();
                        msg.what = 31;
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.what = 30;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }
    //MQTT重连
    private void startReconnect() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!client.isConnected()) {
                    Mqtt_connect();
                }
            }
        }, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);
    }
    //MQTT发布消息
    private void publishmessageplus(String topic,String message2)
    {
        if (client == null || !client.isConnected()) {
            return;
        }
        MqttMessage message = new MqttMessage();
        message.setPayload(message2.getBytes());
        try {
            client.publish(topic,message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}