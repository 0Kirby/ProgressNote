package cn.zerokirby.note;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import cn.zerokirby.note.db.AvatarDatabaseUtil;
import cn.zerokirby.note.db.DatabaseHelper;
import cn.zerokirby.note.userData.UriUtil;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class IconActivity extends BaseActivity {

    private static final int UPLOAD = 0;
    private static final int CHOOSE_PHOTO = 1;
    private static final int PHOTO_REQUEST_CUT = 2;
    private Handler handler;
    private ImageView imageView;
    private Uri cropImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icon);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        imageView = findViewById(R.id.user_icon);
        //每次先从数据库中读取头像
        DatabaseHelper userDbHelper = new DatabaseHelper(IconActivity.this, "ProgressNote.db", null, 1);
        AvatarDatabaseUtil avatarDatabaseUtil = new AvatarDatabaseUtil(this, userDbHelper);
        byte[] imgData = avatarDatabaseUtil.readImage();
        if (imgData != null) {
            //将字节数组转化为位图
            Bitmap bitmap = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
            //将位图显示为图片
            imageView.setImageBitmap(bitmap);
        }

        final Button button = findViewById(R.id.change_icon);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(IconActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(IconActivity.this, new String[]{Manifest.permission
                            .WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, CHOOSE_PHOTO);
                } else
                    openAlbum();
            }
        });

        handler = new Handler(new Handler.Callback() {//用于异步消息处理

            @Override
            public boolean handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case UPLOAD:
                        Toast.makeText(IconActivity.this, "上传成功！", Toast.LENGTH_SHORT).show();//显示解析到的内容
                        break;
                }
                return false;
            }
        });

    }

    private void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "打开成功", Toast.LENGTH_SHORT).show();
                    handleImage(data);
                } else
                    Toast.makeText(this, "取消操作", Toast.LENGTH_SHORT).show();
                break;
            case PHOTO_REQUEST_CUT:
                if (resultCode == RESULT_OK) {
                    displayImage(UriUtil.getPath(this, cropImageUri));
                    uploadImage();
                } else
                    Toast.makeText(this, "取消操作", Toast.LENGTH_SHORT).show();
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CHOOSE_PHOTO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "未授权外置存储读写权限，无法使用！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    private void handleImage(Intent data) {//处理接收到的图片
        String imagePath = null;
        Uri uri = data.getData();
        startPhotoZoom(uri);
    }

    public void startPhotoZoom(Uri uri) {
        File CropPhoto = new File(getExternalCacheDir(), "crop.jpg");
        try {
            if (CropPhoto.exists()) {
                CropPhoto.delete();
            }
            CropPhoto.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        cropImageUri = Uri.fromFile(CropPhoto);
        Intent intent = new Intent("com.android.camera.action.CROP");
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
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    private void displayImage(String imagePath) {//解码并显示图片,同时将图片写入本地数据库
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            imageView.setImageBitmap(bitmap);
            DatabaseHelper userDbHelper = new DatabaseHelper(IconActivity.this, "ProgressNote.db", null, 1);
            AvatarDatabaseUtil avatarDatabaseUtil = new AvatarDatabaseUtil(this, userDbHelper);
            avatarDatabaseUtil.saveImage(bitmap);
        } else
            Toast.makeText(this, "打开失败", Toast.LENGTH_SHORT).show();
    }

    public void uploadImage() {//上传头像
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(Objects.requireNonNull(UriUtil.getPath(IconActivity.this, cropImageUri)));
                final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");//设置媒体类型
                DatabaseHelper userDbHelper = new DatabaseHelper(IconActivity.this, "ProgressNote.db", null, 1);
                AvatarDatabaseUtil avatarDatabaseUtil = new AvatarDatabaseUtil(IconActivity.this, userDbHelper);
                final int id = avatarDatabaseUtil.getUserId();//获取用户id
                OkHttpClient client = new OkHttpClient();
                RequestBody fileBody = RequestBody.create(MEDIA_TYPE_JPEG, file);//媒体类型未jpg
                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("userId", String.valueOf(id))
                        .addFormDataPart("file", id + ".jpg", fileBody).build();
                Request request = new Request.Builder().url("https://0kirby.ga:8443/progress_note_server/UploadAvatarServlet").post(requestBody).build();
                try {
                    Response response = client.newCall(request).execute();
                    Message message = new Message();//发送消息
                    message.what = UPLOAD;
                    handler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
