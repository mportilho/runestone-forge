package com.runestone.expeval_mk3.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ExpressionDiagnostic {

    private final DiagnosticCategory category;
    private final DiagnosticSeverity severity;
    private final String code;
    private final String message;
    private final SourceSpan primarySpan;
    private final List<RelatedInformation> relatedInformation;
    private final List<String> notes;
    private final String suggestion;

    private ExpressionDiagnostic(Builder builder) {
        this.category = Objects.requireNonNull(builder.category, "category");
        this.severity = Objects.requireNonNull(builder.severity, "severity");
        this.code = Objects.requireNonNull(builder.code, "code");
        this.message = Objects.requireNonNull(builder.message, "message");
        this.primarySpan = builder.primarySpan;
        this.relatedInformation = List.copyOf(builder.relatedInformation);
        this.notes = List.copyOf(builder.notes);
        this.suggestion = builder.suggestion;
    }

    public static ExpressionDiagnostic error(DiagnosticCategory category, String code, String message, SourceSpan primarySpan) {
        return builder(category, DiagnosticSeverity.ERROR, code, message).primarySpan(primarySpan).build();
    }

    public static ExpressionDiagnostic warning(DiagnosticCategory category, String code, String message, SourceSpan primarySpan) {
        return builder(category, DiagnosticSeverity.WARNING, code, message).primarySpan(primarySpan).build();
    }

    public static Builder builder(DiagnosticCategory category, DiagnosticSeverity severity, String code, String message) {
        return new Builder(category, severity, code, message);
    }

    public DiagnosticCategory category() {
        return category;
    }

    public DiagnosticSeverity severity() {
        return severity;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }

    public Optional<SourceSpan> primarySpan() {
        return Optional.ofNullable(primarySpan);
    }

    public List<RelatedInformation> relatedInformation() {
        return relatedInformation;
    }

    public List<String> notes() {
        return notes;
    }

    public Optional<String> suggestion() {
        return Optional.ofNullable(suggestion);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ExpressionDiagnostic that)) {
            return false;
        }
        return category == that.category
                && severity == that.severity
                && code.equals(that.code)
                && message.equals(that.message)
                && Objects.equals(primarySpan, that.primarySpan)
                && relatedInformation.equals(that.relatedInformation)
                && notes.equals(that.notes)
                && Objects.equals(suggestion, that.suggestion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, severity, code, message, primarySpan, relatedInformation, notes, suggestion);
    }

    @Override
    public String toString() {
        return "ExpressionDiagnostic[category=" + category
                + ", severity=" + severity
                + ", code=" + code
                + ", message=" + message
                + ", primarySpan=" + primarySpan
                + ", relatedInformation=" + relatedInformation
                + ", notes=" + notes
                + ", suggestion=" + suggestion
                + ']';
    }

    public static final class Builder {

        private final DiagnosticCategory category;
        private final DiagnosticSeverity severity;
        private final String code;
        private final String message;
        private SourceSpan primarySpan;
        private List<RelatedInformation> relatedInformation = new ArrayList<>();
        private List<String> notes = new ArrayList<>();
        private String suggestion;

        private Builder(DiagnosticCategory category, DiagnosticSeverity severity, String code, String message) {
            this.category = category;
            this.severity = severity;
            this.code = code;
            this.message = message;
        }

        public Builder primarySpan(SourceSpan primarySpan) {
            this.primarySpan = primarySpan;
            return this;
        }

        public Builder relatedInformation(List<RelatedInformation> relatedInformation) {
            this.relatedInformation = new ArrayList<>(Objects.requireNonNull(relatedInformation, "relatedInformation"));
            return this;
        }

        public Builder notes(List<String> notes) {
            this.notes = new ArrayList<>(Objects.requireNonNull(notes, "notes"));
            return this;
        }

        public Builder suggestion(String suggestion) {
            this.suggestion = suggestion;
            return this;
        }

        public ExpressionDiagnostic build() {
            return new ExpressionDiagnostic(this);
        }
    }
}
