package com.easy.wtool.sdk.demo.robot;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.easy.wtool.sdk.MessageEvent;
import com.easy.wtool.sdk.OnMessageListener;
import com.easy.wtool.sdk.WToolSDK;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by TK on 2017-02-13.
 */

public class RobotService extends Service {
    
    WToolSDK wToolSDK = null;
    String authCode = "";
    boolean friend = false,chatroom = false,isatme = false;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        authCode  = intent.getStringExtra("authCode");
        friend = intent.getBooleanExtra("firend",true);
        chatroom = intent.getBooleanExtra("chatroom",true);
        isatme = intent.getBooleanExtra("atme",true);
        Log.d(RobotUtils.LOG_TAG,"RobotService.onStartCommand: "+authCode+","+friend+","+chatroom+","+isatme);
        wToolSDK.init(authCode);
        //处理消息 回调的Handler
        final Handler messageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {


                MessageEvent event = (MessageEvent) msg.obj;

                //editContent.append("message: " + event.getTalker() + "," +wToolSDK.decodeValue(event.getContent()) + "\n");
                super.handleMessage(msg);
            }
        };
        wToolSDK.setOnMessageListener(new OnMessageListener() {
            @Override
            public void messageEvent(MessageEvent event) {
                String content = wToolSDK.decodeValue(event.getContent());
                Log.d(RobotUtils.LOG_TAG, "on message: " + event.getTalker() + "," +content);

                //editContent.setText("message: "+event.getTalker()+","+event.getContent());
                //由于该回调是在线程中，因些如果是有UI更新，需要使用Handler
                //messageHandler.obtainMessage(0, event).sendToTarget();
                if(event.getMsgType()==1 && (friend || chatroom)) {
                    if(!chatroom)
                    {
                        if(event.getTalker().indexOf("@chatroom")<0)
                        {
                            Log.d(RobotUtils.LOG_TAG,"not chatroom");
                            return;
                        }
                    }
                    if(chatroom && event.getTalker().indexOf("@chatroom")>0)
                    {
                        if(isatme)
                        {
                            if(!event.isAtMe())
                            {
                                Log.d(RobotUtils.LOG_TAG,"not @me");
                                return;
                            }
                        }
                    }
                    content = RobotUtils.askRobot(content);
                    Log.d(RobotUtils.LOG_TAG, "response: " + content);
                    if (content.length() > 0) {
                        wToolSDK.sendText(event.getTalker(), content);
                    }
                }
            }
        });
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(1);
            jsonArray.put(2);
            jsonObject.put("talkertypes", jsonArray);
            jsonObject.put("froms", new JSONArray());
            jsonArray = new JSONArray();
            jsonArray.put(1);
            jsonArray.put(42);
            jsonObject.put("msgtypes", jsonArray);
            jsonObject.put("msgfilters", new JSONArray());
            String result = wToolSDK.startMessageListener(jsonObject.toString());
            jsonObject = new JSONObject(result);
            if (jsonObject.getInt("result") == 0) {
                //buttonStartMessage.setTag(1);
                //buttonStartMessage.setText("停止监听消息");
                Log.d(RobotUtils.LOG_TAG,"启动机器人成功");
                Toast.makeText(this, "启动机器人成功", Toast.LENGTH_LONG).show();
                intent.putExtra("result",1);
            }
            else
            {
                Log.d(RobotUtils.LOG_TAG,"启动机器人失败>>"+jsonObject.getString("errmsg"));
                Toast.makeText(this, "启动机器人失败>>"+jsonObject.getString("errmsg"), Toast.LENGTH_LONG).show();
                intent.putExtra("result",0);
            }
        } catch (Exception e) {
            Log.e(RobotUtils.LOG_TAG, "err", e);
        }
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onCreate() {
        //Toast.makeText(this, "My Service created", Toast.LENGTH_LONG).show();
        Log.d(RobotUtils.LOG_TAG,"RobotService.onCreate");
        wToolSDK = new WToolSDK();

    }

    @Override
    public void onDestroy() {
        Log.d(RobotUtils.LOG_TAG,"RobotService.onDestroy");
        if(wToolSDK!=null)
        {
            wToolSDK.stopMessageListener();
        }
        Toast.makeText(this, "机器人已关闭", Toast.LENGTH_LONG).show();
    }


}
