package com.profiles.service.domain.model.aggregates;

import com.profiles.service.domain.model.commands.CreateProfileCommand;
import com.profiles.service.domain.model.valueobjects.PersonName;
import com.profiles.service.domain.model.valueobjects.PhoneNumber;
import com.profiles.service.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;

/**
 * UserProfile aggregate root.
 */
@Entity
public class Profile extends AuditableAbstractAggregateRoot<Profile> {

    // User reference identifier (not a foreign key)
    private Long userId;

    @Embedded
    private PersonName name;

    @Embedded
    private PhoneNumber phoneNumber;

    /**
     * Constructor with userId, first name, last name, and phone number
     * @param userId User ID
     * @param firstName First name
     * @param lastName Last name
     * @param phoneNumber Phone number
     */
    public Profile(Long userId, String firstName, String lastName, String phoneNumber){
        this.userId = userId;
        this.name = new PersonName(firstName, lastName);
        this.phoneNumber = new PhoneNumber(phoneNumber);
    }

    public Profile() {}

    public Profile(CreateProfileCommand command) {
        this.userId = command.userId();
        this.name = new PersonName(command.firstName(), command.lastName());
        this.phoneNumber = new PhoneNumber(command.phoneNumber());
    }

    public String getFullName() { return name.getFullName(); }

    public void updateName(String firstName, String lastName) { this.name = new PersonName(firstName, lastName); }

    public void updatePhoneNumber(String phoneNumber) { this.phoneNumber = new PhoneNumber(phoneNumber); }

    public String getPhoneNumber() {
        return phoneNumber.getPhoneNumber();
    }

    public Long getUserId() {
        return userId;
    }
}
