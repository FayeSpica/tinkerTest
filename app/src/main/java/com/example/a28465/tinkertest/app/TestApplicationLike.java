package com.example.a28465.tinkertest.app;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.multidex.MultiDex;

import com.example.a28465.tinkertest.service.PatchResultService;
import com.tencent.tinker.anno.DefaultLifeCycle;
import com.tencent.tinker.lib.listener.DefaultPatchListener;
import com.tencent.tinker.lib.listener.PatchListener;
import com.tencent.tinker.lib.patch.AbstractPatch;
import com.tencent.tinker.lib.patch.UpgradePatch;
import com.tencent.tinker.lib.reporter.DefaultLoadReporter;
import com.tencent.tinker.lib.reporter.DefaultPatchReporter;
import com.tencent.tinker.lib.reporter.LoadReporter;
import com.tencent.tinker.lib.reporter.PatchReporter;
import com.tencent.tinker.lib.service.DefaultTinkerResultService;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.lib.util.UpgradePatchRetry;
import com.tencent.tinker.loader.app.DefaultApplicationLike;
import com.tencent.tinker.loader.shareutil.ShareConstants;

/**
 * Created by 28465 on 2017/6/1.
 */
@SuppressWarnings("unused")
@DefaultLifeCycle(application = "com.example.a28465.tinkertest.app.TestApplication",
                  flags = ShareConstants.TINKER_ENABLE_ALL,
                  loadVerifyFlag = false)
public class TestApplicationLike extends DefaultApplicationLike {
    private static final String TAG = "Tinker.TestApplicationLike";

    public TestApplicationLike(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag,
                                  long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent);
    }

    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);

        MultiDex.install(base);

        UpgradePatchRetry.getInstance(getApplication()).setRetryEnable(true);

        //or you can just use DefaultLoadReporter
        LoadReporter loadReporter = new DefaultLoadReporter(getApplication());
        //or you can just use DefaultPatchReporter
        PatchReporter patchReporter = new DefaultPatchReporter(getApplication());
        //or you can just use DefaultPatchListener
        PatchListener patchListener = new DefaultPatchListener(getApplication());
        //you can set your own upgrade patch if you need
        AbstractPatch upgradePatchProcessor = new UpgradePatch();

        TinkerInstaller.install(this,
                loadReporter, patchReporter, patchListener,
                PatchResultService.class, upgradePatchProcessor);

        Tinker tinker = Tinker.with(getApplication());
    }
}
