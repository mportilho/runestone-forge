package com.runestone.dynafilter.core.statemachine;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class StateMachineCatalog {

    private static final Map<TechnicalStateMachine, StateMachineDefinition> DEFINITIONS = createDefinitions();

    private StateMachineCatalog() {
    }

    public static StateMachineDefinition definition(TechnicalStateMachine name) {
        return DEFINITIONS.get(Objects.requireNonNull(name, "name must not be null"));
    }

    public static Map<TechnicalStateMachine, StateMachineDefinition> definitions() {
        return Map.copyOf(DEFINITIONS);
    }

    private static Map<TechnicalStateMachine, StateMachineDefinition> createDefinitions() {
        Map<TechnicalStateMachine, StateMachineDefinition> definitions = new EnumMap<>(TechnicalStateMachine.class);
        definitions.put(TechnicalStateMachine.STATEMENT_GENERATION, statementGeneration());
        definitions.put(TechnicalStateMachine.DYNAMIC_OPERATION, dynamicOperation());
        definitions.put(TechnicalStateMachine.JPA_RESOLUTION, jpaResolution());
        definitions.put(TechnicalStateMachine.JPA_FETCHING, jpaFetching());
        definitions.put(TechnicalStateMachine.OPENAPI_EXPANSION, openApiExpansion());
        return Map.copyOf(definitions);
    }

    private static StateMachineDefinition statementGeneration() {
        return new StateMachineDefinition(
                TechnicalStateMachine.STATEMENT_GENERATION,
                "InputRecebido",
                Set.of("StatementWrapper", "ErroConfiguracao"),
                List.of(
                        transition("InputRecebido", "MetadadosResolvidos", "findAnnotationData(input)"),
                        transition("MetadadosResolvidos", "SemFiltrosAplicaveis", "nenhum statement criado"),
                        transition("MetadadosResolvidos", "StatementUnico", "um bloco aplicavel"),
                        transition("MetadadosResolvidos", "StatementsMultiplos", "dois ou mais blocos aplicaveis"),
                        transition("SemFiltrosAplicaveis", "NoOpStatement", "ausencia de filtros aplicaveis"),
                        transition("StatementUnico", "StatementWrapper", "wrapper direto"),
                        transition("StatementsMultiplos", "CompoundRootConjunction", "combina blocos por conjunction"),
                        transition("CompoundRootConjunction", "StatementWrapper", "wrapper composto"),
                        transition("MetadadosResolvidos", "ErroConfiguracao", "filtro invalido ou obrigatorio ausente")
                )
        );
    }

    private static StateMachineDefinition dynamicOperation() {
        return new StateMachineDefinition(
                TechnicalStateMachine.DYNAMIC_OPERATION,
                "ValorRecebido",
                Set.of("FiltroCriado", "ErroFormato"),
                List.of(
                        transition("ValorRecebido", "ErroFormato", "valor nao e Object[]"),
                        transition("ValorRecebido", "CodigoExtraido", "primeiro item e String"),
                        transition("CodigoExtraido", "OperacaoPositiva", "codigo com 2 caracteres"),
                        transition("CodigoExtraido", "OperacaoNegada", "codigo com 3 caracteres e prefixo N/n"),
                        transition("CodigoExtraido", "ErroFormato", "codigo invalido"),
                        transition("OperacaoPositiva", "OperacaoResolvida", "resolve codigo positivo"),
                        transition("OperacaoNegada", "OperacaoResolvida", "remove prefixo de negacao"),
                        transition("OperacaoResolvida", "AjusteIN", "codigo IN"),
                        transition("OperacaoResolvida", "AjusteBT", "codigo BT"),
                        transition("OperacaoResolvida", "FiltroCriado", "demais operacoes"),
                        transition("AjusteIN", "FiltroCriado", "agrupa valores multiplos"),
                        transition("AjusteBT", "FiltroCriado", "exatamente dois valores"),
                        transition("AjusteBT", "ErroFormato", "quantidade diferente de dois")
                )
        );
    }

    private static StateMachineDefinition jpaResolution() {
        return new StateMachineDefinition(
                TechnicalStateMachine.JPA_RESOLUTION,
                "StatementRecebido",
                Set.of("SpecificationFinal"),
                List.of(
                        transition("StatementRecebido", "NoOp", "NoOpStatement"),
                        transition("StatementRecebido", "Logical", "LogicalStatement"),
                        transition("StatementRecebido", "Compound", "CompoundStatement"),
                        transition("StatementRecebido", "Negated", "NegatedStatement"),
                        transition("NoOp", "SpecificationUnrestricted", "consulta irrestrita"),
                        transition("Logical", "SpecificationPorOperacao", "FilterOperationService por operation"),
                        transition("Compound", "SpecificationAndOr", "CONJUNCTION and / DISJUNCTION or"),
                        transition("Negated", "SpecificationNot", "Specification.not"),
                        transition("SpecificationUnrestricted", "DecoratorOpcional", "aplica decorator opcional"),
                        transition("SpecificationPorOperacao", "DecoratorOpcional", "aplica decorator opcional"),
                        transition("SpecificationAndOr", "DecoratorOpcional", "aplica decorator opcional"),
                        transition("SpecificationNot", "DecoratorOpcional", "aplica decorator opcional"),
                        transition("DecoratorOpcional", "SpecificationFinal", "specification final")
                )
        );
    }

    private static StateMachineDefinition jpaFetching() {
        return new StateMachineDefinition(
                TechnicalStateMachine.JPA_FETCHING,
                "DecoratorInvocado",
                Set.of("PredicadoBase"),
                List.of(
                        transition("DecoratorInvocado", "CountQuery", "resultType Long/long"),
                        transition("DecoratorInvocado", "QueryNormal", "outros resultTypes"),
                        transition("CountQuery", "SemFetch", "count query nao recebe fetch join"),
                        transition("QueryNormal", "DistinctAtivado", "distinct(true)"),
                        transition("DistinctAtivado", "FetchesCriadosOuReusados", "paths deduplicados"),
                        transition("SemFetch", "PredicadoBase", "delega ao predicado base"),
                        transition("FetchesCriadosOuReusados", "PredicadoBase", "delega ao predicado base")
                )
        );
    }

    private static StateMachineDefinition openApiExpansion() {
        return new StateMachineDefinition(
                TechnicalStateMachine.OPENAPI_EXPANSION,
                "ParametroDoHandler",
                Set.of("Ignorado", "FiltroConstante", "ParametroOpenAPI"),
                List.of(
                        transition("ParametroDoHandler", "Ignorado", "sem annotation dinamica reconhecida"),
                        transition("ParametroDoHandler", "FiltrosListados", "annotation dinamica reconhecida"),
                        transition("FiltrosListados", "RemoveParametroTecnico", "remove parametro tecnico"),
                        transition("RemoveParametroTecnico", "FiltroConstante", "constantValues presente"),
                        transition("RemoveParametroTecnico", "ParametroDinamico", "operation Dynamic"),
                        transition("RemoveParametroTecnico", "ParametroArray", "operation IsIn"),
                        transition("RemoveParametroTecnico", "ParametroBooleano", "operation IsNull"),
                        transition("RemoveParametroTecnico", "ParametroComum", "demais operacoes"),
                        transition("ParametroDinamico", "ParametroOpenAPI", "array de strings minItems=2"),
                        transition("ParametroArray", "ParametroOpenAPI", "array schema"),
                        transition("ParametroBooleano", "ParametroOpenAPI", "boolean schema"),
                        transition("ParametroComum", "ParametroOpenAPI", "schema do field alvo")
                )
        );
    }

    private static StateTransition transition(String origin, String destination, String trigger) {
        return new StateTransition(origin, destination, trigger);
    }
}
