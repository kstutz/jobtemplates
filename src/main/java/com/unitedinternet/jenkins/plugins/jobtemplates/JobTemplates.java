package com.unitedinternet.jenkins.plugins.jobtemplates;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

import org.acegisecurity.AccessDeniedException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.Failure;
import hudson.model.Item;
import hudson.model.RootAction;
import hudson.model.TopLevelItem;
import hudson.security.Permission;


/**
 *
 * @author Kathi Stutz
 */
@Extension
public class JobTemplates implements RootAction {

//    private static final Logger LOG = Logger.getLogger(JobTemplates.class.getName());

    /**
     * The hudson instance.
     */
    private final Hudson jenkins;

    /**
     * Set the {@link Hudson} instance.
     */
    public JobTemplates() {
        jenkins = Hudson.getInstance();
    }
    
    public final Hudson getJenkins(){
        return jenkins;
    }
    
    public final String getIconFileName() {
        return jenkins.hasPermission(AbstractProject.CREATE) ? 
            "/plugin/jobtemplates/icons/jobtemplates-32x32.png" : null;
    }

    public final String getDisplayName() {
        return Messages.displayName();
    }

    public final String getUrlName() {
        return "/jobtemplates";
    }

    public ArrayList<Item> getTemplates() {
        final ArrayList<Item> templateList = new ArrayList<Item>(); 
        
        final List<Item> getitems = Hudson.getInstance().getAllItems(Item.class);
        for (Item item : getitems) {
            if (item.getName().startsWith("Template_")) {
                templateList.add(item);
            }
        }
        return templateList;
    }
    
    public final void doCreateJob(StaplerRequest req, StaplerResponse rsp)
            throws IOException {

        try {
            jenkins.checkPermission(AbstractProject.CREATE);
        } catch (AccessDeniedException ex){
            writeMessage(req, rsp, ex.getMessage());
            return;
        }

        final String newName = req.getParameter("newname");
        final String templateName = req.getParameter("template");
        
        try {
            Jenkins.checkGoodName(newName);
        } catch (Failure f) {
            writeMessage(req, rsp, f.getMessage());
            return;
        }

        if (jenkins.getItem(newName) != null){
            writeMessage(req, rsp, Messages.jobExists());
            return;
        }
        
        if (templateName == null){
            writeMessage(req, rsp, Messages.chooseJob());
            return;
        }
        
        final Item template = jenkins.getItem(templateName);
        final Item newJob = jenkins.copy((TopLevelItem)template, newName);
        rsp.sendRedirect(jenkins.getRootUrl() + newJob.getUrl() + "configure");
    }
    
    private void writeMessage(StaplerRequest req, StaplerResponse rsp, String s)
            throws IOException{
        
        final Writer writer = rsp.getCompressedWriter(req);
        try {
            writer.append(s);
        } finally {
            writer.close();
        }
    }
}