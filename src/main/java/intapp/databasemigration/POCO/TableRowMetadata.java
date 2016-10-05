/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package intapp.databasemigration.POCO;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author sdzyuban
 */
public class TableRowMetadata {

    public TableRowMetadata(ResultSet rs) throws SQLException {
        this.Schema = rs.getString("schema");
        this.Table = rs.getString("table");
        this.Column = rs.getString("column");
        this.Type = rs.getString("type");
    }

    public String Schema;

    public String Table;

    public String Column;

    public String Type;
}
