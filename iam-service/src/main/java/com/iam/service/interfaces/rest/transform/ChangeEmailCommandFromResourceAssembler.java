package com.iam.service.interfaces.rest.transform;

import com.iam.service.domain.model.commands.ChangeEmailCommand;
import com.iam.service.interfaces.rest.resources.ChangeEmailResource;

/**
 * Assembler to convert a ChangeEmailResource to a ChangeEmailCommand.
 */
public class ChangeEmailCommandFromResourceAssembler {

    /**
     * Converts a ChangeEmailResource to a ChangeEmailCommand.
     *
     * @param resource The ChangeEmailResource to convert
     * @param userId The ID of the user whose email will be changed
     * @return The ChangeEmailCommand
     */
    public static ChangeEmailCommand toCommandFromResource(ChangeEmailResource resource, Long userId) {
        return new ChangeEmailCommand(
            userId,
            resource.password(),
            resource.newEmail()
        );
    }
}
