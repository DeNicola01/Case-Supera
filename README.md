# Case Supera - Sistema de Solicitação de Acesso a Módulos

Sistema corporativo desenvolvido em Java/Spring Boot para gerenciamento de solicitações de acesso a módulos. Usuários autenticados podem solicitar acesso a diferentes módulos do sistema, e o acesso é concedido automaticamente após validação das regras de negócio.

## 📋 Índice

- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Pré-requisitos](#pré-requisitos)
- [Como Executar](#como-executar)
- [Como Executar os Testes](#como-executar-os-testes)
- [Como Visualizar Relatório de Cobertura](#como-visualizar-relatório-de-cobertura)
- [Credenciais para Teste](#credenciais-para-teste)
- [Exemplos de Requisições](#exemplos-de-requisições)
- [Arquitetura da Solução](#arquitetura-da-solução)
- [Decisões Técnicas](#decisões-técnicas)
- [Endpoints da API](#endpoints-da-api)

## 🛠 Tecnologias Utilizadas

- **Java 21** (obrigatório)
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **Spring Security** com JWT
- **Spring Validation**
- **PostgreSQL 17**
- **H2** (apenas para testes)
- **Maven**
- **Docker** e **Docker Compose**
- **Nginx** (Load Balancer)
- **Lombok**
- **Swagger/OpenAPI 3**
- **JUnit 5**
- **Mockito**
- **JaCoCo** (Cobertura de testes)
- **Instancio**

## 📦 Pré-requisitos

- Docker Desktop ou Docker Engine 20.10+
- Docker Compose 2.0+
- Maven 3.9+ (opcional, para execução local sem Docker)
- Java 21 (opcional, para execução local sem Docker)

## 🚀 Como Executar

### Executando com Docker Compose (Recomendado)

1. Clone o repositório:
```bash
git clone <url-do-repositorio>
cd Case-Supera
```

2. Execute o docker-compose:
```bash
docker-compose up -d
```

3. Aguarde alguns instantes para todos os serviços iniciarem. Você pode verificar o status com:
```bash
docker-compose ps
```

4. Acesse a aplicação:
   - **API**: http://localhost
   - **Swagger UI**: http://localhost/swagger-ui.html
   - **PostgreSQL**: localhost:5432

### Executando Localmente (Sem Docker)

1. Certifique-se de ter PostgreSQL 17 rodando localmente

2. Configure as variáveis de ambiente ou edite `application.yml`:
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=case_supera
export DB_USER=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=supera-case-secret-key-for-jwt-token-generation-minimum-256-bits
```

3. Execute a aplicação:
```bash
mvn spring-boot:run
```

## 🧪 Como Executar os Testes

### Executando todos os testes:
```bash
mvn test
```

### Executando apenas testes unitários:
```bash
mvn test -Dtest=*Test
```

### Executando apenas testes de integração:
```bash
mvn test -Dtest=*IntegrationTest
```

### Executando testes com cobertura:
```bash
mvn clean test jacoco:report
```

## 📊 Como Visualizar Relatório de Cobertura

Após executar os testes com JaCoCo, o relatório será gerado em:

```
target/site/jacoco/index.html
```

Abra este arquivo no navegador para visualizar a cobertura de código.

**Nota**: O build falhará se a cobertura for menor que 80% conforme configurado no `pom.xml`.

## 🔐 Credenciais para Teste

O sistema vem pré-configurado com os seguintes usuários (senha para todos: `senha123`):

| Email | Departamento | Descrição |
|-------|--------------|-----------|
| ti@supera.com | TI | Acesso a todos os módulos |
| financeiro@supera.com | Financeiro | Acesso a módulos financeiros |
| rh@supera.com | RH | Acesso a módulos de RH |
| operacoes@supera.com | Operações | Acesso a módulos de operações |
| outros@supera.com | Outros | Acesso limitado (Portal e Relatórios) |

## 📝 Exemplos de Requisições

### 1. Autenticação

```bash
curl -X POST http://localhost/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ti@supera.com",
    "password": "senha123"
  }'
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "email": "ti@supera.com",
  "name": "João Silva - TI",
  "department": "TI"
}
```

### 2. Criar Solicitação de Acesso

```bash
curl -X POST http://localhost/api/requests \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "moduleIds": [1, 2],
    "justification": "Preciso destes módulos para realizar minhas atividades profissionais diárias e gerar relatórios gerenciais",
    "urgent": false
  }'
```

**Resposta (Aprovado):**
```json
{
  "message": "Solicitação criada com sucesso! Protocolo: SOL-20240101-0001. Seus acessos já estão disponíveis!"
}
```

**Resposta (Negado):**
```json
{
  "message": "Solicitação negada. Motivo: Departamento sem permissão para acessar este módulo"
}
```

### 3. Listar Solicitações

```bash
curl -X GET "http://localhost/api/requests?page=0&size=10" \
  -H "Authorization: Bearer {token}"
```

### 4. Consultar Detalhes de uma Solicitação

```bash
curl -X GET http://localhost/api/requests/1 \
  -H "Authorization: Bearer {token}"
```

### 5. Listar Módulos Disponíveis

```bash
curl -X GET http://localhost/api/modules \
  -H "Authorization: Bearer {token}"
```

### 6. Renovar Acesso

```bash
curl -X POST http://localhost/api/requests/1/renew \
  -H "Authorization: Bearer {token}"
```

### 7. Cancelar Solicitação

```bash
curl -X POST http://localhost/api/requests/1/cancel \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "reason": "Não preciso mais deste acesso"
  }'
```

## 🏗 Arquitetura da Solução

### Estrutura do Projeto

```
Case-Supera/
├── src/
│   ├── main/
│   │   ├── java/br/com/supera/case_supera/
│   │   │   ├── config/          # Configurações (Security, JWT, OpenAPI)
│   │   │   ├── controller/      # Controllers REST
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── entity/          # Entidades JPA
│   │   │   ├── exception/       # Exceções customizadas
│   │   │   ├── repository/      # Repositórios JPA
│   │   │   └── service/         # Lógica de negócio
│   │   └── resources/
│   │       ├── application.yml  # Configurações da aplicação
│   │       └── data.sql         # Dados iniciais
│   └── test/                    # Testes unitários e de integração
├── docker-compose.yml           # Orquestração dos containers
├── Dockerfile                   # Build da aplicação
├── nginx.conf                   # Configuração do Load Balancer
└── pom.xml                      # Dependências Maven
```

### Arquitetura de Deploy

```
┌─────────────────────────────────────────┐
│           Nginx (Port 80)               │
│         Load Balancer                   │
└──────────────┬──────────────────────────┘
               │
       ┌───────┼───────┐
       │       │       │
   ┌───▼───┐ ┌─▼───┐ ┌─▼───┐
   │ App1  │ │App2 │ │App3 │
   │ :8080 │ │:8080│ │:8080│
   └───┬───┘ └─┬───┘ └─┬───┘
       │       │       │
       └───────┼───────┘
               │
         ┌─────▼─────┐
         │ PostgreSQL│
         │   :5432   │
         └───────────┘
```

### Fluxo de Solicitação de Acesso

1. **Autenticação**: Usuário faz login e recebe token JWT
2. **Criação de Solicitação**: Usuário cria solicitação informando módulos e justificativa
3. **Validações Automáticas**:
   - Verifica se módulos estão ativos
   - Verifica se usuário já possui solicitação ativa
   - Verifica se usuário já possui acesso
   - Valida justificativa (não genérica)
   - Valida compatibilidade de departamento
   - Valida módulos mutuamente exclusivos
   - Valida limite de módulos por usuário
4. **Concessão Automática**: Se aprovado, acesso é concedido imediatamente
5. **Resposta**: Retorna protocolo e status da solicitação

## 🎯 Decisões Técnicas

### 1. Autenticação JWT
- **Decisão**: Implementar autenticação stateless com JWT
- **Motivo**: Facilita escalabilidade e não requer sessão no servidor
- **Expiração**: 15 minutos conforme requisito

### 2. Validação Automática
- **Decisão**: Processar validações e concessão de acesso automaticamente
- **Motivo**: Atende ao requisito de "acesso concedido automaticamente após validação"

### 3. Protocolo Único
- **Decisão**: Formato SOL-YYYYMMDD-NNNN
- **Motivo**: Facilita rastreabilidade e identificação única

### 4. Histórico de Alterações
- **Decisão**: Tabela separada para histórico
- **Motivo**: Mantém auditoria completa das mudanças de status

### 5. Testes sem `any()`
- **Decisão**: Usar valores específicos nos mocks
- **Motivo**: Atende ao requisito rigoroso de não usar `any()` do Mockito

### 6. Docker Multi-stage Build
- **Decisão**: Build em estágio separado
- **Motivo**: Reduz tamanho da imagem final e melhora performance

### 7. Load Balancer Nginx
- **Decisão**: Nginx com algoritmo least_conn
- **Motivo**: Distribui carga de forma eficiente entre as 3 instâncias

## 📡 Endpoints da API

### Autenticação
- `POST /api/auth/login` - Autenticar usuário

### Solicitações de Acesso
- `POST /api/requests` - Criar nova solicitação
- `GET /api/requests` - Listar solicitações (com filtros e paginação)
- `GET /api/requests/{id}` - Detalhes de uma solicitação
- `POST /api/requests/{id}/renew` - Renovar acesso
- `POST /api/requests/{id}/cancel` - Cancelar solicitação

### Módulos
- `GET /api/modules` - Listar módulos disponíveis
- `GET /api/modules/all` - Listar todos os módulos (ativos e inativos)

### Documentação
- `GET /swagger-ui.html` - Interface Swagger UI
- `GET /api-docs` - Documentação OpenAPI (JSON)

## 🔒 Regras de Negócio Implementadas

### Compatibilidade de Departamento
- **TI**: Acesso a todos os módulos
- **Financeiro**: Gestão Financeira, Aprovador Financeiro, Solicitante Financeiro, Relatórios, Portal
- **RH**: Administrador RH, Colaborador RH, Relatórios, Portal
- **Operações**: Gestão de Estoque, Compras, Relatórios, Portal
- **Outros**: Apenas Portal e Relatórios

### Módulos Mutuamente Exclusivos
- Aprovador Financeiro ↔ Solicitante Financeiro
- Administrador RH ↔ Colaborador RH

### Limite de Módulos
- **Usuários comuns**: Máximo 5 módulos ativos
- **Usuários TI**: Máximo 10 módulos ativos

### Validações
- Justificativa: 20-500 caracteres, não pode ser genérica
- Módulos solicitados: 1-3 módulos por solicitação
- Não pode ter solicitação ativa para mesmo módulo
- Não pode solicitar módulo que já possui acesso

## 📈 Cobertura de Testes

O projeto possui cobertura mínima obrigatória de **80%**, configurada no JaCoCo. Os testes são executados automaticamente no build e o relatório é gerado em `target/site/jacoco/index.html`.

### Estrutura de Testes
- **Testes Unitários**: Serviços e componentes isolados
- **Testes de Integração**: Fluxos completos com banco de dados
- **Sem uso de `any()`**: Todos os mocks usam valores específicos

## 🐳 Docker Compose

O `docker-compose.yml` provisiona:
- **PostgreSQL 17**: Banco de dados
- **3 Instâncias da Aplicação**: app1, app2, app3
- **Nginx**: Load Balancer na porta 80

### Variáveis de Ambiente

Você pode configurar o ambiente através de variáveis:

```bash
DB_NAME=case_supera
DB_USER=postgres
DB_PASSWORD=postgres
JWT_SECRET=your-secret-key-minimum-256-bits
```

## 📚 Documentação Adicional

- **Swagger UI**: Acesse http://localhost/swagger-ui.html para documentação interativa
- **Health Check**: http://localhost/health (via Nginx)

## 🚨 Troubleshooting

### Problema: Containers não iniciam
**Solução**: Verifique se as portas 80 e 5432 estão livres:
```bash
docker-compose down
docker-compose up -d
```

### Problema: Erro de conexão com banco
**Solução**: Aguarde o PostgreSQL iniciar completamente antes das aplicações:
```bash
docker-compose logs postgres
```

### Problema: Cobertura abaixo de 80%
**Solução**: Execute os testes e verifique o relatório:
```bash
mvn clean test jacoco:report
```

## 👥 Autor

Desenvolvido como case técnico para vaga de emprego.

## 📄 Licença

Este projeto é um case técnico e não possui licença específica.

