/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package intapp.databasemigration;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.RoundRobinPool;
import intapp.databasemigration.Connection.MsSqlConnectionActor;
import intapp.databasemigration.Connection.PgSqlConnectionActor;
import intapp.databasemigration.Engine.MigrationEngineActor;
import intapp.databasemigration.Metadata.MsSqlSchemaActor;
import intapp.databasemigration.Metadata.PgSqlSchemaActor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 *
 * @author sdzyuban
 */
public class Runner {
     public static void main(String[] args) throws Exception {
        ActorSystem system = ActorSystem.create("data-migration-system");
        
        ActorRef msConnectionActor = system.actorOf(new RoundRobinPool(8).props(Props.create(MsSqlConnectionActor.class, "jdbc:sqlserver://poc-open-sql.fg.local;databaseName=POC-OMM", "omm", "Tsunami9")));
        ActorRef pgConnectionActor = system.actorOf(new RoundRobinPool(8).props(Props.create(PgSqlConnectionActor.class, "jdbc:postgresql://sdzyuban-pc.fg.local:5432/opendb", "postgres", "Tsunami9")));
      
        System.out.println("Actor sysrem data-migration-system started.");
        
        ActorRef engine = system.actorOf(Props.create(MigrationEngineActor.class, msConnectionActor, pgConnectionActor));
        engine.tell("start", null); 
        
         Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                system.shutdown();
                System.out.println("Shutting down ... ");
            }
        });
     }
}
