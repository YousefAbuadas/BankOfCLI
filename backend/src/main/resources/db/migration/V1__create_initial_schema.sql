CREATE TABLE users (
    id varchar(255) NOT NULL,
    first_name varchar(255),
    last_name varchar(255),
    email varchar(255) NOT NULL,
    password_hash varchar(255) NOT NULL,
    role varchar(255) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE accounts (
    id varchar(255) NOT NULL,
    owner_id varchar(255) NOT NULL,
    name varchar(255),
    balance double precision NOT NULL,
    currency varchar(255),
    mask varchar(255),
    type varchar(255) NOT NULL,
    CONSTRAINT pk_accounts PRIMARY KEY (id),
    CONSTRAINT ck_accounts_type CHECK (type IN ('CHECKING', 'SAVINGS'))
);

CREATE TABLE transactions (
    id varchar(255) NOT NULL,
    account_id varchar(255),
    type varchar(255),
    amount double precision NOT NULL,
    date varchar(255),
    description varchar(255),
    CONSTRAINT pk_transactions PRIMARY KEY (id)
);
