package com.marvin.trash;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;


/**
 * ┏┓　   ┏┓
 * ┏┛┻━━━━━┛┻━┓
 * ┃　　　　   ┃
 * ┃　━　━　   ┃
 * ████━████   ┃
 * ┃　　　　   ┃
 * ┃　 ┻　    ┃
 * ┗━┓      ┏━┛
 * 　┃      ┃
 * 　┃ 0BUG ┗━━━┓
 * 　┃0Error     ┣┓
 * 　┃0Warning   ┏┛
 * 　┗┓┓┏━┳┓┏┛ ━
 * 　　┃┫┫ ┃┫┫
 * 　　┗┻┛ ┗┻┛
 * Created by marvin on 2019/6/5
 */
public class DiscoverFragment extends Fragment implements View.OnClickListener {
    private LinearLayout takePhoto;
    private TextView selectFromAlbum;
    private static String requestURL = "http://localhost:9999/Struts_Study/UploadFileServlet";
    private Button selectImage, uploadImage;
    private ImageView imageView;
    private String picPath = null;
    public static final int REQUEST_CAPTURE = 0;
    public static final int REQUEST_ALBUM = 1;
    //    public static final int TAKE_PHOTO = 1;//拍照
//    public static final int CROP_PHOTO = 2;//裁剪
//    public static final int SELECT_PIC = 0;//从相册选择
    private Uri imageUri; //图片路径
    private String filename; //图片名称
    private ImageView ivHead;//头像显示
    private Button btnTakephoto;//拍照
    private Button btnPhotos;//相册
    private Bitmap head;//头像Bitmap
    private static String path = "/sdcard/DemoHead/";//sd路径
    private static final int NONE = 0;
    private static final String IMAGE_UNSPECIFIED = "image/*";
    private static final int PHOTOZOOM = 2; //触发从图库获取图片的按钮
    private static final int PHOTOSHOOT = 1; //触发拍照获取图片的按钮
    private static final int PHOTORESULT = 3;//结果
    private ImageView iView;
    private ComponentName mComponentName;
    String mCurrentPhotoPath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.activity_recognize, null);
        takePhoto = view.findViewById(R.id.take_photo);
        selectFromAlbum = view.findViewById(R.id.select_from_album);
        iView = view.findViewById(R.id.rubbish);
        takePhoto.setOnClickListener(this);
        selectFromAlbum.setOnClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_from_album:
                Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
                intent1.setType("image/*");
                mComponentName = intent1.resolveActivity(this.getActivity().getPackageManager());
                if (mComponentName != null) {
                    startActivityForResult(intent1, REQUEST_ALBUM);
                }
                break;
            case R.id.take_photo:
                Toast.makeText(this.getActivity(), "你点击了这个按钮", Toast.LENGTH_LONG).show();
                String pictureName = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault()).format(new Date()) +
                        "-" + System.currentTimeMillis() + ".jpg";
                File mOutputImage = new File(this.getActivity().getExternalCacheDir(), pictureName);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    imageUri = FileProvider.getUriForFile(this.getActivity(), BuildConfig.APPLICATION_ID + ".provider", mOutputImage);

                    Log.e("陈立中", imageUri.getPath());
                } else {
                    imageUri = Uri.fromFile(mOutputImage);
                }

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                intent.putExtra("return-data", false);
                intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                intent.putExtra("noFaceDetection", true);
                mComponentName = intent.resolveActivity(this.getActivity().getPackageManager());
                if (mComponentName != null) {
                    startActivityForResult(intent, REQUEST_CAPTURE);
                }
                break;
            default:
                break;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);//使得参数传递到之前的activity
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CAPTURE: // 拍照
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(this.getActivity().getContentResolver().openInputStream(imageUri));
                    setPicToView(bitmap);// 保存在SD卡
                    iView.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case REQUEST_ALBUM:
                setPicToView(BitmapFactory.decodeFile(parsePicturePath(this.getActivity(), data.getData())));
                iView.setImageBitmap(BitmapFactory.decodeFile(parsePicturePath(this.getActivity(), data.getData())));
                break;
            default:
                break;
        }
    }

    private void setPicToView(Bitmap mBitmap) {
        String sdStatus = Environment.getExternalStorageState();
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
            return;
        }
        FileOutputStream b = null;
        File file = new File(path);
        file.mkdirs();// 创建文件夹
        String fileName = path + "head.jpg";// 图片名字
        try {
            b = new FileOutputStream(fileName);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                // 关闭流
                b.flush();
                b.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Bitmap bt = getBitmap(path + "head.jpg");
        if (bt != null) {
            @SuppressWarnings("deprecation")
            Drawable drawable = new BitmapDrawable(bt);
            iView.setImageDrawable(drawable);
        }

    }

    private Bitmap getBitmap(String pathString) {
        Bitmap bitmap = null;
        try {
            File file = new File(pathString);
            if (file.exists()) {
                bitmap = BitmapFactory.decodeFile(pathString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    @SuppressLint("NewApi")
    private String parsePicturePath(Context context, Uri uri) {

        if (null == context || uri == null) {
            return null;
        }
        boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentUri
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageDocumentsUri
            if (isExternalStorageDocumentsUri(uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] splits = docId.split(":");
                String type = splits[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + File.separator + splits[1];
                }
            }
            // DownloadsDocumentsUri
            else if (isDownloadsDocumentsUri(uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaDocumentsUri
            else if (isMediaDocumentsUri(uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = "_id=?";
                String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosContentUri(uri)) {
                return uri.getLastPathSegment();
            }
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;

    }

    private String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        String column = "_data";
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;

    }

    private boolean isExternalStorageDocumentsUri(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private boolean isDownloadsDocumentsUri(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private boolean isMediaDocumentsUri(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private boolean isGooglePhotosContentUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * 照片压缩
     *
     * @param uri
     */
    public void startPhotoZoom(Uri uri) {
        //裁剪意图
        Intent intent = new Intent("com.android.camera.action.CROP");
        //设置裁剪的地址和类型
        intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
        //把未裁剪信息附加到intent上
        intent.putExtra("crop", false);
        //设置宽高比例为1:1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        //设置裁剪图片的宽高
        //要返回值
        intent.putExtra("return-data", true);
        //执行意图，赋予请求码
        startActivityForResult(intent, PHOTORESULT);
    }
}
