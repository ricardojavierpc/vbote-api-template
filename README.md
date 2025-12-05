# vBote API - Plantilla Arquitectura Hexagonal

## !!Esta documentación esta crear por Claude!!
## La plantilla tambien esta generada por Claude!! 

Plantilla vacía para implementar la API de gestión de usuarios y sesiones.

## 📁 Estructura del Proyecto

```
src/main/java/com/vbote/api/
│
├── VboteApiApplication.java          ← Clase principal Spring Boot
│
├── domain/                           ← 🎯 NÚCLEO (sin dependencias externas)
│   │
│   ├── model/                        ← Entidades de dominio
│   │   ├── User.java                    → Crear: id, username, password, role, blocked, createdAt, updatedAt
│   │   └── Session.java                 → Crear: id, user, token, ipAddress, createdAt, active
│   │
│   ├── port/
│   │   ├── in/                       ← Puertos de entrada (interfaces de casos de uso)
│   │   │   ├── UserUseCase.java         → Crear: createUser, getAllUsers, getUserById, updateUser, blockUser
│   │   │   └── SessionUseCase.java      → Crear: login, getActiveSessions, logout, closeAllUserSessions
│   │   │
│   │   └── out/                      ← Puertos de salida (interfaces de repositorios)
│   │       ├── UserRepository.java      → Crear: save, findById, findByUsername, findAll, existsByUsername
│   │       ├── SessionRepository.java   → Crear: save, findByToken, findAllActive, deactivateAllByUserId
│   │       ├── PasswordEncoder.java     → Crear: encode, matches
│   │       └── TokenProvider.java       → Crear: generateToken, validateToken, getUsernameFromToken
│   │
│   └── exception/                    ← Excepciones de dominio
│       ├── DomainException.java         → Crear: clase base abstracta
│       ├── UserNotFoundException.java
│       ├── UserAlreadyExistsException.java
│       ├── UserBlockedException.java
│       ├── InvalidCredentialsException.java
│       ├── SessionNotFoundException.java
│       └── UnauthorizedException.java
│
├── application/                      ← 📦 CAPA DE APLICACIÓN
│   │
│   └── service/                      ← Implementación de casos de uso
│       ├── UserService.java             → Implementa UserUseCase, usa @Transactional
│       └── SessionService.java          → Implementa SessionUseCase, usa @Transactional
│
└── infrastructure/                   ← 🔌 ADAPTADORES (frameworks y BD)
    │
    ├── adapter/
    │   │
    │   ├── in/web/                   ← Adaptadores de entrada (HTTP)
    │   │   │
    │   │   ├── controller/           ← REST Controllers
    │   │   │   ├── UserController.java      → @RestController, /api/users
    │   │   │   └── SessionController.java   → @RestController, /api/sessions
    │   │   │
    │   │   ├── servlet/              ← Servlets (requeridos por el ejercicio)
    │   │   │   ├── UserServlet.java         → @WebServlet, /servlet/users/*
    │   │   │   └── SessionServlet.java      → @WebServlet, /servlet/sessions/*
    │   │   │
    │   │   ├── filter/               ← Filtros HTTP
    │   │   │   ├── RequestLoggingFilter.java    → Log de método, endpoint, timestamp
    │   │   │   ├── AuthenticationFilter.java    → Validación de JWT
    │   │   │   └── RateLimitFilter.java         → 10 requests/minuto por IP
    │   │   │
    │   │   ├── dto/                  ← Data Transfer Objects
    │   │   │   ├── UserDto.java             → CreateRequest, UpdateRequest, Response
    │   │   │   ├── SessionDto.java          → LoginRequest, LoginResponse, Response
    │   │   │   └── ErrorResponse.java       → Para errores de API
    │   │   │
    │   │   └── mapper/               ← MapStruct mappers
    │   │       ├── UserWebMapper.java       → DTO ↔ Domain
    │   │       └── SessionWebMapper.java    → DTO ↔ Domain
    │   │
    │   └── out/                      ← Adaptadores de salida
    │       │
    │       ├── persistence/          ← JPA/Hibernate
    │       │   │
    │       │   ├── entity/           ← Entidades JPA
    │       │   │   ├── UserEntity.java      → @Entity, @Table("users")
    │       │   │   └── SessionEntity.java   → @Entity, @Table("sessions")
    │       │   │
    │       │   ├── repository/       ← Spring Data repositories
    │       │   │   ├── JpaUserRepository.java       → extends JpaRepository
    │       │   │   └── JpaSessionRepository.java    → extends JpaRepository
    │       │   │
    │       │   ├── mapper/           ← Entity ↔ Domain mappers
    │       │   │   ├── UserPersistenceMapper.java
    │       │   │   └── SessionPersistenceMapper.java
    │       │   │
    │       │   ├── UserPersistenceAdapter.java      → Implementa UserRepository (port)
    │       │   └── SessionPersistenceAdapter.java   → Implementa SessionRepository (port)
    │       │
    │       └── security/             ← Implementaciones de seguridad
    │           ├── JwtTokenProvider.java        → Implementa TokenProvider
    │           └── BcryptPasswordEncoder.java   → Implementa PasswordEncoder
    │
    └── config/                       ← Configuraciones Spring
        ├── OpenApiConfig.java            → Configuración Swagger
        └── GlobalExceptionHandler.java   → @ControllerAdvice para excepciones
```

## 🔄 Flujo de Dependencias

```
┌─────────────────────────────────────────────────────────────────┐
│                        INFRASTRUCTURE                            │
│  ┌──────────────┐                           ┌──────────────┐    │
│  │  Controllers │                           │  Persistence │    │
│  │  Servlets    │                           │  Adapters    │    │
│  │  Filters     │                           │  Security    │    │
│  └──────┬───────┘                           └──────▲───────┘    │
│         │                                          │             │
│         │ usa                            implementa│             │
│         ▼                                          │             │
│  ┌─────────────────────────────────────────────────┴──────┐     │
│  │                     APPLICATION                         │     │
│  │  ┌─────────────────────────────────────────────────┐   │     │
│  │  │            Services (Use Case Impl)             │   │     │
│  │  └─────────────────────┬───────────────────────────┘   │     │
│  │                        │                                │     │
│  │                        │ usa                            │     │
│  │                        ▼                                │     │
│  │  ┌─────────────────────────────────────────────────┐   │     │
│  │  │                    DOMAIN                        │   │     │
│  │  │  ┌─────────┐  ┌──────────┐  ┌──────────────┐   │   │     │
│  │  │  │ Models  │  │ Ports In │  │  Ports Out   │   │   │     │
│  │  │  │         │  │(UseCases)│  │(Repositories)│   │   │     │
│  │  │  └─────────┘  └──────────┘  └──────────────┘   │   │     │
│  │  └─────────────────────────────────────────────────┘   │     │
│  └────────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────────┘

⚠️ REGLA IMPORTANTE: El dominio NO depende de nada externo
   - domain/ NO importa nada de infrastructure/
   - domain/ NO importa Spring, JPA, etc.
```

## 📝 Orden Recomendado de Implementación

### 1️⃣ Dominio (Primero - Sin dependencias)
1. `domain/model/User.java` - Entidad de dominio
2. `domain/model/Session.java` - Entidad de dominio  
3. `domain/exception/*` - Excepciones de negocio
4. `domain/port/out/*` - Interfaces de repositorios
5. `domain/port/in/*` - Interfaces de casos de uso

### 2️⃣ Aplicación (Segundo - Lógica de negocio)
6. `application/service/UserService.java`
7. `application/service/SessionService.java`

### 3️⃣ Infraestructura (Tercero - Implementaciones)
8. `infrastructure/adapter/out/persistence/entity/*` - Entidades JPA
9. `infrastructure/adapter/out/persistence/repository/*` - JPA Repositories
10. `infrastructure/adapter/out/persistence/mapper/*` - Mappers Entity↔Domain
11. `infrastructure/adapter/out/persistence/*Adapter.java` - Implementan ports
12. `infrastructure/adapter/out/security/*` - JWT y BCrypt
13. `infrastructure/adapter/in/web/dto/*` - DTOs
14. `infrastructure/adapter/in/web/mapper/*` - Mappers DTO↔Domain
15. `infrastructure/adapter/in/web/controller/*` - REST Controllers
16. `infrastructure/adapter/in/web/servlet/*` - Servlets
17. `infrastructure/adapter/in/web/filter/*` - Filtros
18. `infrastructure/config/*` - Configuraciones

## 🚀 Comandos

```bash
# Compilar
mvn clean install

# Ejecutar
mvn spring-boot:run

# Tests
mvn test
```

## 🔗 URLs

- API: http://localhost:8080/api/users
- Swagger: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console

## 📋 Entidades Requeridas

### User
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | PK, auto-generated |
| username | String | Único, requerido |
| password | String | Encriptado con BCrypt |
| role | Enum | ADMIN, USER |
| blocked | Boolean | Default false |
| createdAt | LocalDateTime | Auto-set |
| updatedAt | LocalDateTime | Auto-update |

### Session
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | PK, auto-generated |
| user | User | FK, ManyToOne |
| token | String | JWT token |
| ipAddress | String | IP del cliente |
| createdAt | LocalDateTime | Auto-set |
| active | Boolean | Default true |

## ✅ Requerimientos del Ejercicio

- [x] Java 8+ sprign boot 
- [x] Crear usuarios
- [x] Obtener usuarios con filtros
- [x] Obtener usuario por ID
- [x] Actualizar usuario
- [x] Bloquear usuario
- [x] Iniciar sesión (login)
- [x] Listar sesiones activas
- [x] Cerrar sesión (logout)
- [x] Cerrar todas las sesiones de un usuario
- [x] Implementar 2 Servlets
- [x] Filtro de logging (método, endpoint, timestamp)
- [x] Filtro de autenticación
- [x] Hibernate para persistencia
- [x] Control de transacciones

<img width="611" height="754" alt="Bildschirmfoto 2025-12-05 um 13 49 48" src="https://github.com/user-attachments/assets/f0c48f06-5546-4f88-bbca-21ed29abdd21" />

<img width="889" height="950" alt="Bildschirmfoto 2025-12-05 um 13 48 44" src="https://github.com/user-attachments/assets/e270739b-ea0b-44f1-9655-fcfd30c4cb16" />

