package alekh.imdel;

import com.loopj.android.http.*;

public class ImdelBackendRestClient {

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, int id,  String token, AsyncHttpResponseHandler responseHandler) {
        if (token != null) {
            client.addHeader("Authorization", "Token " + token);
        }
        client.get(url + Integer.toString(id) + "/", responseHandler);
    }

    public static void post(String url, RequestParams params, String token, AsyncHttpResponseHandler responseHandler) {
        if (token != null) {
            client.addHeader("Authorization", "Token " + token);
        }
        client.post(url, params, responseHandler);
    }

}
