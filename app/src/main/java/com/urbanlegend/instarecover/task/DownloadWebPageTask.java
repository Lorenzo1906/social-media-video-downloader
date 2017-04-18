package com.urbanlegend.instarecover.task;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class DownloadWebPageTask extends AsyncTask<String, Void, Map<String, Object> > {

    public static final String IMAGE = "image";
    public static final String VIDEO = "video";
    public static final String USERNAME = "username";
    public static final String PROFILE_PIC = "profilePic";
    public static final String IS_VIDEO = "isVideo";
    public static final String FILENAME = "filename";

    public AsyncResponse delegate = null;


    @Override
    protected Map<String, Object> doInBackground(String... urls) {
        Map<String, Object> result = new HashMap<String, Object>();

        for (String tmpUrl : urls) {
            try {
                Document doc  = Jsoup.connect(tmpUrl).get();
                Elements sharedDatas = doc.select("script[type=text/javascript]");

                for (Element sharedData : sharedDatas) {
                    String data = sharedData.data();
                    if (data.contains("window._sharedData")) {
                        result = parseContent(data);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private Map<String, Object> parseContent(String data) {
        Map<String, Object> result = new HashMap<>();

        try {
            data = data.replace("window._sharedData = ", "");
            JSONObject jsonData = new JSONObject(data);
            JSONObject jsonEntryData = jsonData.getJSONObject("entry_data");
            JSONArray jsonPostPage = jsonEntryData.optJSONArray("PostPage");
            JSONObject jsonMedia = (JSONObject) jsonPostPage.get(0);
            JSONObject jsonGraph = jsonMedia.getJSONObject("graphql");
            JSONObject jsonMediaContent = jsonGraph.getJSONObject("shortcode_media");
            JSONObject jsonOwner = jsonMediaContent.getJSONObject("owner");

            result.put(USERNAME, jsonOwner.getString("username"));
            result.put(PROFILE_PIC, jsonOwner.getString("profile_pic_url"));
            result.put(IMAGE, jsonMediaContent.getString("display_url"));
            result.put(FILENAME, getImageFilename(jsonMediaContent.getString("display_url")));
            boolean isVideo = Boolean.parseBoolean(jsonMediaContent.getString("is_video"));
            result.put(IS_VIDEO, isVideo);

            if (isVideo) {
                result.put(VIDEO, jsonMediaContent.getString("video_url"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    private String getImageFilename(String url) {
        String delim = "/";
        StringTokenizer tokenizer = new StringTokenizer(url, delim, true);
        String lastToken = "";

        boolean expectDelim = false;
        while (tokenizer.hasMoreTokens()) {
            lastToken = tokenizer.nextToken();
            if (delim.equals(lastToken)) {
                if (expectDelim) {
                    expectDelim = false;
                    continue;
                } else {
                    lastToken = null;
                }
            }

            expectDelim = true;
        }

        return lastToken;
    }

    @Override
    protected void onPostExecute(Map<String, Object> result) {
        delegate.processFinish(result);
    }
}
