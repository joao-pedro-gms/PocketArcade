# Pocket Arcade

Aplicativo Android nativo com quatro experiências de jogos e utilidades, desenvolvido em Java e XML com Material Components e View Binding.

## Baixar e instalar

**[Baixar o APK do Pocket Arcade 1.0](https://github.com/joao-pedro-gms/PocketArcade/releases/download/v1.0.0/PocketArcade-1.0.apk)**

1. Baixe o APK no seu celular com Android 6.0 ou superior.
2. Abra o arquivo e, se solicitado pelo Android, permita a instalação de aplicativos pela fonte usada para o download.
3. Toque em **Instalar** e abra o Pocket Arcade.

O arquivo também está disponível na página de [Releases](https://github.com/joao-pedro-gms/PocketArcade/releases). Esta distribuição usa o APK de desenvolvimento, assinado com a chave de debug, para instalação manual.

## Funcionalidades

| Módulo | O que faz |
| --- | --- |
| Scanner de Mensagens | Conta caracteres, palavras e vogais, inverte o texto e identifica palíndromos. |
| Arena | Jogo de Pedra, Papel e Tesoura contra o dispositivo, com placar e resultado da partida. |
| Código Secreto | Desafio para descobrir quatro dígitos diferentes em até oito tentativas, com pistas por cores. |
| Randomizador | Cadastro e sorteio de itens por categoria, com visualização em lista ou grade. |

A navegação inclui entrada por apelido, seleção de módulos e encerramento de sessão. Os jogos têm animações, efeitos sonoros e diálogos de resultado.

## Requisitos

- Android Studio com suporte ao Android Gradle Plugin 9.4.1.
- Android SDK Platform 37 instalado para compilação. O aplicativo suporta Android 6.0 / API 23 ou superior.
- JDK 25 para o daemon do Gradle, conforme `gradle/gradle-daemon-jvm.properties`. A configuração de toolchain pode baixar esse JDK automaticamente.
- Acesso à internet na primeira compilação para baixar o Gradle e as dependências.

O Gradle Wrapper 9.8.1 acompanha o projeto. O código Java usa compatibilidade de linguagem Java 11 e o `targetSdk` é 35.

## Executar localmente

```sh
git clone https://github.com/joao-pedro-gms/PocketArcade.git
cd PocketArcade
```

Abra a pasta no Android Studio, configure o SDK pelo SDK Manager e aguarde a sincronização do Gradle. Selecione um emulador ou dispositivo Android e execute o módulo `app`.

Para compilar pelo terminal, configure `ANDROID_HOME` para o SDK instalado ou crie um `local.properties` na raiz com `sdk.dir` apontando para o SDK. Esse arquivo é local e não é versionado.

No Windows (PowerShell):

```powershell
.\gradlew.bat assembleDebug
```

No Linux ou macOS:

```sh
./gradlew assembleDebug
```

O APK de desenvolvimento será gerado em `app/build/outputs/apk/debug/app-debug.apk`.

## Verificações

Compilar o aplicativo, executar o Android Lint e compilar os testes instrumentados:

```powershell
.\gradlew.bat assembleDebug lintDebug assembleDebugAndroidTest
```

Para executar os testes instrumentados existentes, conecte um dispositivo ou inicie um emulador e rode:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

No Linux e macOS, substitua `.\gradlew.bat` por `./gradlew`. O relatório do Lint fica em `app/build/reports/lint-results-debug.html`.

## Estrutura

```text
app/
  src/main/
    java/br/com/joaop/pocketarcade/  # Activities, adaptadores e utilitários
    res/                           # Layouts, ícones, cores, textos e sons
    assets/                        # Créditos e licença dos ícones
    AndroidManifest.xml
  src/androidTest/                 # Testes instrumentados dos diálogos de resultado
gradle/                           # Wrapper e configuração da JVM do Gradle
build.gradle                      # Plugins do projeto
settings.gradle                   # Repositórios de dependências e módulos
```

Arquivos gerados, configurações da máquina, variáveis de ambiente e chaves de assinatura ficam fora do Git. Consulte [CONTRIBUTING.md](CONTRIBUTING.md) para orientações de desenvolvimento.

## Autoria e créditos

Projeto acadêmico de **João Pedro G M Silva**, da disciplina Desenvolvimento de Aplicações para Dispositivos Móveis.

Ícones: [Lucide](https://lucide.dev/), sob licenças ISC/MIT. O texto completo acompanha a aplicação em [app/src/main/assets/lucide-LICENSE.txt](app/src/main/assets/lucide-LICENSE.txt).
