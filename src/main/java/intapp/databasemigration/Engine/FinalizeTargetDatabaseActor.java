/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package intapp.databasemigration.Engine;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import intapp.databasemigration.POCO.PrepareDatabaseRequest;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author sdzyuban
 */
public class FinalizeTargetDatabaseActor extends PrepareTargetDatabaseActor {
    
    public FinalizeTargetDatabaseActor(ActorRef pg) {
        super(pg);
    }
    
    @Override
    protected String getResponse()
    {
       return "finalized";
    }
}
