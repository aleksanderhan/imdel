package alekh.imdel;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class HttpClient {

    private static final String SERVER_DOMAIN = "imdel.tk";
    private static final String SERVER_PORT = "8000";
    private String serverIP;

    private String getUploadUrl() throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getByName(SERVER_DOMAIN);
        return inetAddress.getHostAddress() + ":" + SERVER_PORT + R.string.upload_path;
    }
}
