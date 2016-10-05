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
