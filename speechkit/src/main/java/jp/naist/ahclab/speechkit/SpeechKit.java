package jp.naist.ahclab.speechkit;


import android.content.Context;
import android.util.Log;
import jp.naist.ahclab.speechkit.Recognizer.Listener;

public class SpeechKit {
	private ServerInfo serverInfo;
    private Context mcontext;

    static String TAG = "SpeechKit";

    public static enum Status {
        INIT, RECOGNIZING, STOPPING, STOPPED, READY
    }
	public SpeechKit(Context mcontext, String appId, String appKey, ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
        this.mcontext = mcontext;
	}

    public static SpeechKit initialize(Context applicationContext, String appId, String appKey, ServerInfo serverInfo) {
        return new SpeechKit(applicationContext, appId, appKey, serverInfo);
    }
	
	public void connect(){
		
	}

	public Recognizer createRecognizer(Listener _listener) {
		return new Recognizer(serverInfo,_listener);
	}
	public void setDefaultRecognizerPrompts(){
		
	}

}
