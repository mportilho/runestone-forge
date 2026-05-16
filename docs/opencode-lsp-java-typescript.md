# Manual de instalacao de LSP Java e TypeScript no opencode

Este documento consolida os procedimentos e pitfalls encontrados ao validar `jdtls` e `typescript-language-server` com o opencode. Use este guia ao preparar uma nova instalacao do opencode para projetos Java e TypeScript.

## Objetivo

Configurar o opencode para usar:

- Java: Eclipse JDT Language Server (`jdtls`).
- TypeScript/JavaScript: `typescript-language-server --stdio`.

Ao final, os comandos `opencode debug lsp diagnostics` e `opencode debug lsp document-symbols` devem iniciar os servidores LSP corretos e retornar diagnosticos ou simbolos.

Para que os agentes/modelos tambem enxerguem a ferramenta `lsp` durante uma conversa, o opencode precisa ser iniciado com `OPENCODE_EXPERIMENTAL_LSP_TOOL=true` ou `OPENCODE_EXPERIMENTAL=true`.

## Pre-requisitos

- opencode instalado e acessivel no `PATH`.
- Java runtime instalado. Para `jdtls`, use JDK moderno, preferencialmente JDK 21 ou superior.
- Node.js e npm instalados para o servidor TypeScript.
- Um shell com acesso aos mesmos paths usados ao iniciar o opencode.

Valide o basico:

```shell
command -v opencode
opencode --version
command -v java
java -version
command -v node
node --version
command -v npm
npm --version
```

## Instalacao dos servidores

### Java: jdtls

Em ambientes com Homebrew:

```shell
brew install jdtls
```

Valide que o executavel existe:

```shell
command -v jdtls
```

Nao use `jdtls --version` como teste de versao. Em algumas instalacoes, esse comando inicia o servidor LSP em `stdio` e fica aguardando handshake, podendo parecer travado. A validacao real deve ser feita pelo `opencode debug lsp`, conforme a secao de validacao.

### TypeScript: typescript-language-server

Instale globalmente:

```shell
npm install -g typescript typescript-language-server
```

Valide:

```shell
command -v typescript-language-server
typescript-language-server --version
```

## Configuracao do opencode

O arquivo global do opencode fica em:

```text
~/.config/opencode/opencode.jsonc
```

Tambem e possivel usar configuracao por projeto em `opencode.json`, `opencode.jsonc` ou `.opencode/opencode.json`, mas para uma instalacao reutilizavel prefira o arquivo global.

Use esta configuracao minima:

```jsonc
{
  "$schema": "https://opencode.ai/config.json",
  "lsp": {
    "jdtls": {
      "command": ["jdtls"],
      "extensions": [".java"]
    },
    "typescript": {
      "command": ["typescript-language-server", "--stdio"],
      "extensions": [".ts", ".tsx", ".js", ".jsx"]
    }
  },
  "permission": {
    "lsp": "allow"
  }
}
```

Use `jdtls` como chave do servidor Java para sobrescrever/configurar o LSP built-in documentado pelo opencode. Usar uma chave customizada como `java` tambem e aceito pelo schema, mas pode criar uma segunda entrada para `.java` em vez de ajustar o built-in.

### Habilitar a ferramenta LSP para agentes/modelos

A configuracao `lsp` inicia e configura os servidores. A ferramenta que permite ao modelo chamar operacoes como `documentSymbol`, `hover`, `findReferences` e `goToDefinition` e experimental e precisa ser habilitada por variavel de ambiente.

Para habilitar em uma execucao:

```shell
OPENCODE_EXPERIMENTAL_LSP_TOOL=true opencode
```

Para deixar persistente no `zsh`, adicione ao `~/.zshrc`:

```shell
export OPENCODE_EXPERIMENTAL_LSP_TOOL=true
```

Depois reinicie o terminal e o opencode. Sessoes ja abertas nao recarregam variaveis de ambiente nem configuracao.

Se algum agente tiver `permission` propria, ela pode sobrescrever a permissao global. Nesse caso, inclua `lsp: allow` no agente especifico:

```jsonc
{
  "agent": {
    "explore": {
      "permission": {
        "lsp": "allow"
      }
    },
    "general": {
      "permission": {
        "lsp": "allow"
      }
    }
  }
}
```

Mesmo com a ferramenta habilitada, o modelo usado precisa suportar tool calling para conseguir chama-la.

### Pitfall critico: extensoes precisam de ponto

No opencode `1.15.3`, configurar extensoes sem ponto impede que o LSP customizado seja selecionado corretamente.

Evite isto:

```jsonc
{
  "lsp": {
    "jdtls": {
      "command": ["jdtls"],
      "extensions": ["java"]
    },
    "typescript": {
      "command": ["typescript-language-server", "--stdio"],
      "extensions": ["ts", "tsx", "js", "jsx"]
    }
  }
}
```

Use isto:

```jsonc
{
  "lsp": {
    "jdtls": {
      "command": ["jdtls"],
      "extensions": [".java"]
    },
    "typescript": {
      "command": ["typescript-language-server", "--stdio"],
      "extensions": [".ts", ".tsx", ".js", ".jsx"]
    }
  }
}
```

Sintoma do problema: `opencode debug lsp diagnostics arquivo.ts` retorna `{}` rapidamente e os logs nao mostram `serverID=typescript spawned lsp server`.

## Aplicacao da configuracao

O opencode carrega a configuracao no startup. Depois de editar `~/.config/opencode/opencode.jsonc`, reinicie o opencode para a sessao atual enxergar a mudanca.

Confirme a configuracao resolvida:

```shell
opencode debug config
```

Procure por:

```json
"lsp": {
  "jdtls": {
    "command": ["jdtls"],
    "extensions": [".java"]
  },
  "typescript": {
    "command": ["typescript-language-server", "--stdio"],
    "extensions": [".ts", ".tsx", ".js", ".jsx"]
  }
}
```

## Validacao Java

Escolha um arquivo Java real do workspace. Exemplo neste projeto:

```shell
opencode debug lsp diagnostics "expression-evaluator/src/main/java/com/runestone/expeval/environment/ExpressionEnvironmentBuilder.java"
```

Resultado esperado para um arquivo sem problemas locais:

```json
{
  "/caminho/do/projeto/.../ExpressionEnvironmentBuilder.java": []
}
```

Tambem valide simbolos do documento. Para `document-symbols`, use URI `file://`:

```shell
opencode debug lsp document-symbols "file:///caminho/absoluto/do/projeto/expression-evaluator/src/main/java/com/runestone/expeval/environment/ExpressionEnvironmentBuilder.java"
```

Resultado esperado: uma lista JSON com campos, classes e metodos.

Para ver o startup do servidor:

```shell
opencode --print-logs --log-level DEBUG debug lsp diagnostics "expression-evaluator/src/main/java/com/runestone/expeval/environment/ExpressionEnvironmentBuilder.java"
```

Nos logs, procure por:

```text
service=lsp serverID=jdtls ... spawned lsp server
service=lsp.client serverID=jdtls sending initialize
service=lsp.client serverID=jdtls initialized
```

## Validacao TypeScript

Se o projeto nao tiver arquivos TypeScript, crie um smoke test temporario dentro do workspace:

```shell
printf '%s\n' \
  'export class LspSmokeTest {' \
  '  answer(): number {' \
  '    return 42;' \
  '  }' \
  '}' > opencode-lsp-smoke.ts
```

Rode diagnostics:

```shell
opencode --print-logs --log-level DEBUG debug lsp diagnostics "opencode-lsp-smoke.ts"
```

Resultado esperado:

```json
{
  "/caminho/do/projeto/opencode-lsp-smoke.ts": []
}
```

Nos logs, procure por:

```text
service=lsp serverID=typescript ... spawned lsp server
service=lsp.client serverID=typescript sending initialize
service=lsp.client serverID=typescript initialized
service=lsp.client serverID=typescript ... textDocument/didOpen
service=lsp.client serverID=typescript ... textDocument/publishDiagnostics
```

Remova o smoke test depois:

```shell
rm opencode-lsp-smoke.ts
```

## Validacao direta do TypeScript LSP

Use esta validacao somente para isolar se o problema esta no `typescript-language-server` ou na camada LSP do opencode.

```shell
node -e 'const { spawn } = require("node:child_process"); const file = "file:///tmp/opencode-lsp-test.ts"; const root = "file://" + process.cwd(); const child = spawn("typescript-language-server", ["--stdio"]); let buffer = Buffer.alloc(0); function send(message) { const body = Buffer.from(JSON.stringify(message)); child.stdin.write(`Content-Length: ${body.length}\r\n\r\n`); child.stdin.write(body); } child.stdout.on("data", chunk => { buffer = Buffer.concat([buffer, chunk]); const text = buffer.toString(); if (text.includes("LspSmokeTest") || text.includes("answer")) { console.log(text); child.kill(); } }); child.stderr.on("data", chunk => process.stderr.write(chunk)); child.on("exit", code => process.exit(code ?? 0)); send({ jsonrpc: "2.0", id: 1, method: "initialize", params: { processId: process.pid, rootUri: root, capabilities: {} } }); setTimeout(() => { send({ jsonrpc: "2.0", method: "initialized", params: {} }); send({ jsonrpc: "2.0", method: "textDocument/didOpen", params: { textDocument: { uri: file, languageId: "typescript", version: 1, text: "export class LspSmokeTest {\n  answer(): number {\n    return 42;\n  }\n}\n" } } }); send({ jsonrpc: "2.0", id: 2, method: "textDocument/documentSymbol", params: { textDocument: { uri: file } } }); }, 1000); setTimeout(() => { console.log(buffer.toString()); child.kill(); }, 8000);'
```

Resultado esperado: resposta JSON-RPC contendo `LspSmokeTest` e `answer`.

Se o teste direto funciona, mas o opencode nao inicia `serverID=typescript`, revise primeiro a lista `extensions` com ponto.

## Ruido esperado e falsos positivos

### `oxlint not found`

O opencode pode tentar usar LSPs built-in para TS/JS, como `oxlint`, `biome` ou `eslint`. Se aparecer:

```text
service=lsp.server oxlint not found, please install oxlint
```

isso nao significa que `typescript-language-server` falhou. Confirme se tambem aparece:

```text
serverID=typescript ... spawned lsp server
```

Se quiser reduzir ruido em uma instalacao que nao usa esses linters, desabilite-os:

```jsonc
{
  "lsp": {
    "oxlint": { "disabled": true },
    "biome": { "disabled": true },
    "eslint": { "disabled": true },
    "typescript": {
      "command": ["typescript-language-server", "--stdio"],
      "extensions": [".ts", ".tsx", ".js", ".jsx"]
    }
  }
}
```

### `InstanceRef not provided`

Durante `opencode debug lsp`, os logs podem mostrar:

```text
ERROR ... e=InstanceRef not provided rejection
```

Nos testes realizados, isso nao impediu o recebimento de diagnostics nem a inicializacao dos servidores. Trate como ruido enquanto `spawned lsp server`, `initialized` e `publishDiagnostics` aparecerem.

### Diagnosticos Java em arquivos gerados

Ao validar Java em projeto Maven com fontes geradas, o `jdtls` pode reportar muitos erros em `target/generated-*`, especialmente de JMH ou annotation processors. Isso confirma que o LSP esta ativo, mas nao necessariamente indica regressao no codigo fonte principal.

Para uma validacao limpa, prefira um arquivo Java especifico e observe se o caminho dele aparece com lista vazia ou diagnosticos coerentes.

## Checklist rapido para nova maquina

1. Instalar opencode.
2. Instalar JDK 21+.
3. Instalar `jdtls`.
4. Instalar Node.js e npm.
5. Instalar `typescript` e `typescript-language-server` globalmente.
6. Criar ou editar `~/.config/opencode/opencode.jsonc` com `jdtls`, `typescript`, extensoes incluindo ponto e `permission.lsp: allow`.
7. Configurar `OPENCODE_EXPERIMENTAL_LSP_TOOL=true` no ambiente que inicia o opencode.
8. Rodar `opencode debug config`.
9. Rodar diagnostics em um `.java` real.
10. Rodar diagnostics em um `.ts` real ou smoke test temporario.
11. Reiniciar o opencode depois de qualquer mudanca de config ou env.

## Troubleshooting

### `opencode debug config` nao mostra a config esperada

- Verifique se o arquivo esta em `~/.config/opencode/opencode.jsonc`.
- Verifique se existe config de projeto sobrescrevendo a global.
- Reinicie o opencode.
- Para testar uma config sem editar arquivo, use `OPENCODE_CONFIG_CONTENT`:

```shell
OPENCODE_CONFIG_CONTENT='{"lsp":{"typescript":{"command":["typescript-language-server","--stdio"],"extensions":[".ts",".tsx",".js",".jsx"]}}}' \
  opencode debug config
```

### `serverID=typescript` nao aparece nos logs

- Confirme que `typescript-language-server` esta no `PATH`.
- Confirme `extensions` com ponto: `".ts"`, nao `"ts"`.
- Rode com logs: `opencode --print-logs --log-level DEBUG debug lsp diagnostics "arquivo.ts"`.
- Teste o servidor diretamente com o smoke test Node da secao anterior.

### `serverID=jdtls` nao aparece nos logs

- Confirme `command -v jdtls`.
- Confirme que o arquivo testado termina com `.java`.
- Confirme `extensions`: `".java"`.
- Rode com logs: `opencode --print-logs --log-level DEBUG debug lsp diagnostics "arquivo.java"`.

### `document-symbols` falha com caminho relativo

O comando `diagnostics` aceita caminho relativo, mas `document-symbols` espera URI `file://`.

Use:

```shell
opencode debug lsp document-symbols "file:///caminho/absoluto/arquivo.java"
```

Evite:

```shell
opencode debug lsp document-symbols "arquivo.java"
```

Erro comum:

```text
The URL must be of scheme file
```

## Configuracao final recomendada

```jsonc
{
  "$schema": "https://opencode.ai/config.json",
  "lsp": {
    "jdtls": {
      "command": ["jdtls"],
      "extensions": [".java"]
    },
    "typescript": {
      "command": ["typescript-language-server", "--stdio"],
      "extensions": [".ts", ".tsx", ".js", ".jsx"]
    }
  },
  "permission": {
    "lsp": "allow"
  }
}
```

No shell que inicia o opencode, deixe tambem:

```shell
export OPENCODE_EXPERIMENTAL_LSP_TOOL=true
```
