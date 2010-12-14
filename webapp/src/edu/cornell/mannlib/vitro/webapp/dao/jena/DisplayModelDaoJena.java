/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.channels.FileChannel;

import javax.servlet.ServletContext;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

import edu.cornell.mannlib.vitro.webapp.dao.DisplayModelDao;

public class DisplayModelDaoJena implements DisplayModelDao {
    WebappDaoFactoryJena wdf;
    
    protected static final String MENU_N3_FILE = "/WEB-INF/ontologies/app/menu.n3";
    protected static final String MENU_N3_FILE_BACKUP = "/WEB-INF/ontologies/app/menu.backup";
    
    public DisplayModelDaoJena(WebappDaoFactoryJena wdfj){
        this.wdf = wdfj;
    }    
    
    public void replaceDisplayModel(String n3, ServletContext context) throws Exception{
        OntModel displayModel = wdf.getOntModelSelector().getDisplayModel();
                
        //get old menu file and turn into model
        Model oldMenuStmts = ModelFactory.createDefaultModel();
        InputStream oldIn = FileManager.get().open( context.getRealPath(MENU_N3_FILE ) );
        if( oldIn == null ){
            throw new Exception("Cannot open existing menu file.");
        }
        try{
            oldMenuStmts.read(oldIn, null, "N3");
        }catch(Throwable th){
            throw new Exception("Cannot read in existing menu file.");
        }
        
        //get menu file and turn into model        
        Model newMenuStmts = ModelFactory.createDefaultModel();
        StringReader newIn = new StringReader( n3 );
        try{
            newMenuStmts.read(newIn, null,"N3");
        }catch(Throwable th){
            throw new Exception("There was an error in the menu N3: "+ th.getMessage());
        }
        
        displayModel.enterCriticalSection(true);
        try{
            //copy old menu file to backup
            File oldMenuFile = new File(context.getRealPath(MENU_N3_FILE));
            File oldMenuFileBackup = new File(context.getRealPath(MENU_N3_FILE_BACKUP));
            copyFile(oldMenuFile , oldMenuFileBackup);
            
            //save new menu file to old menu file
            File newMenuFile = new File(context.getRealPath(MENU_N3_FILE));
            FileWriter mfWriter = new FileWriter(newMenuFile);
            mfWriter.write(n3);
            mfWriter.close();
            
            //remove old menu statements from display model
            displayModel.remove(oldMenuStmts);
            
            //add new menu statements to display model
            displayModel.add(newMenuStmts);
        }finally{
            displayModel.leaveCriticalSection();
        }        
    }
    
    
    public String getDisplayModel(ServletContext context) throws IOException{        
        File oldMenuFile = new File(context.getRealPath(MENU_N3_FILE));
        StringBuffer str = new StringBuffer(2000);
        BufferedReader reader = new BufferedReader(new FileReader(oldMenuFile));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            str.append(readData);
        }
        reader.close();
        return str.toString();    
    }
    
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if(!destFile.exists()) {
         destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;
        try {
         source = new FileInputStream(sourceFile).getChannel();
         destination = new FileOutputStream(destFile).getChannel();
         destination.transferFrom(source, 0, source.size());
        }
        finally {
         if(source != null) {
          source.close();
         }
         if(destination != null) {
          destination.close();
         }
       }
    }


}
