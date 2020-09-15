package cn.zerokirby.note.userData;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import cn.zerokirby.note.db.AvatarDatabaseUtil;
import cn.zerokirby.note.db.DatabaseHelper;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class IconUtil {//关于操作图标的方法

    private final int UPLOAD = 3;//上传图片
    private final int CHOOSE_PHOTO = 4;//选择图片
    private final int PHOTO_REQUEST_CUT = 5;//请求裁剪图片
    private Activity activity;
    private ImageView avatar;
    private Uri cropImageUri;

    public IconUtil(Activity activity, ImageView avatar) {
        this.activity = activity;
        this.avatar = avatar;
    }

    public Uri getCropImageUri() {//获取裁剪后图片的uri
        return cropImageUri;
    }

    public void iconClick() {//点击头像后的操作，用于授权外部存储权限
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission
                    .WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, CHOOSE_PHOTO);
        } else
            openAlbum();
    }

    public void openAlbum() {//开启相册
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");//接受图片类型
        activity.startActivityForResult(intent, CHOOSE_PHOTO);
    }

    public void handleImage(Intent data) {//处理接收到的图片
        Uri uri = data.getData();
        startPhotoZoom(uri);
    }

    private void startPhotoZoom(Uri uri) {//图片缩放
        File CropPhoto = new File(activity.getExternalCacheDir(), "crop.jpg");//创建临时文件
        try {
            if (CropPhoto.exists()) {//如果临时文件存在则删除，否则新建
                CropPhoto.delete();
            }
            CropPhoto.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        cropImageUri = Uri.fromFile(CropPhoto);//获取裁剪图片的地址
        Intent intent = new Intent("com.android.camera.action.CROP");//设置intent类型为裁剪图片
        intent.setDataAndType(uri, "image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
        }
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);

        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        //输出的宽高

        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 200);

        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropImageUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        activity.startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    public void displayImage(String imagePath) {//解码并显示图片,同时将图片写入本地数据库
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);//解码位图
            avatar.setImageBitmap(bitmap);//给ImageView设置头像
            DatabaseHelper userDbHelper = new DatabaseHelper("ProgressNote.db", null, 1);
            AvatarDatabaseUtil avatarDatabaseUtil = new AvatarDatabaseUtil(userDbHelper);
            avatarDatabaseUtil.saveImage(bitmap);
        } else
            Toast.makeText(activity, "打开失败", Toast.LENGTH_SHORT).show();
    }

    public void uploadImage(Handler handler) {//上传头像
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(Objects.requireNonNull(UriUtil.getPath(cropImageUri)));
                final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");//设置媒体类型
                DatabaseHelper userDbHelper = new DatabaseHelper("ProgressNote.db", null, 1);
                AvatarDatabaseUtil avatarDatabaseUtil = new AvatarDatabaseUtil(userDbHelper);
                final int id = avatarDatabaseUtil.getUserId();//获取用户id
                userDbHelper.close();
                OkHttpClient client = new OkHttpClient();
                RequestBody fileBody = RequestBody.create(MEDIA_TYPE_JPEG, file);//媒体类型为jpg
                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("userId", String.valueOf(id))
                        .addFormDataPart("file", id + ".jpg", fileBody).build();
                Request request = new Request.Builder().url("https://zerokirby.cn:8443/progress_note_server/UploadAvatarServlet").post(requestBody).build();
                try {
                    Response response = client.newCall(request).execute();
                    Message message = new Message();//发送消息
                    message.what = UPLOAD;
                    handler.sendMessage(message);
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
