/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package intapp.databasemigration.Table;

import intapp.databasemigration.POCO.Column;
import intapp.databasemigration.POCO.Table;
import java.util.Optional;

/**
 *
 * @author sdzyuban
 */
public class TableFormatter {
    
    private final Table source; 
    
    private final Table destination;
    
    public TableFormatter(Table source, Table destination)
    {
        this.source = source;
        this.destination = destination;
    }
    
    public static TableFormatter Create(Table source, Table destination)
    {
        return new TableFormatter(source, destination);
    }
    
    
    public String MapName(String columnName)
    {     
        if("customfieldsxml".equals(columnName.toLowerCase()))
        {
            return "customfieldsjson";
        }
        
         if("default".equals(columnName.toLowerCase()))
        {
            return String.format("\"%s\"", "Default");
        }
        
        return columnName.toLowerCase();
    }
    
    public String MapValue(String columnName)
    {
        Optional<Column> columnA = source.Columns.stream().filter(m -> m.Name.toLowerCase().equals(columnName.toLowerCase())).findFirst();
        Optional<Column> columnB = destination.Columns.stream().filter(m -> MapName(columnName).equals(m.Name.toLowerCase())).findFirst();
        
        if(columnB.isPresent())
        {
            if(columnB.get().Type.equals("uuid"))
            {
                return "?::uuid";
            }
            else if(columnB.get().Type.equals("jsonb"))
            {
                return "to_json(?::json)";
            }
        }
        return "?";
    }
}
