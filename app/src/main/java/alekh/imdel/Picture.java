package alekh.imdel;


public class Picture {

    private String picturePath;
    private String thumbPath;
    //private int votes; etc etc

    // dummy constructor for development
    public Picture(String thumbPath) {
        this.thumbPath = thumbPath;
    }

    public String getThumbPath() {
        return thumbPath;
    }

    public String getPicturePath() {
        return picturePath;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }
}
