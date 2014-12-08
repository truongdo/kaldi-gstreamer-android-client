package jp.naist.ahclab.speechkit;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * Created by truong-d on 14/12/08.
 */
public class ServerInfo {
    private String addr="bark.phon.ioc.ee";
    private String app_speech="/dev/duplex-speech-api/ws/speech";
    private String app_status="/dev/duplex-speech-api/ws/status";
    private int port=82;

    List<BasicNameValuePair> extraHeaders = Arrays.asList(
            new BasicNameValuePair("Cookie", "session=abcd"),
            new BasicNameValuePair("content-type", "audio/x-raw"),
            new BasicNameValuePair("+layout", "(string)interleaved"),
            new BasicNameValuePair("+rate", "16000"),
            new BasicNameValuePair("+format", "S16LE"),
            new BasicNameValuePair("+channels", "1")
    );

    public ServerInfo(String addr, int port){
        this.addr = addr;
        this.port = port;
    }

    public ServerInfo(){}

    public void setAddr(String addr){this.addr = addr;}

    public void setPort(int port){this.port = port;}

    public void setAppSpeech(String app_speech){this.app_speech = app_speech;}

    public void setAppStatus(String app_status){this.app_status = app_status;}

    public String getAddr(){return this.addr;}

    public int getPort(){return this.port;}

    public String getSpeechServerUrl(){return "ws://"+this.addr+":"+this.port+"/"+app_speech+
            "?content-type=audio/x-raw,+layout=(string)interleaved,+rate=(int)16000,+format=(string)S16LE,+channels=(int)1";}

    public String getStatusServerUrl(){return "ws://"+this.addr+":"+this.port+"/"+app_status;}
}

