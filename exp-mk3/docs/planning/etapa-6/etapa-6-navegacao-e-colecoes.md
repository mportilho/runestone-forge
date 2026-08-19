# Plano Detalhado - Etapa 6 - Navegacao, Subscritos, Filtros e Colecoes (M2)

Este plano detalha a Etapa 6 do `exp-mk3` depois do fechamento do M1. Ele consolida as decisoes registradas em `decisoes-etapa-6-navegacao-e-colecoes.md`, complementa o plano macro e usa o ADR 0016 e o ADR 0018 para a semantica de colecoes e de subscritos.

## Objetivo

Fechar o marco M2 declarando completa a linguagem de navegacao. A funcionalidade ja existe ponta a ponta; o que falta e contrato: diagnostico estavel para toda falha de navegacao, metadata semantica de pureza e nao nulidade, matriz de verificacao explicita e caracterizacao de desempenho registrada.

A Etapa 6 nao reimplementa navegacao, nao antecipa otimizacao e nao introduz forma sintatica nova.

## Autoridade e Premissas

- ADRs aceitos, `CONTEXT.md`, o Corpus de Expressoes e os planos detalhados vigentes definem o contrato.
- O ADR 0018 e produto desta etapa e passa a ser normativo para a familia de Subscrito.
- O plano historico e o codigo existente sao evidencias, nao autoridade quando contradizem contratos posteriores.
- A API publica continua provisoria e pode mudar de forma incompativel antes da GA.
- Toda a suite existente deve permanecer verde; testes que contradizem contratos normativos sao atualizados atomicamente com codigo e corpus.
- Nao ha folding, CSE, reescrita algebrica, especializacao de nos, pooling de escopo, cache de compilacao, Memoria de Calculo ou fusao de pipelines nesta etapa.

## Estado Atual e Estrategia de Reaproveitamento

A tabela e orientada por entregas, nao por classes. Uma classe existente pode ser mantida, dividida ou removida desde que o contrato da linha seja satisfeito.

| Entrega | Estado observado | Acao planejada |
|---|---|---|
| AST de navegacao com sete tipos de elo e lambda como argumento | Completa e selada | Preservar |
| Resolucao de cadeia, subscritos, filtro, curinga, propriedade, metodo e operacao | Completa, com dispatch exaustivo sobre nove vinculos | Preservar |
| Slot de Item Atual por profundidade e limite de profundidade | Completo no resolver e no escopo | Preservar |
| Planner de navegacao sem retencao | Completo, sem caminho de nao suportado | Preservar |
| Runtime de navegacao e das dez operacoes oficiais | Completo | Preservar |
| Falhas de navegacao em runtime | Excecoes cruas fora do seam, sem trecho de fonte | Substituir |
| Significado de `?.` | Cobre apenas receptor nulo, que nenhum dado ordinario produz; operador inerte | Substituir |
| Codigos de runtime de navegacao | Tres ausentes e tres existentes nunca emitidos | Implementar |
| Codigos semanticos de navegacao | Sobrecarregados em codigos genericos | Substituir |
| Pureza de metodo registrado | Ausente no vinculo; resultado sempre impuro | Implementar |
| Contrato de nao nulidade de membro registrado | Implicito no mapeamento de tipos | Registrar e aplicar |
| Diagnostico de chamada sobre item contextual | Vaza tipo interno na mensagem | Substituir |
| Matriz de Navegacao Segura no Corpus | Rasa | Implementar |
| Caso apoiado em excecao crua de Java no Corpus | Um caso remanescente | Substituir |
| Benchmarks de navegacao | Apenas operacoes de colecao e um curinga | Implementar |

## Gate de Entrada

O trabalho executavel comeca apenas depois de verificar os invariantes abaixo. Build verde isolado nao substitui o gate.

- Vinculo de Navegacao presente para todo elo, com tipo, nulidade e pureza, validado por invariante do Modelo Semantico.
- Layout de Frame com slot de Item Atual por profundidade de aninhamento realmente usada.
- `maxCurrentItemDepth` aplicado tanto em filtro quanto em lambda de operacao.
- Checagens Diferidas de limite de subscrito e de Limite de Materializacao produzidas pelo resolver.
- Nenhum caminho de runtime com fallback reflexivo por tipo desconhecido.

Qualquer violacao e bug interno. O runtime nao pode compensar metadata ausente nem redescobrir regra semantica.

## Contrato de Subscrito

Normativo pelo ADR 0018.

Estritez e propriedade do elo, nao da forma. Elo sem `?.` afirma que o que ele nomeia existe; elo com `?.` tolera a ausencia e devolve Valor Nulo de Runtime.

| Forma | Payload | Elo estrito | Elo seguro |
|---|---|---|---|
| `[i]` | inteiro literal, negativo conta do fim | falha fora de `[0, tamanho)` | null fora de `[0, tamanho)` |
| `[a:b]`, `[a:]`, `[:b]` | inteiros literais ou limite omitido | limites presos ao intervalo valido | identico ao estrito |
| `["k"]` | texto literal | falha quando a chave nao existe | null quando a chave nao existe |
| `[*]` | nenhum | colecao de elementos, valores de mapa ou filhos homogeneos registrados | identico, mais receptor nulo tolerado |
| `[?(...)]` | expressao booleana com Item Atual | colecao filtrada | identico, mais receptor nulo tolerado |

- Indice ou chave por expressao nao existe; `x[i]` e erro de parse e a gramatica permanece congelada.
- Fatia com fim normalizado abaixo do inicio produz colecao vazia, assim como inicio igual ou acima do tamanho.
- Fatia falha apenas por Limite de Materializacao, nas duas formas de elo.
- Indice fora de faixa com forma de colecao estaticamente conhecida e diagnostico de compilacao **apenas em elo estrito**; caso contrario e Checagem Diferida.
- Em elo seguro, indice estaticamente fora de faixa e resultado nulo legitimo, nao diagnostico.
- `atributos?.["ausente"] ?? 0` e o idioma para entrada opcional.

## Contrato de Navegacao Segura

- `?.` tolera receptor nulo e ausencia legitima do proprio elo, e nada alem disso.
- `?.` nao mascara receptor de tipo nao subscritavel, membro invalido, falha de acessor, erro de predicado, Limite de Materializacao nem falha de elo seguinte.
- `?.` nao propaga pela cadeia. `a?.b` produz nulidade possivel e o elo seguinte sem `?.` e rejeitado em compilacao.
- As formas corretas de continuar sao `a?.b?.c` e `(a?.b ?? d).c`.
- O diagnostico de nulidade escapando deve sugerir explicitamente essas duas formas.
- A leitura anterior, restrita a receptor nulo, tornava `?.` inerte: nenhum valor ordinario pode ser nulo na v2, de modo que nenhum caminho produzia nulo. A tolerancia de ausencia e o que da conteudo ao operador.

## Metadata de Membros

- Pureza de metodo registrado passa a ser declarada e propagada para o vinculo de metodo.
- Registro explicito de metodo ganha variante que declara pureza; o default permanece impuro.
- Registro em lote de metodos publicos registra tudo como impuro.
- Componente de record e getter bean permanecem presumidos puros.
- Navegacao por campo publico esta fora da v2; a descoberta continua sendo apenas por metodo.
- Membro registrado, valor de mapa, elemento de colecao e retorno de provider nunca sao nulos; violacao e falha de execucao com trecho de fonte.
- Entrada de Mapa e Item de Reducao nao possuem metodos; chamada sobre eles tem diagnostico proprio, que lista os membros validos sem citar nome interno de classe.

## Diagnosticos Semanticos

| Codigo | Cobre |
|---|---|
| `SEMANTIC_NAVIGATION_RECEIVER_NOT_SUPPORTED` | receptor incompativel com indice, fatia, chave, curinga, filtro ou operacao; propriedade sobre mapa; operacao incompativel com o receptor |
| `SEMANTIC_UNKNOWN_MEMBER` | propriedade, metodo, membro contextual e operacao de colecao inexistentes |
| `SEMANTIC_OPERATION_ARGUMENT_MISMATCH` | aridade, lambda contra valor, tipo de argumento e restricao de valor de argumento |

- Nome de operacao de colecao inexistente deixa de usar `SEMANTIC_UNKNOWN_FUNCTION`, que fica exclusivo de chamada global.
- Codigos ja existentes permanecem: `SEMANTIC_COLLECTION_INDEX_OUT_OF_BOUNDS`, `SEMANTIC_MATERIALIZATION_LIMIT_EXCEEDED`, `SEMANTIC_CURRENT_ITEM_OUT_OF_SCOPE`, `SEMANTIC_CURRENT_ITEM_DEPTH_EXCEEDED`, `SEMANTIC_LAMBDA_ARGUMENT_UNSUPPORTED`, `SEMANTIC_NULLABLE_RECEIVER_NOT_ALLOWED`, `SEMANTIC_NULLABLE_PREDICATE_NOT_ALLOWED`, `SEMANTIC_NULLABLE_ARGUMENT_NOT_ALLOWED` e `SEMANTIC_EMPTY_COLLECTION_REQUIRES_CONTEXT`.
- Situacoes nao cobertas pelos tres codigos novos permanecem em `SEMANTIC_OPERATOR_TYPE_MISMATCH`.
- Erros semanticos continuam acumulados em uma unica resolucao.

## Diagnosticos de Runtime

Codigos novos:

| Codigo | Origem |
|---|---|
| `RUNTIME_MAP_KEY_NOT_FOUND` | chave textual ausente no mapa, em elo estrito |
| `RUNTIME_MEMBER_ACCESS_FAILURE` | falha de acessor de propriedade, de invocacao de metodo ou de filho de curinga |
| `RUNTIME_INVALID_OPERATION_ARGUMENT` | valor de argumento invalido, como direcao de ordenacao fora de `asc` e `desc` |

Codigos existentes que passam a ser emitidos do interior do runtime:

| Codigo | Origem |
|---|---|
| `RUNTIME_SUBSCRIPT_OUT_OF_BOUNDS` | indice normalizado fora de `[0, tamanho)`, em elo estrito |
| `RUNTIME_MATERIALIZATION_LIMIT_EXCEEDED` | filtro, fatia, curinga, `keys`, `values`, `map` e `sortBy` acima do limite |
| `RUNTIME_UNDEFINED_OPERATION` | `avg` sobre colecao vazia dinamica |
| `RUNTIME_FORBIDDEN_NULL` | elemento, chave, valor de mapa ou retorno de membro nulo |

- Argumento de valor de operacao divide-se como os limites de subscrito: payload literal invalido, como `sortBy(@ -> @, "cima")`, e `SEMANTIC_OPERATION_ARGUMENT_MISMATCH` em compilacao; valor computado invalido e `RUNTIME_INVALID_OPERATION_ARGUMENT` em execucao.
- Toda falha passa pelo seam `RuntimeFailures`, com trecho de fonte do elo e causa preservada.
- Nenhuma excecao crua de navegacao permanece: nem `IndexOutOfBoundsException`, nem `NullPointerException` de receptor, nem `IllegalStateException` de acessor, de limite ou de operacao.
- Receptor nulo em elo estrito e barrado pelo resolver e permanece guarda de invariante interna, nao diagnostico publico; nao existe `RUNTIME_NULL_RECEIVER`.
- `RUNTIME_FORBIDDEN_NULL` fica restrito a elemento, chave, valor de mapa e retorno de membro violando contrato.
- Runtime para na primeira falha real de execucao.

## Matriz de Verificacao

A matriz e explicita e verificada caso a caso. Nenhuma Tag de Cobertura nova e criada; a estrutura de teste ja exige caso para toda tag existente.

Navegacao Segura, por forma de elo, em tres colunas: a ausencia que o elo seguro tolera, a mesma ausencia falhando no elo estrito, e o erro real que o elo seguro nao pode mascarar.

| Forma | Elo seguro devolve null | Elo estrito falha | Nao pode ser mascarado |
|---|---|---|---|
| `?.prop` | nao se aplica | nao se aplica | membro invalido, falha de acessor, retorno nulo violando contrato |
| `?.call()` | nao se aplica | nao se aplica | membro invalido, falha de invocacao, argumento invalido |
| `?.[i]` | indice fora de faixa | `RUNTIME_SUBSCRIPT_OUT_OF_BOUNDS` | receptor nao indexavel |
| `?.[a:b]` | nao se aplica | nao se aplica | limite de materializacao |
| `?.["k"]` | chave ausente | `RUNTIME_MAP_KEY_NOT_FOUND` | valor de mapa nulo |
| `?.[*]` | nao se aplica | nao se aplica | falha de acessor de filho, limite de materializacao |
| `?.[?(...)]` | nao se aplica | nao se aplica | erro de predicado, limite de materializacao |

As formas de propriedade, chamada, fatia, curinga e filtro nao tem nocao de ausencia legitima: elas toleram apenas receptor nulo, que a nulidade estrita ja impede de ocorrer por dado ordinario. Sua verificacao consiste em provar que o `?.` compila, se comporta identicamente a forma estrita quando o receptor existe, e nao mascara nenhum dos erros da ultima coluna.

Alem da matriz:

- Filtro e lambda aninhados ate `maxCurrentItemDepth`, com valor de Item Atual correto por nivel e restauracao apos cada nivel.
- Profundidade acima do limite rejeitada em compilacao.
- Operacoes de colecao exercitadas sobre `List`, array e `Map`.
- Cadeia mista de propriedade, metodo, subscrito, filtro e operacao sobre Tipo Java Registrado.
- Caso positivo e negativo por codigo semantico e por codigo de runtime introduzido nesta etapa, com assercao de codigo e de trecho.
- Contrato de nao propagacao de `?.` com caso proprio.
- Chamada sobre Entrada de Mapa e sobre Item de Reducao com diagnostico proprio.

## Corpus

- Corpus inteiro executa verde, sem exclusao por fase ou tag e sem teste desabilitado.
- O caso remanescente apoiado em excecao crua de Java migra para diagnostico de runtime tipado.
- A fase `semantic` continua sem uso; diagnostico semantico permanece em caso de fase de runtime invalido.
- Novos casos seguem o esquema vigente, sem campo novo e sem convencao concorrente.
- Renomeacao de codigo atualiza codigo, corpus e testes no mesmo incremento.

## Benchmarks

- Quatro benchmarks novos: filtro, subscrito e fatia, cadeia de propriedade e metodo sobre Tipo Java Registrado, e lambda aninhada em dois niveis.
- Protocolo identico ao da Etapa 5: tres forks, aquecimento e medicao equivalentes, profiler de alocacao, `ns/op` e `B/op`.
- Registro em `docs/perf/performance-history.md` com ambiente, comando, commit, expressoes e resultados.
- Caracterizacao sem limiar de aprovacao; gates continuam nas Etapas 7 a 9 e 12.
- Loops indexados permanecem a regra no caminho quente; sem streams, sem fusao, sem especializacao.

## Incrementos de Implementacao

1. **Metadata de membros.** Declaracao de pureza de metodo registrado, propagacao para o vinculo, contrato de nao nulidade registrado e aplicado, propriedades presumidas puras.
2. **Ausencia tolerada e falhas de runtime de navegacao.** Indice e chave em elo seguro passando a devolver null, restricao do diagnostico estatico de indice ao elo estrito, roteamento pelo seam, tres codigos novos, tres codigos hoje inertes passando a ser emitidos, trecho de fonte e causa preservada em todos.
3. **Diagnosticos semanticos.** Tres codigos novos, reclassificacao de operacao inexistente, atualizacao atomica do corpus afetado.
4. **Verificacao.** Matriz de Navegacao Segura, aninhamento ate o limite, receptores `List`, array e `Map`, diagnostico de item contextual, migracao do caso de excecao crua.
5. **Desempenho.** Quatro benchmarks e registro no historico.

Cada incremento fecha com `mvn -pl exp-mk3 -am test` verde. A decomposicao em issues ocorre depois do design registrado.

## Criterios de Aceite da Etapa 6

- Gate de entrada verificado antes do trabalho executavel.
- Nenhuma excecao crua de navegacao permanece no runtime.
- Toda falha de navegacao tem codigo estavel, trecho de fonte e causa preservada quando aplicavel.
- Os tres codigos semanticos novos distinguem receptor, membro e argumento, com caso positivo e negativo cada.
- Pureza de metodo registrado e declarada e propagada; propriedades permanecem puras.
- Contrato de nao nulidade de membros aplicado com diagnostico proprio.
- Subscrito respeita indice e chave estritos em elo estrito, ausencia tolerada em elo seguro e fatia presa nas duas formas, conforme o ADR 0018.
- `?.` tolera receptor nulo e ausencia legitima do proprio elo, nao mascara erro real e nao propaga pela cadeia.
- Matriz de Navegacao Segura completa por forma de elo, nas tres colunas.
- Aninhamento ate `maxCurrentItemDepth` com Item Atual correto por nivel.
- Operacoes de colecao verificadas sobre `List`, array e `Map`.
- Corpus inteiro verde sem exclusao e sem caso apoiado em excecao crua.
- Quatro benchmarks executados e registrados sem limiar artificial.
- Toda a suite existente permanece verde.

## Fora de Escopo

- Subscrito com indice ou chave por expressao e qualquer mudanca de gramatica.
- Navegacao por campo publico.
- API publica de operacoes de colecao customizadas.
- Otimizacao, especializacao de nos, cache, pooling, Memoria de Calculo e fusao de pipelines.
- `LambdaMetafactory` e `VarHandle` em acessores, que permanecem na Etapa 8.
- Fase `semantic` do Corpus e Tags de Cobertura novas.
- Limiar de desempenho e gate de CI.
- Publicacao de tickets durante o planejamento.

## Impacto nas Etapas Posteriores

- Etapa 7 usa a forma sem otimizacoes como oraculo e preserva indice estrito, chave estrita e fatia presa; passa a poder dobrar prefixo de navegacao puro porque a pureza esta declarada.
- Etapa 8 especializa acessores sobre metadata ja registrada, sem fallback reflexivo e sem alterar semantica de `?.`.
- Etapa 9 mantem os vinculos de navegacao dentro do plano unico compartilhado entre visoes.
- Etapa 10 marca propriedades e metodos registrados por identidade e trecho ja preservados. Operacoes de colecao e todos os seus descendentes sao fronteiras opacas; o plano unico recebe slots primitivos apenas nos nos marcaveis.
- Etapa 11 mantem os diagnosticos de migracao independentes desta etapa.
- Etapa 12 transforma a caracterizacao de M2 em gate e amplia stress de profundidade de Item Atual e de Limite de Materializacao.
