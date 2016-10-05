create table if not exists dropped_foreign_keys (
        seq bigserial primary key,
        sql text
);

do $$ declare t record;
begin
    for t in select conrelid::regclass::varchar table_name, conname constraint_name,
            pg_catalog.pg_get_constraintdef(r.oid, true) constraint_definition
            from pg_catalog.pg_constraint r
            where r.contype = 'f'
            -- current schema only:
            and r.connamespace = (select n.oid from pg_namespace n where n.nspname = current_schema())
        loop

        insert into dropped_foreign_keys (sql) values (
            format('alter table %s add constraint %s %s',
                quote_ident(t.table_name), quote_ident(t.constraint_name), t.constraint_definition));

        execute format('alter table %s drop constraint %s', quote_ident(t.table_name), quote_ident(t.constraint_name));

    end loop;
end $$;

