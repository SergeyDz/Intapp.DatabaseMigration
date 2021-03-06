/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package intapp.databasemigration.Engine;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.RoundRobinPool;
import intapp.databasemigration.POCO.Table;
import intapp.databasemigration.Table.TableCopyActor;
import intapp.databasemigration.POCO.TableCopyRequest;
import intapp.databasemigration.Runner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author sdzyuban
 */
public class DataCopyActor extends UntypedActor {

    private final List<Table> source;

    private final List<Table> destination;

    private final ActorRef msConnectionActor;
    private final ActorRef pgConnectionActor;
    private ActorRef sender;
    
    private int workers;

    private List<String> skip;
    private List<String> statuses;

    public DataCopyActor(List<Table> source, List<Table> destination, ActorRef ms, ActorRef pg) {
        this.source = source;
        this.destination = destination;

        this.msConnectionActor = ms;
        this.pgConnectionActor = pg;
        
        this.workers = 0;
        this.statuses = new ArrayList<>();

        skip = Arrays.asList(Runner.PropertiesMap.get("skip").split(","));
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if ("start".equals(message)) {
            
            this.sender = sender();
            RoundRobinPool pool = new RoundRobinPool(8);        
            
            List<Table> workerItems = this.source.stream().filter(s -> !skip.contains(s.Name) 
                    && destination.stream().filter(m -> m.Name.toLowerCase().equals(s.Name.toLowerCase())).findAny().isPresent())
                    .collect(Collectors.toList());
            this.workers = workerItems.size();

            workerItems.forEach(a -> {
                    Optional<Table> destTable = destination.stream().filter(m -> m.Name.toLowerCase().equals(a.Name.toLowerCase())).findFirst();
                    if (destTable.isPresent()) {
                        ActorRef tableCopyActor = context().actorOf(pool.props(Props.create(TableCopyActor.class, this.msConnectionActor, this.pgConnectionActor)));
                        tableCopyActor.tell(new TableCopyRequest(a, destTable.get()), self());
                    } 
            });
        }
        else if(message instanceof String)
        {
            this.statuses.add(message.toString());
             
             this.workers--;
            
            if(this.workers <= 0)
            {
                System.out.println("Work completed");
                this.statuses.forEach(a->{ System.out.println(a); });
                
                int total = this.statuses.size();
                long errors = this.statuses.stream().filter(a -> a.startsWith("ERROR")).count();
                
                
                this.sender.tell(String.format("Done. Copied tables: %s, Errors: %s", total - errors, errors), self());
            }
        }
    }

}
