package com.urbanleyend.instarecover.task;

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
import java.util.Objects;

public class DownloadWebPageTask extends AsyncTask<String, Void, Map<String, Object> > {

    public static final String IMAGE = "image";
    public static final String VIDEO = "video";
    public static final String USERNAME = "username";
    public static final String PROFILE_PIC = "profilePic";
    public static final String IS_VIDEO = "isVideo";

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
        Map<String, Object> result = new HashMap<String, Object>();

        try {
            data = data.replace("window._sharedData = ", "");
            JSONObject jsonData = new JSONObject(data);
            JSONObject jsonEntryData = jsonData.getJSONObject("entry_data");
            JSONArray jsonPostPage = jsonEntryData.optJSONArray("PostPage");
            JSONObject jsonMedia = (JSONObject) jsonPostPage.get(0);
            JSONObject jsonMediaContent = jsonMedia.getJSONObject("media");
            JSONObject jsonOwner = jsonMediaContent.getJSONObject("owner");

            result.put(USERNAME, jsonOwner.getString("username"));
            result.put(PROFILE_PIC, jsonOwner.getString("profile_pic_url"));
            result.put(IMAGE, jsonMediaContent.getString("display_src"));
            result.put(IS_VIDEO, Boolean.parseBoolean(jsonMediaContent.getString("is_video")));

            if ((Boolean) result.get(IS_VIDEO)) {
                result.put(VIDEO, jsonMediaContent.getString("video_url"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(Map<String, Object> result) {
        delegate.processFinish(result);
    }
}
