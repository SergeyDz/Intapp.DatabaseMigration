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
public class SchemaResult {
    
    public SchemaResult(List<Table> results)
    {
        this.Tables = results;
    }
    
    public final List<Table> Tables;  
}
