# 🚀 Guia Rápido - Como Rodar o Projeto

## Pré-requisitos
- Docker Desktop instalado e rodando
- Portas 80 e 5432 livres

## Passo a Passo

### 1. Abrir terminal no diretório do projeto
```bash
cd D:\Case-Supera
```

### 2. Executar Docker Compose
```bash
docker-compose up -d
```

### 3. Aguardar inicialização (1-2 minutos)
Verifique o status:
```bash
docker-compose ps
```

### 4. Acessar a aplicação
- **Swagger UI**: http://localhost/swagger-ui.html
- **API**: http://localhost
- **Health Check**: http://localhost/health

## Testar Login

### Credenciais de teste:
- **Email**: `ti@supera.com`
- **Senha**: `senha123`

### Exemplo com cURL:
```bash
curl -X POST http://localhost/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"ti@supera.com\",\"password\":\"senha123\"}"
```

### Exemplo com PowerShell:
```powershell
Invoke-RestMethod -Uri "http://localhost/api/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"email":"ti@supera.com","password":"senha123"}'
```

## Parar os containers
```bash
docker-compose down
```

## Ver logs
```bash
docker-compose logs -f
```

## Reconstruir após mudanças
```bash
docker-compose up -d --build
```

## Outros usuários disponíveis:
- `financeiro@supera.com` / `senha123`
- `rh@supera.com` / `senha123`
- `operacoes@supera.com` / `senha123`
- `outros@supera.com` / `senha123`

