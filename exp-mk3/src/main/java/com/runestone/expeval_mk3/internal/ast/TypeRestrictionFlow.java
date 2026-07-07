package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record TypeRestrictionFlow(
        List<ConcreteTypeRestriction> concreteRestrictions,
        List<SameTypeRestriction> sameTypeRestrictions,
        List<VectorElementTypeRestriction> vectorElementTypeRestrictions,
        List<MembershipTypeRestriction> membershipTypeRestrictions,
        List<RegexLeftOperandRestriction> regexLeftOperandRestrictions,
        List<RegexPatternSource> regexPatternSources,
        List<TypeJoinRestriction> joinRestrictions,
        List<NumericConstantRestriction> numericConstantRestrictions,
        List<NumericSource> numericSources,
        List<OffsetDateTimeLiteralSource> offsetDateTimeLiteralSources,
        Set<NodeId> emptyVectorNodes,
        Map<NodeId, SourceSpan> sourceSpans) {

    public TypeRestrictionFlow {
        Objects.requireNonNull(concreteRestrictions, "concreteRestrictions");
        concreteRestrictions = List.copyOf(concreteRestrictions);
        Objects.requireNonNull(sameTypeRestrictions, "sameTypeRestrictions");
        sameTypeRestrictions = List.copyOf(sameTypeRestrictions);
        Objects.requireNonNull(vectorElementTypeRestrictions, "vectorElementTypeRestrictions");
        vectorElementTypeRestrictions = List.copyOf(vectorElementTypeRestrictions);
        Objects.requireNonNull(membershipTypeRestrictions, "membershipTypeRestrictions");
        membershipTypeRestrictions = List.copyOf(membershipTypeRestrictions);
        Objects.requireNonNull(regexLeftOperandRestrictions, "regexLeftOperandRestrictions");
        regexLeftOperandRestrictions = List.copyOf(regexLeftOperandRestrictions);
        Objects.requireNonNull(regexPatternSources, "regexPatternSources");
        regexPatternSources = List.copyOf(regexPatternSources);
        Objects.requireNonNull(joinRestrictions, "joinRestrictions");
        joinRestrictions = List.copyOf(joinRestrictions);
        Objects.requireNonNull(numericConstantRestrictions, "numericConstantRestrictions");
        numericConstantRestrictions = List.copyOf(numericConstantRestrictions);
        Objects.requireNonNull(numericSources, "numericSources");
        numericSources = List.copyOf(numericSources);
        Objects.requireNonNull(offsetDateTimeLiteralSources, "offsetDateTimeLiteralSources");
        offsetDateTimeLiteralSources = List.copyOf(offsetDateTimeLiteralSources);
        Objects.requireNonNull(emptyVectorNodes, "emptyVectorNodes");
        emptyVectorNodes = Set.copyOf(emptyVectorNodes);
        Objects.requireNonNull(sourceSpans, "sourceSpans");
        sourceSpans = Map.copyOf(sourceSpans);
    }

    public static TypeRestrictionFlow empty() {
        return new TypeRestrictionFlow(
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                Set.of(),
                Map.of());
    }
}
