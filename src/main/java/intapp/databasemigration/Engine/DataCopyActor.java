/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package intapp.databasemigration.Engine;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Option;
import akka.routing.RoundRobinPool;
import intapp.databasemigration.Metadata.PgSqlSchemaActor;
import intapp.databasemigration.POCO.Table;
import intapp.databasemigration.Table.TableCopyActor;
import intapp.databasemigration.POCO.TableCopyRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author sdzyuban
 */
public class DataCopyActor extends UntypedActor {

    private final List<Table> source;

    private final List<Table> destination;

    private final ActorRef msConnectionActor;
    private final ActorRef pgConnectionActor;

    private List<String> skip;

    public DataCopyActor(List<Table> source, List<Table> destination, ActorRef ms, ActorRef pg) {
        this.source = source;
        this.destination = destination;

        this.msConnectionActor = ms;
        this.pgConnectionActor = pg;

        skip = Arrays.asList("Configs");
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if ("start".equals(message)) {
            RoundRobinPool pool = new RoundRobinPool(8);
            this.source.forEach(a -> {

                if (!skip.contains(a.Name)) {
                    Optional<Table> destTable = destination.stream().filter(m -> m.Name.toLowerCase().equals(a.Name.toLowerCase())).findFirst();

                    if (destTable.isPresent()) {
                        ActorRef tableCopyActor = context().actorOf(pool.props(Props.create(TableCopyActor.class, this.msConnectionActor, this.pgConnectionActor)));
                        tableCopyActor.tell(new TableCopyRequest(a, destTable.get()), self());
                    } else {
                        System.err.println("Destination table for source " + a.Name + " was not found");
                    }
                } 
                else
                {
                    System.out.println("Table " + a.Name + " skipped.");
                }
                
            });
        }
    }

}
