/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package intapp.databasemigration.Engine;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import intapp.databasemigration.POCO.PrepareDatabaseRequest;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author sdzyuban
 */
public class PrepareTargetDatabaseActor extends UntypedActor {

    protected final ActorRef pgConnectionActor;

    protected ActorRef engine;

    protected Connection connection;

    protected final List<String> queries;
 
    public PrepareTargetDatabaseActor(ActorRef pg) {
        this.pgConnectionActor = pg;

        this.queries = new ArrayList<>();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof PrepareDatabaseRequest) {
            PrepareDatabaseRequest request = (PrepareDatabaseRequest) message;
            this.engine = sender();

            request.Scripts.forEach(script -> {

                try {
                    System.out.println("Loading sql for resource " + script);
                    InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(script);
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(resourceAsStream, writer, Charset.defaultCharset());
                    String sql = writer.toString();
                    queries.add(sql);

                } catch (Exception ex) {
                    System.err.println(ex);
                }
            });

            this.pgConnectionActor.tell("get", self());

        } else if (message instanceof Connection) {
            this.connection = (Connection) message;
            this.queries.forEach(sql -> {
                try {
                    PreparedStatement s1 = connection.prepareStatement(sql);
                    s1.execute();
                } catch (Exception ex) {
                    System.err.println(ex);
                }
            });

            System.out.println("SQL queries executed");
            this.engine.tell(this.getResponse(), null);
        }
    }
    
    protected String getResponse()
    {
       return "target ready";
    }

}
