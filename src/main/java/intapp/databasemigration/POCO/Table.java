/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package intapp.databasemigration.POCO;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sdzyuban
 */
public class Table {

    public Table() {
        this.Columns = new ArrayList<>();
    }

    public Table(String schema, String name) {
        this();
        this.Name = name;
        this.Schema = schema;
    }

    public String Name;

    public String Schema;

    public List<Column> Columns;
}
