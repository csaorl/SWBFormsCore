/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.semanticwb.swbforms.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.semanticwb.datamanager.DataMgr;
import org.semanticwb.datamanager.DataObject;
import org.semanticwb.datamanager.SWBScriptEngine;
import org.semanticwb.datamanager.script.ScriptObject;

@WebListener
public class FormsServletContextListener implements ServletContextListener {

    static Logger logger = Logger.getLogger(FormsServletContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {        
        logger.info("Starting SWBForms");
        DataMgr.createInstance(sce.getServletContext());
        logger.info("SWBForms DataMgr Started");
        
        File fl=new File(DataMgr.getApplicationPath()+"/WEB-INF/classes/loggin.properties");
        if(fl.exists())
        {
            try
            {
                FileInputStream props=new FileInputStream(fl);
                LogManager.getLogManager().readConfiguration(props);
                logger=Logger.getLogger(FormsServletContextListener.class.getName());
                logger.info("Configuring logging.properties");
            }catch(Exception e)
            {
                logger.log(Level.WARNING,"Error processing logging.properties",e);
            }
        }else
        {
            logger.info("Not logging.properties found");            
        }

        SWBScriptEngine engine = DataMgr.getUserScriptEngine("/WEB-INF/global.js", (DataObject)null, false);
        //logger.info("SWBForms SWBScriptEngine Started");

        ScriptObject config = engine.getScriptObject().get("config");
        if (config != null) {
            String base = config.getString("baseDatasource");
            if (base != null) {
                DataMgr.getBaseInstance().setBaseDatasourse(base);
            }
            ScriptObject startup = config.get("startup");
            if(startup!=null)
            {
                DataObject st=startup.toDataObject();
                Iterator<String> it=st.keySet().iterator();
                while (it.hasNext()) {
                    String objname = it.next();
                    DataObject data=st.getDataObject(objname);
                    logger.log(Level.INFO,"Startup " + objname + "...");
                    String clsname = data.getString("class");
                    try
                    {
                        Class cls = Class.forName(clsname);      
                        Constructor c=cls.getConstructor(DataObject.class);
                        c.newInstance(data);
                    } catch (Exception e)
                    {
                        logger.log(Level.WARNING,"Startup load initialization error "+ objname ,e);
                    }                    
                }
            }            
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.log(Level.INFO,"Web Application Stoped");
    }
}
