package com.runestone.dynafilter.modules.jpa.api;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.model.statement.AbstractStatement;
import com.runestone.dynafilter.core.model.statement.StatementAnalyser;
import com.runestone.dynafilter.core.model.statement.StatementVisitor;
import com.runestone.dynafilter.modules.jpa.resolver.SpecificationStatementAnalyser;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;

final class JpaSpecificationRestriction extends AbstractStatement {

    private final Specification<?> specification;

    JpaSpecificationRestriction(Specification<?> specification) {
        this.specification = Objects.requireNonNull(specification, "Restriction specification cannot be null");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R acceptAnalyser(StatementAnalyser<R> analyser) {
        Objects.requireNonNull(analyser, "Statement analyser cannot be null");
        if (!(analyser instanceof SpecificationStatementAnalyser)) {
            throw new DynamicFilterConfigurationException("JPA specification restrictions require a SpecificationStatementAnalyser: "
                    + analyser.getClass().getName());
        }
        // The analyser family fixes the result type; the core SPI cannot express this JPA-specific relationship.
        return (R) specification;
    }

    @Override
    public void acceptVisitor(StatementVisitor visitor) {
        // Specifications are opaque: no native filter paths or parameters can be exposed to visitors.
    }
}
