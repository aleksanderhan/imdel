package alekh.imdel;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;
import android.widget.Button;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
        captureButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setClickable(false);
                        toCameraActivity();
                        v.setClickable(true);
                    }
                }
        );

        pictures = new ArrayList<Picture>();
        getThumbs(9, 0);

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
                getThumbs(9, totalItemsCount);
            }
        };

        // Add scroll listener to recyclerview
        rvPictures.addOnScrollListener(scrollListener);

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
        // TODO: could be moved back to upload activity together with longitude and latitude ?
        if (gps.isGPSEnabled()) {
            File image = new File(imagePath);

            RequestParams params = new RequestParams();
            params.put("latitude", gps.getLatitude());
            params.put("longitude", gps.getLongitude());
            params.put("imageText", imageText);
            params.put("image", image);

            ImdelBackendRestClient.post("upload_image/", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    System.out.println(response);
                }
            });
        } else {
            System.out.println("gps not enabled");
            // TODO: turn on gps or something
        }
    }


    private void getThumbs(final int amount, final int offset) {
        try {
            RequestParams params = new RequestParams();
            params.put("latitude", gps.getLatitude());
            params.put("longitude", gps.getLongitude());
            params.put("radius", "1");
            params.put("amount", amount);
            params.put("offset", offset);
            ImdelBackendRestClient.post("get_thumbnails", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        for (int i=1; i<=response.length(); i++) {
                            JSONObject json = (JSONObject) response.get(Integer.toString(i));
                            byte[] base64Thumb= Base64.decode((String) json.get("base64Thumb"), 0);

                            String filename = (String) json.get("filename");
                            String thumbFileName = "thumb_" + filename;
                            String thumbPath = getApplicationContext().getFilesDir() + "/" + thumbFileName;
                            try {
                                FileOutputStream stream = new FileOutputStream(thumbPath);
                                stream.write(base64Thumb);
                                stream.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                pictures.add(new Picture((int) json.get("id"), filename, thumbPath));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        pictureAdapter.notifyItemRangeInserted(offset, amount);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable throwable) {
                    System.out.println("failure");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
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
