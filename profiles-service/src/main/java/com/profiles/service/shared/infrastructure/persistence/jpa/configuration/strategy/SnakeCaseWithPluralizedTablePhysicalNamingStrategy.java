package com.profiles.service.shared.infrastructure.persistence.jpa.configuration.strategy;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import static io.github.encryptorcode.pluralize.Pluralize.pluralize;

/**
 * Snake Case With Pluralized Table Physical Naming Strategy
 * @summary
 * PhysicalNamingStrategy implementation that converts entity names to snake_case and table names to plural form.
 * @since 1.0.0
 */
public class SnakeCaseWithPluralizedTablePhysicalNamingStrategy implements PhysicalNamingStrategy {
    @Override
    public Identifier toPhysicalCatalogName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return this.toSnakeCase(identifier);
    }

    /**
     * Converts the given Physical Schema Name to snake_case.
     * @param identifier The identifier to convert.
     * @param jdbcEnvironment The JDBC environment.
     * @return The converted identifier.
     */
    @Override
    public Identifier toPhysicalSchemaName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return this.toSnakeCase(identifier);
    }

    /**
     * Converts the given Physical Table Name to snake_case and plural form.
     * @param identifier The identifier to convert.
     * @param jdbcEnvironment The JDBC environment.
     * @return The converted identifier.
     */
    @Override
    public Identifier toPhysicalTableName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return this.toSnakeCase(this.toPlural(identifier));
    }

    /**
     * Converts the given Physical Sequence Name to snake_case.
     * @param identifier The identifier to convert.
     * @param jdbcEnvironment The JDBC environment.
     * @return The converted identifier.
     */
    @Override
    public Identifier toPhysicalSequenceName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return this.toSnakeCase(identifier);
    }


    /**
     * Converts the given Physical Column Name to snake_case.
     * @param identifier The identifier to convert.
     * @param jdbcEnvironment The JDBC environment.
     * @return The converted identifier.
     */
    @Override
    public Identifier toPhysicalColumnName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return this.toSnakeCase(identifier);
    }

    /**
     * Converts the given identifier to snake_case.
     * @param identifier The identifier to convert.
     * @return The converted identifier.
     */
    private Identifier toSnakeCase(final Identifier identifier) {
        if(identifier == null) return null;
        final String regex = "([a-z])([A-Z])";
        final String replacement = "$1_$2";
        final String newName = identifier.getText().replaceAll(regex, replacement).toLowerCase();
        return Identifier.toIdentifier(newName);
    }

    /**
     * Converts the given identifier to plural form.
     * @param identifier The identifier to convert.
     * @return The converted identifier.
     */
    private Identifier toPlural(final Identifier identifier) {
        if (identifier == null) return null;
        final String newName = pluralize(identifier.getText());
        return Identifier.toIdentifier(newName);
    }
}

