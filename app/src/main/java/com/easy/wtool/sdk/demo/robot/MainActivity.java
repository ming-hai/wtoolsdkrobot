package com.easy.wtool.sdk.demo.robot;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ExpandedMenuView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.easy.wtool.sdk.MessageEvent;
import com.easy.wtool.sdk.OnMessageListener;
import com.easy.wtool.sdk.WToolSDK;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private static String LOG_TAG = "javahook";
    private static String DEF_TALKER = "接收人(点击选择)";
    private static String DEF_IMAGEFILE = "图片(点击选择)";
    private static String DEF_VOICEFILE = "语音(点击选择)";
    private static String DEF_VIDEOFILE = "视频(点击选择)";
    private static int RESULT_IMAGE = 1;
    private static int RESULT_VOICE = 2;
    private static int RESULT_VIDEO = 3;
    Context mContext;
    // Used to load the 'native-lib' library on application startup.

    private ConfigUtils configUtils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;

        final WToolSDK wToolSDK = new WToolSDK();
        this.setTitle(this.getTitle() + " - V" + wToolSDK.getVersion());

        configUtils = new ConfigUtils(this);
        wToolSDK.encodeValue("1");

        TextView textViewPrompt = (TextView) findViewById(R.id.textViewPrompt);
        textViewPrompt.setClickable(true);
        textViewPrompt.setMovementMethod(LinkMovementMethod.getInstance());
        String prompt = "<b>本软件基于<a href=\"http://repo.xposed.info/module/com.easy.wtool\">微控工具xp模块-开发版[微信(wechat)二次开发模块]</a>"
                +"开发，使用前请确认模块已经安装，模块最低版本：1.0.0.240[1.0.0.129-开发版]</b>";
        textViewPrompt.setText(Html.fromHtml(prompt));
        // Example of a call to a native method
        //TextView tv = (TextView) findViewById(R.id.sample_text);
        //tv.setText(stringFromJNI());

        final Button buttonStartMessage = (Button) findViewById(R.id.buttonStartMessage);
        //buttonStartMessage.setVisibility(View.INVISIBLE);
        final EditText editAuthCode = (EditText) findViewById(R.id.editAuthCode);
        final CheckBox checkBoxFriend =  (CheckBox) findViewById(R.id.checkBoxFriend);
        final CheckBox checkBoxChatroom =  (CheckBox) findViewById(R.id.checkBoxChatroom);
        final CheckBox checkBoxAtMe =  (CheckBox) findViewById(R.id.checkBoxAtMe);

        checkBoxAtMe.setText("@我才回复");
        final TextView editContent = (TextView) findViewById(R.id.editContent);
        editAuthCode.setText(configUtils.get(ConfigUtils.KEY_AUTHCODE, "0279C8C340306804E57499CD112EB094CB13037A"));
        if (!editAuthCode.getText().toString().equals("")) {
            //初始化
            parseResult(wToolSDK.init(editAuthCode.getText().toString()));
        }


        editContent.setMovementMethod(ScrollingMovementMethod.getInstance());

        if(robotIsRunning())
        {
            buttonStartMessage.setText("关闭机器人");
            buttonStartMessage.setTag(1);
        }
        else {
            buttonStartMessage.setTag(0);
            buttonStartMessage.setText("启动机器人");
        }

        buttonStartMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonStartMessage.getTag().equals(0)) {
                    Intent i = new Intent(mContext, RobotService.class);
                    i.putExtra("authCode", editAuthCode.getText().toString());
                    i.putExtra("firend", checkBoxFriend.isChecked());
                    i.putExtra("chatroom",checkBoxChatroom.isChecked());
                    i.putExtra("atme",checkBoxAtMe.isChecked());
                    startService(i);
                    //Log.d(LOG_TAG,"result: "+i.getIntExtra("result",0));
                    buttonStartMessage.setText("关闭机器人");
                    buttonStartMessage.setTag(1);
                    /*
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
                            buttonStartMessage.setTag(1);
                            buttonStartMessage.setText("停止监听消息");
                        }
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "err", e);
                    }
                    */
                } else {
                    //wToolSDK.stopMessageListener();
                    stopService(new Intent(mContext, RobotService.class));
                    buttonStartMessage.setTag(0);
                    buttonStartMessage.setText("启动机器人");
                }
            }
        });
    }


    private void parseResult(String result) {
        String text = "";
        try {
            JSONObject jsonObject = new JSONObject(result);
            if (jsonObject.getInt("result") == 0) {
                text = "操作成功";
            } else {
                text = jsonObject.getString("errmsg");
            }
        } catch (Exception e) {
            text = "解析结果失败";
        }
        Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
    }
    private boolean robotIsRunning()
    {
        return isServiceRunning(mContext,RobotService.class.getName());
    }
    /**
     * 用来判断服务是否后台运行
     * @param context
     * @param className 判断的服务名字
     * @return true 在运行 false 不在运行
     */
    private boolean isServiceRunning(Context mContext,String className) {
        boolean IsRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList   = activityManager.getRunningServices(30);
        if (!(serviceList.size()>0)) {
            return false;
        }
        for (int i=0; i<serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                IsRunning = true;
                break;
            }
        }
        return IsRunning ;
    }
}
