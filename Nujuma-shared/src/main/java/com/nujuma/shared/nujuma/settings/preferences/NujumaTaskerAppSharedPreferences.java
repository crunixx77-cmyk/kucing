package com.nujuma.shared.nujuma.settings.preferences;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nujuma.shared.android.PackageUtils;
import com.nujuma.shared.settings.preferences.AppSharedPreferences;
import com.nujuma.shared.settings.preferences.SharedPreferenceUtils;
import com.nujuma.shared.nujuma.NujumaConstants;
import com.nujuma.shared.nujuma.NujumaUtils;
import com.nujuma.shared.nujuma.settings.preferences.NujumaPreferenceConstants.NUJUMA_TASKER_APP;
import com.nujuma.shared.logger.Logger;

public class NujumaTaskerAppSharedPreferences extends AppSharedPreferences {

    private static final String LOG_TAG = "NujumaTaskerAppSharedPreferences";

    private  NujumaTaskerAppSharedPreferences(@NonNull Context context) {
        super(context,
            SharedPreferenceUtils.getPrivateSharedPreferences(context,
                NujumaConstants.NUJUMA_TASKER_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION),
            SharedPreferenceUtils.getPrivateAndMultiProcessSharedPreferences(context,
                NujumaConstants.NUJUMA_TASKER_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION));
    }

    /**
     * Get {@link NujumaTaskerAppSharedPreferences}.
     *
     * @param context The {@link Context} to use to get the {@link Context} of the
     *                {@link NujumaConstants#NUJUMA_TASKER_PACKAGE_NAME}.
     * @return Returns the {@link NujumaTaskerAppSharedPreferences}. This will {@code null} if an exception is raised.
     */
    @Nullable
    public static NujumaTaskerAppSharedPreferences build(@NonNull final Context context) {
        Context nujumaTaskerPackageContext = PackageUtils.getContextForPackage(context, NujumaConstants.NUJUMA_TASKER_PACKAGE_NAME);
        if (nujumaTaskerPackageContext == null)
            return null;
        else
            return new NujumaTaskerAppSharedPreferences(nujumaTaskerPackageContext);
    }

    /**
     * Get {@link NujumaTaskerAppSharedPreferences}.
     *
     * @param context The {@link Context} to use to get the {@link Context} of the
     *                {@link NujumaConstants#NUJUMA_TASKER_PACKAGE_NAME}.
     * @param exitAppOnError If {@code true} and failed to get package context, then a dialog will
     *                       be shown which when dismissed will exit the app.
     * @return Returns the {@link NujumaTaskerAppSharedPreferences}. This will {@code null} if an exception is raised.
     */
    public static  NujumaTaskerAppSharedPreferences build(@NonNull final Context context, final boolean exitAppOnError) {
        Context nujumaTaskerPackageContext = NujumaUtils.getContextForPackageOrExitApp(context, NujumaConstants.NUJUMA_TASKER_PACKAGE_NAME, exitAppOnError);
        if (nujumaTaskerPackageContext == null)
            return null;
        else
            return new NujumaTaskerAppSharedPreferences(nujumaTaskerPackageContext);
    }



    public int getLogLevel(boolean readFromFile) {
        if (readFromFile)
            return SharedPreferenceUtils.getInt(mMultiProcessSharedPreferences, NUJUMA_TASKER_APP.KEY_LOG_LEVEL, Logger.DEFAULT_LOG_LEVEL);
        else
            return SharedPreferenceUtils.getInt(mSharedPreferences, NUJUMA_TASKER_APP.KEY_LOG_LEVEL, Logger.DEFAULT_LOG_LEVEL);
    }

    public void setLogLevel(Context context, int logLevel, boolean commitToFile) {
        logLevel = Logger.setLogLevel(context, logLevel);
        SharedPreferenceUtils.setInt(mSharedPreferences, NUJUMA_TASKER_APP.KEY_LOG_LEVEL, logLevel, commitToFile);
    }



    public int getLastPendingIntentRequestCode() {
        return SharedPreferenceUtils.getInt(mSharedPreferences, NUJUMA_TASKER_APP.KEY_LAST_PENDING_INTENT_REQUEST_CODE, NUJUMA_TASKER_APP.DEFAULT_VALUE_KEY_LAST_PENDING_INTENT_REQUEST_CODE);
    }

    public void setLastPendingIntentRequestCode(int lastPendingIntentRequestCode) {
        SharedPreferenceUtils.setInt(mSharedPreferences, NUJUMA_TASKER_APP.KEY_LAST_PENDING_INTENT_REQUEST_CODE, lastPendingIntentRequestCode, false);
    }

}
