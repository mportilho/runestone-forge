# Decisoes de Planejamento da Etapa 4 - Resolver Semantico

Este documento consolida as decisoes tomadas durante a sessao de planejamento da Etapa 4 do `exp-mk3`. Ele registra o estado atual das decisoes, ja considerando revisoes que substituem decisoes anteriores da conversa.

> A ADR 0016 supersede neste registro os contratos anteriores de containers sequenciais, chamadas com sintaxe dedicada, curinga de filho e classificacao de chamadas na AST fonte. O contrato vigente usa apenas `CollectionType<T>`, chamadas navegadas `.`/`?.` classificadas pelo resolvedor e o curinga `[*]`/`?.[*]`.

> A ADR 0017 supersede neste registro a restricao anterior de `root` a grau integral positivo. Potencia e raiz agora aceitam todo resultado decimal real e definido, com classificacao racional estatica ou Checagem Diferida.

## Escopo da Etapa 4

- O `SemanticResolver` e responsavel por decidir o significado semantico da expressao; etapas posteriores executam esse significado.
- O `SemanticResolver` produz um `Modelo Semantico` interno e planejavel apenas quando nao houver diagnosticos de erro.
- O resolver deve acumular todos os problemas independentes em uma unica execucao, evitando parar no primeiro erro.
- O resolver fica em pacote interno, por exemplo `com.runestone.expeval_mk3.internal.semantics`.
- A API publica do modulo nao deve expor o `SemanticResolver` nem o `Modelo Semantico` nesta etapa.
- A API interna principal esperada e `resolve(ExpressionFileNode ast, ExpressionEnvironment environment)`.
- A Etapa 4 decide tipos, simbolos, funcoes, navegacao, operacoes de colecao, layout de frame, nulidade de runtime, valores semanticos preparados e checagens diferidas.
- A Etapa 4 nao executa constant folding, CSE, reordenacao, elisao de `as*`, nem outras otimizacoes de plano.
- A Etapa 4 nao deve virar um runtime parcial.
- A fachada interna do resolver deve continuar simples, por exemplo `resolve(ExpressionFileNode ast, ExpressionEnvironment environment)`.
- Internamente, o resolver deve ser organizado em fases separadas, nao como uma unica visita monolitica.
- Fases internas esperadas: validar arquivo e escopos iniciais; resolver atribuicoes sequenciais; resolver expressoes com restricoes contextuais locais; resolver navegacao/filtros/lambdas/operacoes de colecao; finalizar layout, nulidade, fatos numericos, valores preparados e checagens diferidas; validar invariantes do modelo.
- As fases internas nao sao API publica e podem ser refinadas durante a implementacao.

## ADRs e Pre-Trabalho Obrigatorio

- ADR 0007 define que literais inteiros da linguagem sao apenas decimais.
- ADR 0008 define que simbolos externos exigem valor padrao e politica de sobrescrita.
- ADR 0009 define que a linguagem fonte nao tem literal `null`.
- ADR 0010 define que o modelo semantico aceito exige tipos conhecidos.
- ADR 0011 remove `strict mode` do `Ambiente de Expressao`.
- As ADRs 0007-0014 sao pre-trabalho bloqueante para implementar a Etapa 4.
- O plano macro deve ser ajustado para remover referencias obsoletas a `UnknownType`, `NullType`, simbolos externos sem default, `strict mode`, e politica de hex/octal em subscripts.
- A Etapa 4 tem documento detalhado proprio em `exp-mk3/docs/planning/etapa-4/etapa-4-resolver-semantico.md`.

## Etapa 3.5 - Saneamento antes do Resolver

- A Etapa 3.5 existe para alinhar gramatica, AST, tipos, ambiente, catalogos, testes, corpus e vocabulario aos ADRs 0007-0014 antes da implementacao do `SemanticResolver`.
- A Etapa 3.5 deve remover conceitos obsoletos do contrato publico e do caminho interno planejavel, nao apenas impedir que aparecam em um `Modelo Semantico` de sucesso.
- `strictMode` deve ser removido de `ExpressionEnvironment`, builder, testes e configuracoes representativas.
- Declaracoes de `Simbolo Externo` sem default ou sem `Politica de Sobrescrita de Simbolo` devem ser removidas.
- `NullType` deixa de ser tipo normal de expressao.
- Literal fonte `null` deve ser removido da gramatica, AST, pretty-printer e corpus.
- Inteiros hexadecimais e octais devem ser removidos da gramatica, AST, pretty-printer e corpus.
- Usos publicos de `UnknownType` devem ser substituidos por contratos explicitos; placeholders como `Variavel de Tipo Pendente` e `Tipo Invalido` pertencem apenas ao fluxo interno do resolver.
- Diagnosticos e migracao para sintaxes antigas, como `null`, hex/octal e type hints removidos, devem ficar fora do resolver semantico principal quando pertencerem ao parser ou a migracao.
- A Etapa 8 do plano macro deve ser reescrita para especializacao preservando semantica decimal, sem `FAST`.
- A Etapa 8 ainda deve cobrir nos especializados, reducao de boxing/alocacao, otimizacoes de `BigDecimal`, invocacao de funcoes sem reflexao, accessors rapidos de navegacao e JMH gates de decimal/navegacao.
- Gates e entregas baseados em `computeAsLong`, `computeAsDouble`, zero alocacao em `FAST` e fallback estrutural de `FAST` para decimal devem ser removidos do plano atual.
- A Etapa 3.5 deve preparar o `Catalogo de Operacoes de Colecao` e seus descriptors minimos; a Etapa 4 apenas resolve bindings contra esse catalogo.
- Descriptors minimos de operacao de colecao: `map`, `sum`, `count`, `keys`, `values`, `any` e `all`.
- Descriptors de operacao de colecao devem declarar receiver aceito, tipo de `Item Atual`, tipo de retorno, preservacao de forma, pureza, politica de avaliacao e materializacao.
- O builder do ambiente deve validar descriptors invalidos de operacao de colecao.
- O `Catalogo de Operacoes de Colecao` pertence ao ambiente que o declara; isolamento de cache ocorre pelo UUID da instancia, sem fingerprint canonico do catalogo.
- O catalogo deve ser arquitetado com seam interno para extensao futura, mas a API publica de registro de operacoes de colecao customizadas fica fora da v2 inicial.
- A v2 inicial instala automaticamente as operacoes oficiais em todo ambiente construido, sem helper publico de registro.
- O resolver nao deve depender de hardcode dos built-ins; ele consome descriptors do catalogo.

### Criterios de aceite da Etapa 3.5

- `mvn -pl exp-mk3 -am test` verde apos remover ou ajustar `UnknownType`, `NullType`, `strictMode`, `NumericMode`/`FAST`, literal `null` e inteiros hex/octal.
- A gramatica nao aceita `null`, `0x10` ou `077` como expressoes validas.
- `ExpressionEnvironment` nao expoe `strictMode` nem `NumericMode`.
- `ExternalSymbolCatalog` nao permite simbolo sem default, default null, politica de sobrescrita ausente, container default com null validavel, nem mapa com chave/valor null validavel.
- `FunctionCatalog` nao aceita tipo desconhecido em parametro/retorno, funcao dobravel impura, nem overload duplicado apenas por retorno.
- `JavaTypeCatalog` rejeita membro exposto sem tipo de retorno mapeavel.
- `CollectionOperationCatalog` existe com built-ins oficiais minimos.
- Cada `ExpressionEnvironment` construido recebe um UUID textual opaco; ambientes construidos separadamente nao compartilham ID, ainda que tenham conteudo equivalente.
- Corpus e testes sao atualizados para ADRs 0007-0014.

## Contratos de Parametro sem UnknownType

- `UnknownType` nao deve ser mantido como tipo publico de "qualquer entrada" e nao deve ser renomeado para `AnyType`.
- Tipos de expressao aceitos continuam sendo tipos conhecidos; abertura de argumento deve ser representada por contrato ou restricao de parametro, nao por `ExpressionType` planejavel.
- Funcoes `asNumber/asText/asBool/asDate/asTime/asDateTime` recebem um contrato especial de entrada de assercao/conversao, nao um parametro `UnknownType`.
- `asCollection(x)` generico nao deve ser oferecido como built-in na Etapa 4, porque nao produz elemento conhecido sem contrato adicional.
- Built-ins de colecao devem declarar o elemento no nome ou na assinatura, por exemplo `asCollectionOfNumber(x)` ou assinatura concreta equivalente.
- Regra geral de catalogo: uma funcao de assercao de colecao so e registravel se declarar retorno `CollectionType<T>` com `T` conhecido.
- O `ReflectedFunctionImporter` deve rejeitar no registro qualquer tipo Java sem mapeamento conhecido para tipo de expressao.
- Coercao de borda deve operar sobre valor Java de origem e tipo alvo conhecido, sem modelar a origem como `UnknownType`.
- Defaults externos de mapa ou colecao vazios ou heterogeneos sem tipo declarado devem ser rejeitados no builder do ambiente.

## Gramática e Literais

- Hexadecimal e octal devem ser removidos da gramatica como um todo.
- `0x10`, `077` e formas similares nao sao `INT` valido em nenhuma posicao da fonte.
- A Etapa 4 nao deve conter politica especial de rejeicao de hex/octal em subscripts, porque essas formas nao chegam como AST valida.
- Diagnosticos didaticos para hex/octal antigos pertencem ao parser/migracao, nao ao resolver semantico.
- O literal fonte `null` deve ser removido da gramatica.
- Fonte como `null`, `[null]`, `x = null` e `asNumber(null)` deve falhar antes ou durante migracao, nao como caso semantico normal.
- Literais `DATETIME` com e sem offset sao interpretados pela politica temporal do ambiente.
- Literais `DATETIME` sem offset sao horarios locais no `ZoneId` do ambiente, com offset efetivo inferido pelas `ZoneRules`.
- Literais `DATETIME` com offset explicito sao convertidos para o `ZoneId` do ambiente antes de virar valor semantico preparado.
- `ZoneId` permanece parte da semantica do ambiente, mas nao e codificado no identificador de instancia.

## Tipagem Conhecida

- O `Modelo Semantico` de sucesso nao contem `UnknownType`.
- Todo no de expressao aceito deve ter tipo conhecido em compilacao.
- A Etapa 4 nao cria simbolos fonte implicitos a partir de identificadores desconhecidos.
- `x + 1` sem `x` externo, interno previamente atribuido, `Valor Temporal Corrente` ou `Item Atual` contextual e erro de simbolo desconhecido.
- Toda funcao registrada deve ter retorno conhecido.
- Todo metodo/propriedade Java registrado deve ter tipo de retorno conhecido.
- Toda operacao de colecao deve ter retorno conhecido ou computavel pelos argumentos.
- Navegacao sem metadata suficiente e erro semantico, nao vinculo diferido por tipo desconhecido.
- Resultado final de expressao com tipo indeterminado e erro.
- `Tipo Invalido` existe apenas internamente no fluxo de falha para suprimir cascata de diagnosticos.
- `Tipo Invalido` nunca aparece em `SemanticResolutionSuccess`.
- `Variavel de Tipo Pendente` pode existir apenas durante a resolucao para inferencia contextual local.
- `Variavel de Tipo Pendente` nao representa simbolo fonte implicito nem simbolo externo nao declarado.
- Toda `Variavel de Tipo Pendente` deve resolver para tipo conhecido ou gerar diagnostico antes de sucesso.
- Exemplos de uso de `Variavel de Tipo Pendente`: colecao vazia em `??`, condicional, membership ou parametro de funcao.

## Null e Nulidade de Runtime

- A fonte nao tem literal `null`.
- `Valor Nulo de Runtime` deve ser introduzido pela linguagem apenas por navegacao segura, salvo decisoes especificas de borda/runtime registradas separadamente.
- `Valor Nulo de Runtime` nao pode ser fornecido por override de `Simbolo Externo`.
- `Valor Nulo de Runtime` nao e tipo de expressao.
- `Nulidade de Runtime` e metadata semantica, nao um tipo nullable.
- `Nulidade de Runtime` nao participa de unificacao comum de tipos, mas constructs cujo contrato rejeita null podem usa-la para diagnostico semantico.
- `RuntimeNullability.NEVER_NULL` indica que o no e provado como nunca nulo se executar com sucesso.
- `RuntimeNullability.MAY_BE_NULL` indica que o valor pode ser nulo em runtime.
- A metadata de nulidade apoia auditoria, diagnosticos e possiveis warnings futuros, mas nao participa da compatibilidade comum de tipos.
- Referencia direta a `Simbolo Externo` e `NEVER_NULL`, porque default e override null sao proibidos.
- Para simbolo externo de objeto Java, propriedades e metodos registrados sao tratados como `NEVER_NULL` por contrato.
- Para simbolo externo `Map<V>`, subscript textual retorna `NEVER_NULL` por contrato quando executa com sucesso.
- Defaults e overrides externos nao devem conter elementos null quando o ambiente consegue validar.
- Defaults e overrides externos de `Map` nao devem conter chave null nem valor null quando o ambiente consegue validar.
- `Map<V>["key"]` nao usa `MAY_BE_NULL` para ausencia de chave; chave ausente ou valor null em runtime e erro runtime do link.
- Para colecoes Java externas dinamicas, elementos sao tratados como `NEVER_NULL` por contrato; elemento null encontrado em runtime e erro runtime da operacao que o consome.
- `??` e `?.` sao os constructs explicitos para protecao contra null de runtime.
- `??` aceita operandos de tipos estaticos unificaveis e retorna o primeiro valor runtime nao nulo.
- `??` avalia operandos da esquerda para a direita e para no primeiro valor runtime nao nulo.
- Operandos posteriores nao avaliados por `??` nao podem produzir erro nem efeito.
- Apesar da avaliacao lazy em runtime, o resolver valida semanticamente todos os operandos de `??`.
- `1 ?? unknownSymbol` continua emitindo erro semantico para `unknownSymbol`.
- Binding/modelo de `NullCoalesceNode` deve carregar `Politica de Avaliacao.LAZY_LEFT_TO_RIGHT`.
- `??` nao converte tipo.
- `??` nao exige que o operando esquerdo seja marcado como `MAY_BE_NULL`.
- `1 ?? 2` e semanticamente valido, sem warning na Etapa 4.
- A Etapa 4 nao emite warning por fallback redundante ou operando posterior potencialmente inalcancavel em `??`.
- Se todos os operandos de `??` forem `MAY_BE_NULL`, o resultado tambem e `MAY_BE_NULL`.
- Se uma cadeia `??` tem algum fallback `NEVER_NULL`, o resultado pode ser `NEVER_NULL`.
- Resultado de `??` e `NEVER_NULL` se pelo menos um operando for `RuntimeNullability.NEVER_NULL`.
- Resultado de `??` e `MAY_BE_NULL` apenas se todos os operandos forem `RuntimeNullability.MAY_BE_NULL`.
- `??` e o construct explicito principal para descarregar nulidade de runtime antes de atribuicoes, operadores, funcoes e predicados.
- `x = null` nao existe na linguagem.
- Nao deve haver teste explicito de null na v2, como `isNull(x)`, nesta etapa.
- Funcoes nao aceitam argumento null.
- Nao deve haver `acceptsNull=true` em descriptor de parametro de funcao.
- Metodos de objeto e operacoes de colecao tambem nao aceitam argumentos explicitamente nulos.
- Built-ins `as*` tambem rejeitam null como qualquer funcao.
- Chamada de funcao com argumento marcado como `RuntimeNullability.MAY_BE_NULL` e erro semantico, mesmo quando o tipo estatico e compativel.
- O diagnostico deve apontar para o argumento que pode produzir null.
- O usuario deve proteger explicitamente o argumento, por exemplo `f(x ?? default)`.
- Constructs da linguagem como `??`, `if` e navegacao segura nao sao tratados como funcoes para essa regra, mas cada construct declara se aceita `MAY_BE_NULL`.
- Operadores comuns que nao aceitam null rejeitam operandos `RuntimeNullability.MAY_BE_NULL` semanticamente.
- O usuario protege esses casos com fallback explicito, por exemplo `(customer?.age ?? 0) + 1`.
- Comparacoes com possivel `Valor Nulo de Runtime`, incluindo igualdade e diferenca, sao erro semantico quando algum operando e `MAY_BE_NULL`.
- `customer?.status = "ACTIVE"` e erro semantico; o usuario deve escrever fallback explicito quando quiser comparar ausencia como valor.
- Atribuicoes internas e desestruturacao exigem RHS `RuntimeNullability.NEVER_NULL`; RHS `MAY_BE_NULL` e erro semantico.
- O usuario deve usar `??` para transformar uma navegacao segura em valor atribuivel, por exemplo `city := customer?.address?.city ?? ""`.
- Expressao de resultado tambem exige `RuntimeNullability.NEVER_NULL`; resultado final `MAY_BE_NULL` e erro semantico.
- `customer?.address?.city` como resultado final e erro semantico sem fallback `??`.

## Ambiente de Expressao e Simbolos Externos

- Todo `Simbolo Externo` exige valor padrao nao nulo.
- Todo `Simbolo Externo` exige politica de sobrescrita.
- O tipo do simbolo externo e declarado e validado contra o default, ou inferido do default.
- Declaracao externa apenas com tipo nao e permitida.
- Declaracao externa apenas com nome nao e permitida.
- Default null nao e permitido.
- Override runtime null nao e permitido, mesmo para simbolos sobrescreviveis.
- Runtime input null para simbolo externo e erro de entrada/runtime de borda.
- Simbolo externo `overridable=false` e valor fixo do ambiente.
- Se runtime input tenta sobrescrever simbolo `overridable=false`, isso e erro de entrada/runtime, nao deve ser ignorado.
- Simbolos fixos podem ser dobrados em Etapa 7 se o valor for constante e a expressao for pura.
- Simbolos sobrescreviveis nunca sao constantes dobraveis apenas por terem default.
- Na Etapa 4, todo simbolo externo usado recebe slot no `Layout de Frame`, mesmo se fixo.
- Etapa 7 pode elidir leitura de simbolo fixo dobrado e registrar `foldedVariableReads`.
- Simbolo externo declarado mas nao usado nao cria slot no frame.
- Simbolo externo declarado mas nao usado nao gera warning por padrao.
- Nome, tipo, default e `overridable` continuam parte do contrato de cada simbolo externo, sem serializacao exclusiva para identidade do ambiente.
- Ambientes construidos separadamente nao compartilham plano, independentemente de diferirem ou nao em `overridable`.
- Defaults externos heterogeneos de mapa ou colecao sem tipo declarado devem ser rejeitados.
- Default externo de colecao/mapa vazio exige tipo declarado.
- Default externo Java `List`, array ou `Iterable` entra como `Tipo Colecao<T>`.

## Simbolos Internos e Atribuicoes

- Resolucao de atribuicoes e sequencial.
- Nao ha forward reference para simbolo interno declarado depois.
- A LHS de uma atribuicao so introduz o simbolo depois de resolver a RHS daquela atribuicao.
- `x := x + 1` sem `x` externo ou interno anterior e erro de simbolo desconhecido.
- Se existe `x` externo, `x := x + 1` le o externo na RHS e cria/sombreia um interno na LHS.
- Depois da atribuicao que cria o interno `x`, usos posteriores de `x` resolvem para o simbolo interno.
- Externo `x` e interno `x` tem slots distintos e identidade distinta.
- Sombreamento externo por interno e permitido com warning no target.
- Reatribuicao sequencial de simbolo interno e permitida.
- O mesmo simbolo interno usa um unico slot, atualizado em ordem.
- O tipo de simbolo interno deve permanecer estavel/unificavel ao longo das atribuicoes.
- Reatribuicao interna nao aplica coercao de borda implicita.
- RHS de atribuicao interna deve ser `RuntimeNullability.NEVER_NULL`.
- `x := customer?.address?.city` e erro semantico porque o RHS e `MAY_BE_NULL`.
- `x := customer?.address?.city ?? ""` e valido quando os tipos unificam.
- `x := 1; x := "s"` e erro semantico.
- `x := 1; x := "2"` e erro semantico mesmo que `asNumber("2")` fosse conversivel.
- `x := 1; x := asNumber("2")` e valido se a assercao/conversao explicita for aceita.
- `x := []; x := [1]` pode refinar a colecao vazia para `Colecao<NUMBER>`.
- `x := [1]; x := []` pode tipar a segunda colecao vazia pelo tipo existente de `x`.
- Usos intermediarios tambem participam das restricoes do simbolo interno.
- Atribuicao cujo RHS e `Tipo Invalido` faz o simbolo interno propagar `Tipo Invalido` no fluxo de falha sem cascata.
- Programa com atribuicoes e sem expressao de resultado e semanticamente valido.
- Arquivo sem atribuicoes e sem expressao de resultado e erro semantico de arquivo vazio.
- Span recomendado para arquivo vazio: offset `0`, linha `1`, coluna `1`, comprimento `0`.

## Desestruturacao

- Desestruturacao atual e apenas plana, conforme gramatica e AST.
- Nao projetar suporte a desestruturacao aninhada agora.
- Cada identificador folha da desestruturacao vira um `Simbolo Interno` proprio com slot proprio.
- A ordem de slots da desestruturacao segue a ordem textual esquerda-para-direita.
- Atribuicao de desestruturacao nao cria slot para a colecao inteira, salvo temporario de execucao se necessario.
- Tamanho conhecido de literal de colecao deve ser validado em compile-time.
- `[a, b] := [1, 2]` e valido.
- `[a, b] := [1]` e erro semantico.
- `[a, b] := []` e erro semantico de aridade conhecida incompativel.
- Fonte desestruturavel com tamanho desconhecido e valida quando o RHS tem tipo `Collection<T>` e forma `UNKNOWN_SIZE`; nesse caso o resolver registra `Checagem Diferida` de tamanho minimo.
- Cada alvo de desestruturacao de `Collection<T>` recebe tipo `T`; elementos excedentes sao ignorados.
- `Map` e `Object` nao sao desestruturaveis na Etapa 4.
- Nomes duplicados no mesmo target, como `[a, a]`, sao erro semantico.
- O diagnostico de duplicidade deve apontar para a segunda ocorrencia e relacionar a primeira.

## Layout de Frame

- O `Layout de Frame` e definido durante a resolucao semantica.
- A ordem deve ser canonica e independente de `HashMap`.
- Simbolos externos usados entram em ordem de primeira referencia na AST.
- Simbolos internos entram em ordem da primeira atribuicao que cria cada simbolo.
- Slots de `Item Atual` entram por profundidade usada.
- Slots sinteticos ficam para Etapa 7, depois dos simbolos declarados.
- O frame reserva slots apenas para simbolos externos efetivamente usados.
- Slots de `Item Atual` sao reservados ate a profundidade maxima realmente usada, nao ate o limite configurado.
- Desestruturacao atribui slots por simbolo alvo individual.

## Diagnosticos

- `Diagnostico de Expressao` tem categoria, codigo estavel, severidade, span primario e pode ter spans relacionados/notas.
- Todo erro semantico deve ter `SourceSpan` primario.
- Severidades iniciais: `ERROR` e `WARNING`.
- Apenas `ERROR` bloqueia `Modelo Semantico` planejavel.
- `WARNING` nao bloqueia planejamento nem execucao.
- Sombreamento de simbolo externo por interno gera warning.
- Warning de sombreamento aponta para o target da atribuicao e pode relacionar o simbolo externo quando houver localizacao/configuracao disponivel.
- Eliminacao de atribuicao morta na Etapa 7 pode gerar warning sem bloquear.
- `SemanticResolutionSuccess` contem modelo e diagnosticos, podendo incluir warnings.
- `SemanticResolutionFailure` contem diagnosticos quando existe pelo menos um erro.
- Evitar resultado de sucesso com erros.
- Ordenacao de diagnosticos: `SourceSpan.offset`, depois severidade, depois categoria/codigo estavel.
- Diagnosticos multi-causa usam span primario para ordenacao e spans relacionados para causas secundarias.
- Conflitos de restricoes devem apontar para o uso que torna o conflito evidente e relacionar o uso anterior.
- Simbolo desconhecido deve apontar para o identificador desconhecido.
- Erros independentes devem ser emitidos mesmo que outro ramo da expressao tenha erro.
- Cascatas devem ser suprimidas quando qualquer operando ou simbolo envolvido ja tem `Tipo Invalido`.
- Parser, resolver e runtime devem usar continuidade conceitual de diagnosticos com categorias distintas, como `PARSE`, `SEMANTIC`, `RUNTIME` e possivelmente `MIGRATION`.
- Nulidade de runtime que escapa para contexto `NEVER_NULL` deve gerar diagnostico semantico especifico, nao warning generico.
- Codigos recomendados: `SEMANTIC_NULLABLE_RESULT_NOT_ALLOWED`, `SEMANTIC_NULLABLE_ASSIGNMENT_NOT_ALLOWED`, `SEMANTIC_NULLABLE_OPERAND_NOT_ALLOWED`, `SEMANTIC_NULLABLE_ARGUMENT_NOT_ALLOWED`, `SEMANTIC_NULLABLE_RECEIVER_NOT_ALLOWED` e `SEMANTIC_NULLABLE_PREDICATE_NOT_ALLOWED`.
- Diagnosticos de nulidade devem sugerir fallback explicito com `??` quando aplicavel.

## Resultado de Resolucao e SemanticModel

- `Resultado de Resolucao Semantica` deve ser uma hierarquia fechada com `SemanticResolutionSuccess` e `SemanticResolutionFailure`.
- `SemanticResolutionSuccess` contem `SemanticModel` planejavel e lista de warnings.
- `SemanticResolutionSuccess` nunca contem diagnostico `ERROR`.
- `SemanticResolutionFailure` contem diagnosticos quando existe pelo menos um `ERROR`, podendo tambem conter warnings.
- `SemanticResolutionFailure` nunca contem `SemanticModel`.
- Evitar um resultado unico com `SemanticModel` opcional, porque permite estados invalidos como modelo com erro ou falha sem erro.
- `SemanticModel` deve preservar a AST imutavel e source-faithful.
- Anotacoes semanticas devem ficar em mapas por `NodeId`, nao em wrappers mutaveis de AST.
- Campos esperados incluem AST, tipos resolvidos, bindings de simbolo, bindings de funcao, bindings de navegacao, fatos numericos, nulidade de runtime, formas de colecao conhecidas, valores preparados, checagens diferidas e layout de frame.
- Todo `ExpressionNode` em `SemanticResolutionSuccess` deve ter entrada em `resolvedTypes`.
- Todo `ExpressionNode` valorado em `SemanticResolutionSuccess` deve ter tipo resolvido e nulidade resolvida.
- Elementos de AST que produzem valor, incluindo links de navegacao com `NodeId`, devem ter tipo resolvido ou tipo resultante registrado.
- `navigationBindings` deve ser indexado pelo `NodeId` do link de navegacao.
- `functionBindings` deve ser indexado pelo `NodeId` da chamada.
- `symbolBindings` deve ser indexado pelo `NodeId` da referencia/target relevante.
- `Tipo Invalido` e placeholders de inferencia interna nao aparecem em mapas do modelo de sucesso.
- Binding ausente para funcao, navegacao ou simbolo usado por no aceito e bug interno capturado por invariante/teste, nao diagnostico tardio de usuario.

## Checagens Diferidas

- `Checagem Diferida` existe apenas para pre-condicoes de valor runtime em constructs ja tipados.
- Checagens diferidas nao podem representar escolha de tipo, overload runtime ou navegacao sobre tipo desconhecido.
- Exemplos validos: fatorial integral nao negativo, dominio real/definido de potencia e raiz, bounds de subscript, tamanho minimo de desestruturacao de colecao dinamica e limites de materializacao.
- O `ExecutionPlanBuilder` consome checagens diferidas sem redescobrir regras semanticas.
- Com `strict mode` removido, nao ha politica de rejeicao especial de checagens diferidas por modo estrito.

## Operadores Numericos

- A v2 nao tera modo numerico `FAST`.
- A semantica numerica aceita na Etapa 4 e no produto planejado e decimal.
- `Modo Numerico`/`NumericMode` deve ser removido do contrato publico do `Ambiente de Expressao` enquanto existir apenas semantica decimal.
- O identificador de instancia nao codifica modo numerico nem qualquer outro conteudo de configuracao.
- Referencias existentes a `NumericMode`, `NumericMode.FAST`, caminhos `LONG`/`DOUBLE` como modo publico e gates especificos de FAST devem ser removidas do plano macro em saneamento posterior.
- O ambiente mantem `MathContext` e `transcendentalMathContext`.
- Operadores aritmeticos comuns exigem `NUMBER` e retornam `NUMBER`.
- Operadores aritmeticos comuns exigem operandos `RuntimeNullability.NEVER_NULL`.
- Operadores aritmeticos nao aceitam valores temporais.
- Operacoes temporais devem ser funcoes built-in explicitas.
- Divisao em modo `DECIMAL` e permitida e usa `MathContext` do ambiente.
- Resultado de operacao decimal exposta deve ser `BigDecimal` ou equivalente interno convertido para `BigDecimal` na borda.
- Literais inteiros pequenos podem ser preservados internamente como `long`/`BigInteger` para AST/folding.
- `root` exige operandos `NUMBER`.
- `root` exige operandos `RuntimeNullability.NEVER_NULL`.
- O grau de `root` continua tendo tipo publico `NUMBER`; pertencer ao Dominio Numerico Real e restricao de valor do operador, nao tipo publico separado.
- Grau de `root` pode ser integral, fracionario, positivo ou negativo; grau zero e indefinido.
- Base ou radicando negativo usa a fracao decimal exata reduzida para classificar resultado real, complexo e sinal.
- Violacao estatica do dominio de potencia/raiz e erro semantico; valor dinamico gera Checagem Diferida.
- `x!` exige `NUMBER` com restricao de valor integral nao negativo.
- `x!` exige operando `RuntimeNullability.NEVER_NULL`.
- Fatorial constante negativa/fracionaria e erro semantico.
- Fatorial dinamico gera checagem diferida.
- Limite maximo de fatorial deve ser guard-rail do ambiente, por exemplo `maxFactorialInput`.
- `maxFactorialInput` pertence ao ambiente e afeta aceitacao semantica, sem ser serializado no identificador de instancia.
- Fatorial constante acima de `maxFactorialInput` e erro semantico.
- Fatorial dinamico gera checagem diferida de integralidade, nao-negatividade e limite maximo.
- `%` pos-fixado exige `NUMBER` e retorna `NUMBER`.
- `%` pos-fixado exige operando `RuntimeNullability.NEVER_NULL`.
- `%` pos-fixado nao e reescrito na Etapa 4.
- Em `DECIMAL`, `%` produz categoria numerica decimal mesmo se a entrada for integral.

## Fatos Numericos

- A Etapa 4 deve calcular fatos numericos minimos por no numerico quando forem relevantes para validacao semantica.
- Fatos numericos nao mudam o tipo publico `NUMBER`.
- Fatos iniciais incluem forma integral/fracionaria/desconhecida e fracao decimal exata reduzida quando sua paridade for relevante ao dominio real.
- Literal decimal inteiro recebe `INTEGRAL_KNOWN`.
- Literal decimal fracionario recebe `FRACTIONAL_KNOWN` quando a fracao e evidente na propria literal.
- Simbolos, funcoes e operacoes dinamicas recebem `UNKNOWN_NUMERIC_VALUE_SHAPE`, salvo prova conservadora simples sem folding complexo.
- Potencia e `root` usam fatos numericos para classificar constantes reais, complexas ou indefinidas e registrar checagens diferidas quando o valor e desconhecido; fatorial continua exigindo integral nao negativo dentro do limite.
- A Etapa 4 nao precisa categorizar toda operacao numerica como `DECIMAL`, porque toda execucao numerica planejada e decimal.
- O campo do modelo pode se chamar `numericFacts` em vez de `numericKinds`.

## Operadores Booleanos e Politica de Avaliacao

- `and`, `or`, `nand`, `nor`, `xor` e `xnor` exigem operandos `BOOLEAN` e retornam `BOOLEAN`.
- `and` e `or` tem politica de avaliacao lazy/curto-circuito.
- `nand`, `nor`, `xor` e `xnor` avaliam ambos os lados.
- Operadores booleanos exigem operandos `RuntimeNullability.NEVER_NULL`.
- Operando booleano com `Nulidade de Runtime.MAY_BE_NULL` e erro semantico, mesmo em `and`/`or` com curto-circuito.
- A politica de avaliacao deve ficar no modelo/binding do no para orientar runtime e otimizacoes.
- Predicados de filtro devem ser `BOOLEAN`.
- Predicados de filtro devem ser `RuntimeNullability.NEVER_NULL`.
- Null de runtime nao e tratado como `false` em predicados.
- Usuario deve escrever fallback explicito como `@?.active ?? false`.
- Predicado tipado como `BOOLEAN` e `Nulidade de Runtime.MAY_BE_NULL` e erro semantico.

## Igualdade, Ordenacao e Pertencimento

- Igualdade exige tipos compativeis conhecidos.
- Tipos concretos incompativeis em igualdade geram erro semantico, nao `false` silencioso.
- Igualdade exige operandos `RuntimeNullability.NEVER_NULL`.
- Igualdade nao tem semantica especial para `Valor Nulo de Runtime`; operando `MAY_BE_NULL` e erro semantico.
- Igualdade entre `ObjectType` nao e permitida na Etapa 4.
- `ObjectType` deve ser navegado ou passado para funcoes com contrato explicito, nao usado diretamente em operadores.
- Ordenacao aceita apenas familias homogeneas ordenaveis: `NUMBER`, `STRING`, `DATE`, `TIME`, `DATETIME`.
- Ordenacao exige operandos `RuntimeNullability.NEVER_NULL`.
- `BOOLEAN`, `COLLECTION`, `MAP` e `OBJECT` nao sao ordenaveis por padrao.
- Temporais diferentes nao sao comparaveis diretamente.
- `DATE = DATETIME` e erro semantico.
- `DATE < DATE`, `TIME < TIME` e `DATETIME < DATETIME` sao validos.
- `between` exige que valor e limites unifiquem na mesma familia ordenavel.
- `between` exige valor e limites `RuntimeNullability.NEVER_NULL`.
- `x in Tipo Colecao<T>` e valido se `x` for compativel com `T`.
- `x in Tipo Mapa<V>` testa existencia de chave textual e exige lado esquerdo `STRING`.
- `in` e `not in` exigem operandos `RuntimeNullability.NEVER_NULL`.
- Pertencimento de `ObjectType` em colecao de objetos nao e permitido na Etapa 4; usar funcao explicita quando necessario.
- `value in map` nao testa valores.
- Para valores de mapa, o usuario deve usar `value in map.values()`.
- `x in STRING` nao significa substring.
- Substring deve ser funcao explicita, se existir.
- `x in []` pode usar o tipo conhecido de `x` para tipar a colecao vazia.

## Condicionais e Coalescencia Nula

- A gramatica atual exige `else` em condicionais classicos e funcionais.
- Runtime de condicional avalia apenas o ramo selecionado, mas o resolver valida semanticamente todas as condicoes e todos os ramos.
- Ramo aparentemente inalcancavel por condicao constante ainda pode emitir erro semantico na Etapa 4.
- Todas as condicoes de `if` e `elsif` devem ser `BOOLEAN`.
- Condicao com tipo estatico `BOOLEAN` e `Nulidade de Runtime.MAY_BE_NULL` e erro semantico.
- Null runtime em condicao nao e tratado como `false`; fallback deve ser explicito, por exemplo `customer?.active ?? false`.
- Ramos de resultado de condicional devem unificar para tipo conhecido.
- Como a fonte nao tem `null`, nao ha ramo literal nulo.
- Condicionais podem usar inferencia contextual local para colecao vazia.
- `if c then [] else [1] endif` resolve como `Colecao<NUMBER>`.
- `if c then [] else [] endif` e erro por elemento indeterminado.
- `??` exige operandos de tipos estaticos unificaveis.
- `[] ?? [1]` resolve como `Colecao<NUMBER>`.
- `[] ?? []` e erro por elemento indeterminado.
- `1 ?? "x"` e erro de tipos incompativeis, mesmo que folding posterior pudesse provar alcance.

## Colecoes e Mapas

- Forma/tamanho conhecido de colecao e metadata semantica por no, nao parte de `CollectionType<T>`.
- Metadata de forma inicial recomendada: `FIXED_SIZE(n)` e `UNKNOWN_SIZE`.
- Literal de colecao recebe `FIXED_SIZE(n)`; colecoes externas e resultados dinamicos recebem `UNKNOWN_SIZE`, salvo contrato mais especifico.
- Forma conhecida e usada para validar desestruturacao estatica, bounds de index/slice estaticos e `maxMaterializedSize` do literal.
- `Tipo Colecao<T>` e ordenado, indexavel, fatiavel, desestruturavel, filtravel, mapeavel, quantificavel e agregavel conforme o elemento.
- Arrays, `List`, `Collection` e `Iterable` externos entram como `Tipo Colecao<T>`.
- `Collection<T>[i]` retorna `T`; `Collection<T>[a:b]` retorna `Collection<T>`.
- Indice negativo e contado a partir do fim; slice usa intervalo half-open `[start:end)`.
- Bound omitido usa inicio ou fim da colecao e bound negativo e contado a partir do fim.
- Apos normalizacao, slice exige `0 <= start <= end <= size`.
- Slice fora de bounds ou invertido falha; nao ha clamp silencioso.
- Quando colecao e indice tem forma/tamanho conhecidos em compilacao, bounds impossiveis sao erro semantico.
- Quando bounds dependem de valor runtime, o resolver registra `Checagem Diferida` de bounds.
- Indices de colecao e bounds de slice exigem `NUMBER` `RuntimeNullability.NEVER_NULL`.
- Indice/bound com fato numerico `FRACTIONAL_KNOWN`, como `v[1.5]`, e erro semantico.
- Indice/bound dinamico com fato numerico desconhecido gera `Checagem Diferida` de integralidade.
- Literal de colecao com elementos deve unificar os elementos para tipo conhecido.
- `[1, 2, 3]` resolve como `Tipo Colecao<NUMBER>`.
- `[1, "x"]` e erro semantico.
- `[null]` nao existe porque `null` nao e literal de fonte.
- `[]` e valido apenas quando contexto fornece tipo de elemento conhecido.
- `[]` isolado como expressao final e erro semantico.
- `x := []` e erro semantico se nao houver tipo esperado para `x`.
- `asCollection([])` e erro se `asCollection` nao define elemento.
- Colecoes aninhadas unificam recursivamente quando os elementos sao conhecidos.
- `Tipo Colecao` representa todos os valores sequenciais da linguagem, independentemente da origem.
- `Tipo Mapa` e text-keyed.
- Acesso a mapa por propriedade nao e permitido.
- `m["key"]` e a forma de acessar valor de mapa.
- `m.key` e invalido quando `m` e `Tipo Mapa`.
- Mapa vazio default e valido apenas com `MapType<T>` declarado.

## Funcoes Globais

- Funcoes globais customizadas continuam permitidas publicamente por `FunctionCatalog` e `ReflectedFunctionImporter`.
- Essa extensibilidade nao implica liberar operacoes de colecao customizadas publicamente na v2 inicial.
- Funcoes pertencem a uma instancia de ambiente e nao exigem identidade canonica para compartilhamento entre ambientes.
- Descriptors de funcao preservam nome de linguagem, assinatura de parametros, tipo de retorno, flags `pure`/`foldable`, handle e metadados descritivos da implementacao.
- Metadados refletidos preservam classe, nome e descriptor JVM para diagnostico e auditoria, sem `stableImplementationId` separado.
- Metodos de instancia sao vinculados diretamente ao provider fornecido, sem exigir `providerId`.
- Alteracoes de provider exigem construir outro ambiente, que recebe outro identificador de instancia.
- Chamada global resolve por `FunctionCatalog`.
- `Vinculo de Funcao` deve carregar descriptor, assinatura escolhida, pureza, dobrabilidade e metadados necessarios.
- Overload deve ser deterministico em compile-time.
- `Assinatura de Funcao` e formada por nome e tipos/contratos de parametros; tipo de retorno nao participa da identidade de overload.
- Duas funcoes com mesmo nome e mesmos parametros, mas retorno diferente, sao conflito de catalogo e devem ser rejeitadas no builder/importador.
- Contexto de retorno pode ajudar a escolher entre assinaturas com parametros diferentes, mas nunca entre assinaturas identicas que diferem apenas no retorno.
- Se exatamente uma assinatura e viavel, ela e escolhida.
- Se mais de uma assinatura continua viavel, erro de overload ambiguo.
- Se nenhuma assinatura e viavel, erro semantico.
- Contexto de retorno pode ajudar a desambiguar overload por inferencia bidirecional simples.
- Nao ha runtime overload resolution.
- Funcoes nao aceitam argumento null.
- Coercao de borda nao se aplica implicitamente entre valores internos concretos.
- Resolucao de overload comum nao usa coercao de borda para escolher assinatura.
- `sqrt("1")` e erro semantico.
- Coercao de borda vale para defaults externos, overrides externos, funcoes `as*` e conversao de resultado da API.
- `asNumber("1")` e borda explicita de coercao.
- Constantes em funcoes `as*` devem ser validadas semanticamente quando possivel.
- `asNumber("abc")` pode ser erro semantico se o perfil nao permite converter.
- `asNumber(x)` quando `x` ja e `NUMBER` deve ser marcado como assercao redundante para Etapa 7 elidir.
- Funcoes `as*` refinam apenas o resultado da chamada, nao alteram o tipo global nem o binding do simbolo passado como argumento.
- Se o usuario quiser reutilizar um valor convertido, deve atribuir explicitamente, por exemplo `n := asNumber(x); n + 2`.
- `asCollection(x)` generico nao deve existir como built-in na Etapa 4.
- Assercoes de colecao devem ser explicitas quanto ao elemento, por exemplo `asCollectionOfNumber(x)`, ou declaradas por assinatura concreta equivalente.
- `f([])` pode tipar `[]` pelo parametro se houver assinatura unica, como `f(Collection<NUMBER>)`.
- `f([])` e ambiguo se houver overloads como `f(Collection<NUMBER>)` e `f(Collection<STRING>)`.
- `FunctionCatalog` deve validar que toda `Funcao Dobravel` e `Funcao Pura`.
- Etapa 4 nao executa funcoes dobraveis; apenas marca.
- Funcoes impuras sao semanticamente validas, mas bloqueiam folding, CSE e reordenacao posterior.
- Funcoes impuras podem aparecer em ramos lazy, operandos posteriores de `??`, condicionais e lambdas; a politica de avaliacao decide se executam em runtime.
- Etapa 4 valida semanticamente chamadas impuras mesmo em ramos que uma otimizacao futura poderia considerar inalcancaveis.
- Pureza e metadado para planejamento, auditoria e otimizacao, nao criterio de aceitacao da linguagem.

## Nomes Reservados e Namespaces

- `currDate`, `currTime` e `currDateTime` sao nomes reservados para valores temporais correntes.
- `currDate`, `currTime` e `currDateTime` devem ser representados como `CurrentTemporalValueNode` na AST, nao como `IdentifierNode` resolvido especialmente.
- Simbolo externo com nome reservado deve ser proibido no builder do ambiente.
- Atribuicao a nome reservado deve ser proibida no resolver.
- `currDate` sem parenteses resolve como `Valor Temporal Corrente`.
- `currDate()` e chamada de funcao global e so seria valida se tal funcao existisse, mas o builder deve proibir funcao com nome reservado.
- `sqrt` sem parenteses e simbolo, nao funcao de primeira classe.
- A linguagem nao tem funcoes como valores na Etapa 4.
- Nomes de funcoes globais e nomes de simbolos ficam em namespaces separados.
- `sqrt := 10` e permitido.
- `sqrt(4)` continua resolvendo para funcao global.
- Proibir apenas nomes reservados como simbolos.

## Valores Temporais

- `currDate` resolve para `DATE`.
- `currTime` resolve para `TIME`.
- `currDateTime` resolve para `DATETIME`.
- `currDate`, `currTime` e `currDateTime` sao `RuntimeNullability.NEVER_NULL`.
- Valores temporais correntes sao dinamicos, derivados do clock de execucao.
- Valores temporais correntes nao sao constantes dobraveis.
- Operadores temporais como `DATE + NUMBER` nao devem existir.
- Operacoes temporais ficam em funcoes built-in explicitas.
- Comparacoes temporais sao estritas por tipo: `DATE` com `DATE`, `TIME` com `TIME`, `DATETIME` com `DATETIME`.
- `DATETIME` sem offset usa offset efetivo do `ZoneId` do ambiente.
- Em gaps/overlaps de DST, usar politica padrao de `LocalDateTime.atZone(zone)`.
- Metadata temporal deve registrar se houve offset explicito ou inferido, `ZoneId`, offset efetivo, valor normalizado e possivelmente gap/overlap.
- Comparacao de `DATETIME` usa o `LocalDateTime` normalizado no ambiente.

## Regex

- Operadores regex exigem lado esquerdo `STRING`.
- Operadores regex exigem lado esquerdo `RuntimeNullability.NEVER_NULL`.
- Regex dinamica nao e aceita nesta versao.
- O padrao do lado direito deve ser literal string direto na Etapa 4.
- Expressao constante dobravel de string, como concatenacao de literais, nao e aceita como regex na Etapa 4 porque folding pertence a etapa posterior.
- O resolver deve validar e pre-compilar `Pattern` como `Valor Semantico Preparado`.
- Regex invalida nunca passa da compilacao.

## Navegacao

- `Vinculo de Navegacao` deve ser produzido na Etapa 4 para cada elo de `Cadeia de Navegacao`.
- A Etapa 4 resolve semanticamente toda navegacao, filtros, lambdas e operacoes de colecao mesmo quando a execucao concreta fica para a Etapa 6.
- Etapa 6 executa navegacao consumindo os bindings do `Modelo Semantico`; nao decide semantica nova nem redescobre membros, tipos, filtros ou operacoes.
- `navigationBindings` e por link, nao apenas pela cadeia inteira.
- Propriedade em `Tipo Objeto` conhecido exige membro registrado.
- Metodo em `Tipo Objeto` conhecido exige metodo registrado.
- Nao ha fallback reflexivo para objeto conhecido.
- Fallback reflexivo generico por tipo desconhecido nao existe, porque tipos devem ser conhecidos.
- `Tipo Objeto` nominal sem membros registrados nao permite navegacao de propriedade/metodo.
- `ObjectType` nominal pode participar de navegacao, retorno/passagem de funcao e atribuicoes, mas nao de operadores comuns diretamente.
- `ObjectType` pode ser atribuido a `Simbolo Interno` como valor intermediario para navegacao posterior ou passagem para funcao.
- Expressao de resultado final com tipo `ObjectType` e erro semantico.
- Views publicas de atribuicao nao devem expor valores finais `ObjectType` por padrao; se necessario no futuro, isso exige contrato/API explicito.
- `Collection<ObjectType>` e `Map<ObjectType>` tambem nao podem ser resultado final nem valor exposto por views publicas de atribuicao.
- Containers com `ObjectType` podem existir como intermediarios para filtro, `map`, navegacao explicita e funcoes.
- `Map<ObjectType>` pode ser simbolo externo/intermediario; `map["id"].property` e `map.values().map(@ -> @.property)` sao usos validos quando os membros existem.
- `map[?(@.v.active)]` preserva `Map<ObjectType>` e precisa de projecao antes da borda publica.
- A regra de nao expor `ObjectType` na borda publica e recursiva: qualquer tipo que contenha `ObjectType` em qualquer profundidade nao e exponivel.
- Escalares e containers compostos apenas por escalares ou containers exponiveis podem ser exponiveis.
- Resultado final `Collection` ou `Map` e semanticamente valido quando e `NEVER_NULL` e publicamente exponivel.
- Resultado final `Collection<T>` publicamente exponivel deve ser marcado para materializacao na borda publica; a API nao deve expor `Iterable` Java cru.
- Etapa 5 aplica `Limite de Materializacao` em toda Materializacao Publica; Etapa 6 aplica o limite nas materializacoes introduzidas por navegacao e operacoes de colecao.
- Resultado final `Map<V>` publicamente exponivel deve ser materializado como mapa imutavel com chaves `String`.
- Conversao de borda de mapas e recursiva sobre valores; chave null e valor null nao sao permitidos.
- `Limite de Materializacao` deve contar entradas de mapa materializadas e elementos de colecao.
- `asMath()` exige resultado escalar `NUMBER`.
- `asLogical()` exige resultado escalar `BOOLEAN`.
- `asAssignments()` pode expor simbolos internos com tipos publicamente exponiveis.
- `asAssignments()` deve rejeitar a view se algum simbolo a ser exposto tiver tipo nao exponivel; nao deve omitir simbolos silenciosamente.
- O modelo pode carregar metadata/helper de tipo exponivel publicamente para validar expressao de resultado e views de atribuicao.
- O usuario deve projetar objetos para valores de linguagem antes da borda publica, por exemplo `customers.map(@ -> @.name)`.
- Metodo de objeto e `Vinculo de Navegacao`, nao `Vinculo de Funcao`.
- `customer.fullName()` pertence ao Java type/catalogo de navegacao do receptor.
- `customer?.fullName()` segue politica de navegacao segura.
- Navegacao segura protege apenas receiver null daquele link.
- Navegacao segura nao mascara membro invalido, tipo invalido, indice invalido ou erro de predicado.
- Link de navegacao nao seguro exige receiver `RuntimeNullability.NEVER_NULL`.
- Se o receiver de propriedade, metodo, subscript, filtro, wildcard ou operacao de colecao for `MAY_BE_NULL`, o link deve usar navegacao segura ou o receiver deve receber fallback explicito com `??`.
- Navegacao segura e semanticamente valida quando o link correspondente sem `?.` seria valido.
- Qualquer link com navegacao segura produz resultado `RuntimeNullability.MAY_BE_NULL`, mesmo quando o receiver e `NEVER_NULL`.
- `?.` redundante e permitido sem warning na Etapa 4; otimizacao posterior pode elidir a checagem se provar equivalencia.
- `customer?.age` resolve como `NUMBER` com metadata de nulidade `MAY_BE_NULL`.
- `customer?.age ?? 0` resolve como `NUMBER` com `RuntimeNullability.NEVER_NULL`.
- `customer?.address.city` e erro semantico se `customer?.address` for `MAY_BE_NULL` e `.city` nao for seguro.
- `customer?.address?.city` e semanticamente valido como expressao `MAY_BE_NULL`, mas nao pode ser atribuido sem fallback `??`.
- `customer?.age + 1` e erro semantico; o usuario usa `(customer?.age ?? 0) + 1` para protecao.
- `customer.age` continua `NEVER_NULL` por contrato quando `customer` e `NEVER_NULL`.
- O usuario usa `(customer?.age ?? 0) + 1` para protecao.
- Mapas nao aceitam acesso por propriedade: `m.key` e invalido para `Tipo Mapa`.
- Acesso a valor de mapa usa subscript textual: `m["key"]`.
- `m["key"]` retorna `V` com `RuntimeNullability.NEVER_NULL` quando executa com sucesso.
- Chave ausente ou valor null em `m["key"]` e erro runtime do link, nao valor null normal.
- `m?.["key"]` protege apenas receiver null; nao protege chave ausente nem valor null.
- Subscript textual `receiver["key"]` e valido apenas em `Tipo Mapa<V>`.
- `Tipo Objeto["key"]` e erro semantico; objetos usam apenas membros registrados por propriedade/metodo.
- A Etapa 4 nao tem acesso dinamico a membro de objeto por string.
- Nao ha projecao implicita de propriedade ou chamada sobre `Tipo Colecao<T>`.
- Propriedade e metodo navegam sobre `Tipo Objeto`, nao sobre colecao de objetos.
- Depois de `pessoa.endereco[?(@.principal)]`, o resultado continua sendo colecao de `Endereco`; `.ativo` aplicado diretamente sobre essa colecao e erro semantico.
- Para projetar membros de itens, o usuario deve usar operacao explicita, por exemplo `.map(@ -> @.ativo)`.
- Comparacao entre colecao projetada e escalar continua invalida; quantificacao deve ser explicita com operacoes como `any`/`all` quando disponiveis no catalogo.

## Wildcards

- `[*]` e o unico curinga e resulta `Tipo Colecao<T>` para colecao, `Tipo Colecao<V>` para mapa ou a colecao homogenea de filhos explicitamente registrados de um objeto.
- `?.[*]` e a unica forma segura do curinga.
- O curinga nao reflete automaticamente todos os membros publicos.
- Wildcard produz colecao de valores e nao preserva chaves de mapa.
- Para chaves de mapa, usar `.keys()`.

## Filtros, Lambdas e Item Atual

- Filtro e valido em `Tipo Colecao<T>` e `Tipo Mapa<V>`.
- Filtro exige receiver `RuntimeNullability.NEVER_NULL` e predicado `BOOLEAN` `RuntimeNullability.NEVER_NULL`.
- Resultado de filtro e `RuntimeNullability.NEVER_NULL` quando executa com sucesso.
- Filtro em colecao preserva `Tipo Colecao<T>`.
- Filtro em mapa preserva `Tipo Mapa<V>`.
- Em filtro de colecao, `@` tem tipo do elemento `T` e `RuntimeNullability.NEVER_NULL`.
- Se uma colecao externa contiver elemento null em runtime, filtro, `map`, `any`, `all` ou `sum` falham com diagnostico runtime ao encontrar o elemento.
- `@?.property` e permitido e produz `MAY_BE_NULL`, mas e defensivo/redundante quando `@` vem de colecao bem formada; a Etapa 4 nao emite warning por isso.
- Em filtro de mapa, `@` tem tipo contextual `Entrada de Mapa<V>`.
- `@.k` em filtro de mapa e `STRING`.
- `@.v` em filtro de mapa e `V`.
- `@.v.xyz` e valido se `V` for objeto com membro registrado `xyz`.
- `@` fora de filtro/lambda e erro semantico.
- Em filtros aninhados, `@` sempre aponta para o contexto mais interno.
- A v2 nao tem sintaxe para acessar item atual externo.
- Filtro/lambda aninhado que substitui visualmente o `@` externo pelo interno nao gera warning.
- `maxCurrentItemDepth` conta filtros e lambdas que introduzem `Item Atual`.
- `maxCurrentItemDepth` conta profundidade simultanea/aninhada de contextos de `Item Atual`, nao quantidade total de filtros/lambdas na expressao.
- Pipelines sequenciais nao somam profundidade depois que o lambda/filtro anterior saiu de escopo.
- O limite e validado semanticamente na Etapa 4.
- Diagnostico por excesso de profundidade aponta para o filtro/lambda que tentaria entrar no nivel proibido.
- Chamadas navegadas permanecem nao classificadas na AST; operacoes de colecao com lambda, como `.map(@ -> e)`, sao classificadas e resolvidas na Etapa 4 conforme o tipo do receptor.
- Lambda introduz `Item Atual` com tipo vindo do descriptor da operacao de colecao.
- `LambdaNode` so e valido como argumento sintatico direto de operacao de colecao cujo descriptor declara lambda.
- Funcao global nao recebe lambda na v2; `f(@ -> @.name)` e invalido.
- Lambda nao e valor de expressao, nao pode ser atribuida, retornada ou passada fora de operacao de colecao.
- Descriptor de operacao de colecao define quais argumentos sao lambdas e quais sao posicionais.
- Argumento posicional de operacao de colecao nao introduz novo `Item Atual`.
- `@` em argumento posicional resolve apenas se ja existir contexto externo; caso contrario e erro semantico.
- `Collection<T>.map(@ -> R)` retorna `Collection<R>`.
- `Map<V>.map(@ -> R)` retorna `Collection<R>`, porque nao preserva chaves.
- Corpo de lambda de `map` deve produzir `RuntimeNullability.NEVER_NULL`; `map` nao pode criar container com elementos null.
- `.any(@ -> predicate)` e `.all(@ -> predicate)` devem ser incluidos no `Catalogo de Operacoes de Colecao` da Etapa 4; retornam `BOOLEAN` e exigem predicado `BOOLEAN`.
- Predicado de `any`/`all` deve produzir `RuntimeNullability.NEVER_NULL`.
- `.any` curto-circuita no primeiro `true`; `.all` curto-circuita no primeiro `false`.
- Em colecao vazia, `.any` retorna `false` e `.all` retorna `true`.
- Versoes sem lambda, como `booleans.any()`, ficam fora da Etapa 4 para reduzir superficie inicial.
- `Collection<NUMBER>.sum()` retorna `NUMBER` `RuntimeNullability.NEVER_NULL`; sobre colecao vazia, retorna zero decimal.
- Elemento null encontrado durante `sum()` e erro runtime.
- `Collection<T>.count()` e `Map<V>.count()` retornam `NUMBER` `RuntimeNullability.NEVER_NULL` com `Fato Numerico.INTEGRAL_KNOWN`.
- `map.count()` conta entradas do mapa.
- `count()` nao introduz tipo publico `INTEGER`.
- `count()` em `Collection<T>` pode exigir iteracao runtime, mas nao materializa elementos em novo container.
- `map()` materializa resultado e deve carregar metadata de `Limite de Materializacao`.
- `any()` e `all()` nao materializam resultado e devem carregar `Politica de Avaliacao.LAZY_PER_ELEMENT`.
- Pureza efetiva de `count`, `keys`, `values`, `sum`, `any`, `all` e `map` deve considerar receiver, lambda/corpo e descriptor; nao e apenas flag fixa do descriptor.
- O binding de operacao de colecao carrega descriptor escolhido, tipos de argumento/lambda, pureza e materializacao.
- Safe navigation sobre `@`, como `@?.active`, deve ser permitida.

## Operacoes de Colecao em Mapas

- `map.keys()` retorna `Tipo Colecao<STRING>` e `map.values()` retorna `Tipo Colecao<V>`.
- `map.keys()` e `map.values()` exigem receiver `Map<V>` `RuntimeNullability.NEVER_NULL` e retornam `RuntimeNullability.NEVER_NULL`.
- Se `V` contem `ObjectType`, `map.values()` e valido como intermediario, mas nao como resultado final publicamente exponivel.
- Valor null encontrado em mapa durante `values()` e erro runtime, nao elemento `MAY_BE_NULL`.
- `map.map(@ -> e)` e valido com `@` como `Entrada de Mapa<V>` e retorna `Tipo Colecao<R>` sem preservar chaves.
- `map.any(@ -> predicate)` e `map.all(@ -> predicate)` sao validos com `@` como `Entrada de Mapa<V>` e retornam `BOOLEAN`.
- `map.count()` retorna quantidade de entradas como `NUMBER` integral.
- `map.sum()` nao soma valores implicitamente; para somar valores, usar `map.values().sum()`.
- Filtro sintatico `map[?(...)]` cobre o caso de filtrar preservando mapa.
- Nao e necessario criar uma operacao separada `map.filter(...)` para preservar mapa nesta etapa.

## JavaTypeCatalog e Metadados Java

- `Tipo Java Registrado` deve declarar membros navegaveis por uma politica de exposicao do ambiente.
- Membro nao registrado e erro semantico.
- Membro navegavel registrado sem tipo de retorno mapeavel para tipo de expressao conhecido deve ser rejeitado no registro do catalogo.
- Ambientes nao devem conter tipos Java "meio validos" com membros expostos que so falhariam por tipo desconhecido quando usados.
- Se a politica de exposicao for ampla, ela deve filtrar ou rejeitar membros nao mapeaveis de forma deterministica, sem criar membro com tipo desconhecido.
- O resolver diagnostica membro inexistente, nao exposto ou uso incompativel; nao deve diagnosticar membro registrado com tipo desconhecido.
- Metodos Java registrados devem carregar metadado de pureza/efeitos.
- Por padrao, metodos Java registrados devem ser considerados impuros, salvo marcacao explicita.
- Retornos de propriedades e metodos Java registrados devem ser tratados como `NEVER_NULL` por contrato da exposicao.
- Se um membro Java registrado retornar null em runtime, isso e violacao do contrato de borda/runtime, nao um caso normal de `Nulidade de Runtime.MAY_BE_NULL`.
- A Etapa 4 nao modela nulidade de retorno Java por membro; `MAY_BE_NULL` em navegacao vem de `?.`.

## Limites e Materializacao

- `maxMaterializedSize` deve ser validado na Etapa 4 apenas para materializacoes estaticamente conhecidas.
- Literal de colecao maior que `maxMaterializedSize` e erro semantico.
- `maxMaterializedSize` limita literal de colecao, resultados materializados de operacoes de colecao, entradas de mapa materializado e materializacao de colecao na borda publica.
- `maxMaterializedSize` pertence ao ambiente e afeta aceitacao/execucao, sem ser serializado no identificador de instancia.
- Filtros, `map`, `values`, wildcard e operacoes com tamanho runtime carregam metadata de materializacao.
- Etapa 5 aplica limites em resultados dinamicos de funcoes e na Materializacao Publica; Etapa 6 aplica limites nas materializacoes dinamicas introduzidas por navegacao e operacoes de colecao.
- Grandes colecoes externas nao devem ser rejeitadas semanticamente apenas por tamanho.

## Compilacao, Views e Runtime

- `ExpressionCompilationResult` deve ser orientado a resultado, nao excecao como fluxo esperado.
- Sucesso de compilacao deve conter `CompiledExpression` e warnings.
- Falha de compilacao deve conter errors e warnings.
- `compileOrThrow` pode existir depois como conveniencia, mas nao deve ser o contrato primario.
- `asAssignments()` e valido quando ha apenas warnings.
- `asAssignments()` expõe valores finais de simbolos internos, incluindo reatribuicoes.
- `asMath()` e `asLogical()` devem rejeitar arquivo sem result expression na validacao de view da Etapa 5.
- Runtime errors devem usar diagnosticos com `SourceSpan` e `DiagnosticCode`, categoria `RUNTIME`.

## Testes e Corpus

- Criar suites por eixo semantico, nao um megateste.
- Eixos de teste: literais e valores preparados, simbolos externos, simbolos internos, restricoes, operadores, funcoes, overload, condicionais, coalescencia, navegacao, filtros/lambdas, desestruturacao, frame layout, diagnosticos e corpus gate.
- Cada regra positiva e negativa deve ter teste com codigo de diagnostico e span.
- Corpus deve aceitar `phase: semantic` para casos que parseiam/constroem AST mas falham no resolver.
- Casos validos de corpus podem declarar `expectedType`, warnings esperados e tags de cobertura.
- Casos invalidos de corpus devem declarar `expectedDiagnostic` com codigo, span e spans relacionados opcionais.
- Detalhes internos como slots de frame nao devem ir para corpus geral; devem ficar em testes unitarios do resolver.

## Criterios de Aceite da Etapa 4

- `SemanticResolver.resolve(ast, environment)` retorna `SemanticResolutionSuccess` apenas sem erros e com `SemanticModel` planejavel.
- Suites de teste por eixo: simbolos, atribuicoes, desestruturacao, operadores, nulidade, funcoes/overload, navegacao, filtros/lambdas, operacoes de colecao, temporais, regex, frame layout, diagnosticos e corpus gate.
- Cada regra negativa relevante testa codigo diagnostico e span.
- `@` fora de contexto de `Item Atual` e erro semantico.
- `MAY_BE_NULL` escapando para resultado, atribuicao, operador, funcao, predicado ou navegacao nao segura e erro semantico com sugestao de `??` quando aplicavel.
- Regex invalida ou lado direito de regex que nao seja literal string direto falha na compilacao.
- Arquivo de expressao vazio falha semanticamente.
- Corpus inteiro resolve sem issues inesperadas e inclui casos `phase: semantic`.
- `SemanticModel` de sucesso nao contem placeholders, tipo invalido, tipo desconhecido nem binding ausente.
- `Layout de Frame` e estavel e testado sem depender de ordem de `HashMap`.
- Etapa 4 nao implementa folding, CSE, reordenacao ou outras otimizacoes de plano.

## Decisoes Ainda Pendentes

- Nenhuma pendencia aberta no momento.
