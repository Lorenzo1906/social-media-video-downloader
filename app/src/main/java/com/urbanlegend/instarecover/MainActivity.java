package com.urbanlegend.instarecover;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.urbanlegend.instarecover.customcomponents.ImageDataArrayAdapter;
import com.urbanlegend.instarecover.howtouse.WelcomeActivity;
import com.urbanlegend.instarecover.model.ImageData;
import com.urbanlegend.instarecover.task.AsyncResponse;
import com.urbanlegend.instarecover.task.DownloadWebPageTask;
import com.urbanlegend.instarecover.util.PermissionsUtil;
import com.urbanlegend.instarecover.util.PrefManager;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AsyncResponse {

    private PrefManager pref;
    private PermissionsUtil permissionsUtil;

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String url = "";

        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if(clipboard.hasPrimaryClip()== true){
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            url = item.getText().toString();
        }

        urlHandle(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_instructions) {
            launchHowToUse();
            return true;
        }

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

        ListView listView = (ListView) findViewById(R.id.customListView);
        listView.setAdapter(adapter);
    }

    private void launchHowToUse() {
        pref.setFirstTimeLaunch(false);
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
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
