# Case Supera - Sistema de Solicitação de Acesso a Módulos

Sistema corporativo desenvolvido em Java/Spring Boot para gerenciamento de solicitações de acesso a módulos. Usuários autenticados podem solicitar acesso a diferentes módulos do sistema, e o acesso é concedido automaticamente após validação das regras de negócio.

## 📋 Índice

- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Pré-requisitos](#-pré-requisitos)
- [Como Executar](#-como-executar)
- [Como Executar os Testes](#-como-executar-os-testes)
- [Como Visualizar Relatório de Cobertura](#-como-visualizar-relatório-de-cobertura)
- [Credenciais para Teste](#-credenciais-para-teste)
- [Exemplos de Requisições](#-exemplos-de-requisições)
- [Arquitetura da Solução](#-arquitetura-da-solução)
- [Decisões Técnicas](#-decisões-técnicas)
- [Endpoints da API](#-endpoints-da-api)
- [Regras de Negócio](#-regras-de-negócio-implementadas)

## 🛠 Tecnologias Utilizadas

### Backend e Framework
- **Java 21** (JDK 21.0.9) - Linguagem de programação obrigatória
- **Spring Boot 3.2.0** - Framework principal
- **Spring Data JPA** - Persistência de dados
- **Spring Security 6.2.0** - Segurança e autenticação
- **Spring Validation** - Validação de dados
- **Spring Boot Actuator** - Health checks e monitoramento

### Banco de Dados
- **PostgreSQL 17** - Banco de dados de produção
- **H2 Database** - Banco de dados em memória para testes

### Autenticação e Segurança
- **JWT (JSON Web Token) 0.12.3** - Autenticação stateless
  - `jjwt-api`
  - `jjwt-impl`
  - `jjwt-jackson`
- **BCrypt** - Criptografia de senhas (via Spring Security)

### Build e Gerenciamento de Dependências
- **Apache Maven 3.9.5** - Gerenciador de dependências e build
- **Maven Compiler Plugin 3.13.0** - Compilação Java 21

### Documentação
- **SpringDoc OpenAPI 3 (2.3.0)** - Documentação automática da API
  - Swagger UI integrado
  - OpenAPI 3.0 specification

### Utilitários
- **Lombok 1.18.30** - Redução de boilerplate code
  - `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`

### Testes
- **JUnit 5** - Framework de testes (via Spring Boot Starter Test)
- **Mockito** - Framework de mocking (via Spring Boot Starter Test)
- **Spring Security Test** - Testes de segurança
- **MockMvc** - Testes de integração de controllers
- **Instancio 3.7.1** - Geração de dados de teste
- **JaCoCo 0.8.11** - Análise de cobertura de código

### Containerização e Infraestrutura
- **Docker** - Containerização da aplicação
- **Docker Compose** - Orquestração de múltiplos containers
- **Nginx Alpine** - Load balancer e proxy reverso
- **Eclipse Temurin 21 JRE Alpine** - Runtime Java otimizado

### Versões Específicas
```xml
Java: 21
Spring Boot: 3.2.0
PostgreSQL: 17
JWT: 0.12.3
Lombok: 1.18.30
JaCoCo: 0.8.11
SpringDoc OpenAPI: 2.3.0
Instancio: 3.7.1
Maven Compiler Plugin: 3.13.0
```

## 📦 Pré-requisitos

### Para Execução com Docker (Recomendado)
- **Docker Desktop** 20.10+ ou **Docker Engine** 20.10+
- **Docker Compose** 2.0+
- **4 GB de RAM** disponível (recomendado)
- **Portas livres**: 80 (Nginx), 5432 (PostgreSQL)

### Para Execução Local (Sem Docker)
- **Java 21 JDK** (Eclipse Adoptium, OpenJDK ou Oracle JDK)
- **Apache Maven 3.9+**
- **PostgreSQL 17** instalado e rodando
- **Variáveis de ambiente** configuradas (opcional)

## 🚀 Como Executar

### Opção 1: Executando com Docker Compose (Recomendado)

Esta é a forma mais simples e garante que todos os serviços estejam configurados corretamente.

#### Passo 1: Clone o Repositório
```bash
git clone <url-do-repositorio>
cd Case-Supera
```

#### Passo 2: Execute o Docker Compose
```bash
# Subir todos os serviços em background
docker-compose up -d

# Ou para ver os logs em tempo real
docker-compose up
```

#### Passo 3: Verifique o Status dos Containers
```bash
# Ver status de todos os containers
docker-compose ps

# Ver logs de um serviço específico
docker-compose logs -f app1
docker-compose logs -f postgres
docker-compose logs -f nginx
```

#### Passo 4: Aguarde a Inicialização
Aguarde aproximadamente 30-60 segundos para:
- PostgreSQL inicializar completamente
- As 3 instâncias da aplicação iniciarem
- Nginx configurar o load balancer
- DataInitializer popular os dados iniciais

#### Passo 5: Acesse a Aplicação
- **API Base**: http://localhost
- **Swagger UI**: http://localhost/swagger-ui.html
- **Health Check**: http://localhost/health
- **PostgreSQL**: localhost:5432

#### Comandos Úteis do Docker Compose
```bash
# Parar todos os serviços
docker-compose stop

# Parar e remover containers
docker-compose down

# Parar, remover containers e volumes (limpa dados do banco)
docker-compose down -v

# Reconstruir imagens após mudanças no código
docker-compose up -d --build

# Ver logs de todos os serviços
docker-compose logs -f
```

### Opção 2: Executando Localmente (Sem Docker)

#### Passo 1: Configure o PostgreSQL
Certifique-se de que o PostgreSQL 17 está rodando e crie o banco de dados:
```sql
CREATE DATABASE case_supera;
```

#### Passo 2: Configure as Variáveis de Ambiente

**Windows (PowerShell):**
```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="5432"
$env:DB_NAME="case_supera"
$env:DB_USER="postgres"
$env:DB_PASSWORD="postgres"
$env:JWT_SECRET="supera-case-secret-key-for-jwt-token-generation-minimum-256-bits"
$env:SERVER_PORT="8080"
```

**Linux/Mac:**
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=case_supera
export DB_USER=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=supera-case-secret-key-for-jwt-token-generation-minimum-256-bits
export SERVER_PORT=8080
```

**Ou edite diretamente** o arquivo `src/main/resources/application.yml`

#### Passo 3: Execute a Aplicação
```bash
# Compilar e executar
mvn spring-boot:run

# Ou compilar primeiro e depois executar o JAR
mvn clean package
java -jar target/case-supera-1.0.0.jar
```

#### Passo 4: Acesse a Aplicação
- **API Base**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

## 🧪 Como Executar os Testes

### Executando Todos os Testes
```bash
# Executa todos os testes (unitários e integração)
mvn test

# Com limpeza prévia
mvn clean test
```

### Executando Apenas Testes Unitários
```bash
# Testes unitários (padrão: *Test.java)
mvn test -Dtest=*Test

# Teste específico
mvn test -Dtest=AccessRequestServiceTest
```

### Executando Apenas Testes de Integração
```bash
# Testes de integração (padrão: *IntegrationTest.java)
mvn test -Dtest=*IntegrationTest

# Teste específico
mvn test -Dtest=AccessRequestIntegrationTest
```

### Executando Testes com Cobertura
```bash
# Executa testes e gera relatório de cobertura
mvn clean test jacoco:report

# O relatório será gerado em: target/site/jacoco/index.html
```

### Executando Testes com Verificação de Cobertura
```bash
# Executa testes e verifica se cobertura >= 80%
# O build falhará se a cobertura for menor que 80%
mvn clean test jacoco:check
```

### Executando Testes Específicos por Classe
```bash
# Uma classe específica
mvn test -Dtest=AccessRequestServiceTest

# Múltiplas classes
mvn test -Dtest=AccessRequestServiceTest,AuthServiceTest

# Método específico
mvn test -Dtest=AccessRequestServiceTest#testCreateAccessRequestSuccess
```

### Regras dos Testes
- ✅ **Sem uso de `any()`**: Todos os mocks usam valores específicos (`eq()`, valores literais)
- ✅ **Cobertura mínima**: 80% (configurado no JaCoCo)
- ✅ **Testes isolados**: Cada teste é independente
- ✅ **Mocks específicos**: Uso de `eq()`, `anyString()` é proibido

## 📊 Como Visualizar Relatório de Cobertura

### Gerar o Relatório
```bash
mvn clean test jacoco:report
```

### Localização do Relatório
Após executar o comando acima, o relatório HTML será gerado em:
```
target/site/jacoco/index.html
```

### Visualizar o Relatório
1. Abra o arquivo `target/site/jacoco/index.html` no navegador
2. Navegue pelas classes para ver a cobertura detalhada
3. Verifique a cobertura por:
   - **Linhas** (LINE)
   - **Branches** (BRANCH)
   - **Métodos** (METHOD)
   - **Classes** (CLASS)

### Cobertura Mínima
- **Mínimo obrigatório**: 80% de linhas cobertas
- **Configuração**: `pom.xml` → `jacoco-maven-plugin`
- **Validação automática**: O build falha se cobertura < 80%

### Estrutura do Relatório
```
target/site/jacoco/
├── index.html          # Página principal
├── br/                 # Pacotes Java
│   └── com/
│       └── supera/
│           └── case_supera/
│               ├── controller/
│               ├── service/
│               ├── repository/
│               └── ...
└── jacoco-resources/   # Recursos estáticos
```

## 🔐 Credenciais para Teste

O sistema vem pré-configurado com os seguintes usuários (senha para todos: `senha123`):

| Email | Departamento | Descrição | Módulos Permitidos |
|-------|--------------|-----------|-------------------|
| ti@supera.com | TI | Acesso a todos os módulos | Todos os 10 módulos |
| financeiro@supera.com | Financeiro | Acesso a módulos financeiros | Gestão Financeira, Aprovador Financeiro, Solicitante Financeiro, Relatórios, Portal |
| rh@supera.com | RH | Acesso a módulos de RH | Administrador RH, Colaborador RH, Relatórios, Portal |
| operacoes@supera.com | Operações | Acesso a módulos de operações | Gestão de Estoque, Compras, Relatórios, Portal |
| outros@supera.com | Outros | Acesso limitado | Portal, Relatórios |

**Senha padrão para todos**: `senha123`

## 📝 Exemplos de Requisições

### 1. Autenticação (Login)

```bash
curl -X POST http://localhost/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ti@supera.com",
    "password": "senha123"
  }'
```

**Resposta de Sucesso:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0aUBzdXBlcmEuY29tIiwiaWF0IjoxNzYzNzkyMDgxLCJleHAiOjE3NjM3OTI5ODF9...",
  "type": "Bearer",
  "userId": 1,
  "email": "ti@supera.com",
  "name": "João Silva - TI",
  "department": "TI"
}
```

**Resposta de Erro:**
```json
{
  "message": "Credenciais inválidas",
  "data": null
}
```

### 2. Criar Solicitação de Acesso

```bash
curl -X POST http://localhost/api/requests \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "moduleIds": [1, 2],
    "justification": "Preciso destes módulos para realizar minhas atividades profissionais diárias e gerar relatórios gerenciais para a diretoria",
    "urgent": false
  }'
```

**Resposta (Aprovado):**
```json
{
  "message": "Solicitação criada com sucesso! Protocolo: SOL-20240101-0001. Seus acessos já estão disponíveis!",
  "data": null
}
```

**Resposta (Negado):**
```json
{
  "message": "Solicitação negada. Motivo: Departamento sem permissão para acessar este módulo",
  "data": null
}
```

### 3. Listar Solicitações (Com Filtros e Paginação)

```bash
# Listar todas as solicitações (primeira página)
curl -X GET "http://localhost/api/requests?page=0&size=10" \
  -H "Authorization: Bearer {token}"

# Filtrar por status
curl -X GET "http://localhost/api/requests?status=ATIVO&page=0&size=10" \
  -H "Authorization: Bearer {token}"

# Filtrar por texto (protocolo ou nome do módulo)
curl -X GET "http://localhost/api/requests?search=SOL-2024&page=0&size=10" \
  -H "Authorization: Bearer {token}"

# Filtrar por período
curl -X GET "http://localhost/api/requests?startDate=2024-01-01&endDate=2024-12-31&page=0&size=10" \
  -H "Authorization: Bearer {token}"

# Filtrar por urgente
curl -X GET "http://localhost/api/requests?urgent=true&page=0&size=10" \
  -H "Authorization: Bearer {token}"
```

**Resposta:**
```json
{
  "content": [
    {
      "id": 1,
      "protocol": "SOL-20240101-0001",
      "requestedModules": ["Portal do Colaborador", "Relatórios Gerenciais"],
      "justification": "Preciso destes módulos...",
      "urgent": false,
      "status": "ATIVO",
      "requestDate": "2024-01-01T10:00:00",
      "expirationDate": "2024-06-29T10:00:00",
      "denialReason": null
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalPages": 1,
  "totalElements": 1
}
```

### 4. Consultar Detalhes de uma Solicitação

```bash
curl -X GET http://localhost/api/requests/1 \
  -H "Authorization: Bearer {token}"
```

**Resposta:**
```json
{
  "id": 1,
  "protocol": "SOL-20240101-0001",
  "requestedModules": ["Portal do Colaborador", "Relatórios Gerenciais"],
  "justification": "Preciso destes módulos...",
  "urgent": false,
  "status": "ATIVO",
  "requestDate": "2024-01-01T10:00:00",
  "expirationDate": "2024-06-29T10:00:00",
  "denialReason": null,
  "history": [
    {
      "previousStatus": null,
      "newStatus": "ATIVO",
      "changeDate": "2024-01-01T10:00:00",
      "reason": "Solicitação aprovada automaticamente"
    }
  ]
}
```

### 5. Listar Módulos Disponíveis

```bash
# Módulos ativos apenas
curl -X GET http://localhost/api/modules \
  -H "Authorization: Bearer {token}"

# Todos os módulos (ativos e inativos)
curl -X GET http://localhost/api/modules/all \
  -H "Authorization: Bearer {token}"
```

**Resposta:**
```json
[
  {
    "id": 1,
    "name": "Portal do Colaborador",
    "description": "Portal de acesso para colaboradores",
    "active": true,
    "allowedDepartments": ["TI", "Financeiro", "RH", "Operações", "Outros"],
    "incompatibleModules": []
  }
]
```

### 6. Renovar Acesso

```bash
curl -X POST http://localhost/api/requests/1/renew \
  -H "Authorization: Bearer {token}"
```

**Resposta:**
```json
{
  "message": "Renovação realizada com sucesso!",
  "data": null
}
```

**Pré-requisitos para renovação:**
- Solicitação deve estar com status "ATIVO"
- Deve faltar 30 dias ou menos para expiração (ou já estar expirada)
- Usuário só pode renovar suas próprias solicitações

### 7. Cancelar Solicitação

```bash
curl -X POST http://localhost/api/requests/1/cancel \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "reason": "Não preciso mais deste acesso para realizar minhas atividades"
  }'
```

**Resposta:**
```json
{
  "message": "Solicitação cancelada com sucesso",
  "data": null
}
```

**Validações:**
- Motivo obrigatório (10-200 caracteres)
- Apenas solicitações com status "ATIVO" podem ser canceladas
- Usuário só pode cancelar suas próprias solicitações
- Acesso aos módulos é revogado imediatamente

## 🏗 Arquitetura da Solução

### Estrutura do Projeto

```
Case-Supera/
├── src/
│   ├── main/
│   │   ├── java/br/com/supera/case_supera/
│   │   │   ├── config/              # Configurações
│   │   │   │   ├── DataInitializer.java    # Inicialização de dados
│   │   │   │   ├── JwtAuthenticationFilter.java  # Filtro JWT
│   │   │   │   ├── JwtTokenProvider.java   # Geração/validação JWT
│   │   │   │   ├── OpenApiConfig.java      # Configuração Swagger
│   │   │   │   └── SecurityConfig.java     # Configuração Spring Security
│   │   │   ├── controller/          # Controllers REST
│   │   │   │   ├── AccessRequestController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   └── ModuleController.java
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   │   ├── AccessRequestDTO.java
│   │   │   │   ├── CancelRequestDTO.java
│   │   │   │   ├── LoginDTO.java
│   │   │   │   └── ...
│   │   │   ├── entity/              # Entidades JPA
│   │   │   │   ├── AccessRequest.java
│   │   │   │   ├── AccessHistory.java
│   │   │   │   ├── Department.java
│   │   │   │   ├── Module.java
│   │   │   │   ├── RequestStatus.java
│   │   │   │   ├── User.java
│   │   │   │   └── UserModule.java
│   │   │   ├── exception/           # Exceções customizadas
│   │   │   │   ├── BusinessException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── repository/          # Repositórios JPA
│   │   │   │   ├── AccessRequestRepository.java
│   │   │   │   ├── ModuleRepository.java
│   │   │   │   ├── UserModuleRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   └── service/             # Lógica de negócio
│   │   │       ├── AccessRequestService.java
│   │   │       ├── AuthService.java
│   │   │       └── ModuleService.java
│   │   └── resources/
│   │       ├── application.yml      # Configurações da aplicação
│   │       └── application-test.yml # Configurações de teste
│   └── test/                        # Testes
│       └── java/br/com/supera/case_supera/
│           ├── integration/         # Testes de integração
│           │   └── AccessRequestIntegrationTest.java
│           └── service/             # Testes unitários
│               ├── AccessRequestServiceTest.java
│               └── AuthServiceTest.java
├── docker-compose.yml               # Orquestração dos containers
├── Dockerfile                       # Build da aplicação (multi-stage)
├── nginx.conf                       # Configuração do Load Balancer
├── pom.xml                          # Dependências Maven
└── README.md                        # Este arquivo
```

### Arquitetura de Deploy

```
┌─────────────────────────────────────────┐
│           Nginx (Port 80)               │
│         Load Balancer                   │
│      (least_conn algorithm)             │
└──────────────┬──────────────────────────┘
               │
       ┌───────┼───────┐
       │       │       │
   ┌───▼───┐ ┌─▼───┐ ┌─▼───┐
   │ App1  │ │App2 │ │App3 │
   │ :8080 │ │:8080│ │:8080│
   │(Java) │ │(Java)│ │(Java)│
   └───┬───┘ └─┬───┘ └─┬───┘
       │       │       │
       └───────┼───────┘
               │
         ┌─────▼─────┐
         │ PostgreSQL│
         │   :5432   │
         │  (v17)    │
         └───────────┘
```

### Fluxo de Solicitação de Acesso

```
1. Autenticação
   └─> Usuário faz login
   └─> Sistema retorna token JWT (expira em 15 min)

2. Criação de Solicitação
   └─> Usuário envia: moduleIds[], justification, urgent
   └─> Sistema identifica: userId, department (do token)

3. Validações Automáticas (em ordem)
   ├─> Módulos estão ativos?
   ├─> Usuário já possui solicitação ativa para estes módulos?
   ├─> Usuário já possui acesso ativo?
   ├─> Justificativa válida? (20-500 chars, não genérica)
   ├─> Compatibilidade de departamento?
   ├─> Módulos mutuamente exclusivos?
   └─> Limite de módulos por usuário? (5 geral, 10 TI)

4. Concessão Automática
   ├─> Se APROVADO:
   │   ├─> Gera protocolo único (SOL-YYYYMMDD-NNNN)
   │   ├─> Cria AccessRequest com status "ATIVO"
   │   ├─> Cria UserModule para cada módulo aprovado
   │   ├─> Define expirationDate (180 dias)
   │   ├─> Registra no histórico
   │   └─> Retorna mensagem de sucesso
   └─> Se NEGADO:
       ├─> Cria AccessRequest com status "NEGADO"
       ├─> Define denialReason
       ├─> Registra no histórico
       └─> Retorna mensagem com motivo

5. Resposta
   └─> Retorna protocolo e status da solicitação
```

## 🎯 Decisões Técnicas

### 1. Autenticação JWT Stateless

**Decisão**: Implementar autenticação stateless com JWT ao invés de sessões server-side.

**Motivos**:
- ✅ Facilita escalabilidade horizontal (múltiplas instâncias)
- ✅ Não requer armazenamento de sessão no servidor
- ✅ Compatível com arquitetura de load balancer
- ✅ Token contém informações do usuário (userId, email, department)
- ✅ Expiração de 15 minutos conforme requisito

**Implementação**:
- `JwtTokenProvider`: Gera e valida tokens
- `JwtAuthenticationFilter`: Filtro que intercepta requisições e valida token
- Token no header: `Authorization: Bearer {token}`

### 2. Concessão Automática de Acesso

**Decisão**: Processar validações e conceder acesso automaticamente na criação da solicitação.

**Motivos**:
- ✅ Atende ao requisito: "acesso concedido automaticamente após validação"
- ✅ Elimina necessidade de aprovação manual
- ✅ Resposta imediata ao usuário
- ✅ Regras de negócio aplicadas em tempo real

**Fluxo**:
1. Valida todas as regras de negócio
2. Se aprovado: cria `AccessRequest` com status "ATIVO" e cria `UserModule` imediatamente
3. Se negado: cria `AccessRequest` com status "NEGADO" e define `denialReason`

### 3. Protocolo Único com Formato Específico

**Decisão**: Formato `SOL-YYYYMMDD-NNNN` para protocolos de solicitação.

**Motivos**:
- ✅ Facilita rastreabilidade e identificação única
- ✅ Formato legível e padronizado
- ✅ Inclui data para organização temporal
- ✅ Sequencial para garantir unicidade

**Implementação**:
- Geração baseada em data atual + contador de solicitações
- Garantia de unicidade via constraint `UNIQUE` no banco

### 4. Histórico de Alterações em Tabela Separada

**Decisão**: Tabela `access_history` separada da `access_requests`.

**Motivos**:
- ✅ Mantém auditoria completa e imutável
- ✅ Permite múltiplas alterações de status sem perder histórico
- ✅ Facilita consultas de auditoria
- ✅ Não polui a tabela principal

**Estrutura**:
- `AccessHistory` com relacionamento `@OneToMany` em `AccessRequest`
- Registra: status anterior, novo status, data, motivo

### 5. Testes Rigorosos sem `any()`

**Decisão**: Proibir uso de `any()`, `anyString()`, `anyLong()` do Mockito.

**Motivos**:
- ✅ Atende ao requisito rigoroso do teste técnico
- ✅ Testes mais específicos e confiáveis
- ✅ Facilita identificação de problemas
- ✅ Garante que valores corretos são passados

**Implementação**:
- Uso obrigatório de `eq()`, valores literais, ou objetos específicos
- Exemplo: `when(repo.findById(eq(1L)))` ao invés de `when(repo.findById(anyLong()))`
- Verificação com `verify()` usando valores específicos

### 6. Docker Multi-stage Build

**Decisão**: Build em dois estágios separados.

**Motivos**:
- ✅ Reduz tamanho da imagem final (apenas JRE, não JDK + Maven)
- ✅ Melhora performance de deploy
- ✅ Separação clara entre build e runtime
- ✅ Imagem base otimizada (Alpine Linux)

**Estrutura**:
```dockerfile
Stage 1: Build (maven:3.9.5-eclipse-temurin-21)
  └─> Compila o código
  └─> Gera JAR

Stage 2: Runtime (eclipse-temurin:21-jre-alpine)
  └─> Copia apenas o JAR
  └─> Instala curl (para health checks)
  └─> Executa aplicação
```

### 7. Load Balancer Nginx com Least Connection

**Decisão**: Nginx como load balancer com algoritmo `least_conn`.

**Motivos**:
- ✅ Distribui carga de forma eficiente entre 3 instâncias
- ✅ Algoritmo `least_conn` escolhe servidor com menos conexões ativas
- ✅ Melhor para aplicações stateless (JWT)
- ✅ Configuração simples e robusta

**Configuração**:
- Upstream com 3 servidores (app1, app2, app3)
- Proxy reverso para todas as requisições
- Health check endpoint configurado

### 8. Inicialização de Dados com @PostConstruct

**Decisão**: Usar `@PostConstruct` e `@Transactional` para popular dados iniciais.

**Motivos**:
- ✅ Executa automaticamente na inicialização da aplicação
- ✅ Garante que dados existam antes de processar requisições
- ✅ Transacional: rollback em caso de erro
- ✅ Idempotente: verifica se dados já existem antes de criar

**Implementação**:
- `DataInitializer` com `@Component` e `@PostConstruct`
- Métodos `initUsers()` e `initModules()` com verificação de existência
- População de incompatibilidades via SQL nativo

### 9. Validação de Justificativa Anti-Genérica

**Decisão**: Validar que justificativa não contenha apenas texto genérico.

**Motivos**:
- ✅ Atende ao requisito: "Justificativa não pode conter apenas texto genérico"
- ✅ Melhora qualidade das solicitações
- ✅ Previne abusos

**Implementação**:
- Lista de palavras genéricas: "teste", "aaa", "preciso", etc.
- Validação no service antes de criar solicitação
- Rejeita se justificativa contém apenas palavras genéricas

### 10. Módulos Mutuamente Exclusivos

**Decisão**: Validação hardcoded + tabela de incompatibilidades.

**Motivos**:
- ✅ Garante validação mesmo se tabela não estiver populada
- ✅ Performance: validação rápida em memória
- ✅ Fallback para tabela do banco se necessário

**Implementação**:
- Método `areIncompatible()` com validação hardcoded
- Também verifica tabela `module_incompatibilities`
- Aplicado na criação de solicitação e renovação

### 11. Renovação de Acesso com Vinculação

**Decisão**: Nova solicitação vinculada à anterior via `renewedFrom`.

**Motivos**:
- ✅ Mantém rastreabilidade da renovação
- ✅ Permite consultar histórico de renovações
- ✅ Reaplica todas as regras de negócio
- ✅ Gera novo protocolo único

**Implementação**:
- Campo `renewedFrom` em `AccessRequest`
- Validação: status "ATIVO" e expiração <= 30 dias
- Estende validade em 180 dias se aprovado

### 12. Cancelamento com Revogação Imediata

**Decisão**: Ao cancelar, revogar acesso imediatamente.

**Motivos**:
- ✅ Atende ao requisito: "Acesso aos módulos é revogado imediatamente"
- ✅ Segurança: remove acesso assim que cancelado
- ✅ Auditoria: registra motivo no histórico

**Implementação**:
- Altera status para "CANCELADO"
- Desativa todos os `UserModule` relacionados
- Registra motivo no histórico

### 13. Criptografia de Senhas com BCrypt

**Decisão**: Usar BCrypt para criptografia de senhas.

**Motivos**:
- ✅ Algoritmo seguro e amplamente usado
- ✅ Salt automático (cada hash é único)
- ✅ Integrado ao Spring Security
- ✅ Resistente a ataques de força bruta

**Implementação**:
- `@PrePersist` em `User` criptografa senha antes de salvar
- `BCryptPasswordEncoder` do Spring Security
- Validação no login com `matches()`

### 14. Health Checks com Spring Actuator

**Decisão**: Usar Spring Boot Actuator para health checks.

**Motivos**:
- ✅ Padrão do Spring Boot
- ✅ Integração com Docker health checks
- ✅ Monitoramento simples e eficiente
- ✅ Endpoint `/actuator/health` configurado

**Implementação**:
- Dependência `spring-boot-starter-actuator`
- Endpoint exposto em `/actuator/health`
- Configurado no Docker Compose para health checks

### 15. JaCoCo com Cobertura Mínima de 80%

**Decisão**: Configurar JaCoCo para falhar build se cobertura < 80%.

**Motivos**:
- ✅ Atende ao requisito de cobertura mínima obrigatória
- ✅ Garante qualidade do código
- ✅ Falha o build automaticamente se abaixo do mínimo

**Implementação**:
- Plugin `jacoco-maven-plugin` configurado
- Regra: mínimo 80% de linhas cobertas
- Relatório HTML gerado em `target/site/jacoco/index.html`

## 📡 Endpoints da API

### Autenticação
- `POST /api/auth/login` - Autenticar usuário e receber token JWT

### Solicitações de Acesso
- `POST /api/requests` - Criar nova solicitação de acesso
- `GET /api/requests` - Listar solicitações do usuário (com filtros e paginação)
  - Query params: `page`, `size`, `status`, `search`, `startDate`, `endDate`, `urgent`
- `GET /api/requests/{id}` - Detalhes completos de uma solicitação específica
- `POST /api/requests/{id}/renew` - Renovar acesso (quando faltam ≤30 dias)
- `POST /api/requests/{id}/cancel` - Cancelar solicitação ativa

### Módulos
- `GET /api/modules` - Listar módulos disponíveis (apenas ativos)
- `GET /api/modules/all` - Listar todos os módulos (ativos e inativos)

### Documentação e Monitoramento
- `GET /swagger-ui.html` - Interface Swagger UI (documentação interativa)
- `GET /api-docs` - Documentação OpenAPI em JSON
- `GET /actuator/health` - Health check da aplicação
- `GET /health` - Health check via Nginx (proxy para actuator)

## 🔒 Regras de Negócio Implementadas

### Compatibilidade de Departamento

| Departamento | Módulos Permitidos |
|--------------|-------------------|
| **TI** | Todos os 10 módulos |
| **Financeiro** | Gestão Financeira, Aprovador Financeiro, Solicitante Financeiro, Relatórios Gerenciais, Portal do Colaborador |
| **RH** | Administrador RH, Colaborador RH, Relatórios Gerenciais, Portal do Colaborador |
| **Operações** | Gestão de Estoque, Compras, Relatórios Gerenciais, Portal do Colaborador |
| **Outros** | Portal do Colaborador, Relatórios Gerenciais |

### Módulos Mutuamente Exclusivos

Não é permitido ter acesso simultâneo a:
- **Aprovador Financeiro** ↔ **Solicitante Financeiro**
- **Administrador RH** ↔ **Colaborador RH**

### Limite de Módulos por Usuário

- **Usuários comuns**: Máximo **5 módulos ativos** simultaneamente
- **Usuários TI**: Máximo **10 módulos ativos** simultaneamente

### Validações de Solicitação

1. **Módulos solicitados**: 1-3 módulos por solicitação (obrigatório)
2. **Justificativa**: 20-500 caracteres (obrigatório)
   - Não pode conter apenas texto genérico (ex: "teste", "aaa", "preciso")
3. **Solicitação ativa**: Não pode ter solicitação ativa para o mesmo módulo
4. **Acesso existente**: Não pode solicitar módulo que já possui acesso ativo
5. **Módulos ativos**: Todos os módulos solicitados devem estar ativos

### Motivos de Negação Automática

1. "Departamento sem permissão para acessar este módulo"
2. "Módulo incompatível com outro módulo já ativo em seu perfil"
3. "Limite de módulos ativos atingido"
4. "Justificativa insuficiente ou genérica"

### Renovação de Acesso

**Condições para renovar**:
- Solicitação deve estar com status "ATIVO"
- Deve faltar 30 dias ou menos para expiração (ou já estar expirada)
- Usuário só pode renovar suas próprias solicitações

**Ao renovar**:
- Cria nova solicitação vinculada à anterior (`renewedFrom`)
- Reaplica todas as regras de negócio
- Estende validade em 180 dias (se aprovado)
- Gera novo protocolo único

### Cancelamento de Solicitação

**Condições para cancelar**:
- Solicitação deve estar com status "ATIVO"
- Usuário só pode cancelar suas próprias solicitações
- Motivo obrigatório (10-200 caracteres)

**Ao cancelar**:
- Status muda para "CANCELADO"
- Acesso aos módulos é revogado imediatamente (UserModule desativado)
- Motivo e data registrados no histórico

## 📈 Cobertura de Testes

### Configuração
- **Cobertura mínima obrigatória**: 80% de linhas cobertas
- **Ferramenta**: JaCoCo 0.8.11
- **Validação automática**: Build falha se cobertura < 80%

### Estrutura de Testes

#### Testes Unitários
- **Localização**: `src/test/java/.../service/*Test.java`
- **Foco**: Lógica de negócio isolada
- **Ferramentas**: JUnit 5, Mockito
- **Regra**: Sem uso de `any()`, apenas valores específicos

#### Testes de Integração
- **Localização**: `src/test/java/.../integration/*IntegrationTest.java`
- **Foco**: Fluxos completos com banco de dados
- **Ferramentas**: MockMvc, Spring Boot Test, H2 Database
- **Cenários**: Criação, consulta, renovação, cancelamento

### Executar Testes com Cobertura
```bash
mvn clean test jacoco:report
```

### Visualizar Relatório
Abra `target/site/jacoco/index.html` no navegador.

## 🐳 Docker Compose

### Serviços Provisionados

1. **PostgreSQL 17**
   - Container: `case-supera-postgres`
   - Porta: `5432`
   - Volume: `postgres_data` (persistência)
   - Health check: `pg_isready`

2. **Aplicação Java (3 instâncias)**
   - Containers: `case-supera-app1`, `case-supera-app2`, `case-supera-app3`
   - Porta interna: `8080`
   - Health check: `/actuator/health`
   - Build: Multi-stage Dockerfile

3. **Nginx (Load Balancer)**
   - Container: `case-supera-nginx`
   - Porta: `80`
   - Algoritmo: `least_conn`
   - Health check: `/health`

### Rede Docker
- **Nome**: `case-supera-network`
- **Driver**: `bridge`
- **Comunicação**: Todos os containers na mesma rede

### Variáveis de Ambiente

Configure via arquivo `.env` ou variáveis de ambiente do sistema:

```bash
# Banco de dados
DB_NAME=case_supera
DB_USER=postgres
DB_PASSWORD=postgres

# JWT
JWT_SECRET=supera-case-secret-key-for-jwt-token-generation-minimum-256-bits

# Porta (opcional, padrão 8080)
SERVER_PORT=8080
```

### Comandos Úteis

```bash
# Subir todos os serviços
docker-compose up -d

# Ver logs
docker-compose logs -f

# Parar serviços
docker-compose stop

# Parar e remover containers
docker-compose down

# Parar, remover containers e volumes
docker-compose down -v

# Reconstruir após mudanças
docker-compose up -d --build

# Ver status
docker-compose ps
```

## 📚 Documentação Adicional

### Swagger UI
- **URL**: http://localhost/swagger-ui.html
- **Descrição**: Interface interativa para testar todos os endpoints
- **Autenticação**: Use o botão "Authorize" e insira o token JWT

### Health Check
- **Via Nginx**: http://localhost/health
- **Direto**: http://localhost:8080/actuator/health (se acessar diretamente uma instância)

### OpenAPI Specification
- **JSON**: http://localhost/api-docs
- **Formato**: OpenAPI 3.0

## 🚨 Troubleshooting

### Problema: Containers não iniciam

**Sintomas**: `docker-compose up` falha ou containers ficam em "Restarting"

**Soluções**:
```bash
# Verificar se portas estão livres
netstat -an | findstr "80 5432"  # Windows
lsof -i :80 -i :5432              # Linux/Mac

# Parar e reiniciar
docker-compose down
docker-compose up -d

# Ver logs para identificar erro
docker-compose logs -f
```

### Problema: Erro de conexão com banco

**Sintomas**: Aplicação não consegue conectar ao PostgreSQL

**Soluções**:
```bash
# Verificar se PostgreSQL está rodando
docker-compose ps postgres

# Ver logs do PostgreSQL
docker-compose logs postgres

# Aguardar inicialização completa
docker-compose logs -f postgres
# Aguarde mensagem: "database system is ready to accept connections"
```

### Problema: Cobertura abaixo de 80%

**Sintomas**: `mvn test` falha com erro de cobertura

**Soluções**:
```bash
# Ver relatório detalhado
mvn clean test jacoco:report
# Abra target/site/jacoco/index.html

# Identificar classes com baixa cobertura
# Adicionar testes para essas classes
```

### Problema: Token JWT expirado

**Sintomas**: Requisições retornam 401 Unauthorized

**Solução**: Faça login novamente para obter novo token
```bash
curl -X POST http://localhost/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "ti@supera.com", "password": "senha123"}'
```

### Problema: Erro "Port already in use"

**Sintomas**: Docker não consegue iniciar container na porta 80 ou 5432

**Soluções**:
```bash
# Windows: Verificar processo usando a porta
netstat -ano | findstr :80
taskkill /PID <PID> /F

# Linux/Mac: Verificar processo
lsof -i :80
kill -9 <PID>

# Ou altere as portas no docker-compose.yml
```

### Problema: Dados não são inicializados

**Sintomas**: Login falha ou módulos não aparecem

**Soluções**:
```bash
# Verificar logs do DataInitializer
docker-compose logs app1 | grep DataInitializer

# Verificar se banco está vazio
docker-compose exec postgres psql -U postgres -d case_supera -c "SELECT COUNT(*) FROM users;"

# Reiniciar aplicação
docker-compose restart app1 app2 app3
```

## 👥 Autor

Desenvolvido como case técnico para vaga de emprego.

## 📄 Licença

Este projeto é um case técnico e não possui licença específica.
