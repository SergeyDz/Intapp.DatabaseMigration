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
import intapp.databasemigration.Metadata.MsSqlSchemaActor;
import intapp.databasemigration.Metadata.PgSqlSchemaActor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 *
 * @author sdzyuban
 */
public class Runner {
     public static void main(String[] args) throws Exception {
//        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");        
//        Connection source = DriverManager.getConnection("jdbc:sqlserver://sdzyuban-pc.fg.local;databaseName=POC-OMM", "sa", "Tsunami9");
//        
//        Class.forName("org.postgresql.Driver");
//        String url = "jdbc:postgresql://sdzyuban-pc.fg.local:5432/opendb";
//        Properties props = new Properties();
//        props.setProperty("user","postgres");
//        props.setProperty("password","Tsunami9");
//        Connection destination = DriverManager.getConnection(url, props);
//        
//        Process(source, destination);
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
     
     public static void Process(Connection source, Connection destination) throws SQLException 
     {
         String tables = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.Tables WHERE TABLE_TYPE = 'BASE TABLE' and TABLE_SCHEMA = 'dbo'";
         try (PreparedStatement s1 = source.prepareStatement(tables);
         ResultSet rs = s1.executeQuery()){
             while (rs.next()) {
                 
                String table = rs.getNString(1);
                System.out.println("Starting copy of table: " + table);
                 try
                 {
                    copy(table, source, destination);
                 }
                 catch(SQLException ex)
                 {
                     System.err.println(ex.getNextException());
                 }
             }
         }
     }
    
    public static void copy(String table, Connection from, Connection to) throws SQLException 
    {
    try (PreparedStatement s1 = from.prepareStatement("select * from " + table);
         ResultSet rs = s1.executeQuery()) {
        ResultSetMetaData meta = rs.getMetaData();

        List<String> columns = new ArrayList<>();
        for (int i = 1; i <= meta.getColumnCount(); i++)
            columns.add(meta.getColumnName(i).toLowerCase());

        String sql =  "INSERT INTO " + table.toLowerCase() + " ("
              + columns.stream().map(c -> c.toLowerCase()).collect(Collectors.joining(", "))
              + ") VALUES ("
              + columns.stream().map(c -> "?").collect(Collectors.joining(", "))
              + ")";
        //System.out.println(sql);
        try (PreparedStatement s2 = to.prepareStatement(
               sql
        )) {

            while (rs.next()) {
                for (int i = 1; i <= meta.getColumnCount(); i++)
                    s2.setObject(i, rs.getObject(i));

                s2.addBatch();
            }

            s2.executeBatch();
        }
    }
}
}
