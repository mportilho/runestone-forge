package com.runestone.dynafilter.core.statemachine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StateMachineCatalogTest {

    @Test
    @DisplayName("documents statement generation transitions")
    void documentsStatementGenerationTransitions() {
        StateMachineDefinition definition = StateMachineCatalog.definition(TechnicalStateMachine.STATEMENT_GENERATION);

        assertThat(definition.initialState()).isEqualTo("InputRecebido");
        assertThat(definition.terminalStates()).contains("StatementWrapper", "ErroConfiguracao");
        assertThat(definition.hasTransition("MetadadosResolvidos", "SemFiltrosAplicaveis")).isTrue();
        assertThat(definition.hasTransition("MetadadosResolvidos", "StatementUnico")).isTrue();
        assertThat(definition.hasTransition("MetadadosResolvidos", "StatementsMultiplos")).isTrue();
        assertThat(definition.hasTransition("StatementsMultiplos", "CompoundRootConjunction")).isTrue();
        assertThat(definition.hasTransition("MetadadosResolvidos", "ErroConfiguracao")).isTrue();
    }

    @Test
    @DisplayName("documents dynamic operation transitions")
    void documentsDynamicOperationTransitions() {
        StateMachineDefinition definition = StateMachineCatalog.definition(TechnicalStateMachine.DYNAMIC_OPERATION);

        assertThat(definition.initialState()).isEqualTo("ValorRecebido");
        assertThat(definition.terminalStates()).contains("FiltroCriado", "ErroFormato");
        assertThat(definition.hasTransition("ValorRecebido", "ErroFormato")).isTrue();
        assertThat(definition.hasTransition("CodigoExtraido", "OperacaoPositiva")).isTrue();
        assertThat(definition.hasTransition("CodigoExtraido", "OperacaoNegada")).isTrue();
        assertThat(definition.hasTransition("OperacaoResolvida", "AjusteIN")).isTrue();
        assertThat(definition.hasTransition("OperacaoResolvida", "AjusteBT")).isTrue();
        assertThat(definition.hasTransition("AjusteBT", "ErroFormato")).isTrue();
    }

    @Test
    @DisplayName("documents JPA resolution transitions")
    void documentsJpaResolutionTransitions() {
        StateMachineDefinition definition = StateMachineCatalog.definition(TechnicalStateMachine.JPA_RESOLUTION);

        assertThat(definition.hasTransition("StatementRecebido", "NoOp")).isTrue();
        assertThat(definition.hasTransition("NoOp", "SpecificationUnrestricted")).isTrue();
        assertThat(definition.hasTransition("Logical", "SpecificationPorOperacao")).isTrue();
        assertThat(definition.hasTransition("Compound", "SpecificationAndOr")).isTrue();
        assertThat(definition.hasTransition("Negated", "SpecificationNot")).isTrue();
        assertThat(definition.hasTransition("DecoratorOpcional", "SpecificationFinal")).isTrue();
    }

    @Test
    @DisplayName("documents JPA fetching transitions")
    void documentsJpaFetchingTransitions() {
        StateMachineDefinition definition = StateMachineCatalog.definition(TechnicalStateMachine.JPA_FETCHING);

        assertThat(definition.hasTransition("DecoratorInvocado", "CountQuery")).isTrue();
        assertThat(definition.hasTransition("DecoratorInvocado", "QueryNormal")).isTrue();
        assertThat(definition.hasTransition("CountQuery", "SemFetch")).isTrue();
        assertThat(definition.hasTransition("QueryNormal", "DistinctAtivado")).isTrue();
        assertThat(definition.hasTransition("DistinctAtivado", "FetchesCriadosOuReusados")).isTrue();
    }

    @Test
    @DisplayName("documents OpenAPI expansion transitions")
    void documentsOpenApiExpansionTransitions() {
        StateMachineDefinition definition = StateMachineCatalog.definition(TechnicalStateMachine.OPENAPI_EXPANSION);

        assertThat(definition.hasTransition("ParametroDoHandler", "Ignorado")).isTrue();
        assertThat(definition.hasTransition("ParametroDoHandler", "FiltrosListados")).isTrue();
        assertThat(definition.hasTransition("FiltrosListados", "RemoveParametroTecnico")).isTrue();
        assertThat(definition.hasTransition("RemoveParametroTecnico", "FiltroConstante")).isTrue();
        assertThat(definition.hasTransition("RemoveParametroTecnico", "ParametroDinamico")).isTrue();
        assertThat(definition.hasTransition("RemoveParametroTecnico", "ParametroArray")).isTrue();
        assertThat(definition.hasTransition("RemoveParametroTecnico", "ParametroBooleano")).isTrue();
        assertThat(definition.hasTransition("RemoveParametroTecnico", "ParametroComum")).isTrue();
    }
}
