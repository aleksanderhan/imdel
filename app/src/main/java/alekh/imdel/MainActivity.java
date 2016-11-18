package alekh.imdel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity {

    public static final int TAKE_PICTURE_RESULT_CODE = 1;

    private GPSTracker gps;

    private ArrayList<Picture> pictures;
    private PictureAdapter pictureAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start gps tracker
        gps = new GPSTracker(this);

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


        try {
            RequestParams params = new RequestParams();
            params.put("latitude", gps.getLatitude());
            params.put("longitude", gps.getLongitude());
            params.put("radius", "1");
            params.put("amount", "3");
            params.put("offset", "0");
            ImdelBackendRestClient.post("get_thumbnails", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    // If the response is JSONObject instead of expected JSONArray
                    System.out.println(1);
                    System.out.println(response);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                    // Pull out the first event on the public timeline
                    System.out.println(2);
                    System.out.println(response);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


        // making dummy data
        pictures = new ArrayList<Picture>();
        for (int i=0; i<20; i++) {
            pictures.add(new Picture("path"));
        }


        RecyclerView rvPictures = (RecyclerView) findViewById(R.id.picture_view);
        // Create adapter passing in the sample user data
        pictureAdapter = new PictureAdapter(this, pictures);
        // Attach the adapter to the recyclerview to populate items
        rvPictures.setAdapter(pictureAdapter);
        final GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        // Set layout manager to position the items
        rvPictures.setLayoutManager(layoutManager);
        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                //loadNextDataFromApi(totalItemsCount);
            }
        };

        // Add scroll listener to recyclerview
        rvPictures.addOnScrollListener(scrollListener);

    }


    public void loadNextDataFromApi(int offset) {
        // Send an API request to retrieve appropriate paginated data
        //  --> Send the request including an offset value (i.e `page`) as a query parameter.
        //  --> Deserialize and construct new model objects from the API response
        //  --> Append the new data objects to the existing set of items inside the array of items
        //  --> Notify the adapter of the new items made with `notifyItemRangeInserted()`
        int n = 20;
        for (int i=0; i<n; i++) {
            System.out.println(pictures.size());
            pictures.add(new Picture("path"));
        }
        pictureAdapter.notifyItemRangeInserted(offset, n);
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
