create table player_character (
    id bigint not null auto_increment,
    user_id bigint not null,

    primary key (id),

    constraint fk_player_character_x_unit
        foreign key (id)
        references unit(id)
);

create table enemy (
    id bigint not null auto_increment,

    primary key (id),

    constraint fk_enemy_x_unit
        foreign key (id)
        references unit(id)
);