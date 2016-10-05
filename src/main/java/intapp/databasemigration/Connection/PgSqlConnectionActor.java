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
    private final String user; 
    private final String password;
    
    private Connection connection;
    
    public PgSqlConnectionActor(String connectionString, String user, String password) throws ClassNotFoundException, SQLException
    {
        this.connectionString = connectionString;
        this.user = user;
        this.password = password;
    }
    
    @Override
    public void onReceive(Object message) throws Exception {
        
        if(connection == null)
        {
            System.out.println("Get pgsql connection");

            //Class.forName("org.postgresql.Driver");
            String url = connectionString;
            Properties props = new Properties();
            props.setProperty("user",user);
            props.setProperty("password",password);
            this.connection = DriverManager.getConnection(url, props);
        }
        
        sender().tell(connection, self());
    }
    
}
