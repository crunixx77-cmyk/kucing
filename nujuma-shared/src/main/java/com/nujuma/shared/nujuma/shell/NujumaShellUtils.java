package com.nujuma.shared.nujuma.shell;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nujuma.shared.errors.Error;
import com.nujuma.shared.file.filesystem.FileTypes;
import com.nujuma.shared.nujuma.NujumaConstants;
import com.nujuma.shared.file.FileUtils;
import com.nujuma.shared.logger.Logger;
import com.nujuma.shared.nujuma.settings.properties.NujumaAppSharedProperties;

import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NujumaShellUtils {

    private static final String LOG_TAG = "NujumaShellUtils";

    /**
     * Setup shell command arguments for the execute. The file interpreter may be prefixed to
     * command arguments if needed.
     */
    @NonNull
    public static String[] setupShellCommandArguments(@NonNull String executable, @Nullable String[] arguments) {
        // The file to execute may either be:
        // - An elf file, in which we execute it directly.
        // - A script file without shebang, which we execute with our standard shell $PREFIX/bin/sh instead of the
        //   system /system/bin/sh. The system shell may vary and may not work at all due to LD_LIBRARY_PATH.
        // - A file with shebang, which we try to handle with e.g. /bin/foo -> $PREFIX/bin/foo.
        String interpreter = null;
        try {
            File file = new File(executable);
            try (FileInputStream in = new FileInputStream(file)) {
                byte[] buffer = new byte[256];
                int bytesRead = in.read(buffer);
                if (bytesRead > 4) {
                    if (buffer[0] == 0x7F && buffer[1] == 'E' && buffer[2] == 'L' && buffer[3] == 'F') {
                        // Elf file, do nothing.
                    } else if (buffer[0] == '#' && buffer[1] == '!') {
                        // Try to parse shebang.
                        StringBuilder builder = new StringBuilder();
                        for (int i = 2; i < bytesRead; i++) {
                            char c = (char) buffer[i];
                            if (c == ' ' || c == '\n') {
                                if (builder.length() == 0) {
                                    // Skip whitespace after shebang.
                                } else {
                                    // End of shebang.
                                    String shebangExecutable = builder.toString();
                                    if (shebangExecutable.startsWith("/usr") || shebangExecutable.startsWith("/bin")) {
                                        String[] parts = shebangExecutable.split("/");
                                        String binary = parts[parts.length - 1];
                                        interpreter = NujumaConstants.NUJUMA_BIN_PREFIX_DIR_PATH + "/" + binary;
                                    }
                                    break;
                                }
                            } else {
                                builder.append(c);
                            }
                        }
                    } else {
                        // No shebang and no ELF, use standard shell.
                        interpreter = NujumaConstants.NUJUMA_BIN_PREFIX_DIR_PATH + "/sh";
                    }
                }
            }
        } catch (IOException e) {
            // Ignore.
        }

        List<String> result = new ArrayList<>();
        if (interpreter != null) result.add(interpreter);
        result.add(executable);
        if (arguments != null) Collections.addAll(result, arguments);
        return result.toArray(new String[0]);
    }

    /** Clear files under {@link NujumaConstants#NUJUMA_TMP_PREFIX_DIR_PATH}. */
    public static void clearNujumaTMPDIR(boolean onlyIfExists) {
        // Existence check before clearing may be required since clearDirectory() will automatically
        // re-create empty directory if doesn't exist, which should not be done for things like
        // nujuma-reset (d6eb5e35). Moreover, TMPDIR must be a directory and not a symlink, this can
        // also allow users who don't want TMPDIR to be cleared automatically on nujuma exit, since
        // it may remove files still being used by background processes (#1159).
        if(onlyIfExists && !FileUtils.directoryFileExists(NujumaConstants.NUJUMA_TMP_PREFIX_DIR_PATH, false))
            return;

        Error error;

        NujumaAppSharedProperties properties = NujumaAppSharedProperties.getProperties();
        int days = properties.getDeleteTMPDIRFilesOlderThanXDaysOnExit();

        // Disable currently until FileUtils.deleteFilesOlderThanXDays() is fixed.
        if (days > 0)
            days = 0;

        if (days < 0) {
            Logger.logInfo(LOG_TAG, "Not clearing nujuma $TMPDIR");
        } else if (days == 0) {
            error = FileUtils.clearDirectory("$TMPDIR",
                FileUtils.getCanonicalPath(NujumaConstants.NUJUMA_TMP_PREFIX_DIR_PATH, null));
            if (error != null) {
                Logger.logErrorExtended(LOG_TAG, "Failed to clear nujuma $TMPDIR\n" + error);
            }
        } else {
            error = FileUtils.deleteFilesOlderThanXDays("$TMPDIR",
                FileUtils.getCanonicalPath(NujumaConstants.NUJUMA_TMP_PREFIX_DIR_PATH, null),
                TrueFileFilter.INSTANCE, days, true, FileTypes.FILE_TYPE_ANY_FLAGS);
            if (error != null) {
                Logger.logErrorExtended(LOG_TAG, "Failed to delete files from nujuma $TMPDIR older than " + days + " days\n" + error);
            }
        }
    }

}
