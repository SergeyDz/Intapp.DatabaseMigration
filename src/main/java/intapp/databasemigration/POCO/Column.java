/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package intapp.databasemigration.POCO;

/**
 *
 * @author sdzyuban
 */
public class Column {

    public Column(String name, String type, boolean isNullable) {
        this.Name = name;
        this.Type = type;
        this.IsNullable = isNullable;
    }

    public final String Name;

    public final String Type;
    
    public final boolean IsNullable;
    
    public boolean IsAutoFill;
}
