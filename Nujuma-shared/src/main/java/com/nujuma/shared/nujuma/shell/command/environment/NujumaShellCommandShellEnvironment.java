package com.nujuma.shared.nujuma.shell.command.environment;

import android.content.Context;

import androidx.annotation.NonNull;

import com.nujuma.shared.shell.command.ExecutionCommand;
import com.nujuma.shared.shell.command.environment.ShellCommandShellEnvironment;
import com.nujuma.shared.shell.command.environment.ShellEnvironmentUtils;
import com.nujuma.shared.nujuma.settings.preferences.NujumaAppSharedPreferences;
import com.nujuma.shared.nujuma.shell.NujumaShellManager;

import java.util.HashMap;

/**
 * Environment for Nujuma {@link ExecutionCommand}.
 */
public class NujumaShellCommandShellEnvironment extends ShellCommandShellEnvironment {

    /** Get shell environment containing info for Nujuma {@link ExecutionCommand}. */
    @NonNull
    @Override
    public HashMap<String, String> getEnvironment(@NonNull Context currentPackageContext,
                                                  @NonNull ExecutionCommand executionCommand) {
        HashMap<String, String> environment = super.getEnvironment(currentPackageContext, executionCommand);

        NujumaAppSharedPreferences preferences = NujumaAppSharedPreferences.build(currentPackageContext);
        if (preferences == null) return environment;

        if (ExecutionCommand.Runner.APP_SHELL.equalsRunner(executionCommand.runner)) {
            ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_SHELL_CMD__APP_SHELL_NUMBER_SINCE_BOOT,
                String.valueOf(preferences.getAndIncrementAppShellNumberSinceBoot()));
            ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_SHELL_CMD__APP_SHELL_NUMBER_SINCE_APP_START,
                String.valueOf(NujumaShellManager.getAndIncrementAppShellNumberSinceAppStart()));

        } else if (ExecutionCommand.Runner.TERMINAL_SESSION.equalsRunner(executionCommand.runner)) {
            ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_SHELL_CMD__APP_TERMINAL_SESSION_NUMBER_SINCE_BOOT,
                String.valueOf(preferences.getAndIncrementTerminalSessionNumberSinceBoot()));
            ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_SHELL_CMD__APP_TERMINAL_SESSION_NUMBER_SINCE_APP_START,
                String.valueOf(NujumaShellManager.getAndIncrementTerminalSessionNumberSinceAppStart()));
        } else {
            return environment;
        }

        return environment;
    }

}
