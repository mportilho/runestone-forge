# Operacoes de filtro JPA

Este guia descreve as operacoes de filtro JPA registradas por `SpecificationFilterOperationService`. Cada operacao usa a anotacao `@Filter` para produzir uma `Specification<?>` do Spring Data JPA.

Os exemplos assumem os imports abaixo, conforme a necessidade de cada caso:

```java
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.model.modifiers.ModIgnoreCase;
import com.runestone.dynafilter.core.operation.types.Between;
import com.runestone.dynafilter.core.operation.types.Decorated;
import com.runestone.dynafilter.core.operation.types.Dynamic;
import com.runestone.dynafilter.core.operation.types.EndsWith;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.Greater;
import com.runestone.dynafilter.core.operation.types.GreaterOrEquals;
import com.runestone.dynafilter.core.operation.types.IsIn;
import com.runestone.dynafilter.core.operation.types.IsNull;
import com.runestone.dynafilter.core.operation.types.Less;
import com.runestone.dynafilter.core.operation.types.LessOrEquals;
import com.runestone.dynafilter.core.operation.types.Like;
import com.runestone.dynafilter.core.operation.types.StartsWith;
import com.runestone.dynafilter.core.operation.types.extensions.AnyFieldLike;
import com.runestone.dynafilter.core.operation.types.extensions.CollectionSize;
import com.runestone.dynafilter.core.operation.types.extensions.ContainsAll;
import com.runestone.dynafilter.core.operation.types.extensions.EffectiveAtClosed;
import com.runestone.dynafilter.core.operation.types.extensions.EffectiveAtHalfOpen;
import com.runestone.dynafilter.core.operation.types.extensions.EffectiveAtOpen;
import com.runestone.dynafilter.core.operation.types.extensions.IsBlank;
import com.runestone.dynafilter.core.operation.types.extensions.IsEmptyCollection;
import com.runestone.dynafilter.core.operation.types.extensions.NullOrGreater;
import com.runestone.dynafilter.core.operation.types.extensions.NullOrGreaterOrEquals;
import com.runestone.dynafilter.core.operation.types.extensions.NullOrLess;
import com.runestone.dynafilter.core.operation.types.extensions.NullOrLessOrEquals;
import com.runestone.dynafilter.core.operation.types.extensions.OnDate;
import com.runestone.dynafilter.core.operation.types.extensions.PeriodOverlapsClosed;
import com.runestone.dynafilter.core.operation.types.extensions.PeriodOverlapsHalfOpen;
import com.runestone.dynafilter.core.operation.types.extensions.PeriodOverlapsOpen;
import com.runestone.dynafilter.core.operation.types.extensions.SizeBetween;
import com.runestone.dynafilter.modules.jpa.operation.modifiers.ModJoinTypeLeft;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
```

## Regras comuns

`path` indica o atributo JPA filtrado. Use notacao com ponto para navegar por associacoes, como `customer.email` ou `address.city.name`.

`parameters` indica os nomes dos parametros esperados na requisicao. Se nenhum valor for informado, o filtro nao e aplicado, exceto quando `required = true`.

`targetType` e opcional na anotacao, mas e util para documentacao e para a conversao de valores gerados pela camada de filtros. A implementacao JPA tambem usa o tipo real do path resolvido no Criteria API.

`defaultValues` fornece valores quando a requisicao nao envia o parametro. `constantValues` fixa valores e tem prioridade sobre parametros de requisicao.

`negate = "true"` inverte o predicado produzido pela operacao.

`modifiers = {ModIgnoreCase.class}` aplica comparacoes textuais sem diferenciar maiusculas e minusculas quando a operacao suporta esse modificador.

`modifiers = {ModJoinTypeLeft.class}` altera o tipo de join usado ao navegar por associacoes. Sem modificador, o padrao e `INNER JOIN`.

## Equals

`Equals` compara o valor do atributo com um unico valor informado, gerando um predicado equivalente a `path = value`. Para campos `String`, pode usar `ModIgnoreCase` para comparar ambos os lados em uppercase.

Exemplos:

```java
@Filter(path = "status", parameters = "status", operation = Equals.class, targetType = String.class)

@Filter(path = "customer.email", parameters = "email", operation = Equals.class, targetType = String.class, modifiers = {ModIgnoreCase.class})
```

## Like

`Like` busca ocorrencias parciais em um atributo textual, gerando um `LIKE` com `%valor%`. Use para pesquisa por trecho em nomes, titulos, descricoes e campos similares.

Exemplos:

```java
@Filter(path = "name", parameters = "name", operation = Like.class, targetType = String.class)

@Filter(path = "description", parameters = "q", operation = Like.class, targetType = String.class, modifiers = {ModIgnoreCase.class})
```

## StartsWith

`StartsWith` filtra textos que comecam com o valor informado, gerando um `LIKE` com `valor%`. Use para autocomplete, prefixos de codigo e pesquisas por inicio de nome.

Exemplos:

```java
@Filter(path = "name", parameters = "namePrefix", operation = StartsWith.class, targetType = String.class)

@Filter(path = "sku", parameters = "skuPrefix", operation = StartsWith.class, targetType = String.class, modifiers = {ModIgnoreCase.class})
```

## EndsWith

`EndsWith` filtra textos que terminam com o valor informado, gerando um `LIKE` com `%valor`. Use para sufixos, dominios de email e finais de codigo.

Exemplos:

```java
@Filter(path = "email", parameters = "emailDomain", operation = EndsWith.class, targetType = String.class)

@Filter(path = "externalCode", parameters = "codeSuffix", operation = EndsWith.class, targetType = String.class, modifiers = {ModIgnoreCase.class})
```

## IsIn

`IsIn` verifica se o atributo esta contido em uma lista de valores, gerando um predicado `IN`. Quando o atributo final do path e uma colecao, a operacao faz join no atributo de colecao e verifica se algum elemento pertence aos valores informados.

Exemplos:

```java
@Filter(path = "status", parameters = "statuses", operation = IsIn.class, targetType = String.class)

@Filter(path = "tags", parameters = "tags", operation = IsIn.class, targetType = String.class, modifiers = {ModIgnoreCase.class})
```

## IsNull

`IsNull` recebe um valor booleano. Quando o valor e `true`, filtra registros em que o atributo e `NULL`; quando e `false`, filtra registros em que o atributo nao e `NULL`.

Exemplos:

```java
@Filter(path = "deletedAt", parameters = "deleted", operation = IsNull.class, targetType = Boolean.class)

@Filter(path = "phoneNumber", parameters = "hasPhone", operation = IsNull.class, constantValues = "false", targetType = Boolean.class)
```

## Greater

`Greater` compara valores ordenaveis, gerando `path > value`. Funciona para numeros, datas e outros tipos comparaveis pelo Criteria API.

Exemplos:

```java
@Filter(path = "price", parameters = "minExclusivePrice", operation = Greater.class, targetType = BigDecimal.class)

@Filter(path = "createdAt", parameters = "createdAfter", operation = Greater.class, targetType = LocalDateTime.class)
```

## GreaterOrEquals

`GreaterOrEquals` compara valores ordenaveis, gerando `path >= value`. Use para limites inferiores inclusivos.

Exemplos:

```java
@Filter(path = "age", parameters = "minAge", operation = GreaterOrEquals.class, targetType = Integer.class)

@Filter(path = "total", parameters = "minTotal", operation = GreaterOrEquals.class, targetType = BigDecimal.class)
```

## Less

`Less` compara valores ordenaveis, gerando `path < value`. Use para limites superiores exclusivos.

Exemplos:

```java
@Filter(path = "stock", parameters = "maxExclusiveStock", operation = Less.class, targetType = Integer.class)

@Filter(path = "createdAt", parameters = "createdBefore", operation = Less.class, targetType = Instant.class)
```

## LessOrEquals

`LessOrEquals` compara valores ordenaveis, gerando `path <= value`. Use para limites superiores inclusivos.

Exemplos:

```java
@Filter(path = "price", parameters = "maxPrice", operation = LessOrEquals.class, targetType = BigDecimal.class)

@Filter(path = "dueDate", parameters = "dueUntil", operation = LessOrEquals.class, targetType = LocalDate.class)
```

## Between

`Between` recebe dois valores e verifica se o atributo esta entre eles, com limites inclusivos. A anotacao deve declarar um path e dois parametros.

Exemplos:

```java
@Filter(path = "createdAt", parameters = {"createdFrom", "createdTo"}, operation = Between.class, targetType = LocalDateTime.class)

@Filter(path = "price", parameters = {"minPrice", "maxPrice"}, operation = Between.class, targetType = BigDecimal.class)
```

## AnyFieldLike

`AnyFieldLike` aplica a mesma busca textual parcial em varios paths e combina os predicados com `OR`. E util para uma busca livre em mais de um campo.

Exemplos:

```java
@Filter(path = {"name", "email", "documentNumber"}, parameters = "q", operation = AnyFieldLike.class, targetType = String.class)

@Filter(path = {"title", "subtitle", "author.name"}, parameters = "term", operation = AnyFieldLike.class, targetType = String.class, modifiers = {ModIgnoreCase.class})
```

## CollectionSize

`CollectionSize` compara o tamanho de uma colecao com um numero exato, gerando `size(path) = value`. O path deve apontar para um atributo de colecao.

Exemplos:

```java
@Filter(path = "items", parameters = "itemCount", operation = CollectionSize.class, targetType = Integer.class)

@Filter(path = "comments", parameters = "withoutComments", operation = CollectionSize.class, constantValues = "0", targetType = Integer.class)
```

## ContainsAll

`ContainsAll` verifica se uma colecao contem todos os valores informados. A implementacao cria um `isMember` para cada valor e combina todos com `AND`.

Exemplos:

```java
@Filter(path = "tags", parameters = "requiredTags", operation = ContainsAll.class, targetType = String.class)

@Filter(path = "permissions", parameters = "requiredPermission", operation = ContainsAll.class, constantValues = "READ", targetType = String.class)
```

## EffectiveAtClosed

`EffectiveAtClosed` recebe dois paths de periodo, inicio e fim, e um valor de referencia. O registro e considerado vigente quando `start <= reference` e `end IS NULL OR end >= reference`, ou seja, o intervalo e fechado nos dois lados.

Exemplos:

```java
@Filter(path = {"validFrom", "validTo"}, parameters = "referenceDate", operation = EffectiveAtClosed.class, targetType = LocalDate.class)

@Filter(path = {"contract.startedAt", "contract.endedAt"}, parameters = "activeAt", operation = EffectiveAtClosed.class, targetType = LocalDateTime.class, modifiers = {ModJoinTypeLeft.class})
```

## EffectiveAtHalfOpen

`EffectiveAtHalfOpen` recebe dois paths de periodo e um valor de referencia. O registro e considerado vigente quando `start <= reference` e `end IS NULL OR end > reference`, isto e, intervalo `[start, end)`.

Exemplos:

```java
@Filter(path = {"validFrom", "validTo"}, parameters = "referenceDate", operation = EffectiveAtHalfOpen.class, targetType = LocalDate.class)

@Filter(path = {"subscription.startsAt", "subscription.endsAt"}, parameters = "subscribedAt", operation = EffectiveAtHalfOpen.class, targetType = Instant.class, modifiers = {ModJoinTypeLeft.class})
```

## EffectiveAtOpen

`EffectiveAtOpen` recebe dois paths de periodo e um valor de referencia. O registro e considerado vigente quando `start < reference` e `end IS NULL OR end > reference`, isto e, intervalo aberto nos dois lados.

Exemplos:

```java
@Filter(path = {"startedAt", "finishedAt"}, parameters = "referenceAt", operation = EffectiveAtOpen.class, targetType = LocalDateTime.class)

@Filter(path = {"campaign.startDate", "campaign.endDate"}, parameters = "date", operation = EffectiveAtOpen.class, targetType = LocalDate.class, modifiers = {ModJoinTypeLeft.class})
```

## IsBlank

`IsBlank` recebe um booleano e opera sobre texto. Quando o valor e `true`, filtra registros em que o atributo e `NULL` ou vazio apos `trim`; quando e `false`, filtra registros em que o atributo nao e branco.

Exemplos:

```java
@Filter(path = "description", parameters = "blankDescription", operation = IsBlank.class, targetType = Boolean.class)

@Filter(path = "nickname", parameters = "hasNickname", operation = IsBlank.class, constantValues = "false", targetType = Boolean.class)
```

## IsEmptyCollection

`IsEmptyCollection` recebe um booleano e opera sobre colecoes. Quando o valor e `true`, usa `isEmpty`; quando e `false`, usa `isNotEmpty`.

Exemplos:

```java
@Filter(path = "items", parameters = "emptyItems", operation = IsEmptyCollection.class, targetType = Boolean.class)

@Filter(path = "attachments", parameters = "hasAttachments", operation = IsEmptyCollection.class, constantValues = "false", targetType = Boolean.class)
```

## NullOrGreater

`NullOrGreater` aceita registros em que o atributo e `NULL` ou maior que o valor informado, gerando `path IS NULL OR path > value`. Use para limites opcionais em que `NULL` significa sem restricao.

Exemplos:

```java
@Filter(path = "expiresAt", parameters = "expiresAfter", operation = NullOrGreater.class, targetType = LocalDateTime.class)

@Filter(path = "maxAmount", parameters = "amount", operation = NullOrGreater.class, targetType = BigDecimal.class)
```

## NullOrGreaterOrEquals

`NullOrGreaterOrEquals` aceita registros em que o atributo e `NULL` ou maior ou igual ao valor informado, gerando `path IS NULL OR path >= value`.

Exemplos:

```java
@Filter(path = "validTo", parameters = "validAt", operation = NullOrGreaterOrEquals.class, targetType = LocalDate.class)

@Filter(path = "creditLimit", parameters = "requestedLimit", operation = NullOrGreaterOrEquals.class, targetType = BigDecimal.class)
```

## NullOrLess

`NullOrLess` aceita registros em que o atributo e `NULL` ou menor que o valor informado, gerando `path IS NULL OR path < value`.

Exemplos:

```java
@Filter(path = "availableFrom", parameters = "beforeDate", operation = NullOrLess.class, targetType = LocalDate.class)

@Filter(path = "minAmount", parameters = "amount", operation = NullOrLess.class, targetType = BigDecimal.class)
```

## NullOrLessOrEquals

`NullOrLessOrEquals` aceita registros em que o atributo e `NULL` ou menor ou igual ao valor informado, gerando `path IS NULL OR path <= value`.

Exemplos:

```java
@Filter(path = "availableFrom", parameters = "availableAt", operation = NullOrLessOrEquals.class, targetType = LocalDate.class)

@Filter(path = "minimumAge", parameters = "age", operation = NullOrLessOrEquals.class, targetType = Integer.class)
```

## OnDate

`OnDate` filtra registros que ocorrem em uma data especifica. Para paths `LocalDate`, usa igualdade com a data. Para `LocalDateTime`, `Instant` e `Date`, cria um intervalo de inicio do dia inclusivo ate inicio do proximo dia exclusivo.

Exemplos:

```java
@Filter(path = "eventDate", parameters = "date", operation = OnDate.class, targetType = LocalDate.class)

@Filter(path = "createdAt", parameters = "createdOn", operation = OnDate.class, targetType = LocalDate.class, format = "yyyy-MM-dd")
```

## PeriodOverlapsClosed

`PeriodOverlapsClosed` recebe dois paths do periodo do registro, inicio e fim, e dois parametros do periodo pesquisado. Ha sobreposicao quando `start <= filterEnd` e `end IS NULL OR end >= filterStart`, com limites fechados.

Exemplos:

```java
@Filter(path = {"startsAt", "endsAt"}, parameters = {"from", "to"}, operation = PeriodOverlapsClosed.class, targetType = LocalDate.class)

@Filter(path = {"booking.checkIn", "booking.checkOut"}, parameters = {"checkIn", "checkOut"}, operation = PeriodOverlapsClosed.class, targetType = LocalDate.class, modifiers = {ModJoinTypeLeft.class})
```

## PeriodOverlapsHalfOpen

`PeriodOverlapsHalfOpen` recebe dois paths do periodo do registro e dois parametros do periodo pesquisado. Ha sobreposicao quando `start < filterEnd` e `end IS NULL OR end > filterStart`, modelando intervalos `[start, end)`.

Exemplos:

```java
@Filter(path = {"startsAt", "endsAt"}, parameters = {"from", "to"}, operation = PeriodOverlapsHalfOpen.class, targetType = LocalDateTime.class)

@Filter(path = {"allocation.startDate", "allocation.endDate"}, parameters = {"periodStart", "periodEnd"}, operation = PeriodOverlapsHalfOpen.class, targetType = LocalDate.class, modifiers = {ModJoinTypeLeft.class})
```

## PeriodOverlapsOpen

`PeriodOverlapsOpen` recebe dois paths do periodo do registro e dois parametros do periodo pesquisado. A implementacao aplica a mesma logica de sobreposicao estrita de `PeriodOverlapsHalfOpen`: `start < filterEnd` e `end IS NULL OR end > filterStart`.

Exemplos:

```java
@Filter(path = {"startTime", "endTime"}, parameters = {"windowStart", "windowEnd"}, operation = PeriodOverlapsOpen.class, targetType = LocalDateTime.class)

@Filter(path = {"promotion.startsAt", "promotion.endsAt"}, parameters = {"startsAt", "endsAt"}, operation = PeriodOverlapsOpen.class, targetType = Instant.class, modifiers = {ModJoinTypeLeft.class})
```

## SizeBetween

`SizeBetween` filtra pelo tamanho de uma colecao dentro de um intervalo inclusivo, gerando `size(path) BETWEEN lowerSize AND upperSize`. Os dois valores precisam ser inteiros nao negativos, e o limite inferior deve ser menor ou igual ao superior.

Exemplos:

```java
@Filter(path = "items", parameters = {"minItems", "maxItems"}, operation = SizeBetween.class, targetType = Integer.class)

@Filter(path = "attachments", parameters = {"minAttachments", "maxAttachments"}, operation = SizeBetween.class, defaultValues = {"1", "5"}, targetType = Integer.class)
```

## Pseudo-operacoes do gerador

`Dynamic` e `Decorated` tambem estendem `FilterOperation`, mas nao sao registradas como operacoes JPA em `SpecificationFilterOperationService`.

`Dynamic` e resolvida pelo gerador de statements antes da criacao da `Specification`. O primeiro valor recebido indica a operacao real: `EQ`, `LT`, `LE`, `GT`, `GE`, `LK`, `SW`, `EW`, `IN` ou `BT`. Prefixe com `N`, como `NEQ` ou `NLK`, para negar a operacao escolhida.

Exemplos:

```java
@Filter(path = "age", parameters = "ageFilter", operation = Dynamic.class, targetType = Integer.class)

@Filter(path = "name", parameters = "nameFilter", operation = Dynamic.class, targetType = String.class)
```

`Decorated` nao entra no statement principal. Ele fica disponivel como filtro decorado no `StatementWrapper`, para logicas complementares implementadas fora das operacoes JPA padrao.

Exemplos:

```java
@Filter(path = "job", parameters = "jobDecorated", operation = Decorated.class, targetType = String.class)

@Filter(path = "decorValue", parameters = "decorValue", operation = Decorated.class, targetType = String.class)
```
