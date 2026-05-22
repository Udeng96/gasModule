# KAIST GIS 통합관제 플랫폼 — 💨 가스 이벤트 수집 모듈

> **한 줄 소개**  
> 외부 가스 감지 시스템의 **MSSQL DB를 주기적으로 polling**해서 새 이벤트를 가져와, 자체 PostgreSQL DB에 저장하고 **WebSocket으로 메인 관제 화면에 push**하는 백엔드 모듈입니다.

---

## 🏛️ 전체 시스템 구성

이 저장소는 **KAIST GIS 통합관제 플랫폼**을 구성하는 여러 저장소 중 **가스 이벤트 수집 모듈**입니다. 클릭하면 다른 모듈 저장소로 이동합니다.

| 모듈 | 역할 | 저장소 |
|---|---|---|
| 🖥️ GIS 메인 (대시보드 + 백엔드) | 관제 화면 + 시설·이벤트·순찰 API | [kaist-gis-campus](https://github.com/Udeng96/kaist-gis-campus) |
| 🔥 화재 이벤트 수집 모듈 | 외부 화재 시스템과 TCP 연동 | [ibs-fire-module](https://github.com/Udeng96/ibs-fire-module) |
| 💨 **가스 이벤트 수집 모듈 (이 저장소)** | 외부 가스 DB에서 polling | **gasModule** *(현재)* |
| 🔆 불꽃 감지 모듈 | _(추가 예정)_ | _(추가 예정)_ |

### 시스템 아키텍처 안에서 이 모듈의 위치

```
                ┌──────────────────────┐
                │   외부 가스 감지 시스템   │
                │   (MSSQL Database)   │
                └──────────┬───────────┘
                           │ DB Polling (주기적 조회)
                           ▼
   ┌───────────────────────────────────────────────┐
   │  💨  가스 이벤트 수집 모듈 (이 저장소)              │
   │                                               │
   │   ① 스케줄러가 일정 주기로 MSSQL을 조회           │
   │   ② 새로 들어온 raw 가스 데이터를 가져옴          │
   │   ③ 자체 PostgreSQL에 가스 이벤트·로그로 저장     │
   │   ④ WebSocket으로 메인 GIS에 실시간 push        │
   └───────────────────────────┬───────────────────┘
                               │ WebSocket
                               ▼
                ┌──────────────────────────┐
                │  🖥️  GIS 메인 (대시보드)    │
                │   → 지도에 가스 알림 표시    │
                └──────────────────────────┘
```

> **🔥 화재 모듈과의 차이**: 화재 모듈은 외부 시스템과 **TCP 소켓**으로 직접 연결하는 방식인 반면, 가스 모듈은 외부 시스템의 **MSSQL DB를 주기적으로 polling**하는 방식입니다. 외부 시스템이 어떤 인터페이스를 제공하느냐에 따라 모듈 구조가 다릅니다.

---

## 🎯 이 모듈이 하는 일

KAIST 캠퍼스의 **가스 감지 시스템(외부 업체 시스템)이 MSSQL DB에 쌓아두는 이벤트를 주기적으로 가져와 처리**하고, 관제 화면에 즉시 알릴 수 있는 형태로 만들어주는 역할을 합니다.

구체적으로는:
- 일정 주기마다 **외부 MSSQL DB를 조회**하여 새 가스 이벤트 확인
- 가져온 raw 데이터를 **자체 PostgreSQL에 가스 이벤트/센서/로그로 정규화하여 저장**
- 처리된 이벤트를 **WebSocket으로 메인 GIS 화면에 push** → 운영자가 즉시 인지
- **2개의 DB(외부 MSSQL + 자체 PostgreSQL)를 동시에 연결**해서 사용

---

## 🛠️ 주요 기능

- **다중 DB 연결** — 외부 MSSQL(읽기 전용) + 자체 PostgreSQL(읽기·쓰기) 동시 사용
- **DB Polling 스케줄러** — 일정 주기로 외부 MSSQL을 조회해 새 데이터 발견
- **데이터 정규화** — 외부 raw 데이터를 자체 도메인 모델(GasEvent, GasLog, GasSensor)로 변환·저장
- **센서 캐시** — 자주 조회되는 가스 센서 정보를 메모리에 캐싱
- **WebSocket Push** — 메인 GIS 백엔드/프론트로 새 이벤트 실시간 전달
- **로컬 Mock 설정** — 로컬 개발 시 외부 MSSQL 없이도 동작하도록 Mock 구성

---

## 👤 담당 역할 (단독 개발)

- **외부 가스 시스템 MSSQL DB 연계** 설계 및 구현
- 외부 raw 데이터 → 자체 정규화된 도메인 모델 **변환 로직 설계**
- **다중 DB(MSSQL + PostgreSQL) 환경** 설정 및 트랜잭션 관리
- 외부 DB 없이도 개발할 수 있도록 **로컬 Mock 환경** 구성
- 전체 4개 저장소(메인 + 화재/가스/불꽃 모듈) 단독 개발

---

## 🧱 사용한 기술

| 영역 | 기술 |
|---|---|
| 언어/프레임워크 | Java 1.8, Spring Boot 2.7.18 |
| 라이브러리 | Spring Data JPA, Spring WebSocket, Spring Scheduler, Lombok |
| 데이터베이스 | **PostgreSQL** (자체) + **MSSQL** (외부, 읽기 전용) |
| 외부 연동 | **DB Polling** (외부 MSSQL) |
| 내부 통신 | **WebSocket** (메인 GIS로 이벤트 push) |
| 빌드 | Maven |

---

## 📂 전체 폴더 구조

```
gasModule/
├── src/main/java/com/eseict/gasmodule/
│   ├── schedule/      ⭐ 스케줄러 (주기적 polling)
│   │   └── GasPolling.java        — 외부 MSSQL DB를 주기적으로 조회
│   │
│   ├── repository/    ⭐ 다중 DB 접근
│   │   ├── mssql/     — 외부 MSSQL (raw 가스 데이터 읽기)
│   │   └── postgres/  — 자체 PostgreSQL (정규화된 이벤트 저장)
│   │
│   ├── data/
│   │   ├── mssql/     — 외부 MSSQL DTO·엔티티 (TblEventGas, GasRawInfo)
│   │   ├── postgres/  — 자체 PostgreSQL 엔티티 (GasEvent, GasSensor, GasLog)
│   │   └── constant/  — 상수 (StateEnum, LevelEnum)
│   │
│   ├── serv/          — 비즈니스 로직
│   │   ├── GasEventService.java   — 가스 이벤트 처리·저장
│   │   └── GasRawService.java     — raw 데이터 가공
│   │
│   ├── cache/         — GasCache (메모리 캐시)
│   │
│   ├── websocket/     ⭐ 메인 GIS로 이벤트 push
│   │   ├── WebSocket.java
│   │   ├── WebSocketHandler.java
│   │   └── WebSocketManager.java
│   │
│   ├── config/        ⭐ 다중 DB 설정
│   │   ├── MssqlConfig.java          — 외부 MSSQL 연결 설정
│   │   ├── PostgresqlConfig.java     — 자체 PostgreSQL 연결 설정
│   │   ├── LocalMssqlMockConfig.java — 로컬 개발용 Mock
│   │   └── WebSocketConfig.java
│   │
│   ├── util/          — 공통 유틸
│   └── GasModuleApplication.java
│
├── pom.xml            — Maven 의존성
└── README.md          — 이 문서
```

> ⭐ 표시: 코드를 처음 볼 때 가장 먼저 열어보면 좋은 곳

---

## 🗺️ 기능 → 코드 위치 매핑

**파일/폴더명을 클릭하면 GitHub에서 바로 해당 위치로 이동합니다.**

| 기능 | 어떤 역할을 하는지 | 핵심 파일 |
|---|---|---|
| **DB Polling 스케줄러** | 일정 주기로 외부 MSSQL을 조회해 새 가스 이벤트 발견 | [schedule/GasPolling.java](src/main/java/com/eseict/gasmodule/schedule/GasPolling.java) |
| **외부 MSSQL DB 접근** | 외부 가스 감지 시스템의 raw 이벤트 데이터를 읽어옴 | [repository/mssql/GasRepository.java](src/main/java/com/eseict/gasmodule/repository/mssql/GasRepository.java) |
| **자체 PostgreSQL 접근** | 정규화된 가스 이벤트·센서·로그를 자체 DB에 저장·조회 | [repository/postgres/](src/main/java/com/eseict/gasmodule/repository/postgres) |
| **가스 이벤트 처리 로직** | 외부 raw 데이터를 자체 도메인 모델로 변환해 저장하는 핵심 로직 | [serv/GasEventService.java](src/main/java/com/eseict/gasmodule/serv/GasEventService.java) |
| Raw 데이터 가공 | 외부 MSSQL에서 받은 데이터 가공 처리 | [serv/GasRawService.java](src/main/java/com/eseict/gasmodule/serv/GasRawService.java) |
| **WebSocket Push** | 처리된 이벤트를 메인 GIS로 실시간 전송 | [websocket/WebSocketHandler.java](src/main/java/com/eseict/gasmodule/websocket/WebSocketHandler.java) |
| WebSocket 연결 관리 | 다수 클라이언트 연결 상태 관리 | [websocket/WebSocketManager.java](src/main/java/com/eseict/gasmodule/websocket/WebSocketManager.java) |
| **가스 센서 캐시** | 자주 쓰는 센서 정보를 메모리에 캐싱하여 성능 향상 | [cache/GasCache.java](src/main/java/com/eseict/gasmodule/cache/GasCache.java) |
| 자체 DB 엔티티 | 가스 이벤트·센서·로그 (PostgreSQL용) | [data/postgres/domain/](src/main/java/com/eseict/gasmodule/data/postgres/domain) |
| 외부 DB 엔티티 | 외부 MSSQL의 raw 이벤트 테이블 매핑 | [data/mssql/](src/main/java/com/eseict/gasmodule/data/mssql) |
| **MSSQL 연결 설정** | 외부 MSSQL DataSource 설정 (읽기 전용) | [config/MssqlConfig.java](src/main/java/com/eseict/gasmodule/config/MssqlConfig.java) |
| **PostgreSQL 연결 설정** | 자체 PostgreSQL DataSource 설정 | [config/PostgresqlConfig.java](src/main/java/com/eseict/gasmodule/config/PostgresqlConfig.java) |
| 로컬 Mock 설정 | 로컬 개발 시 외부 MSSQL 없이도 동작하도록 Mock 데이터소스 | [config/LocalMssqlMockConfig.java](src/main/java/com/eseict/gasmodule/config/LocalMssqlMockConfig.java) |

---

## 🔍 처음 코드를 보는 분께 — 추천 탐색 순서

1. **[GasModuleApplication.java](src/main/java/com/eseict/gasmodule/GasModuleApplication.java)** — Spring Boot 진입점
2. **[config/MssqlConfig.java](src/main/java/com/eseict/gasmodule/config/MssqlConfig.java)** + **[PostgresqlConfig.java](src/main/java/com/eseict/gasmodule/config/PostgresqlConfig.java)** — 두 DB가 어떻게 연결되는지
3. **[schedule/GasPolling.java](src/main/java/com/eseict/gasmodule/schedule/GasPolling.java)** — 외부 MSSQL을 어떻게 주기적으로 조회하는지
4. **[serv/GasEventService.java](src/main/java/com/eseict/gasmodule/serv/GasEventService.java)** — raw 데이터가 자체 도메인으로 어떻게 변환되어 저장되는지
5. **[websocket/WebSocketHandler.java](src/main/java/com/eseict/gasmodule/websocket/WebSocketHandler.java)** — 처리된 이벤트가 메인으로 어떻게 전달되는지
6. 메인에서 이 이벤트가 어떻게 화면에 표출되는지 궁금하면 → [kaist-gis-campus](https://github.com/Udeng96/kaist-gis-campus) 참고

---

## 🔗 관련 저장소

- 🖥️ [kaist-gis-campus](https://github.com/Udeng96/kaist-gis-campus) — 메인 GIS 대시보드 + 백엔드 (이 모듈이 push하는 곳)
- 🔥 [ibs-fire-module](https://github.com/Udeng96/ibs-fire-module) — 화재 이벤트 수집 모듈 (자매 모듈, TCP 방식)
- 🔆 불꽃 감지 모듈 _(추가 예정)_
