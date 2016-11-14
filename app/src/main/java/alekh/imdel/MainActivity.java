package alekh.imdel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

public class MainActivity extends AppCompatActivity {

    private static final int TAKE_PICTURE_RESULT_CODE = 1;

    private GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create capture button with font
        Typeface font = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf" );
        Button captureButton = (Button) findViewById(R.id.to_camera_button);
        captureButton.setTypeface(font);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toCameraActivity();
                    }
                }
        );

        gps = new GPSTracker(this);

    }

    public void toCameraActivity() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivityForResult(intent, TAKE_PICTURE_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == TAKE_PICTURE_RESULT_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                String imageText = data.getStringExtra("imageText");
                String imagePath = data.getStringExtra("imagePath");
                try {
                    sendPhoto(imageText, imagePath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("fuck");
            }
        } else {
            System.out.println("wtf");
        }
    }

    private void sendPhoto(String imageText, String imagePath) throws Exception {
        if (gps.isGPSEnabled()) {
            // Rounds coordinates to 6 decimals
            String latitude = Double.toString((double)Math.round(gps.getLatitude() * 1000000d) / 1000000d);
            String longitude = Double.toString((double)Math.round(gps.getLongitude() * 1000000d) / 1000000d);

            Bitmap bitmap = createBitmap(imagePath);

            UploadTask task = new UploadTask();
            task.setData(imageText, latitude, longitude);
            task.execute(bitmap);
        } else {
            System.out.println("gps not enabled");
            // TODO: turn on gps or something
        }

    }


    private class UploadTask extends AsyncTask<Bitmap, Void, Void> {

        private static final String TAG = "upload";

        private String imageText;
        private String latitude;
        private String longitude;

        public void setData(String imageText, String latitude, String longitude) {
            this.imageText = imageText;
            this.latitude = latitude;
            this.longitude = longitude;
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
                HttpPost httppost = new HttpPost(getString(R.string.upload_server)); // server

                // Create an image file name
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + ".jpg";

                MultipartEntity reqEntity = new MultipartEntity();
                reqEntity.addPart("image", imageFileName, in);
                reqEntity.addPart("", ""); // Has to be included for the text part to be received unknown reasons
                reqEntity.addPart("latitude", latitude);
                reqEntity.addPart("longitude", longitude);
                System.out.println(latitude);
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

    private Bitmap createBitmap(String imagePath) {
        return BitmapFactory.decodeFile(imagePath);
    }


}
