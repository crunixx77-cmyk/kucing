package com.nujuma.shared.nujuma.settings.preferences;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nujuma.shared.logger.Logger;
import com.nujuma.shared.android.PackageUtils;
import com.nujuma.shared.settings.preferences.AppSharedPreferences;
import com.nujuma.shared.settings.preferences.SharedPreferenceUtils;
import com.nujuma.shared.nujuma.NujumaUtils;
import com.nujuma.shared.nujuma.settings.preferences.NujumaPreferenceConstants.NUJUMA_API_APP;
import com.nujuma.shared.nujuma.NujumaConstants;

public class NujumaAPIAppSharedPreferences extends AppSharedPreferences {

    private static final String LOG_TAG = "NujumaAPIAppSharedPreferences";

    private NujumaAPIAppSharedPreferences(@NonNull Context context) {
        super(context,
            SharedPreferenceUtils.getPrivateSharedPreferences(context,
                NujumaConstants.NUJUMA_API_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION),
            SharedPreferenceUtils.getPrivateAndMultiProcessSharedPreferences(context,
                NujumaConstants.NUJUMA_API_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION));
    }

    /**
     * Get {@link NujumaAPIAppSharedPreferences}.
     *
     * @param context The {@link Context} to use to get the {@link Context} of the
     *                {@link NujumaConstants#NUJUMA_API_PACKAGE_NAME}.
     * @return Returns the {@link NujumaAPIAppSharedPreferences}. This will {@code null} if an exception is raised.
     */
    @Nullable
    public static NujumaAPIAppSharedPreferences build(@NonNull final Context context) {
        Context nujumaAPIPackageContext = PackageUtils.getContextForPackage(context, NujumaConstants.NUJUMA_API_PACKAGE_NAME);
        if (nujumaAPIPackageContext == null)
            return null;
        else
            return new NujumaAPIAppSharedPreferences(nujumaAPIPackageContext);
    }

    /**
     * Get {@link NujumaAPIAppSharedPreferences}.
     *
     * @param context The {@link Context} to use to get the {@link Context} of the
     *                {@link NujumaConstants#NUJUMA_API_PACKAGE_NAME}.
     * @param exitAppOnError If {@code true} and failed to get package context, then a dialog will
     *                       be shown which when dismissed will exit the app.
     * @return Returns the {@link NujumaAPIAppSharedPreferences}. This will {@code null} if an exception is raised.
     */
    public static NujumaAPIAppSharedPreferences build(@NonNull final Context context, final boolean exitAppOnError) {
        Context nujumaAPIPackageContext = NujumaUtils.getContextForPackageOrExitApp(context, NujumaConstants.NUJUMA_API_PACKAGE_NAME, exitAppOnError);
        if (nujumaAPIPackageContext == null)
            return null;
        else
            return new NujumaAPIAppSharedPreferences(nujumaAPIPackageContext);
    }



    public int getLogLevel(boolean readFromFile) {
        if (readFromFile)
            return SharedPreferenceUtils.getInt(mMultiProcessSharedPreferences, NUJUMA_API_APP.KEY_LOG_LEVEL, Logger.DEFAULT_LOG_LEVEL);
        else
            return SharedPreferenceUtils.getInt(mSharedPreferences, NUJUMA_API_APP.KEY_LOG_LEVEL, Logger.DEFAULT_LOG_LEVEL);
    }

    public void setLogLevel(Context context, int logLevel, boolean commitToFile) {
        logLevel = Logger.setLogLevel(context, logLevel);
        SharedPreferenceUtils.setInt(mSharedPreferences, NUJUMA_API_APP.KEY_LOG_LEVEL, logLevel, commitToFile);
    }


    public int getLastPendingIntentRequestCode() {
        return SharedPreferenceUtils.getInt(mSharedPreferences, NUJUMA_API_APP.KEY_LAST_PENDING_INTENT_REQUEST_CODE, NUJUMA_API_APP.DEFAULT_VALUE_KEY_LAST_PENDING_INTENT_REQUEST_CODE);
    }

    public void setLastPendingIntentRequestCode(int lastPendingIntentRequestCode) {
        SharedPreferenceUtils.setInt(mSharedPreferences, NUJUMA_API_APP.KEY_LAST_PENDING_INTENT_REQUEST_CODE, lastPendingIntentRequestCode, true);
    }

}
