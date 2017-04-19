package com.urbanlegend.instarecover.customcomponents;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.urbanlegend.instarecover.R;
import com.urbanlegend.instarecover.model.ImageData;
import com.urbanlegend.instarecover.task.AsyncImageResponse;
import com.urbanlegend.instarecover.task.AsyncVideoResponse;
import com.urbanlegend.instarecover.task.DownloadImageTask;
import com.urbanlegend.instarecover.task.DownloadVideoTask;
import com.urbanlegend.instarecover.util.PermissionsUtil;

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
                    saveVideoToExternalStorage(image.getVideoUrl(), image.getFilename());
                } else {
                    saveImageToExternalStorage(image.getUrl(), image.getFilename());
                }
            }
        });

        return view;
    }

    private void saveVideoToExternalStorage(String videoUrl, String fileName) {
        try {
            Toast toast = Toast.makeText(this.getContext(), R.string.video_download, Toast.LENGTH_SHORT);
            toast.show();

            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES + "/InstaRecover");
            if (!path.exists()) {
                path.mkdirs();
            }

            DownloadVideoTask task = new DownloadVideoTask();
            task.delegate = this;
            task.execute(path.getAbsolutePath(), fileName, videoUrl);
        } catch (Exception e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(this.getContext(), R.string.video_saved_error, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void saveImageToExternalStorage(String url, String fileName) {

        try {
            Toast toast = Toast.makeText(this.getContext(), R.string.image_download, Toast.LENGTH_SHORT);
            toast.show();

            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/InstaRecover");
            if (!path.exists()) {
                path.mkdirs();
            }

            DownloadImageTask task = new DownloadImageTask();
            task.delegate = this;
            task.execute(path.getAbsolutePath(), fileName, url);
        } catch (Exception e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(this.getContext(), R.string.video_saved_error, Toast.LENGTH_SHORT);
            toast.show();
        }
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
        this.getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(output)));

        Toast toast = Toast.makeText(this.getContext(), R.string.video_saved, Toast.LENGTH_SHORT);
        toast.show();
    }
}
