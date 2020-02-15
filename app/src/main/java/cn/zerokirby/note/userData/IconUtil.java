package cn.zerokirby.note.userData;

import android.app.Activity;
import android.net.Uri;
import android.widget.ImageView;


public class IconUtil {

    private static final int UPLOAD = 3;
    private static final int CHOOSE_PHOTO = 4;
    private static final int PHOTO_REQUEST_CUT = 5;
    private Activity activity;
    private ImageView imageView;
    private Uri cropImageUri;

    public IconUtil(Activity activity, ImageView imageView) {
        this.activity = activity;
        this.imageView = imageView;
    }

    public Uri getCropImageUri() {
        return cropImageUri;
    }

}
