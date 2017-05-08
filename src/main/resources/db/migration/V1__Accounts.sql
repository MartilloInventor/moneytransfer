CREATE TABLE IF NOT EXISTS accounts
(
  id        TEXT UNIQUE NOT NULL,
  balance   int NOT NULL DEFAULT 0,
  CONSTRAINT pk_accounts_id PRIMARY KEY (id)
);


INSERT INTO accounts
(id, balance)
VALUES
('1', 100),
('2', 120),
('3', 700),
('4', 1000);
