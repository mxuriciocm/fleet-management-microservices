package com.payments.service.interfaces.rest.transform;

import com.payments.service.domain.model.commands.CreatePaymentCommand;
import com.payments.service.interfaces.rest.resources.CreatePaymentResource;

public class CreatePaymentCommandFromResourceAssembler {
    public static CreatePaymentCommand toCommandFromResource(CreatePaymentResource resource) {
        return new CreatePaymentCommand(
                resource.userId(),
                resource.subscriptionId(),
                resource.type(),
                resource.method(),
                resource.amount(),
                resource.currency(),
                resource.description(),
                resource.returnUrl(),
                resource.cancelUrl()
        );
    }
}
