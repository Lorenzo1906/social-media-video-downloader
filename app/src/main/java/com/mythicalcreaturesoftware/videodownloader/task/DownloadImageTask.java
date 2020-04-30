package com.mythicalcreaturesoftware.videodownloader.task;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;

public class DownloadImageTask extends AsyncTask<String, Void, File> {

    public AsyncImageResponse delegate = null;

    private final WeakReference<Activity> weakActivity;

    public DownloadImageTask(Activity activity) {
        this.weakActivity = new WeakReference<>(activity);
    }

    @Override
    protected File doInBackground(String... strings) {
        String path = strings[0];
        String fileName = strings[1];
        String urlImage = strings[2];
        boolean isMayorThanQ = Boolean.parseBoolean(strings[3]);

        try {
            URL url = new URL(urlImage);

            URLConnection connection = url.openConnection();

            //5sec
            int timeoutConnection = 5000;
            //30sec
            int timeoutSocket = 30000;

            connection.setReadTimeout(timeoutConnection);
            connection.setConnectTimeout(timeoutSocket);

            InputStream inputStream = connection.getInputStream();
            BufferedInputStream inStream = new BufferedInputStream(inputStream, 1024 * 5);
            FileOutputStream outStream;

            if (isMayorThanQ) {
                Uri uri = Uri.parse(path);
                ParcelFileDescriptor descriptor = weakActivity.get().getContentResolver().openFileDescriptor(uri,"w");
                FileDescriptor fileDescriptor = descriptor.getFileDescriptor();

                outStream = new FileOutputStream(fileDescriptor);
                byte[] buf = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buf)) > 0){
                    outStream.write(buf, 0, bytesRead);
                }

                inputStream.close();
                outStream.close();
                return new File("");
            } else {
                File file = new File(path, fileName);
                file.createNewFile();

                outStream = new FileOutputStream(file);

                byte[] buff = new byte[5 * 1024];

                int len;
                while ((len = inStream.read(buff)) != -1) {
                    outStream.write(buff,0,len);
                }

                outStream.flush();
                outStream.close();
                inStream.close();

                return file;
            }
        } catch(IOException e) {
            System.out.println(e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(File result) {
        delegate.imageProcessFinish(result);
    }
}
