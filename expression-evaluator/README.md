# Expression Evaluator

Compilador e runtime de expressões tipadas para Java 21. Uma expressão é compilada uma vez por uma
`ExpressionEngine` longeva e o plano imutável resultante pode ser executado concorrentemente.

## Início rápido

```java
import com.runestone.expeval.api.CompiledExpression;
import com.runestone.expeval.api.ExpressionEngine;
import com.runestone.expeval.api.ExpressionEnvironment;

ExpressionEngine engine = ExpressionEngine.defaultEngine();
ExpressionEnvironment environment = ExpressionEnvironment.standard();
CompiledExpression expression = engine.compileOrThrow("subtotal := 40; subtotal * 1.05", environment);

System.out.println(expression.asMath().compute()); // 42.00
```

O resultado de `compile(...)` é fechado: `Success` contém a expressão compilada e avisos; `Failure`
contém a lista completa de diagnósticos. `compileOrThrow(...)` é a forma conveniente para configuração
ou fontes controladas.

<!-- runestone-example: NUMBER=7 -->
```runestone
1 + 2 * 3
```

## Escolha do modo de confiança

- `TRUSTED` é o padrão: limita compilação, sem contador no runtime.
- `SAFE` deve ser usado para fonte ou valores controlados por tenant: limita compilação e runtime.
- `UNSAFE` delega toda contenção de recursos ao integrador; os contratos funcionais continuam ativos.

Leia o [guia de hardening](docs/guides/hardening.md) antes de aceitar expressões não confiáveis.

## Referências e guias

- [Referência da linguagem](docs/reference/language.md)
- [Registro de diagnósticos](docs/reference/diagnostics.md)
- [Guia da API Java](docs/guides/api.md)
- [Guia de hardening](docs/guides/hardening.md)
- [Limites e modos de confiança](docs/resource-limits.md)
- [Manifesto de desempenho da Etapa 12](docs/perf/stage12-gates.md)

## Verificação local

```bash
mvn -pl expression-evaluator -am test
mvn -pl expression-evaluator -am verify
```

Os testes executam os blocos `runestone`, conferem a tabela de precedência contra a AST, comparam o
registro público de diagnósticos ao registro interno e o `verify` gera Javadoc com doclint.
