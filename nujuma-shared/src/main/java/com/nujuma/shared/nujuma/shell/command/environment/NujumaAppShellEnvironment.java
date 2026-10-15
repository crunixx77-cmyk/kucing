package com.nujuma.shared.nujuma.shell.command.environment;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nujuma.shared.android.PackageUtils;
import com.nujuma.shared.android.SELinuxUtils;
import com.nujuma.shared.data.DataUtils;
import com.nujuma.shared.shell.command.environment.ShellEnvironmentUtils;
import com.nujuma.shared.nujuma.NujumaBootstrap;
import com.nujuma.shared.nujuma.NujumaConstants;
import com.nujuma.shared.nujuma.NujumaUtils;
import com.nujuma.shared.nujuma.shell.am.NujumaAmSocketServer;

import java.util.HashMap;

/**
 * Environment for {@link NujumaConstants#NUJUMA_PACKAGE_NAME} app.
 */
public class NujumaAppShellEnvironment {

    /** Nujuma app environment variables. */
    public static HashMap<String, String> nujumaAppEnvironment;

    /** Environment variable root scope. */
    public static final String NUJUMA_ENV__S_ROOT = "NUJUMA_"; // Default: "NUJUMA_"

    /** Environment variable scope for Nujuma. */
    public static final String NUJUMA_ENV__S_NUJUMA = NUJUMA_ENV__S_ROOT + "_"; // Default: "NUJUMA__"

    /** Environment variable for the Nujuma app version. */
    public static final String ENV_NUJUMA_VERSION = NujumaConstants.NUJUMA_ENV_PREFIX_ROOT + "_VERSION";

    /** Environment variable prefix for the Nujuma app. */
    public static final String NUJUMA_APP_ENV_PREFIX = NujumaConstants.NUJUMA_ENV_PREFIX_ROOT + "_APP__";

    /** Environment variable for the Nujuma app version name. */
    public static final String ENV_NUJUMA_APP__APP_VERSION_NAME = NUJUMA_APP_ENV_PREFIX + "APP_VERSION_NAME";
    /** Environment variable for the Nujuma app version code. */
    public static final String ENV_NUJUMA_APP__APP_VERSION_CODE = NUJUMA_APP_ENV_PREFIX + "APP_VERSION_CODE";
    /** Environment variable for the Nujuma app package name. */
    public static final String ENV_NUJUMA_APP__PACKAGE_NAME = NUJUMA_APP_ENV_PREFIX + "PACKAGE_NAME";
    /** Environment variable for the Nujuma app process id. */
    public static final String ENV_NUJUMA_APP__PID = NUJUMA_APP_ENV_PREFIX + "PID";
    /** Environment variable for the Nujuma app uid. */
    public static final String ENV_NUJUMA__UID = NUJUMA_ENV__S_NUJUMA + "UID";
    /** Environment variable for the Nujuma app targetSdkVersion. */
    public static final String ENV_NUJUMA_APP__TARGET_SDK = NUJUMA_APP_ENV_PREFIX + "TARGET_SDK";
    /** Environment variable for the Nujuma app is debuggable apk build. */
    public static final String ENV_NUJUMA_APP__IS_DEBUGGABLE_BUILD = NUJUMA_APP_ENV_PREFIX + "IS_DEBUGGABLE_BUILD";
    /** Environment variable for the Nujuma app {@link NujumaConstants} APK_RELEASE_*. */
    public static final String ENV_NUJUMA_APP__APK_RELEASE = NUJUMA_APP_ENV_PREFIX + "APK_RELEASE";
    /** Environment variable for the Nujuma app install path. */
    public static final String ENV_NUJUMA_APP__APK_FILE = NUJUMA_APP_ENV_PREFIX + "APK_FILE";
    /** Environment variable for the Nujuma app is installed on external/portable storage. */
    public static final String ENV_NUJUMA_APP__IS_INSTALLED_ON_EXTERNAL_STORAGE = NUJUMA_APP_ENV_PREFIX + "IS_INSTALLED_ON_EXTERNAL_STORAGE";
    /** Environment variable for the Nujuma app data files selinux context. */
    public static final String ENV_NUJUMA_APP__SE_FILE_CONTEXT = NUJUMA_APP_ENV_PREFIX + "SE_FILE_CONTEXT";
    /** Environment variable for the Nujuma app seInfo tag found in selinux policy used to set app process and app data files selinux context. */
    public static final String ENV_NUJUMA_APP__SE_INFO = NUJUMA_APP_ENV_PREFIX + "SE_INFO";

    /** Environment variable for the current Nujuma process selinux context. */
    public static final String ENV_NUJUMA__SE_PROCESS_CONTEXT = NUJUMA_ENV__S_NUJUMA + "SE_PROCESS_CONTEXT";
    /** Environment variable for the Nujuma app user id. */
    public static final String ENV_NUJUMA__USER_ID = NUJUMA_ENV__S_NUJUMA + "USER_ID";
    /** Environment variable for the Nujuma app profile owner. */
    public static final String ENV_NUJUMA__PROFILE_OWNER = NUJUMA_ENV__S_NUJUMA + "PROFILE_OWNER";

    /** Environment variable for the Nujuma app {@link NujumaBootstrap#NUJUMA_APP_PACKAGE_MANAGER}. */
    public static final String ENV_NUJUMA_APP__PACKAGE_MANAGER = NUJUMA_APP_ENV_PREFIX + "PACKAGE_MANAGER";
    /** Environment variable for the Nujuma app {@link NujumaBootstrap#NUJUMA_APP_PACKAGE_VARIANT}. */
    public static final String ENV_NUJUMA_APP__PACKAGE_VARIANT = NUJUMA_APP_ENV_PREFIX + "PACKAGE_VARIANT";
    /** Environment variable for the Nujuma app files directory. */
    public static final String ENV_NUJUMA_APP__DATA_DIR = NUJUMA_APP_ENV_PREFIX + "DATA_DIR";
    public static final String ENV_NUJUMA_APP__LEGACY_DATA_DIR = NUJUMA_APP_ENV_PREFIX + "LEGACY_DATA_DIR";

    /** Environment variable for the Nujuma app {@link NujumaAmSocketServer#getNujumaAppAMSocketServerEnabled(Context)}. */
    public static final String ENV_NUJUMA_APP__AM_SOCKET_SERVER_ENABLED = NUJUMA_APP_ENV_PREFIX + "AM_SOCKET_SERVER_ENABLED";



    /** Get shell environment for Nujuma app. */
    @Nullable
    public static HashMap<String, String> getEnvironment(@NonNull Context currentPackageContext) {
        setNujumaAppEnvironment(currentPackageContext);
        return nujumaAppEnvironment;
    }

    /** Set Nujuma app environment variables in {@link #nujumaAppEnvironment}. */
    public synchronized static void setNujumaAppEnvironment(@NonNull Context currentPackageContext) {
        boolean isNujumaApp = NujumaConstants.NUJUMA_PACKAGE_NAME.equals(currentPackageContext.getPackageName());

        // If current package context is of nujuma app and its environment is already set, then no need to set again since it won't change
        // Other apps should always set environment again since nujuma app may be installed/updated/deleted in background
        if (nujumaAppEnvironment != null && isNujumaApp)
            return;

        nujumaAppEnvironment = null;

        String packageName = NujumaConstants.NUJUMA_PACKAGE_NAME;
        PackageInfo packageInfo = PackageUtils.getPackageInfoForPackage(currentPackageContext, packageName);
        if (packageInfo == null) return;
        ApplicationInfo applicationInfo = PackageUtils.getApplicationInfoForPackage(currentPackageContext, packageName);
        if (applicationInfo == null || !applicationInfo.enabled) return;

        HashMap<String, String> environment = new HashMap<>();

        ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_NUJUMA_VERSION, PackageUtils.getVersionNameForPackage(packageInfo));
        ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_NUJUMA_APP__APP_VERSION_NAME, PackageUtils.getVersionNameForPackage(packageInfo));
        ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_NUJUMA_APP__APP_VERSION_CODE, String.valueOf(PackageUtils.getVersionCodeForPackage(packageInfo)));

        ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_NUJUMA_APP__PACKAGE_NAME, packageName);
        ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_NUJUMA_APP__PID, NujumaUtils.getNujumaAppPID(currentPackageContext));
        ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_NUJUMA__UID, String.valueOf(PackageUtils.getUidForPackage(applicationInfo)));
        ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_NUJUMA_APP__TARGET_SDK, String.valueOf(PackageUtils.getTargetSDKForPackage(applicationInfo)));
        ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_NUJUMA_APP__IS_DEBUGGABLE_BUILD, PackageUtils.isAppForPackageADebuggableBuild(applicationInfo));
        ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_NUJUMA_APP__APK_FILE, PackageUtils.getBaseAPKPathForPackage(applicationInfo));
        ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_NUJUMA_APP__IS_INSTALLED_ON_EXTERNAL_STORAGE, PackageUtils.isAppInstalledOnExternalStorage(applicationInfo));

        putNujumaAPKSignature(currentPackageContext, environment);

        Context nujumaPackageContext = NujumaUtils.getNujumaPackageContext(currentPackageContext);
        if (nujumaPackageContext != null) {
            /*
            // An app that does not have the same sharedUserId as nujuma app will not be able to get
            // get nujuma context's classloader to get BuildConfig.NUJUMA_PACKAGE_VARIANT via reflection.
            // Check NujumaBootstrap.setNujumaPackageManagerAndVariantFromNujumaApp()
            if (NujumaBootstrap.NUJUMA_APP_PACKAGE_MANAGER != null)
                environment.put(ENV_NUJUMA_APP__PACKAGE_MANAGER, NujumaBootstrap.NUJUMA_APP_PACKAGE_MANAGER.getName());
            if (NujumaBootstrap.NUJUMA_APP_PACKAGE_VARIANT != null)
                environment.put(ENV_NUJUMA_APP__PACKAGE_VARIANT, NujumaBootstrap.NUJUMA_APP_PACKAGE_VARIANT.getName());
             */

            /*
            // Will not be set for plugins
            ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_NUJUMA_APP__AM_SOCKET_SERVER_ENABLED,
                NujumaAmSocketServer.getNujumaAppAMSocketServerEnabled(currentPackageContext));
             */

            ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_NUJUMA_APP__DATA_DIR, applicationInfo.dataDir);
            ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_NUJUMA_APP__LEGACY_DATA_DIR, "/data/data/" + applicationInfo.packageName);

            ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_NUJUMA__SE_PROCESS_CONTEXT, SELinuxUtils.getContext());
            ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_NUJUMA_APP__SE_FILE_CONTEXT, SELinuxUtils.getFileContext(applicationInfo.dataDir));

            String seInfoUser = PackageUtils.getApplicationInfoSeInfoUserForPackage(applicationInfo);
            ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_NUJUMA_APP__SE_INFO, PackageUtils.getApplicationInfoSeInfoForPackage(applicationInfo) +
                (DataUtils.isNullOrEmpty(seInfoUser) ? "" : seInfoUser));

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_NUJUMA__USER_ID, String.valueOf(PackageUtils.getUserIdForPackage(currentPackageContext)));
            ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_NUJUMA__PROFILE_OWNER, PackageUtils.getProfileOwnerPackageNameForUser(currentPackageContext));
        }

        nujumaAppEnvironment = environment;
    }

    /** Put {@link #ENV_NUJUMA_APP__APK_RELEASE} in {@code environment}. */
    public static void putNujumaAPKSignature(@NonNull Context currentPackageContext,
                                             @NonNull HashMap<String, String> environment) {
        String signingCertificateSHA256Digest = PackageUtils.getSigningCertificateSHA256DigestForPackage(currentPackageContext,
            NujumaConstants.NUJUMA_PACKAGE_NAME);
        if (signingCertificateSHA256Digest != null) {
            ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_NUJUMA_APP__APK_RELEASE,
                NujumaUtils.getAPKRelease(signingCertificateSHA256Digest).replaceAll("[^a-zA-Z]", "_").toUpperCase());
        }
    }

    /** Update {@link #ENV_NUJUMA_APP__AM_SOCKET_SERVER_ENABLED} value in {@code environment}. */
    public synchronized static void updateNujumaAppAMSocketServerEnabled(@NonNull Context currentPackageContext) {
        if (nujumaAppEnvironment == null) return;
        nujumaAppEnvironment.remove(ENV_NUJUMA_APP__AM_SOCKET_SERVER_ENABLED);
        ShellEnvironmentUtils.putToEnvIfSet(nujumaAppEnvironment, ENV_NUJUMA_APP__AM_SOCKET_SERVER_ENABLED,
            NujumaAmSocketServer.getNujumaAppAMSocketServerEnabled(currentPackageContext));
    }

}
