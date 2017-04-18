package com.urbanlegend.instarecover.task;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadImageTask extends AsyncTask<String, Void, File> {

    private final int TIMEOUT_CONNECTION = 5000;//5sec
    private final int TIMEOUT_SOCKET = 30000;//30sec

    public AsyncImageResponse delegate = null;

    @Override
    protected File doInBackground(String... strings) {
        String path = strings[0];
        String fileName = strings[1];
        String urlImage = strings[2];

        try {
            File file = new File(path, fileName);
            file.createNewFile();

            URL url = new URL(urlImage);

            URLConnection connection = url.openConnection();

            connection.setReadTimeout(TIMEOUT_CONNECTION);
            connection.setConnectTimeout(TIMEOUT_SOCKET);

            InputStream inputStream = connection.getInputStream();
            BufferedInputStream inStream = new BufferedInputStream(inputStream, 1024 * 5);
            FileOutputStream outStream = new FileOutputStream(file);
            byte[] buff = new byte[5 * 1024];

            int len;
            while ((len = inStream.read(buff)) != -1) {
                outStream.write(buff,0,len);
            }

            outStream.flush();
            outStream.close();
            inStream.close();

            return file;
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
