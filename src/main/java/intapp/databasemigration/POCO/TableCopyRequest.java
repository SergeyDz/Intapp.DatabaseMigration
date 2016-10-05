/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package intapp.databasemigration.POCO;

import intapp.databasemigration.POCO.Table;
import java.util.List;

/**
 *
 * @author sdzyuban
 */
public class TableCopyRequest {

    public final Table source;

    public final Table destination;

    public TableCopyRequest(Table source, Table destination) {
        this.source = source;
        this.destination = destination;
    }

}
