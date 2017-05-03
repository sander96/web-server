CREATE TABLE IF NOT EXISTS user(
	username VARCHAR(50) PRIMARY KEY,
	password VARCHAR(60),
	permission BOOLEAN,
	cookie VARCHAR(60),
	cookie_expiration LONG
);