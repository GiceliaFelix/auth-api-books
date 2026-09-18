# Auth API — Autenticação e Autorização com JWT

API REST desenvolvida em **Java + Spring Boot** para autenticação e autorização de usuários, usando **JWT (access + refresh token)** e controle de acesso por **roles** (`ROLE_USER` / `ROLE_ADMIN`).

## ✨ Funcionalidades

- Registro de usuário com senha criptografada (BCrypt)
- Login com geração de **access token** (15 min) e **refresh token** (7 dias)
- Renovação de access token via refresh token
- Rotas protegidas por autenticação (`Bearer Token`)
- Controle de acesso por papel (`hasRole("ADMIN")`)
- Tratamento global de exceções com respostas padronizadas
- Documentação interativa via Swagger/OpenAPI
- Testes unitários (JUnit + Mockito) e de integração (MockMvc + H2)

## 🧱 Stack técnica

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.3 |
| Segurança | Spring Security 6 + JJWT 0.12 |
| Persistência | Spring Data JPA + PostgreSQL |
| Testes | JUnit 5, Mockito, MockMvc, H2 (em memória) |
| Documentação | springdoc-openapi (Swagger UI) |
| Build | Maven |
| Containerização | Docker + Docker Compose |

## 📂 Estrutura do projeto

```
src/main/java/com/gi/authapi/
├── config/          # Spring Security e OpenAPI
├── controller/       # Endpoints REST
├── dto/               # Records de entrada/saída (DTOs)
├── exception/        # Exceções customizadas + handler global
├── model/             # Entidades JPA (User, Role)
├── repository/       # Interfaces Spring Data JPA
├── security/          # JwtService e filtro de autenticação
└── service/           # Regras de negócio (AuthService, UserDetailsService)
```

## ▶️ Como rodar localmente

### Pré-requisitos
- Java 17+
- Maven 3.8+
- PostgreSQL rodando localmente (ou Docker)

### 1. Criar o banco de dados

```sql
CREATE DATABASE auth_api_db;
```

### 2. Configurar variáveis de ambiente (opcional)

Por padrão, a aplicação usa `postgres/postgres` como usuário/senha e um segredo JWT de exemplo. Para produção, **sempre** defina:

```bash
export DB_USERNAME=seu_usuario
export DB_PASSWORD=sua_senha
export JWT_SECRET=um_segredo_bem_grande_e_aleatorio
```

### 3. Rodar a aplicação

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`.

### 4. Acessar a documentação

```
http://localhost:8080/swagger-ui.html
```

## 🐳 Rodando com Docker

A forma mais rápida de subir a aplicação, já com o banco de dados incluso:

```bash
docker compose up --build
```

Isso sobe dois containers:
- `auth-api-db` — PostgreSQL 16
- `auth-api` — a aplicação, já conectada ao banco acima

A API fica disponível em `http://localhost:8080` e o Swagger em `http://localhost:8080/swagger-ui.html`, exatamente como na execução local.

Para derrubar os containers:

```bash
docker compose down
```

Para derrubar e também apagar os dados do banco:

```bash
docker compose down -v
```

> Por padrão o `docker-compose.yml` usa credenciais de exemplo (`postgres/postgres`) e o mesmo segredo JWT de exemplo do `application.yml`. Para rodar com valores próprios, defina `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET` no seu ambiente antes de subir os containers.

## 🔑 Endpoints principais

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/auth/register` | Público | Cria uma nova conta |
| POST | `/api/auth/login` | Público | Autentica e retorna os tokens |
| POST | `/api/auth/refresh` | Público | Gera novo access token |
| GET | `/api/users/me` | Autenticado | Dados do usuário logado |
| GET | `/api/admin/users` | ROLE_ADMIN | Lista todos os usuários |

### Exemplo — registro

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Gi Developer","email":"gi@example.com","password":"senha123"}'
```

### Exemplo — usando o token

```bash
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer SEU_ACCESS_TOKEN_AQUI"
```

## 🧪 Rodando os testes

```bash
mvn test
```

Os testes cobrem:
- Geração e validação de tokens JWT (`JwtServiceTest`)
- Regras de negócio de registro/login (`AuthServiceTest`, com Mockito)
- Fluxo completo via HTTP com banco H2 em memória (`AuthControllerIntegrationTest`)

## 🔒 Decisões de segurança

- Senhas nunca são armazenadas em texto puro (BCrypt).
- A aplicação é **stateless**: nenhuma sessão é guardada no servidor, só o token.
- O `access token` tem vida curta (15 min) para reduzir o risco em caso de vazamento; o `refresh token` permite renovar sem pedir login de novo.
- O segredo JWT do `application.yml` é apenas um valor de exemplo para rodar localmente — **nunca deve ir para produção**.

## 🚀 Possíveis evoluções

- Revogação de refresh tokens (blacklist em Redis)
- Confirmação de e-mail no registro
- Rate limiting no login
- Pipeline de CI/CD (GitHub Actions) para build e testes automáticos a cada push

---

Projetado como parte do portfólio de desenvolvimento backend em Java.
