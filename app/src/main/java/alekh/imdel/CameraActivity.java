package alekh.imdel;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class CameraActivity extends AppCompatActivity {

    public static final int UPLOAD_IMAGE_RESULT_CODE = 2;
    public static final int BACK_PRESSED = 3;
    public static final int CAMERA_FACEING_BACK = 0;
    public static final int CAMERA_FACEING_FRONT = 1;

    private Camera mCamera;
    private CameraPreview mPreview;
    private FrameLayout preview;
    private String imagePath;
    private int cameraId = CAMERA_FACEING_BACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Create an instance of Camera
        mCamera = getCameraInstance(cameraId);
        setCameraDisplayOrientation(this, cameraId, mCamera);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview, 0);

        // Create capture button with font
        Typeface font = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf" );
        Button captureButton = (Button) findViewById(R.id.capture_button);
        captureButton.setTypeface(font);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        mCamera.takePicture(null, null, photoCallback);
                    }
                }
        );

        // Create camera switch button
        Button switchButton = (Button) findViewById(R.id.camera_switch_button);
        switchButton.setTypeface(font);
        switchButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeCamera();
                    }
                }
        );
    }

    private void changeCamera() {
        if (cameraId == CAMERA_FACEING_BACK && Camera.getNumberOfCameras() > 1) {
            cameraId = CAMERA_FACEING_FRONT;
        } else {
            cameraId = CAMERA_FACEING_BACK;
        }
        // Destroy SurfacePreview
        mPreview.surfaceDestroyed(mPreview.getHolder());
        mPreview.getHolder().removeCallback(mPreview);
        mPreview.destroyDrawingCache();
        preview.removeView(mPreview);
        mCamera.stopPreview();
        mCamera.stopPreview();
        mCamera.setPreviewCallback(null);
        mCamera.release();

        // Create new camera object
        mCamera = getCameraInstance(cameraId);
        setCameraDisplayOrientation(this, cameraId, mCamera);
        mPreview = new CameraPreview(CameraActivity.this, mCamera);
        preview.addView(mPreview, 0);
        try {
            mCamera.setPreviewDisplay(mPreview.getHolder());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }


    private void toUploadActivity() {
        try {
            Intent intent = new Intent(this, UploadActivity.class);
            intent.putExtra("imagePath", imagePath);
            intent.putExtra("cameraId", cameraId);
            startActivityForResult(intent, UPLOAD_IMAGE_RESULT_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == UPLOAD_IMAGE_RESULT_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                System.out.println("User pressed Publish");

                String imageText = (String) data.getStringExtra("imageText");
                Intent returnIntent = new Intent();
                returnIntent.putExtra("imagePath", imagePath);
                returnIntent.putExtra("imageText", imageText);
                setResult(Activity.RESULT_OK, returnIntent);

                finish();
            } else {
                System.out.println("User pressed Cancel");
                // Delete canceled image
                File f = new File(imagePath);
                f.delete();
                finish();
            }
        } else {
            System.out.println("Something very bad happened");
        }
    }


    @Override
    public void onBackPressed() {
        releaseCamera();
        setResult(BACK_PRESSED);
        finish();
    }


    @Override
    public void onPause() {
        super.onPause();
        releaseCamera();
    }


    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }


    /** A safe way to get an instance of the Camera object. */
    private static Camera getCameraInstance(int id){
        Camera c = null;
        try {
            c = Camera.open(id); // attempt to get a Camera instance
            Camera.Parameters cParams = c.getParameters();
            Camera.Size photoSize = getBiggestSupportedPictureSizeSmallerThan(cParams, 1980*1080);
            cParams.setPictureSize(photoSize.width, photoSize.height);
            c.setParameters(cParams);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        File storageDir = this.getFilesDir();
        File image = File.createTempFile(
                "captured_image",  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        imagePath = image.getAbsolutePath();

        return image;
    }


    private Camera.PictureCallback photoCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile;
            try {
                pictureFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            try {
                // Save file
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                toUploadActivity(); // Go to UploadActivity
            }
        }
    };


    private static void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }


    private static Camera.Size getBiggestSupportedPictureSizeSmallerThan(Camera.Parameters params, int biggestArea) {
        Camera.Size result = null;

        for (Camera.Size size : params.getSupportedPictureSizes()) {
            int newArea = size.width * size.height;
            if (result == null && newArea < biggestArea) {
                result = size;
            } else {
                int resultArea = result.width * result.height;
                if (newArea < biggestArea && newArea > resultArea) {
                    result = size;
                }
            }
        }
        return result;
    }


}
