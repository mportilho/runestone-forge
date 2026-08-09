# Decisoes de Planejamento da Etapa 8 - Especializacao de Nos e Invocacao Sem Reflexao

Este documento consolida as decisoes tomadas durante o planejamento da Etapa 8 do `exp-mk3`, depois do fechamento da Etapa 7. Ele registra o estado final da arvore de decisoes e substitui premissas conflitantes do plano historico.

## Autoridade e Revalidacao

- ADRs aceitos, `CONTEXT.md`, Corpus de Expressoes e planos detalhados vigentes sao normativos.
- O plano historico e o codigo atual sao evidencias e substrato, nao autoridade quando contradizem contratos posteriores.
- O ADR 0019 continua normativo para toda transformacao de plano; o ADR 0020 e produto desta etapa.
- A API publica continua provisoria e nao exige compatibilidade antes da GA.
- Nenhum ticket deve ser publicado durante esta sessao; a decomposicao em issues ocorre depois do design registrado e com confirmacao propria.

## Enquadramento

- A Etapa 8 ataca custo por execucao, nao custo de compilacao.
- O perfil de carga que orienta prioridades e calculo financeiro: poucas expressoes, compiladas uma vez, executadas uma vez por parcela, lendo varios Simbolos Externos, escolhendo taxa por condicional e chamando funcoes.
- A ordem de grandeza da carga foi verificada e registrada: a centenas de execucoes por segundo, os numeros atuais consomem fracao desprezivel de um nucleo. A etapa nao compra vazao que falte; ela compra latencia por chamada, pressao de alocacao e margem futura. A decisao de prioriza-la assim mesmo foi tomada com esse dado na mesa.

## Especializacao e Aditiva, Nao Substitutiva

- O plano-mestre dizia "substituicao dos nos genericos por familias especializadas". Substituir apagaria o substrato do Oraculo Sem Otimizacoes, que o ADR 0019 exige manter funcionando, exercitado e mantido enquanto existirem otimizacoes.
- Decisao: especializacao e **aditiva**. Cada familia generica permanece como o no que `buildOracle` constroi.
- O precedente ja existe no codigo e nao e invencao desta etapa: a Etapa 7 especializou `in` com lado direito constante em duas representacoes e manteve `MembershipExecutableNode` generico.
- O campo booleano `folding` e renomeado para `optimizing` porque passa a controlar duas coisas distintas, dobra e especializacao, e o Oraculo continua sem nenhuma das duas. Um enum de modo continua sendo cerimonia para dois valores.

## Contrato de `ExecutableNode`

- A interface pode ganhar pontos de entrada tipados como metodos `default` delegando ao `execute(ExecutionScope)` generico, sobrescritos apenas por nos especializados.
- Decisao pelo `default` em vez de interfaces separadas: mantem uma unica interface, o Oraculo herda o comportamento sem escrever uma linha, e nao existe familia dupla para manter em paridade.
- Nenhum ponto de entrada tipado entra sem benchmark pareado que o justifique, e o alargamento chega junto com a primeira familia que o justificar. Alargar a interface antecipadamente seria decidir por antecipacao exatamente o que a etapa se propos a decidir por medicao.
- Fato que reenquadra a expectativa da secao 12 da estrategia: `BigDecimal` e tipo de referencia, entao nao existe ganho de unboxing para `NUMBER`. O que existe e evitar `BigDecimal` intermediarios e obter retorno primitivo genuino em predicados e cadeias booleanas.
- `AssignmentExecutable` fica fora: tem a mesma forma de `ExecutableNode` mas nao implementa a interface e retorna `void`. Uma atribuicao e uma escrita de slot; o custo esta no lado direito, que ja e `ExecutableNode`.

## Forma do Trabalho

- Piloto medido, nao matriz exaustiva. Especializar aumenta o numero de classes implementadoras em cada call-site pai, e o precedente da issue 85 mostrou indireccao nova custando de um a cinco por cento por chamada. Call-site monomorfico nao e consequencia automatica de especializar.
- Primeiro piloto: Invocacao Sem Reflexao mais Elisao de Coercao de Borda, medidas juntas. Motivo: hoje toda chamada aloca um `Object[]` novo, entra por `invokeWithArguments`, e passa cada argumento e o retorno por uma chamada virtual de conversao de borda. E o mecanismo mais caro por chamada encontrado no inventario, e o unico sem benchmark que o isole hoje.
- Segundo piloto: comparacao e igualdade sem duplo despacho. O braco do `switch` chama `structuralEquals` ou `compareValues`, que re-despacham por tipo em runtime com o tipo do operando ja resolvido na compilacao.
- Aritmetica decimal binaria por ultimo, com expectativa baixa registrada de proposito: a medicao da issue #121 mostrou que `BigDecimal.add` custa mais que uma leitura de frame mais um branch.
- Regra de parada: piloto sem ganho nao encerra a etapa, porque invocacao e despacho de comparacao sao mecanismos independentes. Dois pilotos independentes sem ganho encerram, por decisao registrada.

## Invocacao Sem Reflexao

- O ponto de entrada e gerado no build do Ambiente de Expressao, uma vez por descritor de funcao e por membro registrado, e compartilhado por todos os planos daquele ambiente.
- Isso so e valido porque o ambiente e longevo, construido uma vez e reutilizado. A pre-condicao passa a ser explicita e e o unico conteudo do ADR 0020.
- Aridades um a quatro por `LambdaMetafactory`; as demais por `invokeExact` com handle pre-adaptado. A rota alternativa, `invokeExact` em todas as aridades sem geracao de classe, fica registrada como rejeitada e continua sendo a rota correta caso a pre-condicao de ambiente longevo deixe de valer.
- Nao ha reflexao nova em runtime; ja hoje `java.lang.reflect` esta confinado ao build de catalogo. O que a etapa remove e a coleta de argumentos em array e a entrada lenta do `MethodHandle`.
- Dois itens da lista de entregas do plano-mestre ficaram sem alvo e saem: pre-alocacao de array de varargs por call-site, porque varargs sao rejeitados na importacao e descartados no catalogo de tipos Java; e `VarHandle` para acessores, porque navegacao por campo publico esta fora da v2 e todo acessor e metodo.

## Elisao de Coercao de Borda

- Entra no escopo da etapa, junto com o piloto, porque a carga alvo chama funcao em todo calculo.
- E o mesmo raciocinio da Elisao de Assercao provada na Etapa 7, um nivel abaixo: quando o tipo resolvido do argumento e exatamente o tipo canonico do parametro, a conversao e identidade provada.
- Qualquer outro caso mantem o filtro. Validacao de nao nulidade de resultado de provedor nao e conversao e nao pode ser elidida junto.
- Item associado que substitui o cache de constantes pequenas: os adaptadores numericos de retorno alocam um `BigDecimal` por chamada. Onde o retorno ja e `BigDecimal`, essa alocacao sai com o filtro.

## Metadata Ate Aqui Nao Consumida

- `numericFacts`, `runtimeNullability` e `collectionShapes` existem no Modelo Semantico, sao validados, e nunca foram lidos pelo construtor de plano. Especializacao por tipo e nulidade depende deles.
- Decisao: consumir, sob gate de entrada no formato da Etapa 7, verificado antes de qualquer codigo. Tipo resolvido presente para todo operando e nao apenas para resultados, fato numerico para todo no `NUMBER`, nulidade para todo no.
- Achado associado que entra no gate: o bit `safe` de subscrito de mapa, membro contextual, propriedade e metodo registrados tem **duas origens** — o flag da AST, usado por `CommonSubexpressionAnalyzer` como parte da chave de memo, e `binding.resultNullability()`, usado pelo construtor. Hoje as duas coincidem por construcao, mas a etapa mexe exatamente nos dois lados: o construtor passa a consumir nulidade e os nos de membro registrado sao reconstruidos. Decisao: unificar a origem antes de tocar os acessores, e nao depois de a chave de memo divergir.
- `PowerRealDomainDeferredCheck` e emitida pelo resolver e nao tem consumidor; `RealDomainArithmetic` reclassifica o dominio a cada execucao. Decisao: **consumir**, como trabalho do gate. Onde o resolver provou o dominio e nao emitiu checagem, o runtime pula a classificacao; onde emitiu, a checagem dirige a validacao. Remove-la seria a alternativa honesta se ela fosse vazia, mas nesse caso o gate da Etapa 7 estaria mentindo ha duas etapas.

## Simetria de `MathContext`

- Encontradas duas assimetrias: `sum` acumula com `add` sem `MathContext` enquanto `avg` divide com ele, e `modulo` usa `remainder` sem `MathContext`. Num modulo cujo contrato diz que escala e arredondamento sao observaveis, isso e intencao nao registrada ou defeito.
- Decisao: tornar simetrico, aplicando o `MathContext` do ambiente onde a operacao produz valor novo por aritmetica.
- Excecao decidida: **nao** aplicar a `negate`. Trocar o sinal e exato e sem perda, e arredondar ali truncaria silenciosamente um literal que a linguagem aceitou como exato.
- Sequenciamento: **issue preparatoria antes** da Etapa 8, com registro de decisao proprio. Isso muda valores, e a Etapa 8 e uma etapa cuja regra e nao mudar valor; especializar sobre a semantica final evita refazer prova.
- Condicao: benchmark pareado do laco de `sum` com e sem `MathContext`. Se custar acima da banda, a assimetria fica e vira decisao registrada.

## Potencia Sai da Etapa

- Verificado em fonte e bytecode de `big-math` 2.3.2: `BigDecimalMath.pow(BigDecimal, BigDecimal, MathContext)` ja tenta `longValueExact` no expoente e despacha para exponenciacao binaria, sem `log` nem `exp`, para expoente inteiro. Nao ha ganho a colher especializando `x ^ 12`.
- O ADR 0017 exige o caminho obrigatorio da biblioteca numerica em todos os tiers, entao tambem nao haveria autorizacao para colher.
- Consequencia honesta para a carga alvo: se a formula real contem potencia, ela domina o custo por parcela e a Etapa 8 nao a torna mais rapida. Esse e o preco do contrato decimal, nao um defeito dele. A etapa melhora o que esta em volta.

## Cache de Constantes Pequenas Sai da Etapa

- Um `BigDecimal` cacheado tem escala fixa; substituir uma constante de escala dois por uma de escala zero mudaria um observavel que a definicao de equivalencia protege explicitamente.
- Alem disso o item ja esta quase satisfeito: literais viram valor preparado em `ConstantExecutableNode` e `stripTrailingZeros` nao existe em `src/main`; `setScale` aparece apenas dentro de built-ins, fora do caminho quente.
- Substituido pelo item de alocacao por chamada nos adaptadores numericos de retorno.

## Reordenacao de Curto-Circuito Sai da Etapa

- Ela chegou da Etapa 7 como candidata condicionada a perfil, mas o ADR 0019 e contrato posterior e ja a lista entre as otimizacoes que ficam de fora, por ordem observavel e efeitos.
- O motivo registrado e o contrato, nao a ausencia de perfil. Um perfil favoravel nao a reabriria sem emenda ao ADR.

## Pool de Escopo

- Permanece como ultimo incremento, condicionado a medicao, com condicao numerica declarada de antemao e nao com "se o perfil justificar".
- Condicao: medido na expressao canonica, pareado, o pool so fica se reduzir `B/op` em pelo menos vinte por cento e nao piorar `ns/op` fora da banda de erro.
- Armadilha registrada: os slots de memo da issue #121 sao apensos ao frame e semeados com `UNBOUND`, entao um frame reaproveitado exige limpeza por chamada, e esse preenchimento pode consumir o ganho.
- Risco de despacho registrado: `ExecutionScope` nao e `final` e ja e subclasseada por `ConstantFoldSentinelScope`, entao todo `scope.read` ja e chamada virtual. Uma segunda forma concreta torna o call-site megamorfico, e o pool pode perder no despacho enquanto ganha na alocacao. A medicao precisa ser lida sabendo disso.

## Verificacao e Desempenho

- O criterio de equivalencia e herdado do ADR 0019; a Etapa 8 nao cria o seu.
- A expressao canonica da etapa entra no Corpus de Expressoes como Caso de Expressao, nao apenas como fixture de benchmark, senao a suite de equivalencia nunca executa a forma em torno da qual a etapa foi desenhada.
- A expressao canonica **nao** e gate de item isolado: ela contem potencia, cujo custo mascararia qualquer delta de invocacao, comparacao ou navegacao. Gates por item usam micro-benchmarks isolados.
- Gate por item: ganho pareado otimizado contra oraculo na mesma execucao JMH. Comparacao contra baseline historico de outro dia nao e criterio, porque o proprio historico documenta deltas acima de vinte por cento em codigo inalterado nesta maquina.
- Gate das quatro suites rastreadas: delta alem de mais ou menos um por cento so conta como regressao quando ha causa identificada no codigo; sem causa, o veredito e a re-medicao pareada da mesma expressao. Isso mantem o rigor onde ele mede algo e para de tratar ruido de governor como defeito.
- Os criterios da secao 21 da estrategia sao reformulados por serem intestaveis como escritos: "navegacao aproximadamente igual a getter direto mais indireccao constante" nao e verificavel quando `propertyChain` mede 17,64 ns/op e um getter direto e sub-nanossegundo.

## Documentacao e ADR

- A Etapa 8 possui plano detalhado e registro de decisoes proprios.
- O ADR 0020 registra apenas o que e caro de reverter: invocacao sem reflexao por ponto de entrada gerado no build do ambiente, ambiente longevo como pre-condicao, e `invokeExact` com handle pre-adaptado como rota alternativa rejeitada. A lista de familias especializadas e os limiares de JMH ficam fora do ADR.
- `CONTEXT.md` recebe tres termos: No Especializado, Invocacao Sem Reflexao e Elisao de Coercao de Borda. Ponto de entrada tipado nao entra no glossario: e mecanica de implementacao, nao linguagem de dominio.
- O plano-mestre e corrigido em sete pontos: substituicao dos nos genericos, potencia, reordenacao de curto-circuito, cache de constantes pequenas, criterios da secao 21, pre-alocacao de varargs e `VarHandle`.

## Decomposicao

- Uma issue preparatoria de simetria de `MathContext`, antes da etapa.
- Oito incrementos na etapa, cada um fechando com `mvn -pl exp-mk3 -am test` verde.
- A decomposicao em issues acontece depois desta sessao e com confirmacao propria, seguindo o precedente da Etapa 7.

## Decisoes Ainda Pendentes

- Nenhuma pendencia aberta. As decisoes diferidas sao todas resolvidas por medicao: permanencia de cada familia especializada, permanencia do pool de escopo, permanencia da simetria de `MathContext` sob custo, e continuidade da etapa apos os dois pilotos.
