do $$ declare t record;
begin
    -- order by seq for easier troubleshooting when data does not satisfy FKs
    for t in select * from dropped_foreign_keys order by seq loop
        execute t.sql;
        delete from dropped_foreign_keys where seq = t.seq;
    end loop;
end $$;