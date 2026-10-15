package com.nujuma.shared.nujuma.shell.command.environment;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;

import androidx.annotation.NonNull;

import com.nujuma.shared.android.PackageUtils;
import com.nujuma.shared.errors.Error;
import com.nujuma.shared.file.FileUtils;
import com.nujuma.shared.logger.Logger;
import com.nujuma.shared.shell.command.ExecutionCommand;
import com.nujuma.shared.shell.command.environment.AndroidShellEnvironment;
import com.nujuma.shared.shell.command.environment.ShellEnvironmentUtils;
import com.nujuma.shared.shell.command.environment.ShellCommandShellEnvironment;
import com.nujuma.shared.nujuma.NujumaBootstrap;
import com.nujuma.shared.nujuma.NujumaConstants;
import com.nujuma.shared.nujuma.shell.NujumaShellUtils;

import java.nio.charset.Charset;
import java.util.HashMap;

/**
 * Environment for Nujuma.
 */
public class NujumaShellEnvironment extends AndroidShellEnvironment {

    private static final String LOG_TAG = "NujumaShellEnvironment";

    /** Environment variable for the nujuma {@link NujumaConstants#NUJUMA_PREFIX_DIR_PATH}. */
    public static final String ENV_PREFIX = "PREFIX";

    public NujumaShellEnvironment() {
        super();
        shellCommandShellEnvironment = new NujumaShellCommandShellEnvironment();
    }


    /** Init {@link NujumaShellEnvironment} constants and caches. */
    public synchronized static void init(@NonNull Context currentPackageContext) {
        NujumaAppShellEnvironment.setNujumaAppEnvironment(currentPackageContext);
    }

    /** Init {@link NujumaShellEnvironment} constants and caches. */
    public synchronized static void writeEnvironmentToFile(@NonNull Context currentPackageContext) {
        HashMap<String, String> environmentMap = new NujumaShellEnvironment().getEnvironment(currentPackageContext, false);
        String environmentString = ShellEnvironmentUtils.convertEnvironmentToDotEnvFile(environmentMap);

        // Write environment string to temp file and then move to final location since otherwise
        // writing may happen while file is being sourced/read
        Error error = FileUtils.writeTextToFile("nujuma.env.tmp", NujumaConstants.NUJUMA_ENV_TEMP_FILE_PATH,
            Charset.defaultCharset(), environmentString, false);
        if (error != null) {
            Logger.logErrorExtended(LOG_TAG, error.toString());
            return;
        }

        error = FileUtils.moveRegularFile("nujuma.env.tmp", NujumaConstants.NUJUMA_ENV_TEMP_FILE_PATH, NujumaConstants.NUJUMA_ENV_FILE_PATH, true);
        if (error != null) {
            Logger.logErrorExtended(LOG_TAG, error.toString());
        }
    }

    /** Get shell environment for Nujuma. */
    @NonNull
    @Override
    public HashMap<String, String> getEnvironment(@NonNull Context currentPackageContext, boolean isFailSafe) {

        // Nujuma environment builds upon the Android environment
        HashMap<String, String> environment = super.getEnvironment(currentPackageContext, isFailSafe);

        HashMap<String, String> nujumaAppEnvironment = NujumaAppShellEnvironment.getEnvironment(currentPackageContext);
        if (nujumaAppEnvironment != null)
            environment.putAll(nujumaAppEnvironment);

        /*
        HashMap<String, String> nujumaApiAppEnvironment = NujumaAPIShellEnvironment.getEnvironment(currentPackageContext);
        if (nujumaApiAppEnvironment != null)
            environment.putAll(nujumaApiAppEnvironment);
         */

        ApplicationInfo applicationInfo = PackageUtils.getApplicationInfoForPackage(currentPackageContext, NujumaConstants.NUJUMA_PACKAGE_NAME);
        if (applicationInfo != null && !applicationInfo.enabled) {
            applicationInfo = null;
        }

        if (applicationInfo != null) {
            environment.put("NUJUMA__APPS_DIR", applicationInfo.dataDir + "/nujuma/apps");
        }
        environment.put("NUJUMA__ROOTFS_DIR", NujumaConstants.NUJUMA_FILES_DIR_PATH);
        environment.put(ENV_HOME, NujumaConstants.NUJUMA_HOME_DIR_PATH);
        environment.put("NUJUMA__HOME", NujumaConstants.NUJUMA_HOME_DIR_PATH);
        environment.put(ENV_PREFIX, NujumaConstants.NUJUMA_PREFIX_DIR_PATH);
        environment.put("NUJUMA__PREFIX", NujumaConstants.NUJUMA_PREFIX_DIR_PATH);

        // If failsafe is not enabled, then we keep default PATH and TMPDIR so that system binaries can be used
        if (!isFailSafe) {
            environment.put(ENV_TMPDIR, NujumaConstants.NUJUMA_TMP_PREFIX_DIR_PATH);
            if (NujumaBootstrap.isAppPackageVariantAPTAndroid5()) {
                // Nujuma in android 5/6 era shipped busybox binaries in applets directory
                environment.put(ENV_PATH, NujumaConstants.NUJUMA_BIN_PREFIX_DIR_PATH + ":" + NujumaConstants.NUJUMA_BIN_PREFIX_DIR_PATH + "/applets");
                environment.put(ENV_LD_LIBRARY_PATH, NujumaConstants.NUJUMA_LIB_PREFIX_DIR_PATH);
            } else {
                // Nujuma binaries on Android 7+ rely on DT_RUNPATH, so LD_LIBRARY_PATH should be unset by default
                environment.put(ENV_PATH, NujumaConstants.NUJUMA_BIN_PREFIX_DIR_PATH);
                environment.remove(ENV_LD_LIBRARY_PATH);
            }
        }

        return environment;
    }


    @NonNull
    @Override
    public String getDefaultWorkingDirectoryPath() {
        return NujumaConstants.NUJUMA_HOME_DIR_PATH;
    }

    @NonNull
    @Override
    public String getDefaultBinPath() {
        return NujumaConstants.NUJUMA_BIN_PREFIX_DIR_PATH;
    }

    @NonNull
    @Override
    public String[] setupShellCommandArguments(@NonNull String executable, String[] arguments) {
        return NujumaShellUtils.setupShellCommandArguments(executable, arguments);
    }

}
