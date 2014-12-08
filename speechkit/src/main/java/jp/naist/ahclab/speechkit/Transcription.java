package jp.naist.ahclab.speechkit;

/**
 * Created by truong-d on 14/12/05.
 */
public class Transcription {
    private String transcript="";

    public Transcription(){}

    public void renew(){
        transcript = "";
    }

    public void add_text(String text){
        transcript = transcript+" "+text;
    }

    public String getTranscript(){
        return transcript;
    }
}
