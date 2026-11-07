# Passo a passo — Running Tracker no Android Studio

## 1) Instale e configure
1. Atualize o Android Studio.
2. Acesse **Tools → SDK Manager** e instale o **Android SDK 36**.
3. Configure **Gradle JDK 17** em **Settings → Build, Execution, Deployment → Build Tools → Gradle**.
4. Prepare um dispositivo Android API 26+ ou emulador com **Google Play**.

## 2) Monte o projeto com Gradle Wrapper
1. No Android Studio: **New Project → Empty Activity**.
2. Nome: `RunningTracker`. Pacote: `com.exemplo.runningtracker`. Linguagem: Kotlin. Minimum SDK: API 26.
3. Aguarde a geração do novo projeto.
4. Feche-o e **descompacte o ZIP entregue**.
5. Copie o conteúdo da pasta `RunningTrackerAndroid` para dentro da pasta criada pelo Android Studio, **substituindo os arquivos existentes**.
6. Preserve os arquivos **`local.properties`, `gradlew`, `gradlew.bat` e `gradle/wrapper/gradle-wrapper.jar`** que o Android Studio gerou — o pacote contém o arquivo `gradle-wrapper.properties`, mas não o arquivo binário `.jar`.
7. Abra a pasta no Android Studio em **File → Open** e aguarde **Gradle Sync** (Gradle 8.13, Kotlin 2.2.20, AGP 8.11.1).

## 3) Coloque a chave Google Maps
1. No [Google Cloud Console](https://console.cloud.google.com/), crie projeto, ative **Maps SDK for Android** e configure faturamento, se solicitado.
2. Crie uma API key.
3. No arquivo `local.properties` da raiz, **mantenha `sdk.dir`** e acrescente:

```properties
MAPS_API_KEY=SUA_CHAVE_AQUI
```

4. Para distribuição, restrinja a chave ao pacote `com.exemplo.runningtracker`, SHA-1 do certificado e Maps SDK for Android.

## 4) Execute as duas telas
1. Em **Device Manager**, inicie um emulador com Google Play (ou conecte celular com Depuração USB).
2. Clique em **Run ▶ app**.
3. Em **Mapa**, toque no botão verde Iniciar e autorize localização **precisa, durante o uso**.
4. Movimente-se; o GPS traça a rota no Google Map e atualiza cronômetro, distância, calorias e pace.
5. Use **Pausar** e depois **Finalizar e salvar**.
6. Na tela **Histórico**, veja o registro salvo em Room. Use o calendário para filtrar por dia ou o botão de ordenação.
7. Feche e reabra o app: o histórico permanece armazenado.

## 5) Teste o rastreamento
- **GPS real:** teste em área aberta, caminhando/correndo por vários metros.
- **Emulador:** em **More → Location**, simule pontos ou carregue uma rota GPX/KML, dependendo da versão.
- **Em segundo plano:** com uma corrida iniciada, apague a tela e confira a notificação persistente do serviço. Não force o encerramento do app.
- **Testes unitários:** `gradlew.bat testDebugUnitTest` no Windows ou `./gradlew testDebugUnitTest` no Linux/macOS.

## 6) Gere o APK
**Build → Build Bundle(s) / APK(s) → Build APK(s)**. Resultado esperado: `app/build/outputs/apk/debug/app-debug.apk`.

**Observações:** o sensor BPM é demonstrativo (132 BPM) e as calorias são estimadas usando 70 kg. A corrida ativa não é recuperada após morte do processo; já as corridas salvas ficam persistidas no Room. Para explicações completas, consulte `README.md`.
