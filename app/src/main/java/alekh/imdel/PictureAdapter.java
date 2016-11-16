package alekh.imdel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.ArrayList;

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

    // Store a member variable for the contacts
    private ArrayList<Picture> mPictures;
    // Store the context for easy access
    private Context mContext;

    // Pass in the picture array into the constructor
    public PictureAdapter(Context context, ArrayList<Picture> contacts) {
        mPictures = contacts;
        mContext = context;
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
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
        Picture pic = mPictures.get(position);

        //String thumbPath = pic.getPath();
        //Bitmap thumb = BitmapFactory.decodeFile(thumbPath, bmOptions);
        Bitmap thumb = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.test_thumb);

        ImageButton imageButton = viewHolder.imageButton;
        imageButton.setImageBitmap(thumb);
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mPictures.size();
    }
}




