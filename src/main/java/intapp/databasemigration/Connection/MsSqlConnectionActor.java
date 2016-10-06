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
public class MsSqlConnectionActor extends UntypedActor {

    private final String connectionString;
    
    private Connection connection;
    
    public MsSqlConnectionActor(String connectionString) throws ClassNotFoundException, SQLException
    {
        this.connectionString = connectionString;
    }
    
    @Override
    public void onReceive(Object message) throws Exception {
        
        if(connection == null)
        {
            //Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); 
            try 
            {
                this.connection = DriverManager.getConnection(connectionString);
            } 
            catch(Exception ex)
            {
               System.err.println(ex);
               context().system().shutdown();
            }
        }
        
        sender().tell(this.connection, self());
    }
    
}
