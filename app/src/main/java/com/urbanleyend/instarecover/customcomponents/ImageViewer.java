package com.urbanleyend.instarecover.customcomponents;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.urbanleyend.instarecover.R;

public class ImageViewer extends LinearLayout {

    private ImageView mImage;
    private ImageView mProfileImage;
    private TextView mTitle;

    public ImageViewer(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ImageViewer, 0, 0);
        a.recycle();

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.image_viewer, this);

        mImage = (ImageView) findViewById(R.id.imageViewerMainImage);
        mProfileImage = (ImageView) findViewById(R.id.imageViewerProfileImage);
        mTitle = (TextView) findViewById(R.id.imageViewerUsername);
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

    public Bitmap getImageBitmap() {
        Bitmap bitmap = ((BitmapDrawable) mImage.getDrawable()).getBitmap();

        return bitmap;
    }
}
