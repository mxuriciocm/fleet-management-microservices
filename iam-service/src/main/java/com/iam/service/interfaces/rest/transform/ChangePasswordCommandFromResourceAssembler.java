package com.iam.service.interfaces.rest.transform;

import com.iam.service.domain.model.commands.ChangePasswordCommand;
import com.iam.service.interfaces.rest.resources.ChangePasswordResource;

/**
 * Assembler to convert a ChangePasswordResource to a ChangePasswordCommand.
 */
public class ChangePasswordCommandFromResourceAssembler {

    /**
     * Converts a ChangePasswordResource to a ChangePasswordCommand.
     *
     * @param resource The ChangePasswordResource to convert
     * @param userId The ID of the user whose password will be changed
     * @return The ChangePasswordCommand
     */
    public static ChangePasswordCommand toCommandFromResource(ChangePasswordResource resource, Long userId) {
        return new ChangePasswordCommand(
            userId,
            resource.currentPassword(),
            resource.newPassword()
        );
    }
}
