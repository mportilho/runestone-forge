# Plano Detalhado - Etapa 3.5 e Etapa 4 - Resolver Semantico

Este plano detalha o saneamento necessario antes do resolver semantico e a implementacao da Etapa 4 do `exp-mk3`. Ele consolida as decisoes registradas em `exp-mk3/docs/decisoes-etapa-4-resolver-semantico.md` e nos ADRs 0007-0014.

## Objetivo

Implementar um `SemanticResolver` interno que transforma uma `Arvore Semantica de Expressao` e um `Ambiente de Expressao` em um `Modelo Semantico` planejavel apenas quando a expressao inteira estiver semanticamente valida.

O resolver deve decidir tipos, simbolos, funcoes, navegacao, operacoes de colecao, nulidade, fatos numericos, valores preparados, checagens diferidas e layout de frame. Ele nao deve executar runtime, folding, CSE, reordenacao, elisao de `as*` ou qualquer otimizacao de plano.

## Premissas Consolidadas

- A linguagem fonte nao tem literal `null`.
- Literais inteiros sao apenas decimais.
- O `Ambiente de Expressao` nao expoe `strictMode`.
- A v2 nao expoe `NumericMode` nem modo `FAST`; a semantica numerica e decimal.
- O modelo aceito exige tipos conhecidos.
- `UnknownType`, `NullType` e placeholders de inferencia nao aparecem em sucesso.
- Todo `Simbolo Externo` exige default nao nulo e politica de sobrescrita.
- Override runtime null nao e permitido.
- `MAY_BE_NULL` e introduzido pela linguagem por navegacao segura e deve ser descarregado com `??` antes de resultado, atribuicao, operador, funcao, predicado ou navegacao nao segura.
- `ObjectType` e valor intermediario para navegacao e funcoes explicitas; nao e exposto por resultado publico, assignments publicos ou containers em qualquer profundidade.

## Etapa 3.5 - Saneamento Antes do Resolver

### Objetivo

Remover conceitos obsoletos do contrato publico e do caminho interno planejavel antes de implementar o resolver.

### Entregas

- Remover `strictMode` de `ExpressionEnvironment`, builder, configuracoes representativas e testes.
- Remover `NumericMode` e todas as referencias a `FAST` do contrato publico e dos testes atuais.
- Remover `UnknownType` e `NullType` do sistema de tipos planejavel.
- Remover literal fonte `null` da gramatica, AST, pretty-printer, corpus e testes.
- Remover inteiros hexadecimais/octais da gramatica, AST, pretty-printer, corpus e testes.
- Remover declaracoes de `Simbolo Externo` sem default ou sem politica de sobrescrita.
- Validar defaults externos: sem default null, sem containers/mapas com null validavel, sem mapa com chave null.
- Rejeitar defaults externos heterogeneos de mapa/colecao sem tipo declarado.
- Renomear `maxVectorSize` para `maxMaterializedSize` como limite geral de materializacao.
- Adicionar `maxFactorialInput` como guard-rail de ambiente.
- Substituir `ExpressionEnvironmentId` e sua canonicalizacao de conteudo por um UUID textual opaco, gerado internamente para cada ambiente construido e estavel apenas durante a reutilizacao dessa instancia.

### Catalogos

- `FunctionCatalog` permanece publico/extensivel.
- `ReflectedFunctionImporter` deve rejeitar tipo Java sem mapeamento conhecido.
- Funcoes dobraveis devem ser puras.
- Overload duplicado apenas por retorno deve ser rejeitado no builder/importador.
- Metadados de funcao preservam classe, metodo e descriptor JVM para diagnostico/auditoria, sem identidade estavel exclusiva para cache.
- Provider de instancia e vinculado diretamente ao descriptor e nao exige `providerId`.
- `JavaTypeCatalog` deve rejeitar membro exposto sem tipo de retorno mapeavel.
- Propriedades/metodos Java registrados sao tratados como `NEVER_NULL` por contrato de exposicao.
- `CollectionOperationCatalog` deve existir com seam interno para extensao futura, mas sem API publica de registro custom na v2 inicial.
- Operacoes oficiais minimas: `map`, `sum`, `count`, `keys`, `values`, `any`, `all`.

### Criterios de Aceite

- `mvn -pl exp-mk3 -am test` verde.
- A gramatica nao aceita `null`, `0x10` ou `077` como expressoes validas.
- `ExpressionEnvironment` nao expoe `strictMode` nem `NumericMode`.
- `ExternalSymbolCatalog` exige default nao nulo e politica de sobrescrita.
- `FunctionCatalog`, `JavaTypeCatalog` e `CollectionOperationCatalog` validam contratos invalidos no builder/importador.
- Corpus e testes estao alinhados aos ADRs 0007-0014.

## Etapa 4 - Resolver Semantico

### API Interna

Entrada principal esperada:

```java
SemanticResolutionResult resolve(ExpressionFileNode ast, ExpressionEnvironment environment)
```

Resultado fechado:

- `SemanticResolutionSuccess(SemanticModel model, List<ExpressionDiagnostic> warnings)`.
- `SemanticResolutionFailure(List<ExpressionDiagnostic> diagnostics)`.

`Success` nunca contem `ERROR`. `Failure` contem pelo menos um `ERROR` e nunca contem `SemanticModel`.

### Fases Internas Recomendadas

1. Validar arquivo e preparar escopos iniciais do ambiente.
2. Resolver atribuicoes sequenciais, RHS antes de introduzir target.
3. Resolver expressoes com restricoes contextuais locais.
4. Resolver funcoes globais e contratos especiais de `as*`.
5. Resolver navegacao, filtros, lambdas e operacoes de colecao.
6. Finalizar layout de frame, nulidade, fatos numericos, formas conhecidas, valores preparados e checagens diferidas.
7. Validar invariantes do `SemanticModel` antes de retornar sucesso.

As fases internas nao sao API publica.

### SemanticModel

Campos esperados:

- AST imutavel original.
- `resolvedTypes` por `NodeId`.
- `runtimeNullability` por `NodeId`/binding valorado.
- `numericFacts` por no numerico relevante.
- `collectionShapes` por no/container relevante.
- `symbolBindings` por referencia/target.
- `functionBindings` por chamada.
- `navigationBindings` por link de navegacao.
- `collectionOperationBindings` quando separado dos bindings de navegacao.
- valores semanticos preparados, como `Pattern` e temporais normalizados.
- checagens diferidas.
- `FrameLayout`.
- diagnostics de warning.

Invariantes de sucesso:

- Todo `ExpressionNode` valorado tem tipo e nulidade resolvidos.
- Todo link de navegacao valorado tem tipo resultante ou binding resolvido.
- Nenhum `Tipo Invalido`, placeholder de inferencia ou tipo desconhecido aparece no modelo.
- Bindings exigidos por nos aceitos existem.
- Tipos de resultado/assignments publicos sao publicamente exponiveis.

## Regras Semanticas Principais

### Simbolos e Atribuicoes

- Nao ha simbolos fonte implicitos.
- Identificador desconhecido e erro semantico.
- Resolucao de atribuicoes e sequencial.
- RHS e resolvido antes de LHS introduzir ou atualizar simbolo.
- Forward reference para simbolo interno posterior nao existe.
- Sombreamento de externo por interno e permitido com warning.
- Reatribuicao interna exige tipo estavel/unificavel sem coercao de borda implicita.
- RHS de atribuicao interna e desestruturacao exige `NEVER_NULL`.
- Programa com atribuicoes e sem result expression e valido.
- Arquivo sem atribuicoes e sem result expression e erro semantico.

### Desestruturacao

- Apenas plana.
- Nomes duplicados no target sao erro; diagnostico aponta para a segunda ocorrencia e relaciona a primeira.
- RHS `Vector<T>` com shape conhecido valida aridade em compilacao.
- RHS `Vector<T>` com shape desconhecido registra checagem diferida de aridade exata.
- `Collection<T>`, `Map` e `ObjectType` nao sao desestruturaveis.

### Nulidade

- `RuntimeNullability.NEVER_NULL`: valor provado como nao nulo quando executa com sucesso.
- `RuntimeNullability.MAY_BE_NULL`: valor pode ser nulo por uso explicito de `?.`.
- Qualquer link `?.` produz `MAY_BE_NULL`, mesmo se o receiver for `NEVER_NULL`.
- `??` aceita operandos unificaveis e e lazy left-to-right.
- Resultado de `??` e `NEVER_NULL` se algum operando e `NEVER_NULL`; caso contrario e `MAY_BE_NULL`.
- `MAY_BE_NULL` escapando para resultado, atribuicao, operador, funcao, predicado ou navegacao nao segura e erro semantico.
- Diagnosticos de nulidade devem sugerir fallback explicito com `??` quando aplicavel.

### Numeros

- Tipo publico numerico e `NUMBER`.
- Semantica numerica e decimal.
- `Fato Numerico` substitui categoria operacional: `INTEGRAL_KNOWN`, `FRACTIONAL_KNOWN`, `UNKNOWN_NUMERIC_VALUE_SHAPE`.
- Fatorial exige `NUMBER`, `NEVER_NULL`, integral nao negativo e `<= maxFactorialInput` quando estatico; caso dinamico gera checagem diferida.
- `root` exige `NUMBER`, `NEVER_NULL` e grau integral positivo quando estatico; caso dinamico gera checagem diferida.
- `%` pos-fixado nao e reescrito na Etapa 4.

### Operadores

- Operadores comuns exigem operandos `NEVER_NULL`.
- Igualdade nao e permitida para `ObjectType`.
- `ObjectType` nao participa diretamente de operadores comuns.
- Ordenacao aceita apenas familias homogeneas ordenaveis: `NUMBER`, `STRING`, `DATE`, `TIME`, `DATETIME`.
- `DATE`, `TIME` e `DATETIME` nao sao comparaveis entre si.
- `in` em mapa testa existencia de chave textual e exige lado esquerdo `STRING`.
- `in`/`not in` de `ObjectType` em colecao/vetor nao e permitido.

### Funcoes

- Chamada global resolve por `FunctionCatalog`.
- Overload e deterministico em compilacao.
- Tipo de retorno nao participa da identidade da assinatura.
- Overload comum nao usa coercao de borda.
- Funcoes nao aceitam argumento `MAY_BE_NULL`.
- `as*` refinam apenas o resultado da chamada, nunca o simbolo original.
- `asVector(x)` generico nao existe; assercoes vetoriais devem declarar elemento conhecido.
- Funcoes impuras sao semanticamente validas, mas bloqueiam folding/CSE/reordenacao posterior.

### Navegacao

- A Etapa 4 resolve todos os links de navegacao; a Etapa 6 apenas executa bindings.
- Propriedade/metodo em `ObjectType` exige membro registrado.
- Mapa nao aceita propriedade; objeto nao aceita subscript textual.
- Subscript textual e valido apenas em `Map<V>`.
- `Map<V>["key"]` retorna `V NEVER_NULL` quando executa com sucesso; chave ausente ou valor null e erro runtime do link.
- Link nao seguro exige receiver `NEVER_NULL`.
- `?.` protege apenas receiver null daquele link e nao mascara membro invalido, indice invalido, chave ausente ou erro de predicado.
- Nao ha projecao implicita sobre `Vector<T>` ou `Collection<T>`.

### Vetores, Colecoes e Mapas

- `Vector<T>` e ordenado, indexavel e fatiavel.
- `Collection<T>` e iteravel, filtravel, mapeavel, quantificavel e agregavel, mas nao indexavel/fatiavel.
- `STRING` nao e indexavel/fatiavel na Etapa 4.
- Index/slice exigem indice/bounds `NUMBER NEVER_NULL` e integralidade conhecida ou checagem diferida.
- Slice usa intervalo half-open `[start:end)`, bounds negativos a partir do fim, e nao faz clamp silencioso.
- `[*]` e valido apenas em `Vector<T>` e retorna `Collection<T>`.
- `.*` em mapa retorna `Collection<V>`.
- Filtro `[?(...)]` e selecao iteravel e vale para vetor, colecao e mapa.
- `map` preserva vetor, preserva colecao e transforma mapa em colecao.
- `any/all` sao lazy por elemento.
- `sum` de vetor/colecao vazia retorna zero decimal.
- `count` retorna `NUMBER NEVER_NULL` com `Fato Numerico.INTEGRAL_KNOWN`.
- Operacoes que materializam carregam metadata de `Limite de Materializacao`.

### ObjectType e Borda Publica

- `ObjectType` pode ser intermediario para navegacao, atribuicao interna e funcao explicita.
- Resultado final `ObjectType` e erro semantico.
- Containers contendo `ObjectType` em qualquer profundidade nao sao publicamente exponiveis.
- Resultado final `Vector`, `Collection` ou `Map` e valido se `NEVER_NULL` e publicamente exponivel.
- `Collection<T>` publicamente exponivel deve ser materializada na borda; nao expor `Iterable` Java cru.
- `Map<V>` publicamente exponivel deve ser materializado como mapa imutavel com chaves `String`.

### Temporais

- Literais `DATETIME` com offset explicito sao convertidos para o `ZoneId` do ambiente.
- Literais `DATETIME` sem offset usam `ZoneRules` do ambiente.
- Gaps/overlaps de DST usam comportamento padrao de `LocalDateTime.atZone(zone)`.
- Comparacao de `DATETIME` usa o valor local normalizado no ambiente.
- `currDate`, `currTime` e `currDateTime` sao `NEVER_NULL`, dinamicos e nao dobraveis.

### Regex

- Lado esquerdo exige `STRING NEVER_NULL`.
- Lado direito deve ser literal string direto.
- Regex dinamica e concatenacao dobravel de string nao sao aceitas na Etapa 4.
- O resolver valida e pre-compila `Pattern` como `Valor Semantico Preparado`.

## Layout de Frame

- Externos usados entram em ordem de primeira referencia.
- Internos entram em ordem da primeira atribuicao que cria cada simbolo.
- Desestruturacao cria slots por identificador folha, em ordem textual.
- Slots de `Item Atual` sao reservados por profundidade simultanea maxima usada.
- Slots sinteticos ficam para Etapa 7, depois dos simbolos declarados.
- A ordem deve ser canonica e independente de `HashMap`.

## Diagnosticos

- Todo erro semantico tem `SourceSpan` primario.
- Warnings nao bloqueiam sucesso.
- Diagnosticos sao ordenados por offset, severidade, categoria/codigo.
- Cascatas devem ser suprimidas por `Tipo Invalido` interno.
- Erros independentes devem ser acumulados.
- Familia de diagnosticos de nulidade deve cobrir resultado, atribuicao, operando, argumento, receiver e predicado.

## Testes

Suites por eixo:

- simbolos externos/internos;
- atribuicoes e desestruturacao;
- nulidade e `??`/`?.`;
- operadores numericos, booleanos, igualdade, ordenacao e pertencimento;
- funcoes e overload;
- navegacao;
- filtros, lambdas e `@`;
- operacoes de colecao;
- temporais;
- regex;
- frame layout;
- diagnosticos;
- corpus gate.

Cada regra negativa relevante deve testar codigo diagnostico e span.

## Criterios de Aceite da Etapa 4

- `SemanticResolver.resolve(ast, environment)` retorna sucesso apenas sem erros.
- `SemanticModel` de sucesso e planejavel e passa invariantes.
- `@` fora de contexto e erro.
- Regex invalida ou nao literal falha na compilacao.
- Arquivo vazio falha semanticamente.
- Corpus inteiro resolve sem issues inesperadas e inclui casos `phase: semantic`.
- `Layout de Frame` e estavel.
- Nenhuma otimizacao de plano e implementada nesta etapa.
