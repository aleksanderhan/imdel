package alekh.imdel;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

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

    public static final int TAKE_PICTURE_REQUEST_CODE = 1;
    public static final int LOGIN_REQUEST_CODE = 2;

    private GPSTracker gps;
    private String token;
    private int userID;

    private ArrayList<Picture> pictures;
    private PictureAdapter pictureAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start gps tracker
        gps = new GPSTracker(this);

        // Login/Register
        toLogin();

        // Delete all files from previous sessions
        deleteRecursively(this.getFilesDir());

        // Create capture button with font
        Typeface font = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf" );
        final Button captureButton = (Button) findViewById(R.id.to_camera_button);
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

        // Create settings button
        Button settingsButton = (Button) findViewById(R.id.settings_button);
        settingsButton.setTypeface(font);
        settingsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }
        );

        // Create refresh button
        Button refreshButton = (Button) findViewById(R.id.refresh_button);
        refreshButton.setTypeface(font);
        refreshButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }
        );

        // Create dropdown menu
        Spinner dropdown = (Spinner)findViewById(R.id.sort_by_menu);
        String[] items = new String[]{"Distance", "Date", "Vote"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);


        // Create empty picture array
        pictures = new ArrayList<Picture>();

        RecyclerView rvPictures = (RecyclerView) findViewById(R.id.picture_view);

        // Create adapter passing in the sample user data
        pictureAdapter = new PictureAdapter(this, pictures, token);

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
                getThumbs(21, totalItemsCount);
            }
        };

        // Add scroll listener to recyclerview
        rvPictures.addOnScrollListener(scrollListener);

    }


    public void toLogin() {
        Intent loginIntent = new Intent(this, Login.class);
        startActivityForResult(loginIntent, LOGIN_REQUEST_CODE);
    }


    public void toCameraActivity() {
        Intent cameraIntent = new Intent(this, CameraActivity.class);
        startActivityForResult(cameraIntent, TAKE_PICTURE_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == TAKE_PICTURE_REQUEST_CODE) {
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
                System.out.println("back pressed");
            } else {
                // Go back to CameraActivity instead of to MainActivity
                toCameraActivity();
            }
        } else if (requestCode == LOGIN_REQUEST_CODE){
            token = data.getStringExtra("token");
            userID = data.getIntExtra("user_id", -1);

            // Load in the first data from the backend
            getThumbs(21, 0);
        } else {

        }
    }


    private void sendPhoto(String imageText, String imagePath) throws Exception {
        if (gps.isGPSEnabled()) {
            File image = new File(imagePath);

            RequestParams params = new RequestParams();
            params.put("latitude", gps.getLatitude());
            params.put("longitude", gps.getLongitude());
            params.put("imageText", imageText);
            params.put("image", image);
            params.put("publisher", userID);

            ImdelBackendRestClient.post(getString(R.string.publish_url), params, token, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Toast.makeText(getApplicationContext(), "Photo successfully shared.", Toast.LENGTH_LONG).show();
                    System.out.println(response);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Toast.makeText(getApplicationContext(), "Photo not shared, try again.", Toast.LENGTH_LONG).show();
                    System.out.println(errorResponse);
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "GPS not enabled.", Toast.LENGTH_LONG).show();
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
            ImdelBackendRestClient.post(getString(R.string.fetch_thumbnail_url), params, token, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        for (int i=1; i<=response.length(); i++) {
                            JSONObject json = (JSONObject) response.get(Integer.toString(i));
                            byte[] base64Thumb= Base64.decode((String) json.get("base64Thumb"), 0);

                            int id = (int) json.get("id");
                            String filename = (String) json.get("filename");
                            System.out.println("filename: " + filename);
                            String thumbPath = getApplicationContext().getFilesDir() + "/" + "thumb_" + filename;
                            System.out.println("thumb path: " + thumbPath);
                            String pub_date = (String) json.get("published_date");
                            String text = (String) json.get("text");
                            try {
                                FileOutputStream stream = new FileOutputStream(thumbPath);
                                stream.write(base64Thumb);
                                stream.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            pictures.add(new Picture(id, filename, thumbPath, pub_date, text));
                        }
                        pictureAdapter.notifyItemRangeInserted(offset, amount);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    System.out.println(errorResponse);
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
