package com.nujuma.shared.nujuma.settings.preferences;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nujuma.shared.logger.Logger;
import com.nujuma.shared.android.PackageUtils;
import com.nujuma.shared.settings.preferences.AppSharedPreferences;
import com.nujuma.shared.settings.preferences.SharedPreferenceUtils;
import com.nujuma.shared.nujuma.NujumaUtils;
import com.nujuma.shared.nujuma.settings.preferences.NujumaPreferenceConstants.NUJUMA_BOOT_APP;
import com.nujuma.shared.nujuma.NujumaConstants;

public class NujumaBootAppSharedPreferences extends AppSharedPreferences {

    private static final String LOG_TAG = "NujumaBootAppSharedPreferences";

    private NujumaBootAppSharedPreferences(@NonNull Context context) {
        super(context,
            SharedPreferenceUtils.getPrivateSharedPreferences(context,
                NujumaConstants.NUJUMA_BOOT_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION),
            SharedPreferenceUtils.getPrivateAndMultiProcessSharedPreferences(context,
                NujumaConstants.NUJUMA_BOOT_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION));
    }

    /**
     * Get {@link NujumaBootAppSharedPreferences}.
     *
     * @param context The {@link Context} to use to get the {@link Context} of the
     *                {@link NujumaConstants#NUJUMA_BOOT_PACKAGE_NAME}.
     * @return Returns the {@link NujumaBootAppSharedPreferences}. This will {@code null} if an exception is raised.
     */
    @Nullable
    public static NujumaBootAppSharedPreferences build(@NonNull final Context context) {
        Context nujumaBootPackageContext = PackageUtils.getContextForPackage(context, NujumaConstants.NUJUMA_BOOT_PACKAGE_NAME);
        if (nujumaBootPackageContext == null)
            return null;
        else
            return new NujumaBootAppSharedPreferences(nujumaBootPackageContext);
    }

    /**
     * Get {@link NujumaBootAppSharedPreferences}.
     *
     * @param context The {@link Context} to use to get the {@link Context} of the
     *                {@link NujumaConstants#NUJUMA_BOOT_PACKAGE_NAME}.
     * @param exitAppOnError If {@code true} and failed to get package context, then a dialog will
     *                       be shown which when dismissed will exit the app.
     * @return Returns the {@link NujumaBootAppSharedPreferences}. This will {@code null} if an exception is raised.
     */
    public static NujumaBootAppSharedPreferences build(@NonNull final Context context, final boolean exitAppOnError) {
        Context nujumaBootPackageContext = NujumaUtils.getContextForPackageOrExitApp(context, NujumaConstants.NUJUMA_BOOT_PACKAGE_NAME, exitAppOnError);
        if (nujumaBootPackageContext == null)
            return null;
        else
            return new NujumaBootAppSharedPreferences(nujumaBootPackageContext);
    }



    public int getLogLevel(boolean readFromFile) {
        if (readFromFile)
            return SharedPreferenceUtils.getInt(mMultiProcessSharedPreferences, NUJUMA_BOOT_APP.KEY_LOG_LEVEL, Logger.DEFAULT_LOG_LEVEL);
        else
            return SharedPreferenceUtils.getInt(mSharedPreferences, NUJUMA_BOOT_APP.KEY_LOG_LEVEL, Logger.DEFAULT_LOG_LEVEL);
    }

    public void setLogLevel(Context context, int logLevel, boolean commitToFile) {
        logLevel = Logger.setLogLevel(context, logLevel);
        SharedPreferenceUtils.setInt(mSharedPreferences, NUJUMA_BOOT_APP.KEY_LOG_LEVEL, logLevel, commitToFile);
    }

}
