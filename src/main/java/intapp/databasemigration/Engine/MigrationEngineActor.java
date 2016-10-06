/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package intapp.databasemigration.Engine;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import intapp.databasemigration.Metadata.MsSqlSchemaActor;
import intapp.databasemigration.Metadata.PgSqlSchemaActor;
import intapp.databasemigration.POCO.Column;
import intapp.databasemigration.POCO.PrepareDatabaseRequest;
import intapp.databasemigration.POCO.SchemaResult;
import intapp.databasemigration.POCO.Table;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author sdzyuban
 */
public class MigrationEngineActor extends UntypedActor {

    private final ActorRef msConnectionActor;
    private final ActorRef pgConnectionActor;

    public MigrationEngineActor(ActorRef ms, ActorRef pg) {
        this.msConnectionActor = ms;
        this.pgConnectionActor = pg;
    }

    private List<Table> MsSqlTables;
    private List<Table> PgSqlTables;

    @Override
    public void onReceive(Object message) throws Exception {
        if ("start".equals(message)) {
            this.msConnectionActor.tell("get", self());
        } else if (message instanceof Connection) {
            Connection connection = (Connection) message;
            if (connection.getMetaData().getURL().contains("sqlserver")) {
                ActorRef schemaActor = context().actorOf(Props.create(MsSqlSchemaActor.class), "MsSqlSchemaActor");
                schemaActor.tell(connection, self());
            } else {
                ActorRef schemaActor = context().actorOf(Props.create(PgSqlSchemaActor.class), "PgSqlSchemaActor");
                schemaActor.tell(connection, self());
            }
        } else if (message instanceof SchemaResult) {
            SchemaResult schema = (SchemaResult) message;
            if (sender().path().name().contains("MsSqlSchemaActor")) {
                MsSqlTables = schema.Tables;

                this.pgConnectionActor.tell("get", self());
            } else {
                this.PgSqlTables = schema.Tables;

                System.out.println("Metadata scan completed");
                System.out.println("Found source tables: " + this.MsSqlTables.size());
                System.out.println("Found destination tables: " + this.PgSqlTables.size());

                ActorRef dataCopyActor = context().actorOf(Props.create(PrepareTargetDatabaseActor.class, this.pgConnectionActor));
                dataCopyActor.tell(new PrepareDatabaseRequest(Arrays.asList("CleanUp.sql", "RemoveForeignKeys.sql")), self());
            }

        } 
        else if ("target ready".equals(message)) {
            
            this.determineAndFixNullableColumns();
            
            ActorRef dataCopyActor = context().actorOf(Props.create(DataCopyActor.class, this.MsSqlTables, this.PgSqlTables, this.msConnectionActor, this.pgConnectionActor));
            dataCopyActor.tell("start", self());
        }
         else if ("finalized".equals(message)) {
            context().system().shutdown();
        }
        else if(message.toString().startsWith("Done."))
        {
            System.out.println(message);
            ActorRef dataCopyActor = context().actorOf(Props.create(FinalizeTargetDatabaseActor.class, this.pgConnectionActor));
            dataCopyActor.tell(new PrepareDatabaseRequest(Arrays.asList("RestoreForeignKeys.sql")), self());
        }
    }
    
    private void determineAndFixNullableColumns()
    {
        this.MsSqlTables.forEach(source -> {
            Optional<Table> d1 = this.PgSqlTables.stream().filter(x -> x.Name.toLowerCase().equals(source.Name.toLowerCase())).findFirst();
            if(d1.isPresent())
            {
                Table d2 = d1.get();
                List<Column> columnsToAdd = new ArrayList<>();
                
                d2.Columns.forEach(x -> {
                    Optional<Column> d3 = source.Columns.stream().filter(y -> y.Name.toLowerCase().equals(x.Name.toLowerCase())).findFirst();
                    
                    if(d3.isPresent() || x.IsNullable)
                    {
                        // it's ok
                    }
                    else
                    {
                        Column c = new Column(x.Name, x.Type, x.IsNullable);
                        c.IsAutoFill = true;
                        columnsToAdd.add(c);
                    }
                });
                
                if(columnsToAdd.size() > 0)
                {
                    columnsToAdd.forEach(x -> {
                        source.Columns.add(x);
                    });
                }
            }
            
        });
        
    }

}
