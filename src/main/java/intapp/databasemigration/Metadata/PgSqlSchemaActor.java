/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package intapp.databasemigration.Metadata;

/**
 *
 * @author sdzyuban
 */
public class PgSqlSchemaActor extends SchemaActor {

    public PgSqlSchemaActor() {
        super();

        this.sql = "SELECT table_schema as \"schema\", table_name as \"table\", column_name as \"column\", data_type as \"type\""
                + " FROM information_schema.columns"
                + " WHERE table_schema = 'public'";
    }

}
