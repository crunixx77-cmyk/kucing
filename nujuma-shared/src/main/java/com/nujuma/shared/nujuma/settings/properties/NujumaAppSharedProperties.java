package com.nujuma.shared.nujuma.settings.properties;

import android.content.Context;

import androidx.annotation.NonNull;

import com.nujuma.shared.nujuma.NujumaConstants;

public class NujumaAppSharedProperties extends NujumaSharedProperties {

    private static NujumaAppSharedProperties properties;


    private NujumaAppSharedProperties(@NonNull Context context) {
        super(context, NujumaConstants.NUJUMA_APP_NAME,
            NujumaConstants.NUJUMA_PROPERTIES_FILE_PATHS_LIST, NujumaPropertyConstants.NUJUMA_APP_PROPERTIES_LIST,
            new NujumaSharedProperties.SharedPropertiesParserClient());
    }

    /**
     * Initialize the {@link #properties} and load properties from disk.
     *
     * @param context The {@link Context} for operations.
     * @return Returns the {@link NujumaAppSharedProperties}.
     */
    public static NujumaAppSharedProperties init(@NonNull Context context) {
        if (properties == null)
            properties = new NujumaAppSharedProperties(context);

        return properties;
    }

    /**
     * Get the {@link #properties}.
     *
     * @return Returns the {@link NujumaAppSharedProperties}.
     */
    public static NujumaAppSharedProperties getProperties() {
        return properties;
    }

}
