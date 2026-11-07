# Running Tracker — Kotlin + Jetpack Compose + Room + MVVM

Aplicativo Android de **2 telas** inspirado nas referências `docs/referencias/`. Implementação fundamentada em `docs/projeto_de_app_de_corrida.md`.

## O que já foi implementado

- **Tela Mapa / TrackingScreen:** Google Maps Compose, marcador do primeiro/último ponto, Polyline, distância, calorias estimadas, pace, BPM demonstrativo, cronômetro e botão Iniciar/Pausar/Retomar. Ao pausar, **Finalizar e salvar**.
- **Tela Histórico / HistoryScreen:** lista com as corridas persistidas, resumo, ordenar por data crescente/decrescente, DatePicker para filtrar por dia, limpar filtros.
- **Navigation Compose:** navegação inferior entre as duas telas.
- **Room Database:** `RunEntity`, `RunDao`, `RunDatabase`, `RouteJsonConverter` e `RoomRunRepository`.
- **MVVM:** `TrackingViewModel`, `HistoryViewModel` e `StateFlow`. Organização em camadas.
- **Foreground Service:** `TrackingService` com FusedLocationProviderClient e notificação persistente. Registra a corrida com a tela apagada **enquanto o serviço estiver ativo e o processo não for encerrado**.
- **Testes unitários:** `MetricsCalculatorTest`.

> **Atenção:** o mapa exige sua própria chave do Google Maps (pode exigir faturamento habilitado na Google Cloud). A frequência cardíaca **não é medida**: 132 BPM é um número demonstrativo. As calorias usam estimativa simples de 70 kg × quilômetros, não uma medida clínica. O projeto não inclui persistência de uma corrida *em andamento* se o processo for encerrado pelo sistema.

## Tutorial — configurar e executar no Android Studio (Windows/macOS/Linux)

### 1. Preparar ferramentas

1. Instale o **Android Studio** atualizado.
2. Em **Tools > SDK Manager**, instale **Android SDK Platform 36**, Build Tools e Android SDK Platform-Tools.
3. Em **Settings > Build, Execution, Deployment > Build Tools > Gradle**, selecione **Gradle JDK 17** (o JDK embutido no Studio pode servir se for 17).
4. Prepare um dispositivo físico Android 8+ (API 26+) com GPS **ou** emulador com imagem **Google Play / Google APIs**. O Maps não funciona bem em um emulador AOSP sem os Google Play services.

### 2. Criar o projeto-base com Gradle Wrapper

O arquivo de inicialização binário `gradle-wrapper.jar` **não está incluído** nesta distribuição. A forma mais simples de obtê-lo é deixar o Android Studio gerar um projeto novo:

1. **File > New > New Project > Empty Activity**.
2. Nome **RunningTracker**, pacote **`com.exemplo.runningtracker`**, linguagem **Kotlin**, SDK mínimo **26**.
3. Aguarde o Android Studio gerar os arquivos `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar` e `local.properties`.
4. Feche o projeto no Studio e abra sua pasta no explorador de arquivos.
5. Copie **todo o conteúdo** deste ZIP para dentro da pasta recém-criada, **substituindo** os arquivos com mesmo nome (ex.: `app/build.gradle.kts`, `build.gradle.kts`, `settings.gradle.kts`, `app/src/main` e `gradle/wrapper/gradle-wrapper.properties`). **Mantenha os arquivos gerados** `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar` e `local.properties`.
6. Abra novamente o projeto com **File > Open** e clique em **Sync Project with Gradle Files**. O Gradle Wrapper usará a distribuição 8.13 definida no arquivo deste projeto.

Alternativa para quem já tem o comando `gradle` instalado: abra esta pasta no terminal e execute `gradle wrapper --gradle-version 8.13` para gerar os arquivos do wrapper; depois abra a pasta no Android Studio.

### 3. Configurar o Google Maps

1. Vá ao [Google Cloud Console](https://console.cloud.google.com/).
2. Crie ou selecione um projeto e habilite o **Maps SDK for Android** (Google Maps Platform). Configure uma conta de faturamento se o console exigir.
3. Em **APIs e serviços > Credenciais**, crie uma **chave de API**.
4. No arquivo `local.properties` da raiz do projeto, inclua **uma linha** (não apague `sdk.dir`, se existir):

   ```properties
   MAPS_API_KEY=SUA_CHAVE_AQUI
   ```

5. Para produção, **restrinja a chave** ao aplicativo Android com package name `com.exemplo.runningtracker` e impressão digital SHA-1 do certificado. Para identificar o SHA-1 de debug, use o painel Gradle **Tasks > android > signingReport** (se disponível) ou `./gradlew signingReport` / `gradlew.bat signingReport`.
6. Restrinja a chave também à **Maps SDK for Android**. Não envie `local.properties` ao GitHub.

Se o mapa aparecer cinza ou em branco, confira API, faturamento, chave, rede, SHA-1, pacote e imagem do emulador; consulte os erros de autorização no **Logcat**.

### 4. Executar

1. Conecte um celular com **Depuração USB** ativada, ou inicie o emulador.
2. Clique em **Run ▶ app**.
3. Na tela **Mapa**, toque no botão verde **Play**.
4. Autorize acesso à localização **Durante o uso do app**, preferencialmente **Localização precisa**. No Android 13+, a notificação de corrida também pode pedir autorização.
5. Caminhe/corra por alguns metros (ao ar livre, com GPS) e observe distância, rota e tempo.
6. Toque em **Pausar**. Toque em **Retomar** para continuar ou em **Finalizar e salvar** para armazenar a corrida.
7. O aplicativo abrirá **Histórico**. Verifique os cartões, o botão de ordenação e o calendário (filtro por dia).
8. Feche e reabra o app para confirmar que os registros **continuam salvos no Room**.

### 5. Testar sem sair de casa (emulador)

1. Execute o aplicativo em um AVD com **Google Play**.
2. No emulador: **More (...) > Location**. Carregue um arquivo GPX/KML de percurso (se a sua versão do emulador permitir) ou simule pontos de localização em sequência, com intervalos de alguns segundos.
3. Inicie a corrida no aplicativo e execute a rota de teste; deslocamentos inferiores a **2,5 m** são ignorados, assim como saltos de mais de **100 m** entre duas amostras (filtro básico anti-deriva).
4. Pause e salve. Distância e pace só serão significativos se o emulador receber **múltiplos pontos reais/simulados** ao longo do tempo.
5. Com a corrida ativa, apague a tela do dispositivo e confira a notificação: o serviço de primeiro plano deve continuar coletando GPS, conforme política/permissões do sistema.

### 6. Gerar APK instalável

No Android Studio: **Build > Build Bundle(s) / APK(s) > Build APK(s)**. O arquivo de debug será gerado em `app/build/outputs/apk/debug/app-debug.apk`.

Ou no terminal:

```bash
# macOS/Linux
./gradlew assembleDebug

# Windows (Prompt/PowerShell)
gradlew.bat assembleDebug
```

Para publicar na Play Store, gere um **AAB assinado** e revise políticas de localização, notificação e uso de foreground service.

## Arquitetura

```text
app/src/main/java/com/exemplo/runningtracker/
├── MainActivity.kt                  # Activity única
├── RunningTrackerApplication.kt     # DI manual e criação do banco
├── data/
│   ├── local/                      # RunEntity, RunDao, RunDatabase, RouteJsonConverter
│   └── repository/                 # Interface RunRepository + implementação Room
├── domain/
│   ├── model/                      # RoutePoint
│   ├── service/                    # TrackingService, TrackingSession
│   └── util/                       # MetricsCalculator, RunFormatters
├── presentation/
│   ├── navigation/                 # AppNavigation e Route
│   ├── tracking/                   # TrackingViewModel, TrackingScreen
│   └── history/                    # HistoryViewModel, HistoryScreen
└── ui/theme/                       # Paleta de cores
```

### Fluxo de informações

```text
TrackingScreen --> TrackingViewModel --> TrackingService --> FusedLocationProviderClient (GPS)
                         ^                    |
                         |                    v
                    TrackingSession <-- StateFlows (tempo, distância, pontos)
                         |
                  Finalizar e salvar
                         v
                   RunRepository --> RunDao --> SQLite (Room)
                                                  |
                    HistoryScreen <-- HistoryViewModel <--- Flow<List<RunEntity>>
```

### Entidade persistida

| Campo | Tipo | Uso |
| --- | --- | --- |
| id | Int | Chave primária autogerada |
| timestamp | Long | Data/hora de início |
| durationInMillis | Long | Duração |
| distanceInMeters | Float | Distância |
| caloriesBurned | Int | Estimativa de energia |
| avgPace | Float | Minutos por quilômetro |
| avgBpm | Int | BPM demonstrativo |
| routeCoordinates | String (JSON) | Rota codificada por `RouteJsonConverter` |

### Comandos Room relevantes

- `insertRun(run)` — inserir a corrida finalizada.
- `getAllRunsSortedByDateDesc()` — listar recentes primeiro.
- `getAllRunsSortedByDateAsc()` — listar antigas primeiro.
- `getRunsBySpecificDate(startOfDay, endOfDay)` — buscar registros no intervalo de um dia local.

## Limitações conscientes da versão 1.0

- **Sem sensor cardíaco real:** 132 BPM é demonstrativo e está rotulado como tal na UI.
- **Sem cálculo clínico de calorias:** usa fórmula aproximada com peso de referência fixo (70 kg); pode ser substituída por perfil de atleta.
- **Sessão em andamento fica apenas em memória:** se Android eliminar o processo, a corrida ainda não salva pode se perder. Para produção, persistir checkpoint de posição/tempo e recuperar o serviço após reinicialização controlada.
- **Sem segments para pausas:** os pontos de retomada permanecem na mesma Polyline; o cálculo de distância não soma salto ocorrido durante a pausa.
- **Sem animação personalizada de mapa:** as imagens de referência inspiram cores e hierarquia visual, mas não são uma réplica pixel a pixel.
- **Mapas exigem conectividade e conta do Google Maps Platform.** Os registros históricos ficam locais/offline.
- **Permissão de localização em segundo plano não solicitada:** o serviço é *iniciado a partir da interface visível* e pode continuar com a tela apagada. As restrições do Android ainda podem interromper o serviço.

## Bibliografia técnica

- [Maps Compose — Google Developers](https://developers.google.com/maps/documentation/android-sdk/maps-compose)
- [Android — tipos de serviços de primeiro plano](https://developer.android.com/develop/background-work/services/fgs/service-types)
- [Room Database — Android Developers](https://developer.android.com/training/data-storage/room)
- [Navigation Compose — Android Developers](https://developer.android.com/develop/ui/compose/navigation)
- [Permissões de localização — Android Developers](https://developer.android.com/develop/sensors-and-location/location/permissions)

## Rodar testes

```bash
./gradlew testDebugUnitTest   # macOS/Linux
# ou gradlew.bat testDebugUnitTest (Windows)
```

Os testes JUnit cobrem os cálculos básicos de pace e calorias.
