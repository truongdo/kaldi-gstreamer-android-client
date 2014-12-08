package jp.naist.ahclab.speechkit;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by truong-d on 14/12/08.
 */
public class ServerStatus {
    private int num_worker = 0;
    private int num_request = 0;

    public ServerStatus(int num_worker, int num_request){
        this.num_worker = num_worker;
        this.num_request = num_request;
    }

    public int getNumRequest(){
        return num_request;
    }

    public int getNumWorker(){
        return num_worker;
    }

    public boolean isReady(){
        return (num_worker > 0)? Boolean.TRUE: Boolean.FALSE;
    }

    public static ServerStatus parseResult(String data) throws JSONException {

        JSONObject jObj = new JSONObject(data);
        int _num_worker = jObj.getInt("num_workers_available");
        int _num_request = jObj.getInt("num_requests_processed");
        return new ServerStatus(_num_worker,_num_request);
    }
}

