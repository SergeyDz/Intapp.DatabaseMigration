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

    public static Map<String, String> PropertiesMap;
    
    public static void main(String[] args) throws Exception {
        ActorSystem system = ActorSystem.create("data-migration-system");
         
        PropertiesMap = new HashMap<>();
        
        if(args != null && args.length > 0)
        {
            for (String arg : args) {
                if (arg.contains("=")) {
                    String key = arg.substring(0, arg.indexOf('='));
                    String value = arg.substring(arg.indexOf('=') + 1);
                    PropertiesMap.put(key, value);
                }
            }
        }
        
        RoundRobinPool pool = new RoundRobinPool(8);
        
        ActorRef msConnectionActor = system.actorOf(pool.props(Props.create(MsSqlConnectionActor.class, PropertiesMap.get("mssql-connection"))));
        ActorRef pgConnectionActor = system.actorOf(pool.props(Props.create(PgSqlConnectionActor.class, PropertiesMap.get("pgsql-connection"))));

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
