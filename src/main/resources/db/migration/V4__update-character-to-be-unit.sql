rename table `character` to unit;

alter table unit drop foreign key unit_ibfk_1;

alter table unit drop column user_id;

alter table unit drop foreign key fk_character_x_character_attributes;

alter table unit rename column base_character_attributes_id to base_unit_attributes_id;

rename table character_attributes to unit_attributes;

ALTER TABLE unit
ADD CONSTRAINT fk_unit_x_unit_attributes
FOREIGN KEY (base_unit_attributes_id)
REFERENCES unit_attributes(id);