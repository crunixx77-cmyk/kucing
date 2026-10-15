package com.nujuma.shared.nujuma.settings.preferences;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nujuma.shared.logger.Logger;
import com.nujuma.shared.android.PackageUtils;
import com.nujuma.shared.settings.preferences.AppSharedPreferences;
import com.nujuma.shared.settings.preferences.SharedPreferenceUtils;
import com.nujuma.shared.nujuma.NujumaUtils;
import com.nujuma.shared.nujuma.settings.preferences.NujumaPreferenceConstants.NUJUMA_WIDGET_APP;
import com.nujuma.shared.nujuma.NujumaConstants;

import java.util.UUID;

public class NujumaWidgetAppSharedPreferences extends AppSharedPreferences {

    private static final String LOG_TAG = "NujumaWidgetAppSharedPreferences";

    private NujumaWidgetAppSharedPreferences(@NonNull Context context) {
        super(context,
            SharedPreferenceUtils.getPrivateSharedPreferences(context,
                NujumaConstants.NUJUMA_WIDGET_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION),
            SharedPreferenceUtils.getPrivateAndMultiProcessSharedPreferences(context,
                NujumaConstants.NUJUMA_WIDGET_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION));
    }

    /**
     * Get {@link NujumaWidgetAppSharedPreferences}.
     *
     * @param context The {@link Context} to use to get the {@link Context} of the
     *                {@link NujumaConstants#NUJUMA_WIDGET_PACKAGE_NAME}.
     * @return Returns the {@link NujumaWidgetAppSharedPreferences}. This will {@code null} if an exception is raised.
     */
    @Nullable
    public static NujumaWidgetAppSharedPreferences build(@NonNull final Context context) {
        Context nujumaWidgetPackageContext = PackageUtils.getContextForPackage(context, NujumaConstants.NUJUMA_WIDGET_PACKAGE_NAME);
        if (nujumaWidgetPackageContext == null)
            return null;
        else
            return new NujumaWidgetAppSharedPreferences(nujumaWidgetPackageContext);
    }

    /**
     * Get the {@link NujumaWidgetAppSharedPreferences}.
     *
     * @param context The {@link Context} to use to get the {@link Context} of the
     *                {@link NujumaConstants#NUJUMA_WIDGET_PACKAGE_NAME}.
     * @param exitAppOnError If {@code true} and failed to get package context, then a dialog will
     *                       be shown which when dismissed will exit the app.
     * @return Returns the {@link NujumaWidgetAppSharedPreferences}. This will {@code null} if an exception is raised.
     */
    public static NujumaWidgetAppSharedPreferences build(@NonNull final Context context, final boolean exitAppOnError) {
        Context nujumaWidgetPackageContext = NujumaUtils.getContextForPackageOrExitApp(context, NujumaConstants.NUJUMA_WIDGET_PACKAGE_NAME, exitAppOnError);
        if (nujumaWidgetPackageContext == null)
            return null;
        else
            return new NujumaWidgetAppSharedPreferences(nujumaWidgetPackageContext);
    }



    public static String getGeneratedToken(@NonNull Context context) {
        NujumaWidgetAppSharedPreferences preferences = NujumaWidgetAppSharedPreferences.build(context, true);
        if (preferences == null) return null;
        return preferences.getGeneratedToken();
    }

    public String getGeneratedToken() {
        String token =  SharedPreferenceUtils.getString(mSharedPreferences, NUJUMA_WIDGET_APP.KEY_TOKEN, null, true);
        if (token == null) {
            token = UUID.randomUUID().toString();
            SharedPreferenceUtils.setString(mSharedPreferences, NUJUMA_WIDGET_APP.KEY_TOKEN, token, true);
        }
        return token;
    }



    public int getLogLevel(boolean readFromFile) {
        if (readFromFile)
            return SharedPreferenceUtils.getInt(mMultiProcessSharedPreferences, NUJUMA_WIDGET_APP.KEY_LOG_LEVEL, Logger.DEFAULT_LOG_LEVEL);
        else
            return SharedPreferenceUtils.getInt(mSharedPreferences, NUJUMA_WIDGET_APP.KEY_LOG_LEVEL, Logger.DEFAULT_LOG_LEVEL);
    }

    public void setLogLevel(Context context, int logLevel, boolean commitToFile) {
        logLevel = Logger.setLogLevel(context, logLevel);
        SharedPreferenceUtils.setInt(mSharedPreferences, NUJUMA_WIDGET_APP.KEY_LOG_LEVEL, logLevel, commitToFile);
    }

}
