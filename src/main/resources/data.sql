-- Inserir usuários (senhas são "senha123" criptografadas com BCrypt)
INSERT INTO users (email, password, name, department) VALUES
('ti@supera.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'João Silva - TI', 'TI'),
('financeiro@supera.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Maria Santos - Financeiro', 'FINANCEIRO'),
('rh@supera.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Pedro Oliveira - RH', 'RH'),
('operacoes@supera.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Ana Costa - Operações', 'OPERACOES'),
('outros@supera.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Carlos Mendes - Outros', 'OUTROS');

-- Inserir módulos
INSERT INTO modules (name, description, active) VALUES
('Portal do Colaborador', 'Portal principal para colaboradores', true),
('Relatórios Gerenciais', 'Sistema de relatórios gerenciais', true),
('Gestão Financeira', 'Sistema de gestão financeira', true),
('Aprovador Financeiro', 'Módulo para aprovação de solicitações financeiras', true),
('Solicitante Financeiro', 'Módulo para solicitação de recursos financeiros', true),
('Administrador RH', 'Módulo administrativo de recursos humanos', true),
('Colaborador RH', 'Módulo para colaboradores do RH', true),
('Gestão de Estoque', 'Sistema de gestão de estoque', true),
('Compras', 'Sistema de gestão de compras', true),
('Auditoria', 'Módulo de auditoria do sistema', true);

-- Configurar departamentos permitidos por módulo
-- Portal do Colaborador - todos os departamentos
INSERT INTO module_allowed_departments (module_id, department) VALUES
(1, 'TI'), (1, 'FINANCEIRO'), (1, 'RH'), (1, 'OPERACOES'), (1, 'OUTROS');

-- Relatórios Gerenciais - todos os departamentos
INSERT INTO module_allowed_departments (module_id, department) VALUES
(2, 'TI'), (2, 'FINANCEIRO'), (2, 'RH'), (2, 'OPERACOES'), (2, 'OUTROS');

-- Gestão Financeira - Financeiro e TI
INSERT INTO module_allowed_departments (module_id, department) VALUES
(3, 'FINANCEIRO'), (3, 'TI');

-- Aprovador Financeiro - Financeiro e TI
INSERT INTO module_allowed_departments (module_id, department) VALUES
(4, 'FINANCEIRO'), (4, 'TI');

-- Solicitante Financeiro - Financeiro e TI
INSERT INTO module_allowed_departments (module_id, department) VALUES
(5, 'FINANCEIRO'), (5, 'TI');

-- Administrador RH - RH e TI
INSERT INTO module_allowed_departments (module_id, department) VALUES
(6, 'RH'), (6, 'TI');

-- Colaborador RH - RH e TI
INSERT INTO module_allowed_departments (module_id, department) VALUES
(7, 'RH'), (7, 'TI');

-- Gestão de Estoque - Operações e TI
INSERT INTO module_allowed_departments (module_id, department) VALUES
(8, 'OPERACOES'), (8, 'TI');

-- Compras - Operações e TI
INSERT INTO module_allowed_departments (module_id, department) VALUES
(9, 'OPERACOES'), (9, 'TI');

-- Auditoria - apenas TI
INSERT INTO module_allowed_departments (module_id, department) VALUES
(10, 'TI');

-- Configurar incompatibilidades
-- Aprovador Financeiro (4) e Solicitante Financeiro (5) são incompatíveis
INSERT INTO module_incompatibilities (module_id, incompatible_module_id) VALUES
(4, 5), (5, 4);

-- Administrador RH (6) e Colaborador RH (7) são incompatíveis
INSERT INTO module_incompatibilities (module_id, incompatible_module_id) VALUES
(6, 7), (7, 6);

