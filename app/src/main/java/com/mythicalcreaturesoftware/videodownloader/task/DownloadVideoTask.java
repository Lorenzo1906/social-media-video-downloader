package com.mythicalcreaturesoftware.videodownloader.task;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadVideoTask extends AsyncTask<String, Void, File> {

    public AsyncVideoResponse delegate = null;

    private final WeakReference<Activity> weakActivity;

    public DownloadVideoTask(Activity activity) {
        this.weakActivity = new WeakReference<>(activity);
    }

    @Override
    protected File doInBackground(String... strings) {
        String path = strings[0];
        String filename = strings[1];
        String urlVideo = strings[2];
        boolean isMayorThanQ = Boolean.parseBoolean(strings[3]);

        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;

        File file;
        try {


            URL url = new URL(urlVideo);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            input = connection.getInputStream();
            String contentType = connection.getHeaderField("Content-Type");

            if (contentType.contains("mp4")) {
                filename = filename + ".mp4";
            }

            if (isMayorThanQ) {
                Uri uri = Uri.parse(path);
                ParcelFileDescriptor descriptor = weakActivity.get().getContentResolver().openFileDescriptor(uri,"w");
                FileDescriptor fileDescriptor = descriptor.getFileDescriptor();

                output = new FileOutputStream(fileDescriptor);
                byte[] buf = new byte[1024];
                int bytesRead;
                while ((bytesRead = input.read(buf)) > 0){
                    output.write(buf, 0, bytesRead);
                }

                input.close();
                output.close();
                file = new File("");
            } else {
                file = new File(path, filename);
                file.createNewFile();

                output = new FileOutputStream(file);

                byte data[] = new byte[4096];
                int count;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    output.write(data, 0, count);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (IOException ignored) {
            }

            if (connection != null) {
                connection.disconnect();
            }
        }

        return file;
    }

    @Override
    protected void onPostExecute(File result) {
        delegate.videoProcessFinish(result);
    }
}
