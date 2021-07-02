package com.mythicalcreaturesoftware.videodownloader.task;

import android.os.AsyncTask;

import com.mythicalcreaturesoftware.videodownloader.model.ImageData;
import com.mythicalcreaturesoftware.videodownloader.util.CookieManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class DownloadWebPageTask extends AsyncTask<String, Void, Map<String, Object> > {

    public static final String DATA = "data";

    public AsyncResponse delegate = null;


    @Override
    protected Map<String, Object> doInBackground(String... urls) {
        Map<String, Object> result = new HashMap<>();

        for (String tmpUrl : urls) {
            if (tmpUrl.contains("tiktok")) {
                result = processTikTok(tmpUrl);
            } else if (tmpUrl.contains("instagram")) {
                result = processInstagram(tmpUrl);
            }

        }

        return result;
    }

    private Map<String, Object> processTikTok(String url) {
        Map<String, Object> result = new HashMap<>();

        try {
            Document doc  = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("https://www.tiktok.com/")
                    .cookies(CookieManager.getInstance().getCookies())
                    .get();

            Elements sharedDatas = doc.select("#videoObject");
            for (Element sharedData : sharedDatas) {
                String data = sharedData.data();
                result = parseContentTikTokSecondOption(data);
            }

            if (result.size() == 0) {
                sharedDatas = doc.select("script");

                for (Element sharedData : sharedDatas) {
                    if (sharedData.id().equals("__NEXT_DATA__")) {
                        String data = sharedData.data();
                        result = parseContentTikTokFirstOption(data);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private Map<String, Object> processInstagram(String url) {
        Map<String, Object> result = new HashMap<>();

        try {
            Document doc  = Jsoup.connect(url).get();
            Elements sharedDatas = doc.select("script[type=text/javascript]");

            for (Element sharedData : sharedDatas) {
                String data = sharedData.data();
                if (data.contains("window._sharedData")) {
                    result = parseContentInstagram(data);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private Map<String, Object> parseContentTikTokFirstOption(String data) {
        Map<String, Object> result = new HashMap<>();
        List<ImageData> resultData = new ArrayList<>();

        try {
            ImageData imageData = new ImageData();

            JSONObject jsonData = new JSONObject(data);
            JSONObject jsonEntryData = jsonData.getJSONObject("props");
            JSONObject pageProps = jsonEntryData.getJSONObject("pageProps");
            JSONObject itemInfo = pageProps.getJSONObject("itemInfo");
            JSONObject itemStruct  = itemInfo.getJSONObject("itemStruct");
            JSONObject video  = itemStruct.getJSONObject("video");
            JSONObject author  = itemStruct.getJSONObject("author");

            imageData.setVideo(true);
            imageData.setVideoUrl(video.getString("playAddr"));
            imageData.setUrl(video.getString("cover"));

            imageData.setUsername(author.getString("nickname"));
            imageData.setUserImageUrl(author.getString("avatarMedium"));
            imageData.setFilename(author.getString("uniqueId")+"-"+video.getString("id")+"."+video.getString("format"));

            resultData.add(imageData);

            result.put(DATA, resultData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    private Map<String, Object> parseContentTikTokSecondOption(String data) {
        Map<String, Object> result = new HashMap<>();
        List<ImageData> resultData = new ArrayList<>();

        try {
            ImageData imageData = new ImageData();

            JSONObject jsonData = new JSONObject(data);
            JSONObject creator = jsonData.getJSONObject("creator");
            JSONArray thumbnails = jsonData.optJSONArray("thumbnailUrl");

            imageData.setVideo(true);
            imageData.setVideoUrl(jsonData.getString("contentUrl"));
            imageData.setUrl(thumbnails.getString(0));

            imageData.setUsername(creator.getString("alternateName"));
            imageData.setFilename(getImageFilename(imageData.getVideoUrl()));

            resultData.add(imageData);

            result.put(DATA, resultData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    private Map<String, Object> parseContentInstagram(String data) {
        Map<String, Object> result = new HashMap<>();
        List<ImageData> resultData = new ArrayList<>();

        try {
            data = data.replace("window._sharedData = ", "");


            JSONObject jsonData = new JSONObject(data);
            JSONObject jsonEntryData = jsonData.getJSONObject("entry_data");
            JSONArray jsonPostPage = jsonEntryData.optJSONArray("PostPage");
            JSONObject jsonMedia = (JSONObject) jsonPostPage.get(0);
            JSONObject jsonGraph = jsonMedia.getJSONObject("graphql");
            JSONObject jsonMediaContent = jsonGraph.getJSONObject("shortcode_media");
            JSONObject jsonOwner = jsonMediaContent.getJSONObject("owner");
            JSONObject jsonSideCar = jsonMediaContent.optJSONObject("edge_sidecar_to_children");

            if (jsonSideCar != null) {
                JSONArray edges = jsonSideCar.optJSONArray("edges");

                for (int i = 0; i < edges.length(); i++) {
                    JSONObject edge = edges.getJSONObject(i);
                    JSONObject image = edge.getJSONObject("node");

                    ImageData imageData = new ImageData();
                    imageData.setUrl(image.getString("display_url"));
                    imageData.setFilename(getImageFilename(image.getString("display_url")));
                    imageData.setUserImageUrl(jsonOwner.getString("profile_pic_url"));
                    imageData.setUsername(jsonOwner.getString("username"));
                    boolean isVideo = Boolean.parseBoolean(image.getString("is_video"));
                    imageData.setVideo(isVideo);
                    if (isVideo) {
                        imageData.setVideoUrl(image.getString("video_url"));
                        imageData.setFilename(getImageFilename(image.getString("video_url")));
                    }

                    resultData.add(imageData);
                }
            } else {
                ImageData tmpDataSingle = new ImageData();
                tmpDataSingle.setUsername(jsonOwner.getString("username"));
                tmpDataSingle.setUserImageUrl(jsonOwner.getString("profile_pic_url"));
                tmpDataSingle.setUrl(jsonMediaContent.getString("display_url"));
                tmpDataSingle.setFilename(getImageFilename(jsonMediaContent.getString("display_url")));
                boolean isVideo = Boolean.parseBoolean(jsonMediaContent.getString("is_video"));
                tmpDataSingle.setVideo(isVideo);
                if (isVideo) {
                    tmpDataSingle.setVideoUrl(jsonMediaContent.getString("video_url"));
                    tmpDataSingle.setFilename(getImageFilename(jsonMediaContent.getString("video_url")));
                }

                resultData.add(tmpDataSingle);
            }

            result.put(DATA, resultData);
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
