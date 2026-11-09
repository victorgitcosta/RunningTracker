**Nome do Aluno: João Victor Costa Pereira**

**Data de Entrega: 08/11/2026**

**Justificativa da Escolha do Tema: Sou corredor e não gosto dos aplicativos mais comuns para monitoramento de corrida. Eles requerem muitas permissões do usuário, sendo um possível risco de concessão de dados.**

**Descrição do Funcionamento do Aplicativo: O aplicativo tem 2 telas, a primeira tela possui um botão de "Começar" para começar o monitoramento de tempo de corrida, trajeto no mapa, dentre indicadores fisiológicos. O trajeto no mapa não está funcional devido ao planejamento inicial de usar Google Maps, mas o Google Maps requer pagamento pelo uso do API, essa parte está incompleta. Não há método de coleta dos indicadores fisiológicos adequadas também, está sendo feito uma presunção com base na idade, sexo e IMC do usuário.**


O Projeto RunningTracker monitora o percurso e indicadores de performance de corrida. Essa aplicação é uma alternativa fora da nuvem e sem requisição de conta para acompanhamento digital de corrida através de smartphone. O projeto foi realizado utilizando Figma para prototipação e Kotlin, Android Studio, JetPack Compose, e Room conforme solicitado pelo entregável do curso de Kotlin Intermediário. Esse projeto usou IA para a geração de documentação e geração de código através do Gemini e Chat GPT.

<img width="461" height="858" alt="1 run_track" src="https://github.com/user-attachments/assets/0909e6c1-7f41-40be-a948-a0d1ff232865" />
<img width="466" height="879" alt="2 runs" src="https://github.com/user-attachments/assets/99680baa-f023-4ebe-ad4b-eab072c3c64c" />

---

## 1. Mapeamento de Entidades e Banco de Dados (Room Database)
A entidade central do aplicativo é a corrida (`Run`). O Room armazenará esses dados localmente.

### Entidade: `RunEntity`
| Variável | Tipo | Descrição |
| :--- | :--- | :--- |
| `id` | `Int` (AutoGenerate) | Chave primária única da corrida. |
| `timestamp` | `Long` | Data e hora do início da corrida (útil para filtros e ordenação). |
| `durationInMillis` | `Long` | Tempo total de corrida. |
| `distanceInMeters` | `Float` | Distância total percorrida. |
| `caloriesBurned` | `Int` | Gasto calórico estimado. |
| `avgPace` | `Float` | Ritmo médio (min/km). |
| `avgBpm` | `Int` | Batimentos por minuto médios (simulado ou via sensor). |
| `routeCoordinates` | `String` (JSON) | Lista de coordenadas (Lat/Lng) convertidas para String usando um `TypeConverter`. |

### Interface DAO (Data Access Object): `RunDao`
*   **`insertRun(run: RunEntity)`**: Salva a corrida no banco após o clique no botão "Pausar/Finalizar".
*   **`getAllRunsSortedByDateDesc(): Flow<List<RunEntity>>`**: Retorna corridas em ordem decrescente.
*   **`getAllRunsSortedByDateAsc(): Flow<List<RunEntity>>`**: Retorna corridas em ordem crescente.
*   **`getRunsBySpecificDate(startOfDay: Long, endOfDay: Long): Flow<List<RunEntity>>`**: Filtra corridas por um dia específico.

---

## 2. Mapeamento de Serviços (Localização e Sensores)
Necessário para manter o monitoramento de GPS ativo em background.

### Classe: `TrackingService` (Foreground Service)
*   **Variáveis:**
    *   `isTracking: MutableStateFlow<Boolean>`: Status atual do rastreamento.
    *   `pathPoints: MutableStateFlow<List<LatLng>>`: Lista observável das coordenadas atuais.
    *   `timeRunInMillis: MutableStateFlow<Long>`: Cronômetro em tempo real.
*   **Métodos:**
    *   `startTrackingService()`: Inicia o serviço e a requisição de atualizações de GPS via `FusedLocationProviderClient`.
    *   `pauseTrackingService()`: Pausa o cronômetro e as atualizações de localização.
    *   `addPathPoint(location: Location)`: Adiciona a nova coordenada na lista `pathPoints`.

---

## 3. Mapeamento de ViewModels

### 3.1. `TrackingViewModel`
Responsável pela lógica da tela de rastreamento de corrida. Conecta-se ao `TrackingService` e ao `RunRepository`.

*   **Variáveis de Estado (UI State):**
    *   `isTracking: StateFlow<Boolean>`
    *   `pathPoints: StateFlow<List<LatLng>>`
    *   `currentTimeInMillis: StateFlow<Long>`
    *   `currentDistance: StateFlow<Float>`
    *   `calories: StateFlow<Int>`
    *   `currentPace: StateFlow<Float>`
    *   `currentBpm: StateFlow<Int>`
*   **Métodos:**
    *   `toggleTracking()`: Alterna entre iniciar ou pausar o serviço.
    *   `finishRun()`: Pega os dados atuais, cria um `RunEntity` e salva no Repositório.
    *   `calculateMetrics()`: Atualiza calorias e ritmo conforme distância e tempo.

### 3.2. `HistoryViewModel`
Responsável por buscar e manipular os dados salvos para a tabela de registros.

*   **Variáveis de Estado (UI State):**
    *   `runs: StateFlow<List<RunEntity>>`
    *   `sortType: StateFlow<SortType>`: Enum (DATE_ASC, DATE_DESC).
    *   `dateFilter: StateFlow<Long?>`
*   **Métodos:**
    *   `loadRuns()`: Busca os dados no DAO.
    *   `setSortType(type: SortType)`: Altera a ordenação e refaz a query.
    *   `filterByDate(dateInMillis: Long)`: Aplica o filtro de data.
    *   `clearFilters()`: Remove filtros e traz todo o histórico.

---

## 4. Mapeamento de UI (Jetpack Compose) e Navegação

### Navegação
*   **`AppNavigation` (NavHost)** contendo `Route.Tracking` e `Route.History`.

### Tela 1: `TrackingScreen` (Inicial)
*   **GoogleMap**: Renderiza o mapa com `Polyline` conectando os `pathPoints`. Adiciona `Marker` no primeiro ponto (início) e último ponto (fim/pausa).
*   **MetricsCard**: Exibe Tempo, BPM, Pace, Calorias e Distância em tempo real.
*   **FloatingActionButton**: Iniciar/Pausar. Ao pausar, exibe a opção de Salvar.

### Tela 2: `HistoryScreen` (Registros)
*   **TopAppBar**: Título, Ícone de Calendário (filtro) e Ícone de Ordenação.
*   **DatePickerDialog**: Seleção do dia para filtro.
*   **LazyColumn**: Tabela/lista de registros.
*   **RunItem**: Card individual exibindo Data, Distância, Tempo, Calorias e Pace médio.
