# Desenvolvimento

Siga os requisitos e as instruções de compilação do [README](README.md). Use o Gradle Wrapper incluído no projeto.

## Organização

- Código Java: `app/src/main/java/br/com/joaop/pocketarcade/`.
- Layouts e demais recursos Android: `app/src/main/res/`.
- Testes que dependem do Android: `app/src/androidTest/`.
- Textos da interface: `app/src/main/res/values/strings.xml`.

Siga o estilo dos arquivos existentes e as configurações de `.editorconfig`. Preserve os créditos dos recursos de terceiros.

## Validação

Execute na raiz, usando PowerShell:

```powershell
.\gradlew.bat assembleDebug lintDebug assembleDebugAndroidTest
```

Para alterações de comportamento, execute também os testes instrumentados com dispositivo ou emulador conectado:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

No Linux e macOS, use `./gradlew`. Verifique manualmente as telas afetadas e descreva no pull request o comportamento alterado e as verificações realizadas.

## Arquivos locais

Não adicione caches, APKs, arquivos `.env`, `local.properties` ou chaves de assinatura aos commits. Mantenha credenciais fora do código e revise `git diff --cached` antes de publicar.
