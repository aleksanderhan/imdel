package alekh.imdel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.File;


public class MainActivity extends AppCompatActivity {

    public static final int TAKE_PICTURE_RESULT_CODE = 1;

    private GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Delete all files from previous sessions
        deleteRecursively(this.getFilesDir());

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
            } else if (resultCode == CameraActivity.BACK_PRESSED) {
                System.out.println("back pressed!");
            } else {
                // Go back to CameraActivity instead of to MainActivity
                toCameraActivity();
            }
        } else {
            System.out.println("wtf?!");
        }
    }

    private void sendPhoto(String imageText, String imagePath) throws Exception {
        if (gps.isGPSEnabled()) {
            // Rounds coordinates to 6 decimals
            String latitude = Double.toString((double)Math.round(gps.getLatitude() * 1000000d) / 1000000d);
            String longitude = Double.toString((double)Math.round(gps.getLongitude() * 1000000d) / 1000000d);

            Bitmap bitmap = createBitmap(imagePath);

            UploadTask task = new UploadTask();
            task.setData(this, imageText, latitude, longitude);
            task.execute(bitmap);
        } else {
            System.out.println("gps not enabled");
            // TODO: turn on gps or something
        }
    }


    private Bitmap createBitmap(String imagePath) {
        return BitmapFactory.decodeFile(imagePath);
    }




    void listFiles(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                listFiles(child);

        System.out.println(fileOrDirectory.getAbsolutePath());
    }

    void deleteRecursively(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursively(child);

        fileOrDirectory.delete();
    }



}
