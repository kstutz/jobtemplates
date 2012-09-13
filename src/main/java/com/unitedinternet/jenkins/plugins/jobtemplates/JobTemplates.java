package com.unitedinternet.jenkins.plugins.jobtemplates;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.Extension;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.RootAction;
import hudson.model.TopLevelItem;




/**
 *
 * @author Kathi Stutz
 */
@Extension
public class JobTemplates implements RootAction {

    /**Meet our logger.*/
    private static final Logger LOGGER = Logger.getLogger(JobTemplates.class.getName());

    /**
     * {@inheritDoc}
     */
    public final String getIconFileName() {
        return "/plugin/jobtemplates/icons/jobtemplates-32x32.png";
    }

    /**
     * {@inheritDoc}
     */
    public final String getDisplayName() {
        return "JobTemplates";
    }

    /**
     * {@inheritDoc}
     */
    public final String getUrlName() {
        return "/jobtemplates";
    }

    public ArrayList<Item> getTemplates() {
        final ArrayList<Item> templateList = new ArrayList<Item>(); 
        
        //ersetzen durch getAllJobNames (Collection!)
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
        
        final Hudson jenkins = Hudson.getInstance();
        final String newName = req.getParameter("newname");
        final String templateName = req.getParameter("template");
        
        final Item template = jenkins.getItem(templateName);
        final Item newJob = Hudson.getInstance().copy((TopLevelItem)template, newName);
        
        rsp.sendRedirect(jenkins.getRootUrl() + newJob.getUrl() + "configure");
    }
}
