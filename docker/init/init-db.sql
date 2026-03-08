IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'computershop')
BEGIN
    CREATE DATABASE computershop;
END
GO
