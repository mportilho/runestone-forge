# Decisoes de Planejamento da Etapa 5 - Runtime Minimo e API Publica

Este documento consolida as decisoes tomadas durante o replanejamento da Etapa 5 do `exp-mk3`, depois das alteracoes substanciais nas Etapas 0-4. Ele registra o estado final da arvore de decisoes e substitui premissas conflitantes do plano historico.

## Autoridade e Revalidacao

- ADRs aceitos, `CONTEXT.md`, Corpus de Expressoes e planos detalhados vigentes sao normativos.
- O plano historico e o codigo atual sao evidencias e substrato, nao autoridade quando contradizem contratos posteriores.
- O planejamento da Etapa 5 inclui uma analise de impacto nas Etapas 6-13, mas nao as replaneja detalhadamente.
- O plano-mestre deve mudar apenas onde premissas posteriores foram invalidadas.
- A API publica atual e provisoria e nao exige compatibilidade antes da GA.
- Nenhum ticket deve ser publicado durante esta sessao; decomposicao em issues ocorre depois do design registrado.

## Fronteira da Etapa 5

- M1 continua cobrindo runtime escalar, atribuicoes, desestruturacao, literais de colecao e funcoes globais.
- Navegacao, subscripts, filtros, lambdas e operacoes receptoras continuam no gate formal da Etapa 6.
- Codigo antecipado da Etapa 6 permanece quando compativel, mas nao amplia o criterio de aceite de M1.
- Toda a suite existente deve permanecer verde.
- Testes existentes que contradizem contratos normativos devem ser corrigidos atomicamente com codigo e corpus.
- A Etapa 5 nao implementa otimizacao, especializacao, cache, pooling, auditoria ou fusao.

## Gate da Etapa 4

- Pendencias normativas da Etapa 4 nao devem ser renomeadas como features da Etapa 5.
- Elas formam um gate bloqueante antes do `ExecutionPlanBuilder`.
- O gate inclui nulidade estrita, fatos numericos, checagens diferidas, bindings completos, layout canonico e arquivo apenas com atribuicoes.
- Build verde sem satisfazer os invariantes documentados nao fecha o gate.
- O planner nao pode compensar metadata ausente nem redescobrir regra semantica.
- Binding, tipo ou metadata obrigatoria ausente em modelo de sucesso e bug interno.

## Resultado de Compilacao

- `compile(source, environment)` retorna `ExpressionCompilationResult` fechado.
- Sucesso contem `CompiledExpression` e warnings.
- Falha contem ao menos um erro e tambem pode conter warnings.
- Erro de fonte e resultado esperado de compilacao, nao excecao obrigatoria.
- `compileOrThrow` existe como conveniencia.
- `compileOrThrow` lanca `ExpressionCompilationException` com os mesmos diagnosticos imutaveis da falha orientada a resultado.
- Warnings continuam acessiveis por `CompiledExpression.compilationDiagnostics()`.

## Visoes Publicas

- `CompiledExpression` nao expoe `compute()` diretamente.
- `asResult()` produz `ResultExpression`, para qualquer resultado publicamente exponivel.
- `asMath()` produz `MathExpression` apenas para resultado `NUMBER`.
- `asLogical()` produz `LogicalExpression` apenas para resultado `BOOLEAN`.
- `asAssignments()` produz `AssignmentsExpression` quando existe ao menos uma atribuicao e todos os simbolos expostos tem tipo publicamente exponivel.
- Todas as visoes compartilham o mesmo Plano Imutavel.
- Todas expoem `compute()` e `compute(Map<String, ?> overrides)`.
- `ResultExpression` retorna `Object` canonico.
- `MathExpression` retorna `BigDecimal`.
- `LogicalExpression` retorna `boolean`.
- `AssignmentsExpression` retorna `Map<String, Object>`.
- Selecao incompativel falha imediatamente com `ExpressionViewException` e razao fechada.
- Arquivo apenas com atribuicoes aceita somente a visao de atribuicoes entre as quatro visoes iniciais.

## Semantica da Visao de Atribuicoes

- `AssignmentsExpression` executa apenas atribuicoes.
- Uma expressao final opcional nao e avaliada por essa visao.
- Efeitos e falhas da expressao final nao ocorrem ao computar atribuicoes.
- O mapa nao inclui a expressao final.
- O mapa e imutavel.
- A ordem segue a primeira criacao de cada Simbolo Interno.
- Reatribuicoes atualizam o valor final sem mudar a posicao.
- A visao inteira e rejeitada se qualquer simbolo tiver tipo nao exponivel.
- Quantidade de simbolos acima do Limite de Materializacao pode rejeitar a visao antes da execucao.

## Diagnosticos Publicos

- Parsing, semantica, runtime e migracao compartilham `ExpressionDiagnostic`.
- O diagnostico contem categoria, severidade, codigo textual estavel, mensagem, trecho primario opcional, informacoes relacionadas, notas e sugestao opcional.
- Falhas originadas na fonte sempre tem trecho primario.
- Falhas puramente externas podem nao ter trecho primario.
- `ExpressionExecutionException` carrega exatamente um diagnostico.
- Runtime para na primeira falha porque execucao pode conter efeitos.
- Falhas de provider e biblioteca preservam a causa.
- `VirtualMachineError`, `ThreadDeath` e `LinkageError` propagam sem wrapping.
- `InterruptedException` restaura o status da thread antes de wrapping.
- Familias da Etapa 5: entrada invalida, dominio complexo, operacao indefinida, calculo/arredondamento, limite, provider, contrato de retorno, desestruturacao e null indevido.
- Codigos de navegacao/filtro ficam para a Etapa 6.

## Plano Imutavel

- O planner consome exclusivamente um `SemanticModel` de sucesso e completo.
- O plano nao retem parse tree, AST, modelo, fonte ou ambiente inteiro.
- O plano retem somente nos, valores preparados, handles, defaults, descritores minimos, contextos, zona e limites necessarios.
- Providers vinculados e handles podem ser retidos porque fazem parte da execucao daquele ambiente.
- `ExecutableNode` preserva `NodeId` e `SourceSpan`.
- Nos sao genericos por familia nesta etapa.
- Switch por operador dentro de uma familia e aceitavel.
- Closures opacas que capturem AST, modelo ou ambiente nao sao aceitas.
- Nos especializados ficam para a Etapa 8.

## Forma sem Otimizacoes

- Deve existir uma forma interna de gerar plano sem transformacoes de otimizacao.
- Ela usa a mesma pipeline, nos e runtime do plano normal.
- Na Etapa 5, nenhuma transformacao e aplicada.
- Nas Etapas 7-8, pular transformacoes produz o oraculo de equivalencia.
- Nao ha flag publica.
- Nao ha segundo runtime.
- Nao ha copia congelada do codigo historico da Etapa 5.
- A permanencia importante e a capacidade de executar sem transformacoes, nao a preservacao de cada classe original.

## Checagens Diferidas

- Checagens Diferidas sao tipadas e fechadas.
- Elas representam apenas pre-condicoes de valor de constructs ja tipados.
- Cada checagem preserva identidade, trecho e codigo de falha.
- O resolver produz e o planner consome; o runtime nao redescobre a regra.
- Violacao constante e diagnostico de compilacao.
- Violacao dependente de entrada e checagem runtime.
- Tipos, overloads, membros e navegacao nao podem ser diferidos.

## Frame e Entradas

- Cada execucao cria um unico `Object[] frame`.
- O frame nasce de copia de template imutavel com defaults.
- `UNBOUND` e distinto de `null`.
- Toda entrada e validada antes de atribuicao ou funcao com efeito.
- A menor chave nao declarada em ordem lexicografica falha primeiro.
- Depois, simbolos declarados sao validados na ordem canonica do plano.
- Override valida politica, null, coercao e containers recursivos.
- Coercao ocorre uma vez por valor.
- Simbolo declarado mas nao usado e aceito, validado e descartado.
- Simbolo nao usado nao recebe slot.
- O plano retem descritor minimo para validar declaracoes nao usadas.
- Simbolo fixo nao pode ser sobrescrito, usado ou nao pela expressao.

## Valores Temporais Correntes

- `Clock` e dependencia de runtime, nao configuracao do Ambiente de Expressao.
- O relogio nao integra a identidade do ambiente.
- `RuntimeServices` interno fica associado ao `CompiledExpression`, separado do plano.
- A API publica inicial usa o relogio padrao.
- Testes internos podem injetar relogio.
- Sem Valor Temporal Corrente usado, a execucao nao consulta o relogio.
- Com algum valor usado, `Clock.instant()` e chamado exatamente uma vez.
- O instante e truncado para segundos antes da conversao pela zona do ambiente.
- Apenas os valores temporais usados precisam ser derivados.

## Ordem e Politica de Avaliacao

- Toda construcao eager avalia filhos da esquerda para a direita.
- Isso vale para binarios, `nand`, `nor`, `xor`, `xnor`, argumentos e elementos de colecao.
- `and` e `or` usam curto-circuito comum.
- `??` e lazy da esquerda para a direita.
- Condicional avalia apenas o ramo escolhido.
- `between` avalia valor, limite inferior e somente quando necessario o limite superior.
- A forma negada de `between` inverte o resultado, nao a ordem.
- Todos os ramos sao semanticamente validados mesmo quando podem nao executar.

## Dominio Numerico

- `NUMBER` tem semantica decimal e dominio real.
- Operacao classificada como matematicamente definida com resultado real e semanticamente admissivel.
- Admissibilidade nao garante calculabilidade: limite, representacao ou falha de `big-math` na precisao pedida produz diagnostico de calculo/arredondamento ou limite.
- Resultado complexo e rejeitado.
- Operacao matematicamente indefinida e rejeitada por categoria distinta.
- Todo decimal finito e uma fracao exata canonica reduzida para classificacao de dominio e sinal, com denominador positivo.
- A classificacao nao pode construir denominador proporcional a `10^scale`.
- Para base negativa e expoente reduzido `p/q`, o resultado e real quando `q` e impar.
- O sinal dessa potencia e negativo quando `abs(p)` e impar.
- Para `degree root radicand`, grau `a/b` equivale ao expoente `b/a`.
- Radicando negativo produz resultado real quando `abs(a)` e impar; o resultado e negativo quando o denominador positivo `b` e impar e positivo quando `b` e par.
- Grau de raiz pode ser integral, fracionario, positivo ou negativo.
- Grau zero e indefinido.
- Toda base nao zero elevada a zero resulta em `1`; `0 ^ 0` tambem resulta em `1` por convencao.
- Zero elevado a expoente positivo resulta em zero.
- `0` elevado a expoente negativo e indefinido.
- Radicando zero com grau positivo resulta em zero; com grau negativo e indefinido.
- `big-math` calcula toda potencia e raiz em todos os tiers.
- Resultado negativo usa contexto de magnitude com `CEILING`/`FLOOR` trocados antes de negacao exata; outros modos permanecem iguais.
- Base negativa com expoente real sempre usa `BigDecimalMath.pow(abs(base), expoente, contextoDaMagnitude)`; numerador impar restaura sinal negativo e numerador par devolve magnitude positiva.
- Grau negativo usa `reciprocal(root(abs(radicando), abs(grau), contextoDaMagnitude), contextoDaMagnitude)`; o arredondamento intermediario integra o contrato.
- Para entrada negativa real, a magnitude e calculada sobre o valor absoluto e o sinal e restaurado exatamente pela camada da linguagem.
- `BigComplexMath` nao e usado.
- Excecoes de `big-math` viram diagnostico estavel e preservam a causa.
- Mensagem e tipo interno da biblioteca nao integram o contrato publico.

## MathContext, Escala e Resto

- `mathContext` se aplica a soma, subtracao, multiplicacao, divisao, potencia e raiz.
- `transcendentalMathContext` se aplica a funcoes transcendentais.
- Literais, negacao unaria, percentual, fatorial, comparacoes e `mod` sao exatos.
- `mod` usa `BigDecimal.remainder` sem contexto.
- O sinal de `mod` segue o dividendo.
- Contexto com precisao zero e rejeitado no builder do ambiente.
- `RoundingMode.UNNECESSARY` e rejeitado no builder do ambiente.
- As duas restricoes valem para os dois contextos.
- Resultado publico nao e normalizado por escala.
- Igualdade e ordenacao decimal usam valor numerico, nao `BigDecimal.equals`.

## Funcoes

- Runtime usa o `MethodHandle` escolhido no ambiente e no resolver.
- Nao ha discovery, reflexao, lookup textual ou overload runtime.
- Argumentos avaliam da esquerda para a direita.
- Provider pode ser puro ou impuro conforme descriptor.
- Retorno e normalizado e validado imediatamente na borda.
- Null, tipo incompativel e container invalido sao violacoes de contrato distintas.
- Arrays de varargs pre-alocados, `LambdaMetafactory` e outras especializacoes ficam para a Etapa 8.

## Materializacao Publica

- Resultado publico e convertido por tipo resolvido.
- `ObjectType` nao atravessa a borda.
- Containers contendo `ObjectType` tambem nao atravessam.
- Colecoes e mapas viram snapshots recursivos e imutaveis.
- Nesta etapa, a borda cria novo snapshot mesmo se o valor interno ja for imutavel.
- Null nao e permitido em chave, valor ou elemento.
- Mapas usam chaves textuais em ordem canonica.
- Cada snapshot e verificado independentemente contra `maxMaterializedSize`.
- O mapa de atribuicoes tambem e uma materializacao.
- Compartilhamento de estruturas internas so pode ser avaliado depois de perfil.

## Concorrencia

- Plano, `CompiledExpression` e visoes sao compartilhaves entre threads.
- Cada chamada tem frame e resultado isolados.
- O plano nao tem estado mutavel de execucao.
- Nao ha pool de escopo na Etapa 5.
- Teste concorrente basico integra M1.
- Stress sob contencao continua na Etapa 12.

## Corpus e Testes

- Corpus cobre comportamento de linguagem do M1, nao detalhes de API ou slots.
- Visoes e resultado de compilacao ficam em testes de API.
- Layout, frame e planner ficam em testes internos.
- Dominio real usa casos explicitos e testes de propriedade.
- A matriz cobre adapters de potencia com base negativa para numeradores reduzidos impares/pares e prova que `BigDecimalMath.pow` nao recebe base negativa nesses caminhos reais fracionarios.
- O oraculo das propriedades nao pode ser a funcao sob teste.
- Funcoes impuras com contador/ordem provam politicas eager e lazy.
- Relogio fixo prova instante unico e truncamento em segundos.
- Ausencia atual de corpus real v1 nao bloqueia M1.
- Todos os casos reais disponiveis devem passar.
- Nao adicionar campo de milestone ao YAML nesta etapa.
- Tags e fases existentes identificam o subconjunto M1.

## Desempenho

- Baseline mede aritmetica hot, logica hot, compilacao sem cache e materializacao publica.
- Compilacao sem cache usa JVM/parser aquecidos, mas executa toda a pipeline por operacao.
- Startup continua em benchmark `SingleShot` separado.
- Medir `ns/op` e `B/op` quando aplicavel.
- Usar tres forks e profiler `gc` em protocolo reproduzivel.
- Registrar ambiente, comando, commit, expressoes, inputs e resultados no historico.
- JSONs permanecem durante a analise, mas nao sao versionados automaticamente.
- A Etapa 5 nao define limiar de aprovacao.
- Etapas 7-9 definem comparacoes depois de observar variabilidade.
- Excecao futura a gate exige justificativa documentada.

## Documentacao e ADR

- A Etapa 5 possui plano detalhado e registro de decisoes proprios.
- O plano-mestre deve incorporar somente premissas invalidadas das etapas posteriores.
- `CONTEXT.md` deve registrar Resultado de Compilacao, Visao de Expressao, Dominio Numerico Real e Materializacao Publica.
- O conceito de Modelo Semantico nao deve mencionar decisoes de tipo diferidas para runtime.
- ADR 0017 complementa, sem alterar o historico do ADR 0013.
- O ADR 0017 registra dominio real, fracao decimal exata, `big-math`, arredondamento e operacoes indefinidas.

## Impacto Posterior

- Etapa 6 deve produzir gap plan sobre o que ja existe e executar bindings sem redescoberta.
- Etapa 7 usa forma sem otimizacoes como oraculo e nao aplica reescrita que mude escala, contexto, falha ou efeitos.
- Etapa 8 preserva `big-math` e o dominio real em especializacoes.
- Etapa 9 compartilha um plano entre visoes e mantem runtime services fora da identidade.
- Etapa 10 usa identidade e trecho dos nos para instrumentacao separada.
- Etapa 11 continua responsavel pelo corpus real diferencial.
- Etapa 12 transforma baselines em gates e amplia testes de stress.
- Etapa 13 preserva fallback/oraculo sem otimizacoes.

## Decisoes Ainda Pendentes

- Nenhuma pendencia aberta no momento.
