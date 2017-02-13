package com.easy.wtool.sdk.demo.robot;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by TK on 2017-02-13.
 */

public class RobotUtils {
    public static String LOG_TAG = "javahook";
    //http get
    private static String httpPost(String urlPath,String datas)
    {


        try {
            String reqstr = datas;
            URL url = new URL(urlPath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(60000);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");// 设置请求 参数类型
            byte[] sendData = reqstr.getBytes("UTF-8");// 将请求字符串转成UTF-8格式的字节数组
            connection.setRequestProperty("Content-Length", sendData.length
                    + "");// 请求参数的长度

            OutputStream outputStream = connection.getOutputStream();// 得到输出流对象
            outputStream.write(sendData);
            outputStream.flush();
            outputStream.close();

            InputStream inputStream = connection.getInputStream();

                InputStreamReader inputStreamReader = new InputStreamReader(
                        inputStream);
                BufferedReader bReader = new BufferedReader(inputStreamReader);

                String str = "";
                String temp = "";
                while ((temp = bReader.readLine()) != null) {
                    str = str + temp + "\n";
                }
                return str;// 返回响应数据


        }
        catch (Exception e) {
            Log.e(LOG_TAG,"ERR",e);
            return "";
        }
    }

    //图灵机器人
    public static String askRobot(String question)
    {
        final String urlPath = "http://www.tuling123.com/api/product_exper/chat.jhtml";

        try {
            //String reqstr = "info="+ URLEncoder.encode(question,"utf-8")+"&userid=88cedf62-735f-4f7e-a800-7a23ba8e1f33&_xsrf=";
            String reqstr = "info="+ question+"&userid=88cedf62-735f-4f7e-a800-7a23ba8e1f33&_xsrf=";
            reqstr = httpPost(urlPath,reqstr);
            //<xml><ToUserName><![CDATA[null]]></ToUserName><FromUserName><![CDATA[null]]></FromUserName><CreateTime>null</CreateTime><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[每天都挺嗨]]></Content></xml>
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(reqstr));
                Document document = builder.parse(is);

                Element root = document.getDocumentElement();


                NodeList nodes = root.getElementsByTagName("Content");

                if (nodes != null && nodes.getLength() > 0) {

                    Element el = (Element) nodes.item(0);

                    reqstr = el.getTextContent();


                } else {
                    reqstr = "";
                }

                return reqstr;

            }
            catch (Exception e)
            {
                Log.e(LOG_TAG,"ERR",e);
                return "";
            }

        }
        catch (Exception e) {
            Log.e(LOG_TAG,"ERR",e);
            return "";
        }
    }
}
