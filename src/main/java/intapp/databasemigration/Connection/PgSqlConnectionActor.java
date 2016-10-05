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
import java.util.Properties;

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
            System.out.println("Get pgsql connection");
            this.connection = DriverManager.getConnection(this.connectionString);
        }

        sender().tell(connection, self());
    }

}
