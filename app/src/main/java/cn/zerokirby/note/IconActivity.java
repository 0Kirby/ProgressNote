package cn.zerokirby.note;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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

import java.util.Objects;

import cn.zerokirby.note.db.AvatarDatabaseUtil;
import cn.zerokirby.note.db.UserDatabaseHelper;
import cn.zerokirby.note.userData.UriUtil;

public class IconActivity extends BaseActivity {

    public static final int CHOOSE_PHOTO = 2;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icon);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        imageView = findViewById(R.id.user_icon);

        UserDatabaseHelper userDbHelper = new UserDatabaseHelper(IconActivity.this, "User.db", null, 1);
        AvatarDatabaseUtil avatarDatabaseUtil = new AvatarDatabaseUtil(this, userDbHelper);
        byte[] imgData = avatarDatabaseUtil.readImage();
        if (imgData != null) {
            //将字节数组转化为位图
            Bitmap imagebitmap = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
            //将位图显示为图片
            imageView.setImageBitmap(imagebitmap);
        }

        final Button button = findViewById(R.id.change_icon);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(IconActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(IconActivity.this, new String[]{Manifest.permission
                            .WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, CHOOSE_PHOTO);
                } else
                    openAlbum();
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
        imagePath = UriUtil.getPath(this, uri);
        displayImage(imagePath);
    }

    private void displayImage(String imagePath) {//解码并显示图片,同时将图片写入本地数据库
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            imageView.setImageBitmap(bitmap);
            UserDatabaseHelper userDbHelper = new UserDatabaseHelper(IconActivity.this, "User.db", null, 1);
            AvatarDatabaseUtil avatarDatabaseUtil = new AvatarDatabaseUtil(this, userDbHelper);
            avatarDatabaseUtil.saveImage(bitmap);
        } else
            Toast.makeText(this, "打开失败", Toast.LENGTH_SHORT).show();
    }
}
