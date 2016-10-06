/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package intapp.databasemigration.Metadata;

import intapp.databasemigration.POCO.TableRowMetadata;
import intapp.databasemigration.POCO.SchemaResult;
import akka.actor.UntypedActor;
import intapp.databasemigration.POCO.Column;
import intapp.databasemigration.POCO.Table;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author sdzyuban
 */
public class SchemaActor extends UntypedActor {

    private Connection connection;

    private final List<Table> tables;

    protected String sql;

    public SchemaActor() {
        this.tables = new ArrayList<>();
    }

    @Override
    public void onReceive(Object message) throws Exception {

        this.connection = (Connection) message;

        List<TableRowMetadata> rows = new ArrayList<>();

        try (PreparedStatement s1 = connection.prepareStatement(sql);
                ResultSet rs = s1.executeQuery()) {
            while (rs.next()) {
                rows.add(new TableRowMetadata(rs));
            }
        } catch (Exception ex) {
            System.err.println(ex);
        }

        System.out.println("Database schema scan completed. Found " + rows.size() + " found");

        Map<String, List<TableRowMetadata>> groupedMetadata = rows.stream().collect(Collectors.groupingBy(w -> w.Table));

        groupedMetadata.forEach((key, values) -> {
            Table t = new Table(values.get(0).Schema, key);
            values.forEach(c -> {
                t.Columns.add(new Column(c.Column, c.Type, c.IsNullable));
            });

            tables.add(t);
        });

        sender().tell(new SchemaResult(tables), self());
    }

}
