create table `character`(
    id bigint not null auto_increment,
    name varchar(100) not null,
    base_character_attributes_id bigint not null,
    created_at timestamp not null,
    user_id bigint not null,

    primary key(id),
    foreign key (user_id) references user(id)
);

create table character_attributes(
    id bigint not null auto_increment,
    level smallint not null,
    strength smallint not null,
    dexterity smallint not null,
    intelligence smallint not null,
    constitution smallint not null,
    willpower smallint not null,
    perception smallint not null,
    luck smallint not null,

    primary key(id)
);

ALTER TABLE `character`
ADD CONSTRAINT fk_character_x_character_attributes
FOREIGN KEY (base_character_attributes_id)
REFERENCES character_attributes(id);
