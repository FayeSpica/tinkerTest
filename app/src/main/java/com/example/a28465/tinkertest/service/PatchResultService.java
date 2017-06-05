package com.example.a28465.tinkertest.service;

import android.os.Handler;
import android.widget.Toast;

import com.tencent.tinker.lib.service.AbstractResultService;
import com.tencent.tinker.lib.service.DefaultTinkerResultService;
import com.tencent.tinker.lib.service.PatchResult;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerLoadResult;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.lib.util.TinkerServiceInternals;

import java.io.File;

/**
 * Created by 28465 on 2017/6/1.
 */

public class PatchResultService extends AbstractResultService {
    private static final String TAG = "Tinker.PatchResultService";

    Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(getMainLooper());
    }

    @Override
    public void onPatchResult(PatchResult result) {
        if (result == null) {
            TinkerLog.e(TAG, "PatchResultService received null result!!!!");
            return;
        }
        TinkerLog.i(TAG, "PatchResultService received a result:%s ", result.toString());

        //first, we want to kill the recover process
        TinkerServiceInternals.killTinkerPatchServiceProcess(getApplicationContext());

        // if success and newPatch, it is nice to delete the raw file, and restart at once
        // only main process can load an upgrade patch!
        if (result.isSuccess) {
            //deleteRawPatchFile(new File(result.rawPatchFilePath));
            if (checkIfNeedKill(result)) {
                //android.os.Process.killProcess(android.os.Process.myPid());
                //Toast.makeText(getApplicationContext(),"补丁加载成功，手动重启App后生效",Toast.LENGTH_SHORT).show();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"补丁加载成功，重启App后生效",Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                TinkerLog.i(TAG, "I have already install the newly patch version!");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"已经安装最新的补丁",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            //Toast.makeText(getApplicationContext(),"补丁加载失败",Toast.LENGTH_SHORT).show();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),"补丁加载失败",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public boolean checkIfNeedKill(PatchResult result) {
        Tinker tinker = Tinker.with(getApplicationContext());
        if (tinker.isTinkerLoaded()) {
            TinkerLoadResult tinkerLoadResult = tinker.getTinkerLoadResultIfPresent();
            if (tinkerLoadResult != null) {
                String currentVersion = tinkerLoadResult.currentVersion;
                if (result.patchVersion != null && result.patchVersion.equals(currentVersion)) {
                    return false;
                }
            }
        }
        return true;
    }
}
