
CREATE DATABASE mini_dish_db;

CREATE USER mini_dish_manager WITH PASSWORD 'mini123';

GRANT ALL PRIVILEGES ON DATABASE mini_dish_db TO mini_dish_manager;

\c mini_dish_db

GRANT ALL ON SCHEMA public TO mini_dish_manager;

ALTER SCHEMA public OWNER TO mini_dish_manager;
