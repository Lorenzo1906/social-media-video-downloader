package com.mythicalcreaturesoftware.videodownloader.customcomponents;

import android.app.Activity;;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mythicalcreaturesoftware.videodownloader.R;
import com.squareup.picasso.Picasso;
import com.mythicalcreaturesoftware.videodownloader.model.ImageData;
import com.mythicalcreaturesoftware.videodownloader.task.AsyncImageResponse;
import com.mythicalcreaturesoftware.videodownloader.task.AsyncVideoResponse;
import com.mythicalcreaturesoftware.videodownloader.task.DownloadImageTask;
import com.mythicalcreaturesoftware.videodownloader.task.DownloadVideoTask;
import com.mythicalcreaturesoftware.videodownloader.util.PermissionsUtil;

import java.io.File;
import java.util.List;

public class ImageDataArrayAdapter extends ArrayAdapter<ImageData> implements AsyncVideoResponse, AsyncImageResponse {

    private Context context;
    private List<ImageData> images;
    private ImageView mImage;
    private ImageView mImageVideoOverlay;
    private ImageView mProfileImage;
    private TextView mTitle;

    public ImageDataArrayAdapter(@NonNull Context context, @LayoutRes int resource, List<ImageData> objects) {
        super(context, resource, objects);

        this.context = context;
        this.images = objects;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ImageData image = images.get(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.image_viewer, null);

        mImage = (ImageView) view.findViewById(R.id.imageViewerMainImage);
        mImageVideoOverlay = (ImageView) view.findViewById(R.id.imageViewerPlay);
        mProfileImage = (ImageView) view.findViewById(R.id.imageViewerProfileImage);
        mTitle = (TextView) view.findViewById(R.id.imageViewerUsername);

        setImage(image.getUrl());
        setUser(image.getUsername());
        setProfileImage(image.getUserImageUrl());
        setVideoInfo(image.isVideo());

        FloatingActionButton button = (FloatingActionButton) view.findViewById(R.id.fab);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PermissionsUtil permissionsUtil = new PermissionsUtil();
                if (!permissionsUtil.haveWritePermissions(view.getContext(), (Activity) view.getContext())) {
                    Toast toast = Toast.makeText(view.getContext(), R.string.no_allow_to_save, Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                if (image.isVideo()) {
                    saveVideoToExternalStorage(image.getVideoUrl(), image.getFilename(), image.getUsername());
                } else {
                    saveImageToExternalStorage(image.getUrl(), image.getFilename(), image.getUsername());
                }
            }
        });

        return view;
    }

    private void saveVideoToExternalStorage(String videoUrl, String fileName, String nickname) {
        try {
            Toast toast = Toast.makeText(this.getContext(), R.string.video_download, Toast.LENGTH_SHORT);
            toast.show();

            boolean isMayorThanQ = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;

            DownloadVideoTask task = new DownloadVideoTask((Activity) context);
            task.delegate = this;
            task.execute(createFilePath(nickname, Environment.DIRECTORY_MOVIES, "video", fileName, isMayorThanQ), fileName, videoUrl, String.valueOf(isMayorThanQ));

        } catch (Exception e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(this.getContext(), R.string.video_saved_error, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void saveImageToExternalStorage(String url, String fileName, String nickname) {

        try {
            Toast toast = Toast.makeText(this.getContext(), R.string.image_download, Toast.LENGTH_SHORT);
            toast.show();

            boolean isMayorThanQ = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;

            DownloadImageTask task = new DownloadImageTask((Activity) context);
            task.delegate = this;
            task.execute(createFilePath(nickname, Environment.DIRECTORY_PICTURES, "image", fileName, isMayorThanQ), fileName, url, String.valueOf(isMayorThanQ));

        } catch (Exception e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(this.getContext(), R.string.video_saved_error, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private String createFilePath (String nickname, String env, String type, String filename, boolean isMayorThanQ) {
        String file = "";

        if (isMayorThanQ) {
            Uri uri;
            ContentValues contentValues = new ContentValues();

            if (type.equals("video")) {
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

                contentValues.put(MediaStore.Video.Media.DISPLAY_NAME, filename);
                contentValues.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
                contentValues.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
                contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/SocialMediaVideoDownloader/" + nickname);
            } else {
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

                contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
                contentValues.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
                contentValues.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SocialMediaVideoDownloader/" + nickname);
            }

            Uri fileUri = context.getContentResolver().insert(uri, contentValues);

            assert fileUri != null;
            file = fileUri.toString();
        } else {
            File fileOld = Environment.getExternalStoragePublicDirectory(env + "/SocialMediaVideoDownloader/" + nickname);

            if (fileOld != null)  {
                if (fileOld.exists() || fileOld.mkdirs()) {
                    file = fileOld.getAbsolutePath();
                }
            }
        }

        return file;
    }

    private void setImage(String imageUrl) {
        Picasso.with(this.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.loading_animation)
                .into(mImage);
    }

    private void setUser(String imageUsername) {
        mTitle.setText(imageUsername);
    }

    private void setProfileImage(String imageUrl) {
        Picasso.with(this.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.loading_animation)
                .into(mProfileImage);
    }

    private void setVideoInfo(boolean isVideo) {
        if (isVideo) {
            mImageVideoOverlay.setVisibility(View.VISIBLE);
            return;
        }

        mImageVideoOverlay.setVisibility(View.GONE);
    }

    @Override
    public void imageProcessFinish(File output) {
        this.getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(output)));

        Toast toast = Toast.makeText(this.getContext(), R.string.image_saved, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void videoProcessFinish(File output) {
        if (output != null) {
            this.getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(output)));

            Toast toast = Toast.makeText(this.getContext(), R.string.video_saved, Toast.LENGTH_SHORT);
            toast.show();
        } else {
            Toast toast = Toast.makeText(this.getContext(), R.string.image_saved_error, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
