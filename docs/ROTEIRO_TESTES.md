# Roteiro de validação funcional

- [ ] Gradle sincroniza sem erros (Android SDK 36, JDK 17, wrapper 8.13).
- [ ] `Maps SDK for Android` configurada e mapa renderizado.
- [ ] Acesso à localização solicitado ao tocar em Play.
- [ ] Sem permissão, app avisa e não inicia serviço.
- [ ] Com permissão e GPS, o mapa segue a posição e desenha a Polyline.
- [ ] Cronômetro progride quando em andamento.
- [ ] Pausar interrompe relógio e captura de novos pontos.
- [ ] Retomar reinicia ambos sem zerar os dados da sessão.
- [ ] Botão Finalizar e salvar aparece apenas após pausar.
- [ ] Registro salvo no Room aparece na tela Histórico.
- [ ] Ordenação ascendente/descendente funciona.
- [ ] Calendário filtra por data local corretamente.
- [ ] Limpar filtros restaura a lista completa.
- [ ] Ao reiniciar o aplicativo, o histórico persiste.
- [ ] Durante corrida ativa e com a tela apagada, notificação e serviço permanecem ativos em dispositivo de teste compatível.
- [ ] `testDebugUnitTest` passa.

**Aviso:** não usar BPM fictício ou estimativa de calorias para decisões médicas ou de treino. Testar GPS em área aberta ou rota simulada realista.
