create table app_user (
    id varchar(36) PRIMARY KEY NOT NULL,
    email varchar(500) NOT NULL UNIQUE,
    password varchar(500) NOT NULL,
    display_name varchar(250),
    verified boolean
);

create table token (
    token varchar(50) NOT NULL PRIMARY KEY,
    user_id varchar(36) NOT NULL REFERENCES app_user(id) ON DELETE CASCADE
);