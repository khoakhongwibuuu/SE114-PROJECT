
CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role::text = ANY (ARRAY['USER'::character varying, 'DOCTOR'::character varying, 'ADMIN'::character varying]::text[]));

INSERT INTO users (email, password_hash, role, full_name, is_active, is_verified, created_at, updated_at)
VALUES 
('admin@gmail.com', crypt('Password123!', gen_salt('bf', 10)), 'ADMIN', 'Admin', true, true, NOW(), NOW()),
('doletuankiet06@gmail.com', crypt('Kiet13012006', gen_salt('bf', 10)), 'USER', 'Tuan Kiet', true, true, NOW(), NOW()),
('kiet@gmail.com', crypt('Kiet13012006', gen_salt('bf', 10)), 'USER', 'Kiet Tuan', true, true, NOW(), NOW()),
('bacsinhikhoa@gmail.com', crypt('Bacsinhikhoa', gen_salt('bf', 10)), 'DOCTOR', 'Bác sĩ Nhi Khoa', true, true, NOW(), NOW()),
('bacsidakhoa@gmail.com', crypt('Bacsidakhoa', gen_salt('bf', 10)), 'DOCTOR', 'Bác sĩ Đa Khoa', true, true, NOW(), NOW())
ON CONFLICT (email) DO UPDATE SET 
password_hash = EXCLUDED.password_hash, 
role = EXCLUDED.role, 
full_name = EXCLUDED.full_name;
