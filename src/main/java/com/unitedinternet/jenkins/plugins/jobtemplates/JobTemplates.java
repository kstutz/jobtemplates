package com.unitedinternet.jenkins.plugins.jobtemplates;

import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.Extension;
import hudson.model.Hudson;
import hudson.model.Failure;
import hudson.model.Item;
import hudson.model.RootAction;
import hudson.model.TopLevelItem;
import hudson.util.FormValidation;


/**
 *
 * @author Kathi Stutz
 */
@Extension
public class JobTemplates implements RootAction {

    private static final Logger LOG = Logger.getLogger(JobTemplates.class.getName());

    public final String getIconFileName() {
        return "/plugin/jobtemplates/icons/jobtemplates-32x32.png";
    }

    public final String getDisplayName() {
        return "New Job from Template";
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
        
        final Hudson jenkins = Hudson.getInstance();
        final String newName = req.getParameter("newname");
        final String templateName = req.getParameter("template");
        
        try {
            Jenkins.checkGoodName(newName);
        } catch (Failure f) {
            writeMessage(req, rsp, f.getMessage());
            return;
        }

        if (jenkins.getItem(newName) != null){
            writeMessage(req, rsp, "A job with this name already exists.");
            return;
        }
        
        if (templateName == null){
            writeMessage(req, rsp, "Please choose a job to copy.");
            return;
        }
        
        final Item template = jenkins.getItem(templateName);
        final Item newJob = Hudson.getInstance().copy((TopLevelItem)template, newName);
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