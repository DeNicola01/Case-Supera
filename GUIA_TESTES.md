# 🧪 Guia Completo de Testes - Case Supera

Este guia fornece um processo passo a passo para testar todas as funcionalidades do sistema.

## 📋 Pré-requisitos

1. **Aplicação rodando**: Execute `docker-compose up -d` e aguarde todos os serviços iniciarem
2. **Acesso ao Swagger**: http://localhost/swagger-ui.html
3. **Ferramentas opcionais**:
   - Postman ou Insomnia (para requisições HTTP)
   - curl (linha de comando)
   - Navegador web (para Swagger UI)

## 🔐 Credenciais de Teste

| Email | Senha | Departamento | Permissões |
|-------|-------|--------------|------------|
| ti@supera.com | senha123 | TI | Acesso a todos os módulos |
| financeiro@supera.com | senha123 | Financeiro | Módulos financeiros |
| rh@supera.com | senha123 | RH | Módulos de RH |
| operacoes@supera.com | senha123 | Operações | Módulos de operações |
| outros@supera.com | senha123 | Outros | Apenas Portal e Relatórios |

---

## 🚀 Processo de Teste Completo

### **ETAPA 1: Autenticação**

#### 1.1 Login com Sucesso

**Via Swagger:**
1. Acesse http://localhost/swagger-ui.html
2. Expanda o endpoint `POST /api/auth/login`
3. Clique em "Try it out"
4. Preencha:
   ```json
   {
     "email": "ti@supera.com",
     "password": "senha123"
   }
   ```
5. Clique em "Execute"
6. **Resultado esperado**: Status 200, retorna token JWT

**Via curl:**
```bash
curl -X POST http://localhost/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ti@supera.com",
    "password": "senha123"
  }'
```

**Resposta esperada:**
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

**⚠️ IMPORTANTE**: Copie o `token` retornado para usar nos próximos testes!

#### 1.2 Login com Credenciais Inválidas

**Teste:**
```bash
curl -X POST http://localhost/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ti@supera.com",
    "password": "senha_errada"
  }'
```

**Resultado esperado**: Status 401, mensagem de erro

---

### **ETAPA 2: Consultar Módulos Disponíveis**

#### 2.1 Listar Módulos Disponíveis (Ativos)

**Via Swagger:**
1. Clique no botão "Authorize" no topo da página
2. Cole o token JWT no campo "Value" (formato: `Bearer {token}` ou apenas `{token}`)
3. Clique em "Authorize" e depois "Close"
4. Expanda `GET /api/modules`
5. Clique em "Try it out" → "Execute"

**Via curl:**
```bash
curl -X GET http://localhost/api/modules \
  -H "Authorization: Bearer {SEU_TOKEN_AQUI}"
```

**Resultado esperado**: Lista de módulos ativos com:
- ID, nome, descrição
- Departamentos permitidos
- Módulos incompatíveis (se houver)
- Status ativo

#### 2.2 Listar Todos os Módulos (Ativos e Inativos)

```bash
curl -X GET http://localhost/api/modules/all \
  -H "Authorization: Bearer {SEU_TOKEN_AQUI}"
```

---

### **ETAPA 3: Criar Solicitação de Acesso**

#### 3.1 Criar Solicitação Aprovada (Sucesso)

**Cenário**: Usuário TI solicitando Portal do Colaborador

**Via Swagger:**
1. Expanda `POST /api/requests`
2. Clique em "Try it out"
3. Preencha:
   ```json
   {
     "moduleIds": [1],
     "justification": "Preciso deste módulo para realizar minhas atividades profissionais diárias e gerar relatórios gerenciais para a diretoria",
     "urgent": false
   }
   ```
4. Execute

**Via curl:**
```bash
curl -X POST http://localhost/api/requests \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {SEU_TOKEN_AQUI}" \
  -d '{
    "moduleIds": [1],
    "justification": "Preciso deste módulo para realizar minhas atividades profissionais diárias e gerar relatórios gerenciais para a diretoria",
    "urgent": false
  }'
```

**Resultado esperado**: Status 200
```json
{
  "message": "Solicitação criada com sucesso! Protocolo: SOL-20240101-0001. Seus acessos já estão disponíveis!"
}
```

**✅ Validações automáticas que devem passar:**
- Módulo está ativo ✓
- Usuário não tem solicitação ativa para este módulo ✓
- Usuário não tem acesso ativo a este módulo ✓
- Justificativa válida (20-500 caracteres, não genérica) ✓
- Departamento tem permissão ✓
- Não excede limite de módulos ✓

#### 3.2 Criar Solicitação Negada (Departamento sem Permissão)

**Cenário**: Usuário "Outros" tentando acessar módulo restrito

1. Faça login com `outros@supera.com`
2. Tente solicitar módulo "Gestão Financeira" (ID 3):
```bash
curl -X POST http://localhost/api/requests \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN_OUTROS}" \
  -d '{
    "moduleIds": [3],
    "justification": "Preciso deste módulo para realizar minhas atividades profissionais diárias e gerar relatórios gerenciais",
    "urgent": false
  }'
```

**Resultado esperado**: Status 200
```json
{
  "message": "Solicitação negada. Motivo: Departamento sem permissão para acessar este módulo"
}
```

#### 3.3 Criar Solicitação Negada (Justificativa Genérica)

**Teste com justificativa "teste":**
```json
{
  "moduleIds": [1],
  "justification": "teste",
  "urgent": false
}
```

**Resultado esperado**: Status 400, erro de validação

**Teste com justificativa "aaa":**
```json
{
  "moduleIds": [1],
  "justification": "aaa",
  "urgent": false
}
```

**Teste com justificativa "preciso":**
```json
{
  "moduleIds": [1],
  "justification": "preciso",
  "urgent": false
}
```

#### 3.4 Criar Solicitação Negada (Módulos Mutuamente Exclusivos)

**Cenário**: Solicitar "Aprovador Financeiro" e "Solicitante Financeiro" juntos

1. Faça login com `financeiro@supera.com`
2. Solicite ambos os módulos:
```json
{
  "moduleIds": [4, 5],
  "justification": "Preciso destes módulos para realizar minhas atividades profissionais diárias e gerar relatórios gerenciais",
  "urgent": false
}
```

**Resultado esperado**: Status 200, mas solicitação negada
```json
{
  "message": "Solicitação negada. Motivo: Módulo incompatível com outro módulo já ativo em seu perfil"
}
```

#### 3.5 Criar Solicitação Negada (Limite de Módulos Excedido)

**Cenário**: Usuário comum com 5 módulos ativos tentando solicitar mais um

1. Crie 5 solicitações aprovadas
2. Tente criar a 6ª:
```json
{
  "moduleIds": [1],
  "justification": "Preciso deste módulo para realizar minhas atividades profissionais diárias e gerar relatórios gerenciais",
  "urgent": false
}
```

**Resultado esperado**: Status 200, mas solicitação negada
```json
{
  "message": "Solicitação negada. Motivo: Limite de módulos ativos atingido"
}
```

**Nota**: Usuários TI têm limite de 10 módulos.

#### 3.6 Criar Solicitação com Múltiplos Módulos (1-3 módulos)

**Teste válido:**
```json
{
  "moduleIds": [1, 2, 3],
  "justification": "Preciso destes módulos para realizar minhas atividades profissionais diárias e gerar relatórios gerenciais para a diretoria",
  "urgent": true
}
```

**Teste inválido (mais de 3 módulos):**
```json
{
  "moduleIds": [1, 2, 3, 4],
  "justification": "Preciso destes módulos...",
  "urgent": false
}
```

**Resultado esperado**: Status 400, erro de validação

---

### **ETAPA 4: Listar Solicitações**

#### 4.1 Listar Todas as Solicitações (Paginação)

```bash
curl -X GET "http://localhost/api/requests?page=0&size=10" \
  -H "Authorization: Bearer {SEU_TOKEN_AQUI}"
```

**Resultado esperado**: Página com solicitações do usuário autenticado

#### 4.2 Filtrar por Status

```bash
curl -X GET "http://localhost/api/requests?status=ATIVO&page=0&size=10" \
  -H "Authorization: Bearer {SEU_TOKEN_AQUI}"
```

**Status disponíveis**: `ATIVO`, `NEGADO`, `CANCELADO`

#### 4.3 Filtrar por Urgente

```bash
curl -X GET "http://localhost/api/requests?urgent=true&page=0&size=10" \
  -H "Authorization: Bearer {SEU_TOKEN_AQUI}"
```

#### 4.4 Filtrar por Período

```bash
curl -X GET "http://localhost/api/requests?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59&page=0&size=10" \
  -H "Authorization: Bearer {SEU_TOKEN_AQUI}"
```

#### 4.5 Buscar por Texto (Protocolo ou Nome do Módulo)

```bash
curl -X GET "http://localhost/api/requests?searchText=SOL-20240101&page=0&size=10" \
  -H "Authorization: Bearer {SEU_TOKEN_AQUI}"
```

#### 4.6 Combinar Filtros

```bash
curl -X GET "http://localhost/api/requests?status=ATIVO&urgent=true&page=0&size=10" \
  -H "Authorization: Bearer {SEU_TOKEN_AQUI}"
```

---

### **ETAPA 5: Detalhes de Solicitação**

#### 5.1 Consultar Detalhes de Solicitação Própria

**Substitua `{id}` pelo ID da solicitação:**
```bash
curl -X GET http://localhost/api/requests/1 \
  -H "Authorization: Bearer {SEU_TOKEN_AQUI}"
```

**Resultado esperado**: Detalhes completos incluindo:
- Protocolo
- Módulos solicitados
- Justificativa
- Status
- Data de solicitação
- Data de expiração (se aprovado)
- Motivo da negação (se negado)
- Histórico de alterações

#### 5.2 Tentar Consultar Solicitação de Outro Usuário

1. Faça login com `ti@supera.com` e crie uma solicitação (anote o ID)
2. Faça login com `financeiro@supera.com`
3. Tente consultar a solicitação do usuário TI

**Resultado esperado**: Status 403 ou 400, erro de permissão

---

### **ETAPA 6: Renovar Acesso**

#### 6.1 Renovar Acesso (Sucesso)

**Pré-requisito**: Solicitação ativa com menos de 30 dias para expirar

**Nota**: Para testar, você pode criar uma solicitação e ajustar manualmente a data de expiração no banco, ou aguardar que falte menos de 30 dias.

```bash
curl -X POST http://localhost/api/requests/1/renew \
  -H "Authorization: Bearer {SEU_TOKEN_AQUI}"
```

**Resultado esperado**: Status 200
```json
{
  "message": "Solicitação criada com sucesso! Protocolo: SOL-20240101-0002. Seus acessos já estão disponíveis!"
}
```

#### 6.2 Tentar Renovar Acesso Muito Cedo

**Cenário**: Solicitação com mais de 30 dias para expirar

**Resultado esperado**: Status 400
```json
{
  "message": "Renovação só é permitida quando faltam menos de 30 dias para expiração"
}
```

#### 6.3 Tentar Renovar Solicitação Não Ativa

**Cenário**: Tentar renovar solicitação com status NEGADO ou CANCELADO

**Resultado esperado**: Status 400
```json
{
  "message": "Apenas solicitações ativas podem ser renovadas"
}
```

---

### **ETAPA 7: Cancelar Solicitação**

#### 7.1 Cancelar Solicitação Ativa (Sucesso)

```bash
curl -X POST http://localhost/api/requests/1/cancel \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {SEU_TOKEN_AQUI}" \
  -d '{
    "reason": "Não preciso mais deste acesso pois mudei de departamento"
  }'
```

**Resultado esperado**: Status 200
```json
{
  "message": "Solicitação cancelada com sucesso"
}
```

**✅ Validações:**
- Status muda para `CANCELADO` ✓
- Acesso aos módulos é revogado ✓
- Motivo é registrado no histórico ✓

#### 7.2 Tentar Cancelar Solicitação Não Ativa

**Cenário**: Tentar cancelar solicitação já cancelada ou negada

**Resultado esperado**: Status 400
```json
{
  "message": "Apenas solicitações ativas podem ser canceladas"
}
```

#### 7.3 Cancelar com Motivo Inválido

**Teste com motivo muito curto (< 10 caracteres):**
```json
{
  "reason": "teste"
}
```

**Teste com motivo muito longo (> 200 caracteres):**
```json
{
  "reason": "a".repeat(201)
}
```

**Resultado esperado**: Status 400, erro de validação

---

## 📊 Checklist de Funcionalidades

### Autenticação
- [ ] Login com credenciais válidas
- [ ] Login com credenciais inválidas
- [ ] Token JWT expira em 15 minutos

### Módulos
- [ ] Listar módulos disponíveis (ativos)
- [ ] Listar todos os módulos (ativos e inativos)
- [ ] Ver departamentos permitidos por módulo
- [ ] Ver módulos incompatíveis

### Criar Solicitação
- [ ] Solicitação aprovada automaticamente
- [ ] Solicitação negada (departamento sem permissão)
- [ ] Solicitação negada (módulos mutuamente exclusivos)
- [ ] Solicitação negada (limite de módulos excedido)
- [ ] Solicitação negada (justificativa genérica)
- [ ] Validação: mínimo 1 módulo, máximo 3 módulos
- [ ] Validação: justificativa 20-500 caracteres
- [ ] Validação: não pode ter solicitação ativa para mesmo módulo
- [ ] Validação: não pode solicitar módulo que já possui acesso

### Listar Solicitações
- [ ] Listar com paginação
- [ ] Filtrar por status
- [ ] Filtrar por urgente
- [ ] Filtrar por período
- [ ] Buscar por texto (protocolo ou nome do módulo)
- [ ] Combinar múltiplos filtros
- [ ] Ordenação: mais recentes primeiro

### Detalhes de Solicitação
- [ ] Consultar detalhes de solicitação própria
- [ ] Não pode consultar solicitação de outro usuário
- [ ] Exibir histórico de alterações

### Renovar Acesso
- [ ] Renovar quando faltam menos de 30 dias
- [ ] Não pode renovar muito cedo (> 30 dias)
- [ ] Não pode renovar solicitação não ativa
- [ ] Não pode renovar solicitação de outro usuário

### Cancelar Solicitação
- [ ] Cancelar solicitação ativa
- [ ] Não pode cancelar solicitação não ativa
- [ ] Acesso aos módulos é revogado
- [ ] Motivo é registrado no histórico
- [ ] Validação: motivo 10-200 caracteres

---

## 🎯 Cenários de Teste por Regra de Negócio

### Regra 1: Compatibilidade de Departamento

| Departamento | Módulos Permitidos |
|--------------|-------------------|
| TI | Todos os módulos |
| Financeiro | Gestão Financeira, Aprovador Financeiro, Solicitante Financeiro, Relatórios, Portal |
| RH | Administrador RH, Colaborador RH, Relatórios, Portal |
| Operações | Gestão de Estoque, Compras, Relatórios, Portal |
| Outros | Portal, Relatórios |

**Teste**: Faça login com cada departamento e tente solicitar módulos permitidos e não permitidos.

### Regra 2: Módulos Mutuamente Exclusivos

- **Aprovador Financeiro** ↔ **Solicitante Financeiro**
- **Administrador RH** ↔ **Colaborador RH**

**Teste**: 
1. Solicite "Aprovador Financeiro" (deve ser aprovado)
2. Tente solicitar "Solicitante Financeiro" (deve ser negado)
3. Cancele a primeira solicitação
4. Solicite "Solicitante Financeiro" (deve ser aprovado)

### Regra 3: Limite de Módulos

- **Usuários comuns**: Máximo 5 módulos ativos
- **Usuários TI**: Máximo 10 módulos ativos

**Teste**:
1. Crie solicitações até atingir o limite
2. Tente criar uma solicitação adicional (deve ser negada)

### Regra 4: Validação de Justificativa

**Justificativas genéricas rejeitadas:**
- "teste"
- "aaa"
- "preciso"
- Textos muito curtos (< 20 caracteres)
- Textos apenas com letras repetidas

**Teste**: Tente criar solicitações com cada uma dessas justificativas.

---

## 🔧 Dicas de Teste

### 1. Usar Swagger UI
- Mais fácil para testes manuais
- Interface visual
- Permite testar autenticação facilmente
- Mostra exemplos de requisições

### 2. Usar Postman/Insomnia
- Criar coleção de requisições
- Salvar tokens automaticamente
- Executar testes em sequência
- Compartilhar com equipe

### 3. Usar curl (Scripts)
- Criar scripts bash para testes automatizados
- Útil para CI/CD
- Fácil de documentar

### 4. Verificar Logs
```bash
docker-compose logs -f app1
```

### 5. Verificar Banco de Dados
```bash
docker-compose exec postgres psql -U postgres -d case_supera
```

---

## ⚠️ Problemas Comuns

### Token Expirado
**Sintoma**: Erro 401 Unauthorized
**Solução**: Faça login novamente e obtenha um novo token

### Solicitação Não Aparece na Lista
**Causa**: Filtros aplicados ou paginação
**Solução**: Verifique filtros e navegue pelas páginas

### Erro 403 Forbidden
**Causa**: Tentando acessar recurso de outro usuário
**Solução**: Use token do usuário correto

### Erro de Validação
**Causa**: Dados inválidos (justificativa curta, muitos módulos, etc.)
**Solução**: Verifique os requisitos de validação

---

## 📝 Exemplo de Fluxo Completo

1. **Login** → Obter token
2. **Listar módulos** → Ver módulos disponíveis
3. **Criar solicitação** → Solicitar acesso a módulo
4. **Listar solicitações** → Verificar status
5. **Detalhes da solicitação** → Ver informações completas
6. **Renovar acesso** (se aplicável) → Estender validade
7. **Cancelar solicitação** (se necessário) → Revogar acesso

---

## 🎓 Próximos Passos

Após testar todas as funcionalidades:

1. **Testes Automatizados**: Execute `mvn test` para rodar testes unitários e de integração
2. **Cobertura**: Verifique relatório JaCoCo em `target/site/jacoco/index.html`
3. **Performance**: Teste com múltiplas requisições simultâneas
4. **Segurança**: Teste tentativas de acesso não autorizado

---

**Boa sorte com os testes! 🚀**

