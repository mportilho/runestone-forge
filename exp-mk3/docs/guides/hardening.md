# Guia de hardening

## Modelo de ameaça

Uma Fonte de Expressão Não Confiável é texto fornecido por tenant. Em `SAFE`, também trate overrides,
containers, mapas, resultados de providers e resultados públicos como valores potencialmente hostis.
O avaliador contém trabalho controlado pela linguagem; **não é sandbox de Java**. Providers, accessors,
`Clock`, conversores e código do integrador são confiáveis e podem fazer I/O, bloquear ou consumir CPU.

## Escolha do modo

- Use `SAFE` quando fonte **ou valores** forem controlados por tenant. É o padrão recomendado em
  fronteiras multi-tenant.
- Use `TRUSTED` quando a fonte for controlada/deployada pela aplicação e a contenção de runtime não for
  necessária. É o default por preservar limites de compilação com overhead próximo de zero na execução.
- Use `UNSAFE` apenas quando fonte e valores forem internos e a aplicação já impuser contenção completa.
  Ele remove limites de recurso da expressão, não tipagem, nulidade, domínio numérico, snapshots
  imutáveis, cache limitado ou limpeza do parser.

## Escopo de enforcement

| Fronteira | `UNSAFE` | `TRUSTED` | `SAFE` |
|---|---:|---:|---:|
| fonte, tokens, sintaxe e nós AST | — | ✓ | ✓ |
| constantes/folding e forma de defaults | — | ✓ | ✓ |
| overrides, retorno de provider e resultado público | — | — | ✓ |
| materialização e passos de percurso durante execução | — | — | ✓ |
| contratos funcionais e cache limitado | ✓ | ✓ | ✓ |

Em `TRUSTED` e `SAFE`, funções/operações cujo trabalho depende da entrada não são executadas durante
folding. Esgotamento de orçamento é terminal na fase e produz diagnóstico estruturado.

## Defaults e tetos absolutos

Zero significa capacidade zero. Os tetos protegem a JVM de configuração acidental e não devem ser
adotados como defaults.

| Propriedade | Default | Teto inclusivo | O que limita |
|---|---:|---:|---|
| `maxSourceLength` | 16.384 | 262.144 | unidades UTF-16 da fonte |
| `maxTokenCount` | 4.096 | 65.536 | tokens produzidos |
| `maxSyntaxDepth` | 64 | 256 | profundidade estrutural projetada |
| `maxAstNodeCount` | 4.096 | 65.536 | nós semânticos |
| `maxCurrentItemDepth` | 32 | 64 | filtros/lambdas `@` simultâneos |
| `maxMaterializedSize` | 10.000 | 100.000 | itens/entradas de um snapshot |
| `maxFactorialInput` | 1.000 | 10.000 | operando de fatorial |
| `maxTextLength` | 1.048.576 | 8.388.608 | texto materializado |
| `maxValueDepth` | 64 | 256 | profundidade recursiva de valores |
| `maxNumericPrecision` | 10.000 | 100.000 | dígitos significativos |
| `maxNumericScaleMagnitude` | 10.000 | 100.000 | magnitude da escala decimal |
| `maxRegexPatternLength` | 1.024 | 8.192 | unidades UTF-16 do padrão RE2/J |
| `maxTraversalSteps` | 1.000.000 | 100.000.000 | visitas cumulativas em runtime `SAFE` |

Comece abaixo dos defaults quando o domínio permitir, meça rejeições e aumente uma dimensão por vez.
Não use o teto como substituto de análise de capacidade.

## Margem de percurso

Um passo é cada item/entrada alcançado por iteração, conversão/validação recursiva ou materialização
controlada pelo evaluator. Percursos aninhados compartilham o saldo da execução; operações lazy debitam
somente o que alcançam. Otimizações podem eliminar visitas sem mudar o resultado, portanto não configure
o limite exatamente no número observado. Reserve margem para forma dos dados, materialização pública e
mudanças equivalentes de plano. O limite não estima tempo, CPU, invocações, complexidade ou custo de
provider.

## Providers confiáveis e deadline externo

Revise providers como código da aplicação: limite I/O, configure timeouts, evite locks globais, torne
instâncias thread-safe e valide custo máximo. Declare pureza honestamente; nunca exponha scanner amplo de
classes não revisadas. Como o evaluator não interrompe provider nem mede tempo, execute a operação sob
deadline/cancelamento do serviço (HTTP timeout, future estruturado, bulkhead etc.). Deadline externo
complementa `SAFE`; não substitui os limites determinísticos e pode não interromper código Java que
ignora cancelamento.

Regex Linear impede backtracking catastrófico, mas padrões e entradas continuam sujeitos a limites de
texto e ao deadline da requisição.

## Cache

O cache pertence a cada Engine. Defaults: 1.024 resultados, 64 Mi unidades conservadoras de Peso Retido
de Compilação e sem expiração. Configure `maximumEntries`, `maximumRetainedWeight` e, se necessário,
`expireAfterAccess`. O peso não é byte exato. Preserve ambos os limites: quantidade protege contra muitas
fontes pequenas e peso contra poucas fontes grandes. Não crie Engine por requisição; faça quota/rate
limit por tenant fora do evaluator, pois a chave usa fonte exata e `environmentId`.

## Operação e observabilidade

Registre código, categoria, severidade e span de diagnósticos; não use mensagem como chave. Separe falha
de compilação de `ExpressionExecutionException`, conte rejeições por código/modo e alerte sobre pressão
de deadline/provider. Não registre fonte, overrides ou Memória de Cálculo sem política de dados.

Antes de publicar mudanças em limites ou providers, execute `mvn -pl exp-mk3 -am test`; para mudanças de
contenção, execute também o perfil `stage12-stress` e o script `scripts/run-stage12-gates.sh` no Temurin
21 de referência.
