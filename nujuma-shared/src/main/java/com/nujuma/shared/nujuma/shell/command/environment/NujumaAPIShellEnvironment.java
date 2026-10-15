package com.nujuma.shared.nujuma.shell.command.environment;

import android.content.Context;
import android.content.pm.PackageInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nujuma.shared.android.PackageUtils;
import com.nujuma.shared.shell.command.environment.ShellEnvironmentUtils;
import com.nujuma.shared.nujuma.NujumaConstants;
import com.nujuma.shared.nujuma.NujumaUtils;

import java.util.HashMap;

/**
 * Environment for {@link NujumaConstants#NUJUMA_API_PACKAGE_NAME} app.
 */
public class NujumaAPIShellEnvironment {

    /** Environment variable prefix for the Nujuma:API app. */
    public static final String NUJUMA_API_APP_ENV_PREFIX = NujumaConstants.NUJUMA_ENV_PREFIX_ROOT + "_API_APP__";

    /** Environment variable for the Nujuma:API app version. */
    public static final String ENV_NUJUMA_API_APP__VERSION_NAME = NUJUMA_API_APP_ENV_PREFIX + "VERSION_NAME";

    /** Get shell environment for Nujuma:API app. */
    @Nullable
    public static HashMap<String, String> getEnvironment(@NonNull Context currentPackageContext) {
        if (NujumaUtils.isNujumaAPIAppInstalled(currentPackageContext) != null) return null;

        String packageName = NujumaConstants.NUJUMA_API_PACKAGE_NAME;
        PackageInfo packageInfo = PackageUtils.getPackageInfoForPackage(currentPackageContext, packageName);
        if (packageInfo == null) return null;

        HashMap<String, String> environment = new HashMap<>();

        ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_NUJUMA_API_APP__VERSION_NAME, PackageUtils.getVersionNameForPackage(packageInfo));

        return environment;
    }

}
