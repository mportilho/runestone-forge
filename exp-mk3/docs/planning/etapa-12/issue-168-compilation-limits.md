# Issue #168 — limites estruturais de compilação

`TRUSTED` e `SAFE` aplicam os quatro limites antes da fase seguinte. `UNSAFE`
preserva o caminho sem enforcement estrutural.

| Dimensão | Contagem | Diagnóstico terminal |
| --- | --- | --- |
| Fonte | Unidades UTF-16, antes de chave/cache e novamente no pipeline | `COMPILE_SOURCE_LENGTH_EXCEEDED` |
| Tokens | Todos os tokens armazenados, incluindo whitespace oculto; exclui EOF e comentários descartados pelo lexer | `COMPILE_TOKEN_COUNT_EXCEEDED` |
| Profundidade | Projeção iterativa conservadora antes de SLL/LL | `COMPILE_SYNTAX_DEPTH_EXCEEDED` |
| AST | Todos os nós com identidade, incluindo arquivo, alvos, branches, lambdas e links | `COMPILE_AST_NODE_COUNT_EXCEEDED` |

Fonte excessiva não é admitida no cache; seu diagnóstico aponta para a inserção
no início da fonte, sem percorrer ou reter o texto. Tokens e profundidade apontam
para o token que excede o limite. A construção da AST reserva cada identidade
antes da alocação do nó e verifica novamente o total durante a atribuição de IDs.
Esgotamento descarta diagnósticos anteriores da fase e devolve somente o terminal.

A projeção usa profundidade um para folhas e combina o máximo dos irmãos com
os operadores do segmento. Soma operadores sem reproduzir a precedência; pode,
portanto, rejeitar conservadoramente expressões mistas antes da profundidade
exata da AST. Grupos, coleções, chamadas, filtros e lambdas participam da conta.
Condicionais incluem o nível de `ConditionalBranchNode`; a sintaxe funcional é
reconhecida pelo separador de argumentos dentro dos parênteses. Uma condição
clássica pode continuar após um grupo, como `if (true) and true then …`.
Links sequenciais, coalesce variádico e sequências postfix contam uma camada,
enquanto prefixos e operações binárias left-deep/right-deep acumulam níveis.

O probe JOL de 10/100/1.000 nós usa agora somas balanceadas: mantém a quantidade
exata de nós executáveis e cabe nos defaults sem depender de cadeias left-deep.

## Verificação reproduzível

```sh
mvn -pl exp-mk3 -am test
mvn -pl exp-mk3 -am -Pcompilation-resource-stress \
  -Dtest=CompilationResourceStress -Dsurefire.failIfNoSpecifiedTests=false test
```

O stress lança JVM filha com `-Xms512m -Xmx512m -Xss1m`, timeout de 90 segundos e
saída em `exp-mk3/target/stage12/compilation-resource-stress.log`. Exercita os
tetos de fonte/tokens, profundidade 255/256/257 nas famílias recursivas, rejeição
de condicionais adversariais e construção de AST com 65.535/65.536/65.537 nós.
O teste isolado de AST usa o parser interno sem limite de tokens para alcançar
essa dimensão independentemente do teto de tokens. Os tetos originais foram
preservados após execução em Java 21.0.8+9-LTS.
