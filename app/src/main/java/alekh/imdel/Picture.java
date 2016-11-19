package alekh.imdel;


public class Picture {

    private int id;
    private String filename;
    private String picturePath;
    private String thumbPath;
    private boolean downloaded = false;

    // dummy constructor for development
    public Picture(int id, String filename, String thumbPath) {
        this.id = id;
        this.filename = filename;
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

    public int getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public boolean isDownloaded() {
        return downloaded;

    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }
}
