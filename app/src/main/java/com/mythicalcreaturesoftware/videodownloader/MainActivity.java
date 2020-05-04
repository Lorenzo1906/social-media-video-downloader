package com.mythicalcreaturesoftware.videodownloader;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.mythicalcreaturesoftware.videodownloader.customcomponents.ImageDataArrayAdapter;
import com.mythicalcreaturesoftware.videodownloader.model.ImageData;
import com.mythicalcreaturesoftware.videodownloader.task.AsyncResponse;
import com.mythicalcreaturesoftware.videodownloader.task.DownloadWebPageTask;
import com.mythicalcreaturesoftware.videodownloader.util.PermissionsUtil;
import com.mythicalcreaturesoftware.videodownloader.util.PrefManager;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements AsyncResponse {

    private PrefManager pref;
    private PermissionsUtil permissionsUtil;

    private TextInputEditText textInputUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pref = new PrefManager(this);
        permissionsUtil = new PermissionsUtil();

        // Checking for first time launch - before calling setContentView()
        if (pref.isFirstTimeLaunch()) {
            launchHowToUse();
            finish();
        } else {
            if (permissionsUtil.haveWritePermissions(this, this)) {
                permissionsUtil.setWriteAccepted(true);
            } else {
                Toast toast = Toast.makeText(this, R.string.no_allow_to_save, Toast.LENGTH_SHORT);
                toast.show();
            }
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textInputUrl = findViewById(R.id.textInputUrl);
        textInputUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    urlHandle(Objects.requireNonNull(textInputUrl.getText()).toString());
                }
            }
        });

        final ImageButton button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                loadImages();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

       loadImages();
    }

    private void loadImages() {
        String url = getValueFromClipboard();
        textInputUrl.setText(url);
    }

    private String getValueFromClipboard() {
        String url = "";

        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        assert clipboard != null;
        if(clipboard.hasPrimaryClip()){
            ClipData.Item item = Objects.requireNonNull(clipboard.getPrimaryClip()).getItemAt(0);
            url = item.getText().toString();
        }

        return url;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void processFinish( Map<String, Object> output) {
        if (!output.containsKey(DownloadWebPageTask.DATA)) {
            Toast toast = Toast.makeText(this, R.string.image_not_found, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        List<ImageData> data = (List<ImageData>) output.get(DownloadWebPageTask.DATA);
        ArrayAdapter<ImageData> adapter = new ImageDataArrayAdapter(this, 0, data);

        ListView listView = findViewById(R.id.customListView);
        listView.setAdapter(adapter);
    }

    private void launchHowToUse() {
        pref.setFirstTimeLaunch(false);
    }

    private void urlHandle(String url) {
        if (!Patterns.WEB_URL.matcher(url).matches()) {
            Toast toast = Toast.makeText(this, R.string.invalid_url, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        DownloadWebPageTask task = new DownloadWebPageTask();
        task.delegate = this;
        task.execute(url);
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults){
        switch(permsRequestCode){
            case 200:
                permissionsUtil.setWriteAccepted(grantResults[0]==PackageManager.PERMISSION_GRANTED);
                break;
        }
    }
}
