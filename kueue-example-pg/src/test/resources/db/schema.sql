create table kueue_messages (
    id uuid,
    topic varchar(255),
    message text,
    class text,
    created timestamp with time zone,
    primary key(id)
);
