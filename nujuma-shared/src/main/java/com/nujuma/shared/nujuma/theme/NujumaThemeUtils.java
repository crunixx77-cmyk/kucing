package com.nujuma.shared.nujuma.theme;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nujuma.shared.nujuma.settings.properties.NujumaPropertyConstants;
import com.nujuma.shared.nujuma.settings.properties.NujumaSharedProperties;
import com.nujuma.shared.theme.NightMode;

public class NujumaThemeUtils {

    /** Get the {@link NujumaPropertyConstants#KEY_NIGHT_MODE} value from the properties file on disk
     * and set it to app wide night mode value. */
    public static void setAppNightMode(@NonNull Context context) {
        NightMode.setAppNightMode(NujumaSharedProperties.getNightMode(context));
    }

    /** Set name as app wide night mode value. */
    public static void setAppNightMode(@Nullable String name) {
        NightMode.setAppNightMode(name);
    }

}
