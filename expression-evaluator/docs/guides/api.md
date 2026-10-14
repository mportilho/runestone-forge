# Guia da API Java

## Engine e Ambiente

Mantenha uma `ExpressionEngine` por fronteira de aplicação. Ela possui `Clock`, cache de compilação e
serviços de runtime. `defaultEngine()` é singleton lazy; `builder()` cria uma Engine isolada. A chave do
cache é `(fonte exata, environmentId)`, portanto reutilize a mesma instância de `ExpressionEnvironment`
para compartilhar compilações. Engines e ambientes distintos não compartilham entradas.

O `ExpressionEnvironment` é um snapshot imutável de tipos, símbolos, funções, coerção, semântica
decimal, fuso, modo e limites. `standard()` é suficiente para a linguagem embutida. Cada `build()` gera
um `environmentId` opaco novo; ele não é hash de conteúdo nem identificador persistente.

```java
ExpressionEnvironment environment = ExpressionEnvironment.builder()
        .zoneId(ZoneId.of("America/Sao_Paulo"))
        .mathContext(MathContext.DECIMAL128)
        .trustMode(ExpressionTrustMode.SAFE)
        .externalSymbol("amount", BigDecimal.ZERO, ExternalSymbolOverwritePolicy.OVERRIDABLE)
        .build();
ExpressionEngine engine = ExpressionEngine.builder()
        .clock(Clock.systemUTC())
        .build();
```

Builders são mutáveis; objetos construídos são imutáveis.

## Compilação e diagnósticos

`compile(source, environment)` retorna `ExpressionCompilationResult.Success` ou `Failure`. Trate toda a
lista de diagnósticos em ordem; códigos/categoria/severidade/span são o contrato estável.
`compileOrThrow` lança `ExpressionCompilationException` em falha e é apropriado quando a fonte faz parte
da configuração da aplicação. Falhas de execução lançam `ExpressionExecutionException`.

Sucessos e falhas determinísticas residentes são single-flight e cacheados. Uma entrada pesada demais é
entregue, mas não fica residente. `compute` nunca consulta o cache.

## Modos e limites

| Modo | Limites de compilação | Limites de runtime | Uso típico |
|---|---|---|---|
| `UNSAFE` | não | não | fonte e valores internos, contenção externa completa |
| `TRUSTED` (padrão) | sim | não | fonte controlada, runtime de baixo overhead |
| `SAFE` | sim | sim | fonte ou valores de tenant |

`ExpressionResourceLimits` reúne os 13 limites. Zero significa capacidade zero, não ilimitado. O builder
valida os tetos em todos os modos. Veja defaults, tetos e tuning no [guia de hardening](hardening.md).
`CacheConfig` é separado porque limita o ciclo de vida da Engine, não a interpretação da expressão.

## Visões e execução

Uma `CompiledExpression` não executa diretamente. Selecione uma visão sem recompilar:

- `asResult()` expõe qualquer resultado público materializável;
- `asMath()` exige `NUMBER` e retorna `BigDecimal`;
- `asLogical()` exige `BOOLEAN` e retorna `boolean`;
- `asAssignments()` executa somente atribuições e retorna mapa imutável na ordem de criação.

Cada visão oferece `compute()`/`compute(overrides)` e `computeWithMemory(...)`. Overrides desconhecidos,
não sobrescrevíveis, nulos ou incoercíveis falham antes de efeitos. Resultados públicos são snapshots
recursivos, imutáveis e sem null/objeto não exponível.

<!-- runestone-example: NUMBER=42 -->
```runestone
answer := 6 * 7;
answer
```

## Concorrência

Engine, Ambiente, expressão compilada e visões podem ser compartilhados por threads de plataforma ou
virtuais. Cada chamada cria Escopo de Execução, frame, saldo `SAFE`, instante corrente e Memória de
Cálculo isolados. Providers vinculados por instância precisam ser thread-safe para uso concorrente.
Compilação em alto volume pode preferir pool de plataforma para reaproveitar o parser `ThreadLocal`;
isso é recomendação de desempenho, não requisito de correção.

## Providers e tipos Java

Providers são Java confiável, importado no build do Ambiente. Métodos elegíveis são resolvidos e
adaptados uma vez; não há reflexão no caminho de execução. Declare `FunctionPurity` corretamente:
função `PURE` promete mesmo resultado para mesmos argumentos; função dobrável também precisa ser pura.
Instâncias permanecem vinculadas ao Ambiente e seus métodos devem cumprir não-null, tipo, forma de
container, lifetime e concorrência.

Use `functions(ReflectedFunctionImporter...)`/`functionsFrom(...)` para funções e
`registerJavaType...` para navegação nominal. Registro explícito não transforma provider em código não
confiável: seus loops, I/O e bloqueios não são medidos pelo saldo de percurso da linguagem. Defina um
deadline externo quando ele puder bloquear ou executar trabalho caro.

## Memória de Cálculo

`computeWithMemory()` retorna `ComputationWithMemory<T>` com o mesmo resultado de `compute()` e uma
`CalculationMemory` imutável daquela execução. Ela contém símbolos efetivos participantes e Pontos de
Cálculo alcançados; não é log temporal, árvore de execução nem snapshot de objetos Java mutáveis.

Para persistência sequencial, prefira o percurso indexado, que não cria entry, lista ou iterator:

```java
CalculationMemory memory = expression.asMath().computeWithMemory(overrides).memory();
for (int index = 0; index < memory.variableCount(); index++) {
    persistVariable(memory.variableKeyAt(index), memory.variableValueAt(index));
}
for (int index = 0; index < memory.calculationCount(); index++) {
    persistCalculation(memory.calculationKeyAt(index), memory.calculationValueAt(index));
}
```

`variables()` e `calculations()` são projeções convenientes e imutáveis, mas criam view/entries
transientes. Copiar valores mutáveis, serializar, abrir transação e tratar falha pertencem ao adapter de
persistência; a memória não retém plano, ambiente ou fonte.
