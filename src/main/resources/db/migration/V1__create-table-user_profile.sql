create table user(
    id bigint not null auto_increment,
    name varchar(100) not null,
    password varchar(500) not null,
    email varchar(100) not null unique,

    enabled boolean not null default true,
    account_non_locked boolean not null default true,
    account_non_expired boolean not null default true,
    credentials_non_expired boolean not null default true,

    password_changed_at timestamp not null,
    account_created_at timestamp not null,

    token_version int not null default 0,

    primary key(id)
);

create table profile(
     id tinyint not null auto_increment,
     name varchar(20) not null,

     primary key(id)
);

create table user_x_profile(
    user_id bigint not null,
    profile_id tinyint not null,

    primary key(user_id, profile_id),

    foreign key (user_id) references user(id),
    foreign key (profile_id) references profile(id)
);

insert into profile(name)
values('ADMIN');
insert into profile(name)
values('BASIC');

/* password 1234567890 for tests only */
insert into user(
    name,
    password,
    email,
    password_changed_at,
    account_created_at
) values(
    'Admin User',
    '$2a$10$bZxIK957JA31x66sCP0ive0qsKvuLjT/XEO27hPGjk.rNO8PaAVW6',
    'admin@admin.admin',
    sysdate(),
    sysdate()
);
insert into user(
    name,
    password,
    email,
    password_changed_at,
    account_created_at
) values(
    'Basic User',
    '$2a$10$bZxIK957JA31x66sCP0ive0qsKvuLjT/XEO27hPGjk.rNO8PaAVW6',
    'basic@basic.basic',
    sysdate(),
    sysdate()
);

insert into user_x_profile(user_id, profile_id)
values(1,1);
insert into user_x_profile(user_id, profile_id)
values(2,2);


