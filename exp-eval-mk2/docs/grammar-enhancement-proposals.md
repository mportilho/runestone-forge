# Propostas de Melhoria para a Gramática ExpressionEvaluatorV2

Após análise da gramática `ExpressionEvaluatorV2.g4` e do contexto do projeto `exp-eval-mk2`, foram identificadas as seguintes oportunidades de incremento funcional:

## 1. Suporte a Nulidade e Operadores de Segurança (Null-Safety)
Atualmente, a gramática não possui uma representação explícita para `null`. Adicionar suporte a valores nulos permitiria:
*   **Literal `null`:** Essencial para representar ausência de valor em dados externos.
*   **Operador de Coalescência Nula (`??`):** `x ?? "valor padrão"`.
*   **Navegação Segura (`?.`):** Se o motor suportar objetos/mapas no futuro, `usuario?.nome`.

## 2. Operadores de Coleção (Vetores)
O suporte a `vector` já existe, mas pode ser expandido:
*   **Operador `in`:** Para verificar pertinência em vetores: `x in [1, 2, 3]`.
*   **Slicing:** `meuVetor[1..3]` para extrair sub-vetores.
*   **Acesso por Índice:** `meuVetor[0]` (atualmente a gramática focar em vetores como entidades completas ou destruturação em atribuições).

## 3. Melhorias em Strings
*   **Concatenação Explícita:** Se o `+` for ambíguo com números, um operador como `||` ou `concat()` dedicado.
*   **Interpolação/Template Strings:** Suporte a algo como `"Olá ${nome}"`.
*   **Operadores de Regex:** `texto ~= "/padrão/"` para validações rápidas.

## 4. Estruturas de Dados Complexas (Mapas/Objetos)
*   **Map Literals:** `{"chave": "valor"}`.
*   **Acesso via Ponto:** `pedido.item.preco`. Isso seria extremamente útil para integrar com modelos de domínio complexos sem precisar "achatar" os dados antes da avaliação.

## 5. Operadores Bitwise Numéricos
A gramática já possui `NAND`, `NOR`, `XOR` etc., mas eles parecem estar no fluxo de `logicalExpression`. Se houver necessidade de manipulação de bits em nível de inteiros (como em sistemas de permissões ou protocolos):
*   `<<`, `>>` (Shifts).
*   `&`, `|`, `^` (Bitwise AND/OR/XOR para números).

## 6. Funcionalidades de Data/Hora Avançadas
*   **Intervalos/Duração:** Suporte a literais de duração como `P1D` (ISO-8601) ou `10m`, `2h`.
*   **Aritmética de Datas:** `currDate + 5d` ou `currDate - 1month`.

## 7. Lambdas e Funções de Ordem Superior
Se o motor de execução permitir, a gramática poderia suportar funções anônimas simples para operações em vetores:
*   `filter([1, 2, 3], (x) => x > 1)`

---
**Observações:**
Estas propostas visam expandir o poder de expressão do módulo mantendo a compatibilidade e a performance, aproveitando as capacidades de análise já estabelecidas no `exp-eval-mk2`.
