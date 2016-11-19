package alekh.imdel;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.util.List;

import cz.msebera.android.httpclient.Header;

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public ImageButton imageButton;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            imageButton = (ImageButton) itemView.findViewById(R.id.thumbnail);
        }
    }

    // Store a member variable for the pictures
    private List<Picture> mPictures;
    // Store the context for easy access
    private Context mContext;

    // Pass in the picture array into the constructor
    public PictureAdapter(Context context, List<Picture> pictures) {
        mPictures = pictures;
        mContext = context;
    }


    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public PictureAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.picture_item, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(PictureAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        final Picture pic = mPictures.get(position);

        String thumbPath = pic.getThumbPath();
        Bitmap thumb = BitmapFactory.decodeFile(thumbPath);

        ImageButton imageButton = viewHolder.imageButton;
        imageButton.setImageBitmap(thumb);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pic.isDownloaded()) {
                    toShowPictureActivity(pic.getPicturePath());
                } else {
                    v.setClickable(false);
                    downloadPicture(pic);
                    v.setClickable(true);
                }
            }
        });
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        if (mPictures == null) {
            return 0;
        }
        return mPictures.size();
    }


    private void toShowPictureActivity(String path) {
        Intent intent = new Intent(mContext, ShowPictureActivity.class);
        intent.putExtra("picturePath", path);
        mContext.startActivity(intent);
    }


    public void downloadPicture(final Picture pic) {
        String path = String.valueOf(mContext.getFilesDir()) +"/" + pic.getFilename();
        File tempFile = new File(path);

        RequestParams params = new RequestParams();
        params.put("id", pic.getId());

        ImdelBackendRestClient.post("get_picture/", params, new FileAsyncHttpResponseHandler(tempFile) {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                pic.setPicturePath(file.getAbsolutePath());
                pic.setDownloaded(true);
                toShowPictureActivity(pic.getPicturePath());
            }
        });
    }

}




