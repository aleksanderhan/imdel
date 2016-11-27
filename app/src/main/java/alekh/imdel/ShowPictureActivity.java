package alekh.imdel;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class ShowPictureActivity extends AppCompatActivity {

    private ImageView mImageView;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_picture);

        Typeface font = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf" );
        Button upvoteButton = (Button) findViewById(R.id.upvote_button);
        upvoteButton.setTypeface(font);
        upvoteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }
        );

        Button downvoteButton = (Button) findViewById(R.id.downvote_button);
        downvoteButton.setTypeface(font);
        downvoteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }
        );

        //mTextView = (TextView) findViewById(R.id.display_text);
        //mTextView.setText("asd");

    }


    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mImageView = (ImageView) findViewById(R.id.picture_view);
        String path = getIntent().getExtras().getString("picturePath");
        setPic(path);
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

        mImageView.setImageBitmap(bitmap);
    }
}
