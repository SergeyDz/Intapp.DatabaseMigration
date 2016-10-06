/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package intapp.databasemigration.Connection;

import akka.actor.UntypedActor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author sdzyuban
 */
public class PgSqlConnectionActor extends UntypedActor {

    private final String connectionString;

    private Connection connection;

    public PgSqlConnectionActor(String connectionString) throws ClassNotFoundException, SQLException {
        this.connectionString = connectionString;
    }

    @Override
    public void onReceive(Object message) throws Exception {

        if (connection == null) {
            try
            {
                System.out.println("Get pgsql connection");
                Class.forName("org.postgresql.Driver");
                this.connection = DriverManager.getConnection(this.connectionString);
            }
            catch(Exception ex)
            {
               System.err.println(ex);
               context().system().shutdown();
            }
        }

        sender().tell(connection, self());
    }

}
