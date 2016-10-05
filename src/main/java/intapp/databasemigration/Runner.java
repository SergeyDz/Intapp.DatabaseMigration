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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sdzyuban
 */
public class Runner {

    private static Map<String, String> propertiesMap;
    
    public static void main(String[] args) throws Exception {
        ActorSystem system = ActorSystem.create("data-migration-system");
         
        propertiesMap = new HashMap<>();
        
        if(args != null && args.length > 0)
        {
            for (String arg : args) {
                if (arg.contains("=")) {
                    String key = arg.substring(0, arg.indexOf('='));
                    String value = arg.substring(arg.indexOf('=') + 1);
                    propertiesMap.put(key, value);
                }
            }
        }
        
        ActorRef msConnectionActor = system.actorOf(new RoundRobinPool(8).props(Props.create(MsSqlConnectionActor.class, propertiesMap.get("mssql-connection"))));
        ActorRef pgConnectionActor = system.actorOf(new RoundRobinPool(8).props(Props.create(PgSqlConnectionActor.class, propertiesMap.get("pgsql-connection"))));

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
