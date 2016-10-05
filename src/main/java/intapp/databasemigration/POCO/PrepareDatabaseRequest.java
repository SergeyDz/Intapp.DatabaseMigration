/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package intapp.databasemigration.POCO;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author sdzyuban
 */
public class PrepareDatabaseRequest {
    
    public PrepareDatabaseRequest(List<String> args)
    {
        this.Scripts = args;
    }
    
    public List<String> Scripts;
}
