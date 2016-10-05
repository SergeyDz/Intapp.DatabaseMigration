/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package intapp.databasemigration.Table;

import intapp.databasemigration.POCO.TableCopyRequest;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import intapp.databasemigration.POCO.Table;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.json.XML;

/**
 *
 * @author sdzyuban
 */
public class TableCopyActor extends UntypedActor {

    private Table sourceTable;
    private Table destinationTable;

    private final ActorRef msConnectionActor;
    private final ActorRef pgConnectionActor;
    private ActorRef sender;
    
    private int pageSize;

    private Connection sourceConnection;
    private Connection destinationConnection;

    public TableCopyActor(ActorRef ms, ActorRef pg) {
        this.msConnectionActor = ms;
        this.pgConnectionActor = pg;
        
        this.pageSize = 1000;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof TableCopyRequest) {
            TableCopyRequest request = (TableCopyRequest) message;
            this.sender = sender();
            this.sourceTable = request.source;
            this.destinationTable = request.destination;
            System.out.println("Starting copy data for Table: " + sourceTable.Name);
            
            if("requestdefinitions".equals(this.destinationTable.Name))
            {
                this.pageSize = 100;
            }
            
            msConnectionActor.tell("get", self());
        } else if (message instanceof Connection) {
            Connection connection = (Connection) message;
            if (connection.getMetaData().getURL().contains("sqlserver")) {
                sourceConnection = connection;
                pgConnectionActor.tell("get", self());
            } else {
                destinationConnection = connection;

                final TableFormatter formatter = TableFormatter.Create(sourceTable, destinationTable);
                String sql = "insert into " + destinationTable.Name + " ("
                            + sourceTable.Columns.stream().map(c -> formatter.MapName(c.Name)).collect(Collectors.joining(", "))
                            + ") values ("
                            + sourceTable.Columns.stream().map(c -> formatter.MapValue(c.Name)).collect(Collectors.joining(", "))
                            + ")";
                 
                int from = 1;
                int to = from + this.pageSize;
                int totalItems = 0;
                
                boolean result = true;
                
                while(result)
                {
                    int iterationCount = ProcessPage(from, to, sql);
                    totalItems = totalItems + iterationCount;
                    
                    if(iterationCount == pageSize)
                    {
                        from = to;
                        to = to + pageSize;
                    } 
                    else
                    {
                        result = false;
                    }
                } 
                 
                this.sender.tell(String.format("Done. %s completed. Total rows: %s.", destinationTable.Name, totalItems), self());
            }
        }
    }

    private int ProcessPage(int from, int to, String sql) throws SQLException {
        try (PreparedStatement s1 = sourceConnection.prepareStatement(String.format("SELECT * FROM (SELECT *, ROW_NUMBER() OVER (ORDER BY %s) AS RowNum FROM %s.%s ) AS RowConstrainedResult WHERE RowNum >= %s AND RowNum < %s ORDER BY RowNum", 
                this.sourceTable.Columns.get(0).Name,
                this.sourceTable.Schema, this.sourceTable.Name, from, to));
                ResultSet rs = s1.executeQuery()) {
            ResultSetMetaData meta = rs.getMetaData();
            int counter = 0;
            
            System.out.println(sql);
            try(PreparedStatement s2 = destinationConnection.prepareStatement(sql)) {
                while (rs.next()) {
                    for (int i = 1; i <= meta.getColumnCount() - 1; i++) {
                        String columnName = meta.getColumnName(i);
                        if ("CustomFieldsXml".equals(columnName)) {
                            s2.setObject(i, XML.toJSONObject(rs.getString(i)), java.sql.Types.VARCHAR);
                        } else {
                            s2.setObject(i, rs.getObject(i));
                        }
                    }
                    
                    s2.addBatch();
                    counter++;
                }
                
                System.out.println(String.format("insert into %s from %s to %s", destinationTable.Name, from, to));
                s2.executeBatch();
                System.out.println(String.format("insert into %s from %s to %s copleted. Records: %s", destinationTable.Name, from, to, counter));
                s2.close();
            }
            catch (SQLException ex) {
                String error = "ERROR processing table " + this.sourceTable.Name + ": " + ex + ". " + ex.getNextException();
                System.err.println(error);
                this.sender.tell(error, self());
                
                return 0;
            }
            catch (Exception ex) {
                String error = "ERROR processing table " + this.sourceTable.Name + ": " + ex;
                System.err.println(error);
                this.sender.tell(error, self());
                
                return 0;
            }
            
            return counter;
        }
    }
}
