/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package intapp.databasemigration.Table;

import intapp.databasemigration.POCO.TableCopyRequest;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import intapp.databasemigration.Metadata.MsSqlSchemaActor;
import intapp.databasemigration.Metadata.PgSqlSchemaActor;
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

    private Connection sourceConnection;
    private Connection destinationConnection;

    public TableCopyActor(ActorRef ms, ActorRef pg) {
        this.msConnectionActor = ms;
        this.pgConnectionActor = pg;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof TableCopyRequest) {
            TableCopyRequest request = (TableCopyRequest) message;
            this.sourceTable = request.source;
            this.destinationTable = request.destination;

            System.out.println("Starting copy data for Table: " + sourceTable.Name);

            msConnectionActor.tell("get", self());
        } else if (message instanceof Connection) {
            Connection connection = (Connection) message;
            if (connection.getMetaData().getURL().contains("sqlserver")) {
                sourceConnection = connection;
                pgConnectionActor.tell("get", self());
            } else {
                destinationConnection = connection;

                try (PreparedStatement s1 = sourceConnection.prepareStatement("select * from " + this.sourceTable.Name);
                        ResultSet rs = s1.executeQuery()) {
                    ResultSetMetaData meta = rs.getMetaData();

                    List<String> columns = new ArrayList<>();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        columns.add(meta.getColumnName(i).toLowerCase());
                    }

                    String sql = "insert into " + destinationTable.Name + " ("
                            + sourceTable.Columns.stream().map(c -> TableFormatter.MapName(c.Name, destinationTable)).collect(Collectors.joining(", "))
                            + ") values ("
                            + sourceTable.Columns.stream().map(c -> TableFormatter.MapValue(c.Name, destinationTable)).collect(Collectors.joining(", "))
                            + ")";

                    int counter = 0;

                    System.out.println(sql);
                    PreparedStatement s2 = null;
                    try {
                        s2 = destinationConnection.prepareStatement(sql);
                        while (rs.next()) {
                            for (int i = 1; i <= meta.getColumnCount(); i++) {
                                String columnName = meta.getColumnName(i);
                                if ("CustomFieldsXml".equals(columnName)) {

                                    s2.setObject(i, XML.toJSONObject(rs.getString(i)), java.sql.Types.VARCHAR);
                                } else {
                                    s2.setObject(i, rs.getObject(i));
                                }
                            }

                            s2.addBatch();
                            counter++;

                            if (counter % 1000 == 0) {
                                System.out.println(destinationTable.Name + ": " + counter + " items.");
                                System.out.println("insert into " + destinationTable.Name + ": " + counter + " batch pre-started...");
                                s2.executeBatch();
                                System.out.println("insert into " + destinationTable.Name + ": " + counter + " batch commited.");

                                s2 = destinationConnection.prepareStatement(sql);
                            }
                        }

                        System.out.println("insert into " + destinationTable.Name + ": " + counter + " batch started...");
                        s2.executeBatch();
                        System.out.println("insert into " + destinationTable.Name + ": " + counter + " batch completed.");
                        s2.close();
                    } catch (SQLException ex) {
                        System.err.println("ERROR processing table " + this.sourceTable.Name + ": " + ex + ". " + ex.getNextException());
                        s2.close();
                    } catch (Exception ex) {
                        System.err.println("ERROR processing table " + this.sourceTable.Name + ": " + ex);
                        s2.close();
                    }
                }
            }
        }

    }

}
