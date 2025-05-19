DO
$$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'custom_user') THEN
            CREATE USER custom_user WITH PASSWORD 'sd$2sq1K52Fa';
        END IF;
    END
$$;

DO
$$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'weather_bot_db') THEN
            CREATE DATABASE weather_bot_db OWNER custom_user;
        END IF;
    END
$$;
