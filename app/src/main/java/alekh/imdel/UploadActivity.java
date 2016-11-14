package alekh.imdel;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


public class UploadActivity extends AppCompatActivity {

    private ImageView mImageView;
    private Bitmap rotatedBMP;
    private EditText addText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // TextEdit
        addText = (EditText)findViewById(R.id.addText);

        // Set font
        Typeface font = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf" );

        // Create upload button
        Button uploadButton = (Button) findViewById(R.id.publish_button);
        uploadButton.setTypeface(font);
        uploadButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            String imageText = addText.getText().toString();

                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("imageText", imageText);
                            setResult(Activity.RESULT_OK, returnIntent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            finish();
                        }
                    }
                }
        );

        // Create cancel button
        Button cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setTypeface(font);
        cancelButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent returnIntent = new Intent();
                            setResult(Activity.RESULT_CANCELED, returnIntent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            finish();
                        }
                    }
                }
        );
    }


    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mImageView = (ImageView) findViewById(R.id.captured_view);
        String imagePath = getIntent().getExtras().getString("imagePath");

        if (rotatedBMP == null) {
            setPic(imagePath);
        } else {
            rotatedBMP.recycle();
        }
    }


    private void setPic(String imagePath) {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);

        Matrix mtx = new Matrix();
        mtx.postRotate(90);
        // Rotating Bitmap
        rotatedBMP = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mtx, true);

        if (rotatedBMP != bitmap) {
            bitmap.recycle();
        }

        mImageView.setImageBitmap(rotatedBMP);
    }








}
