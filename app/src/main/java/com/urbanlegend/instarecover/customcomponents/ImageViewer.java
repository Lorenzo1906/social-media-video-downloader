package com.urbanlegend.instarecover.customcomponents;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.urbanlegend.instarecover.R;
import com.urbanlegend.instarecover.task.AsyncVideoResponse;
import com.urbanlegend.instarecover.task.DownloadVideoTask;
import com.urbanlegend.instarecover.util.PermissionsUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;

public class ImageViewer extends LinearLayout implements View.OnClickListener, AsyncVideoResponse {

    private ImageView mImage;
    private ImageView mImageVideoOverlay;
    private ImageView mProfileImage;
    private TextView mTitle;
    private boolean isVideo;
    private String videoUrl;

    public final static String APP_FILENAME_PREFIX = R.string.app_name + "Image";
    public final static String APP_FILENAME_VIDEO_PREFIX = R.string.app_name + "Video";

    public ImageViewer(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ImageViewer, 0, 0);
        a.recycle();

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.image_viewer, this);

        mImage = (ImageView) findViewById(R.id.imageViewerMainImage);
        mImageVideoOverlay = (ImageView) findViewById(R.id.imageViewerPlay);
        mProfileImage = (ImageView) findViewById(R.id.imageViewerProfileImage);
        mTitle = (TextView) findViewById(R.id.imageViewerUsername);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
    }

    public ImageViewer(Context context) {
        this(context, null);
    }

    public void setImage(String imageUrl) {
       Picasso.with(this.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.loading_animation)
                .into(mImage);
    }

    public void setUser(String imageUsername) {
        mTitle.setText(imageUsername);
    }

    public void setProfileImage(String imageUrl) {
        Picasso.with(this.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.loading_animation)
                .into(mProfileImage);
    }

    public void setVideoInfo(boolean isVideo, String videoUrl) {
        if (isVideo) {
            mImageVideoOverlay.setVisibility(View.VISIBLE);
            this.videoUrl = videoUrl;
            this.isVideo = isVideo;

            return;
        }

        mImageVideoOverlay.setVisibility(View.GONE);
        this.isVideo = isVideo;
    }

    public String getVideo() {
        return videoUrl;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public Bitmap getImageBitmap() {
        try{
            Bitmap bitmap = ((BitmapDrawable) mImage.getDrawable()).getBitmap();
            return bitmap;
        } catch (ClassCastException ex) {
            ex.printStackTrace();
            Toast toast = Toast.makeText(this.getContext(), R.string.image_saved_error, Toast.LENGTH_SHORT);
            toast.show();
            return null;
        }
    }

    @Override
    public void onClick(View view) {
        PermissionsUtil permissionsUtil = new PermissionsUtil();
        if (!permissionsUtil.haveWritePermissions(this.getContext(), (Activity) this.getContext())) {
            Toast toast = Toast.makeText(this.getContext(), R.string.no_allow_to_save, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        if (isVideo()) {
            saveVideo();
        } else {
            saveImage();
        }
    }

    private void saveVideo() {
        String videoUrl = getVideo();

        saveVideoToExternalStorage(videoUrl);
    }

    private void saveImage() {
        Bitmap bitmap = getImageBitmap();

        saveImageToExternalStorage(bitmap);
    }

    private void saveVideoToExternalStorage(String videoUrl) {
        try {
            Toast toast = Toast.makeText(this.getContext(), R.string.video_download, Toast.LENGTH_SHORT);
            toast.show();

            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES + "/InstaRecover");
            if (!path.exists()) {
                path.mkdirs();
            }

            String filename = APP_FILENAME_VIDEO_PREFIX + Calendar.getInstance().getTimeInMillis()+".mp4";

            DownloadVideoTask task = new DownloadVideoTask();
            task.delegate = this;
            task.execute(path.getAbsolutePath(), filename, videoUrl);
        } catch (Exception e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(this.getContext(), R.string.video_saved_error, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void saveImageToExternalStorage(Bitmap image) {
        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/InstaRecover");
            if (!path.exists()) {
                path.mkdirs();
            }

            String filename = APP_FILENAME_PREFIX + Calendar.getInstance().getTimeInMillis()+".png";
            File file = new File(path, filename);
            file.createNewFile();
            OutputStream fOut = new FileOutputStream(file);

            image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();

            MediaStore.Images.Media.insertImage(this.getContext().getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());

            Toast toast = Toast.makeText(this.getContext(), R.string.image_saved, Toast.LENGTH_SHORT);
            toast.show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(this.getContext(), R.string.image_saved_error, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void videoProcessFinish(File output) {
        this.getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(output)));

        Toast toast = Toast.makeText(this.getContext(), R.string.video_saved, Toast.LENGTH_SHORT);
        toast.show();
    }
}
