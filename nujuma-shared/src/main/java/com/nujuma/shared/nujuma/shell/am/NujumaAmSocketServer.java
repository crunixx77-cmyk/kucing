package com.nujuma.shared.nujuma.shell.am;

import android.content.Context;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nujuma.shared.errors.Error;
import com.nujuma.shared.logger.Logger;
import com.nujuma.shared.net.socket.local.LocalClientSocket;
import com.nujuma.shared.net.socket.local.LocalServerSocket;
import com.nujuma.shared.net.socket.local.LocalSocketManager;
import com.nujuma.shared.net.socket.local.LocalSocketManagerClientBase;
import com.nujuma.shared.net.socket.local.LocalSocketRunConfig;
import com.nujuma.shared.shell.am.AmSocketServerRunConfig;
import com.nujuma.shared.shell.am.AmSocketServer;
import com.nujuma.shared.nujuma.NujumaConstants;
import com.nujuma.shared.nujuma.crash.NujumaCrashUtils;
import com.nujuma.shared.nujuma.plugins.NujumaPluginUtils;
import com.nujuma.shared.nujuma.settings.properties.NujumaAppSharedProperties;
import com.nujuma.shared.nujuma.settings.properties.NujumaPropertyConstants;
import com.nujuma.shared.nujuma.shell.command.environment.NujumaAppShellEnvironment;

/**
 * A wrapper for {@link AmSocketServer} for nujuma-app usage.
 *
 * The static {@link #nujumaAmSocketServer} variable stores the {@link LocalSocketManager} for the
 * {@link AmSocketServer}.
 *
 * The {@link NujumaAmSocketServerClient} extends the {@link AmSocketServer.AmSocketServerClient}
 * class to also show plugin error notifications for errors and disallowed client connections in
 * addition to logging the messages to logcat, which are only logged by {@link LocalSocketManagerClientBase}
 * if log level is debug or higher for privacy issues.
 *
 * It uses a filesystem socket server with the socket file at
 * {@link NujumaConstants.NUJUMA_APP#NUJUMA_AM_SOCKET_FILE_PATH}. It would normally only allow
 * processes belonging to the nujuma user and root user to connect to it. If commands are sent by the
 * root user, then the am commands executed will be run as the nujuma user and its permissions,
 * capabilities and selinux context instead of root.
 *
 * The `$PREFIX/bin/nujuma-am` client connects to the server via `$PREFIX/bin/nujuma-am-socket` to
 * run the am commands. It provides similar functionality to "$PREFIX/bin/am"
 * (and "/system/bin/am"), but should be faster since it does not require starting a dalvik vm for
 * every command as done by "am" via nujuma/NujumaAm.
 *
 * The server is started by nujuma-app Application class but is not started if
 * {@link NujumaPropertyConstants#KEY_RUN_NUJUMA_AM_SOCKET_SERVER} is `false` which can be done by
 * adding the prop with value "false" to the "~/.nujuma/nujuma.properties" file. Changes
 * require nujuma-app to be force stopped and restarted.
 *
 * The current state of the server can be checked with the
 * {@link NujumaAppShellEnvironment#ENV_NUJUMA_APP__AM_SOCKET_SERVER_ENABLED} env variable, which is exported
 * for all shell sessions and tasks.
 *
 * https://github.com/nujuma/nujuma-am-socket
 * https://github.com/nujuma/NujumaAm
 */
public class NujumaAmSocketServer {

    public static final String LOG_TAG = "NujumaAmSocketServer";

    public static final String TITLE = "NujumaAm";

    /** The static instance for the {@link NujumaAmSocketServer} {@link LocalSocketManager}. */
    private static LocalSocketManager nujumaAmSocketServer;

    /** Whether {@link NujumaAmSocketServer} is enabled and running or not. */
    @Keep
    protected static Boolean NUJUMA_APP_AM_SOCKET_SERVER_ENABLED;

    /**
     * Setup the {@link AmSocketServer} {@link LocalServerSocket} and start listening for
     * new {@link LocalClientSocket} if enabled.
     *
     * @param context The {@link Context} for {@link LocalSocketManager}.
     */
    public static void setupNujumaAmSocketServer(@NonNull Context context) {
        // Start nujuma-am-socket server if enabled by user
        boolean enabled = false;
        if (NujumaAppSharedProperties.getProperties().shouldRunNujumaAmSocketServer()) {
            Logger.logDebug(LOG_TAG, "Starting " + TITLE + " socket server since its enabled");
            start(context);
            if (nujumaAmSocketServer != null && nujumaAmSocketServer.isRunning()) {
                enabled = true;
                Logger.logDebug(LOG_TAG, TITLE + " socket server successfully started");
            }
        } else {
            Logger.logDebug(LOG_TAG, "Not starting " + TITLE + " socket server since its not enabled");
        }

        // Once nujuma-app has started, the server state must not be changed since the variable is
        // exported in shell sessions and tasks and if state is changed, then env of older shells will
        // retain invalid value. User should force stop the app to update state after changing prop.
        NUJUMA_APP_AM_SOCKET_SERVER_ENABLED = enabled;
        NujumaAppShellEnvironment.updateNujumaAppAMSocketServerEnabled(context);
    }

    /**
     * Create the {@link AmSocketServer} {@link LocalServerSocket} and start listening for new {@link LocalClientSocket}.
     */
    public static synchronized void start(@NonNull Context context) {
        stop();

        AmSocketServerRunConfig amSocketServerRunConfig = new AmSocketServerRunConfig(TITLE,
            NujumaConstants.NUJUMA_APP.NUJUMA_AM_SOCKET_FILE_PATH, new NujumaAmSocketServerClient());

        nujumaAmSocketServer = AmSocketServer.start(context, amSocketServerRunConfig);
    }

    /**
     * Stop the {@link AmSocketServer} {@link LocalServerSocket} and stop listening for new {@link LocalClientSocket}.
     */
    public static synchronized void stop() {
        if (nujumaAmSocketServer != null) {
            Error error = nujumaAmSocketServer.stop();
            if (error != null) {
                nujumaAmSocketServer.onError(error);
            }
            nujumaAmSocketServer = null;
        }
    }
    
    /**
     * Update the state of the {@link AmSocketServer} {@link LocalServerSocket} depending on current
     * value of {@link NujumaPropertyConstants#KEY_RUN_NUJUMA_AM_SOCKET_SERVER}.
     */
    public static synchronized void updateState(@NonNull Context context) {
        NujumaAppSharedProperties properties = NujumaAppSharedProperties.getProperties();
        if (properties.shouldRunNujumaAmSocketServer()) {
            if (nujumaAmSocketServer == null) {
                Logger.logDebug(LOG_TAG, "updateState: Starting " + TITLE + " socket server");
                start(context);
            }
        } else {
            if (nujumaAmSocketServer != null) {
                Logger.logDebug(LOG_TAG, "updateState: Disabling " + TITLE + " socket server");
                stop();
            }
        }
    }
    
    /**
     * Get {@link #nujumaAmSocketServer}.
     */
    public static synchronized LocalSocketManager getNujumaAmSocketServer() {
        return nujumaAmSocketServer;
    }

    /**
     * Show an error notification on the {@link NujumaConstants#NUJUMA_PLUGIN_COMMAND_ERRORS_NOTIFICATION_CHANNEL_ID}
     * {@link NujumaConstants#NUJUMA_PLUGIN_COMMAND_ERRORS_NOTIFICATION_CHANNEL_NAME} with a call
     * to {@link NujumaPluginUtils#sendPluginCommandErrorNotification(Context, String, CharSequence, String, String)}.
     *
     * @param context The {@link Context} to send the notification with.
     * @param error The {@link Error} generated.
     * @param localSocketRunConfig The {@link LocalSocketRunConfig} for {@link LocalSocketManager}.
     * @param clientSocket The optional {@link LocalClientSocket} for which the error was generated.
     */
    public static synchronized void showErrorNotification(@NonNull Context context, @NonNull Error error,
                                                          @NonNull LocalSocketRunConfig localSocketRunConfig,
                                                          @Nullable LocalClientSocket clientSocket) {
        NujumaPluginUtils.sendPluginCommandErrorNotification(context, LOG_TAG,
            localSocketRunConfig.getTitle() + " Socket Server Error", error.getMinimalErrorString(),
            LocalSocketManager.getErrorMarkdownString(error, localSocketRunConfig, clientSocket));
    }



    public static Boolean getNujumaAppAMSocketServerEnabled(@NonNull Context currentPackageContext) {
        boolean isNujumaApp = NujumaConstants.NUJUMA_PACKAGE_NAME.equals(currentPackageContext.getPackageName());
        if (isNujumaApp) {
            return NUJUMA_APP_AM_SOCKET_SERVER_ENABLED;
        } else {
            // Currently, unsupported since plugin app processes don't know that value is set in nujuma
            // app process NujumaAmSocketServer class. A binder API or a way to check if server is actually
            // running needs to be used. Long checks would also not be possible on main application thread
            return null;
        }

    }





    /** Enhanced implementation for {@link AmSocketServer.AmSocketServerClient} for {@link NujumaAmSocketServer}. */
    public static class NujumaAmSocketServerClient extends AmSocketServer.AmSocketServerClient {

        public static final String LOG_TAG = "NujumaAmSocketServerClient";

        @Nullable
        @Override
        public Thread.UncaughtExceptionHandler getLocalSocketManagerClientThreadUEH(
            @NonNull LocalSocketManager localSocketManager) {
            // Use nujuma crash handler for socket listener thread just like used for main app process thread.
            return NujumaCrashUtils.getCrashHandler(localSocketManager.getContext());
        }

        @Override
        public void onError(@NonNull LocalSocketManager localSocketManager,
                            @Nullable LocalClientSocket clientSocket, @NonNull Error error) {
            // Don't show notification if server is not running since errors may be triggered
            // when server is stopped and server and client sockets are closed.
            if (localSocketManager.isRunning()) {
                NujumaAmSocketServer.showErrorNotification(localSocketManager.getContext(), error,
                    localSocketManager.getLocalSocketRunConfig(), clientSocket);
            }

            // But log the exception
            super.onError(localSocketManager, clientSocket, error);
        }

        @Override
        public void onDisallowedClientConnected(@NonNull LocalSocketManager localSocketManager,
                                                @NonNull LocalClientSocket clientSocket, @NonNull Error error) {
            // Always show notification and log error regardless of if server is running or not
            NujumaAmSocketServer.showErrorNotification(localSocketManager.getContext(), error,
                localSocketManager.getLocalSocketRunConfig(), clientSocket);
            super.onDisallowedClientConnected(localSocketManager, clientSocket, error);
        }



        @Override
        protected String getLogTag() {
            return LOG_TAG;
        }

    }

}
