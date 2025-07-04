package com.payments.service.interfaces.rest.transform;

import com.payments.service.domain.model.commands.CreateSubscriptionCommand;
import com.payments.service.interfaces.rest.resources.CreateSubscriptionResource;

public class CreateSubscriptionCommandFromResourceAssembler {
    public static CreateSubscriptionCommand toCommandFromResource(CreateSubscriptionResource resource) {
        return new CreateSubscriptionCommand(
                resource.userId(),
                resource.plan(),
                resource.returnUrl(),
                resource.cancelUrl()
        );
    }
}
