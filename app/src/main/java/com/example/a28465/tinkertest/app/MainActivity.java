package com.example.a28465.tinkertest.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a28465.tinkertest.BuildConfig;
import com.example.a28465.tinkertest.R;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals;

import java.io.File;


public class MainActivity extends AppCompatActivity {
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;

    TextView txvAppInfo;
    TextView txvPatchInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().hide();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        txvAppInfo = (TextView)findViewById(R.id.txv_app_info);
        txvPatchInfo = (TextView)findViewById(R.id.txv_patch_info);

        initialInfoTxv(getApplicationContext());
        initialPatchInfoTxv();
        permissionCheck();
    }

    public void initialInfoTxv(Context context) {
        // add more Build Info
        StringBuilder sb = new StringBuilder();
        Tinker tinker = Tinker.with(getApplicationContext());
        if (tinker.isTinkerLoaded()) {
            sb.append(String.format("[patch is loaded] \n"));
            sb.append(String.format("versionName %s \n",getVersion()));
            sb.append(String.format("[buildConfig TINKER_ID] %s \n", BuildConfig.TINKER_ID));
            sb.append(String.format("[buildConfig BASE_TINKER_ID] %s \n", BaseBuildInfo.BASE_TINKER_ID));

            sb.append(String.format("[buildConfig MESSSAGE] %s \n", BuildConfig.MESSAGE));
            sb.append(String.format("[TINKER_ID] %s \n", tinker.getTinkerLoadResultIfPresent().getPackageConfigByName(ShareConstants.TINKER_ID)));
            sb.append(String.format("[packageConfig patchMessage] %s \n", tinker.getTinkerLoadResultIfPresent().getPackageConfigByName("patchMessage")));
            sb.append(String.format("[TINKER_ID Rom Space] %d k \n", tinker.getTinkerRomSpace()));

        } else {
            sb.append(String.format("[patch is not loaded] \n"));
            sb.append(String.format("versionName %s \n",getVersion()));
            sb.append(String.format("[buildConfig TINKER_ID] %s \n", BuildConfig.TINKER_ID));
            sb.append(String.format("[buildConfig BASE_TINKER_ID] %s \n", BaseBuildInfo.BASE_TINKER_ID));

            sb.append(String.format("[buildConfig MESSSAGE] %s \n", BuildConfig.MESSAGE));
            sb.append(String.format("[TINKER_ID] %s \n", ShareTinkerInternals.getManifestTinkerID(getApplicationContext())));
        }
        txvAppInfo.setText(sb);
        txvAppInfo.setTextSize(10);
    }

    public void initialPatchInfoTxv(){
        Tinker tinker = Tinker.with(getApplicationContext());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Patch Info:\n");
        stringBuilder.append("currentVersion:\n"+tinker.getTinkerLoadResultIfPresent().currentVersion);
        //stringBuilder.append("currentVersion:\n"+tinker.);
        txvPatchInfo.setText(stringBuilder);
        txvAppInfo.setTextSize(10);
    }

    public String getVersion(){
        try{
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(),0);
            String version = info.versionName;
            return version;
        }catch (Exception e){
            e.printStackTrace();
            return "Error";
        }
    }

    public void permissionCheck() {
        Log.d("MainActivity", "permissionCheck:");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
            Log.d("MainActivity", "permissionCheck:requestPermissions");
        }
    }

    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_remove_patch:
                Tinker.with(getApplicationContext()).cleanPatch();
                Log.d("MainActivity", "onClick: btn_remove_patch");
                Toast.makeText(this,"重启后生效",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_load_patch:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,1);
                break;

            case R.id.btn_quit:
                ShareTinkerInternals.killAllOtherProcess(getApplicationContext());
                android.os.Process.killProcess(android.os.Process.myPid());
                break;

            case R.id.btn_rollback_patch:
                Tinker.with(getApplicationContext()).rollbackPatch();
                Toast.makeText(this,"重启后生效",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == 1){
                Uri uri = data.getData();
                TinkerInstaller.onReceiveUpgradePatch(getApplicationContext(), uri.getPath());
            }
        }
    }

}
