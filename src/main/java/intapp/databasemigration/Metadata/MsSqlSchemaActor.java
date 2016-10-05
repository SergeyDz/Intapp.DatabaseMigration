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
public class MsSqlSchemaActor extends SchemaActor {

    public MsSqlSchemaActor() {
        super();

        this.sql = "SELECT t.TABLE_SCHEMA as 'schema', t.TABLE_NAME as 'table', c.COLUMN_NAME as 'column', c.DATA_TYPE as 'type'"
                + " FROM INFORMATION_SCHEMA.Tables t"
                + " inner join INFORMATION_SCHEMA.COLUMNS c on c.TABLE_NAME = t.TABLE_NAME"
                + " WHERE t.TABLE_TYPE = 'BASE TABLE' and t.TABLE_SCHEMA = 'dbo'";
    }

}
