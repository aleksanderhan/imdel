package alekh.imdel;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class UploadTask extends AsyncTask<Bitmap, Void, Void> {

    private static final String TAG = "upload";

    private String imageText;
    private String latitude;
    private String longitude;
    private Context context;

    public void setData(Context context, String imageText, String latitude, String longitude) {
        this.imageText = imageText;
        this.latitude = latitude;
        this.longitude = longitude;
        this.context = context;
    }

    protected Void doInBackground(Bitmap... bitmaps) {
        if (bitmaps[0] == null) {
            return null;
        }

        Bitmap bitmap = bitmaps[0];
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream); // convert Bitmap to ByteArrayOutputStream
        InputStream in = new ByteArrayInputStream(stream.toByteArray()); // convert ByteArrayOutputStream to ByteArrayInputStream

        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
            String serverAddress = context.getResources().getString(R.string.upload_server);
            HttpPost httppost = new HttpPost(serverAddress); // server

            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + ".jpg";

            MultipartEntity reqEntity = new MultipartEntity();
            reqEntity.addPart("image", imageFileName, in);
            reqEntity.addPart("", ""); // Has to be included for the text part to be received unknown reasons
            reqEntity.addPart("latitude", latitude);
            reqEntity.addPart("longitude", longitude);
            reqEntity.addPart("imageText", imageText);
            httppost.setEntity(reqEntity);

            Log.i(TAG, "request " + httppost.getRequestLine());
            HttpResponse response = null;
            try {
                response = httpclient.execute(httppost);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (response != null)
                    Log.i(TAG, "response " + response.getStatusLine().toString());
            } finally {

            }

        } finally {

        }

        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        //Toast.makeText(MainActivity.this, R.string.uploaded, Toast.LENGTH_LONG).show();
    }
}