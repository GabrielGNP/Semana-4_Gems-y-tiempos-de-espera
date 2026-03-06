-- Inicialización de la base de datos para el proyecto Reports Query
-- Crea extensión necesaria y la tabla 'tickets' con esquema mínimo

-- Crear extensión pgcrypto para gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Crear tabla tickets si no existe
CREATE TABLE IF NOT EXISTS tickets (
  ticket_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  line_number VARCHAR(20) NOT NULL,
  email VARCHAR(255),
  type VARCHAR(50) NOT NULL,
  description TEXT,
  status VARCHAR(50) NOT NULL,
  priority VARCHAR(50) NOT NULL,
  created_at TIMESTAMPTZ DEFAULT now(),
  processed_at TIMESTAMPTZ
);

-- Índices útiles
CREATE INDEX IF NOT EXISTS idx_tickets_line_number ON tickets(line_number);
CREATE INDEX IF NOT EXISTS idx_tickets_status ON tickets(status);
CREATE INDEX IF NOT EXISTS idx_tickets_priority ON tickets(priority);

-- Tickets de ejemplo
INSERT INTO tickets (line_number, email, type, description, status, priority, created_at) VALUES
  ('300-001-001', 'carlos.garcia@email.com',   'NO_SERVICE',           'Sin servicio desde las 8am',                'RECEIVED',    'HIGH',   now() - interval '2 hours'),
  ('300-001-002', 'maria.lopez@email.com',      'NO_SERVICE',           'No hay conexión en toda la zona',           'RECEIVED',    'HIGH',   now() - interval '3 hours'),
  ('300-002-001', 'juan.perez@email.com',        'INTERMITTENT_SERVICE', 'Se cae cada 10 minutos',                   'RECEIVED',    'MEDIUM', now() - interval '5 hours'),
  ('300-002-002', 'ana.martinez@email.com',      'SLOW_CONNECTION',      'Velocidad muy baja, menos de 1 Mbps',      'IN_PROGRESS', 'HIGH',   now() - interval '1 day'),
  ('300-003-001', 'luis.rodriguez@email.com',    'ROUTER_ISSUE',         'El router reinicia solo constantemente',    'RECEIVED',    'HIGH',   now() - interval '4 hours'),
  ('300-003-002', 'sofia.hernandez@email.com',   'BILLING_QUESTION',     'Cobro duplicado en la factura de febrero',  'RECEIVED',    'LOW',    now() - interval '2 days'),
  ('300-004-001', 'pedro.sanchez@email.com',     'NO_SERVICE',           'Lleva 2 días sin servicio',                 'RECEIVED',    'HIGH',   now() - interval '6 hours'),
  ('300-004-002', 'laura.gomez@email.com',       'OTHER',                'Solicitud de cambio de plan',               'IN_PROGRESS', 'LOW',    now() - interval '12 hours'),
  ('300-005-001', 'diego.torres@email.com',      'INTERMITTENT_SERVICE', 'Internet intermitente en horas pico',       'RECEIVED',    'MEDIUM', now() - interval '8 hours'),
  ('300-005-002', 'valentina.diaz@email.com',    'NO_SERVICE',           'Sin servicio después de tormenta eléctrica','RECEIVED',    'HIGH',   now() - interval '1 hour');
