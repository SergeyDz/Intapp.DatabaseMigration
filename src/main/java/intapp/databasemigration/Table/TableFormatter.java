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
    
    public static String MapName(String columnName, Table destination)
    {
        Optional<Column> destinationColumn = destination.Columns.stream().filter(m -> m.Name.toLowerCase().equals(columnName.toLowerCase())).findFirst();
        if("customfieldsxml".equals(columnName.toLowerCase()))
        {
            return "customfieldsjson";
        }
        
         if("default".equals(columnName.toLowerCase()))
        {
            return String.format("\"%s\"", destinationColumn.get().Name);
        }
        
        return columnName.toLowerCase();
    }
    
    public static String MapValue(String columnName, Table destination)
    {
        Optional<Column> destinationColumn = destination.Columns.stream().filter(m -> m.Name.toLowerCase().equals(MapName(columnName, destination))).findFirst();
        
        if(destinationColumn.isPresent())
        {
            if(destinationColumn.get().Type.equals("uuid"))
            {
                return "?::uuid";
            }
            else if(destinationColumn.get().Type.equals("jsonb"))
            {
                return "to_json(?::json)";
            }
        }
        return "?";
    }
}
