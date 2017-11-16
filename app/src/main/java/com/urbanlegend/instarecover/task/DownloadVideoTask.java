package com.urbanlegend.instarecover.task;

import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadVideoTask extends AsyncTask<String, Void, File> {

    public AsyncVideoResponse delegate = null;

    @Override
    protected File doInBackground(String... strings) {
        String path = strings[0];
        String filename = strings[1];
        String urlVideo = strings[2];

        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        File file = new File(path, filename);
        try {
            file.createNewFile();

            URL url = new URL(urlVideo);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            input = connection.getInputStream();
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

            return file;
        }
    }

    @Override
    protected void onPostExecute(File result) {
        delegate.videoProcessFinish(result);
    }
}
