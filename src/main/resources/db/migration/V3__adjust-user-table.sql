alter table user
modify column account_created_at timestamp default CURRENT_TIMESTAMP;