# Projeto de Aplicativo de Monitoramento de Corrida
**Tecnologias:** Kotlin, Android Studio, Jetpack Compose, Navigation Compose, Room Database, MVVM, Foreground Services, Google Maps Compose.

## 1. Arquitetura do Sistema (MVVM + Clean)
Para garantir escalabilidade e facilitar o desenvolvimento, o projeto deve ser dividido nas seguintes camadas:

*   **UI Layer (Camada de Visão):** Telas construídas 100% em Jetpack Compose. Observam os *States* expostos pelos ViewModels.
*   **Navigation:** Utiliza o Navigation Compose para transitar entre a `TrackingScreen` (Tela Inicial) e a `HistoryScreen` (Tela de Registros).
*   **Presentation Layer (ViewModels):** Gerenciam o estado da UI e as regras de negócio de apresentação. Comunicam-se com os Repositórios e Serviços.
*   **Domain/Service Layer:** Um `Foreground Service` (Serviço de Primeiro Plano) é estritamente necessário para capturar a localização (GPS) continuamente, mesmo se a tela for desligada.
*   **Data Layer (Repository & Room DB):** O Repositório abstrai a fonte de dados (Room) para que os ViewModels não saibam como os dados são salvos.

---

## 2. Mapeamento de Entidades e Banco de Dados (Room Database)
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

## 3. Mapeamento de Serviços (Localização e Sensores)
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

## 4. Mapeamento de ViewModels

### 4.1. `TrackingViewModel`
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

### 4.2. `HistoryViewModel`
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

## 5. Mapeamento de UI (Jetpack Compose) e Navegação

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