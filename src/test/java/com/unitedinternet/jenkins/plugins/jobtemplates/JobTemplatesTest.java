package com.unitedinternet.jenkins.plugins.jobtemplates;

import java.io.IOException;

import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.security.HudsonPrivateSecurityRealm;
import hudson.security.LegacyAuthorizationStrategy;

import org.jvnet.hudson.test.HudsonTestCase;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class JobTemplatesTest extends HudsonTestCase {

    public void testWithTwoTemplates() throws Exception {
        final String templateName1 = "Template_1";
        final String templateName2 = "Template_2";
        final String projectName = "Test1";
        final String description = "That's no moon.";

        createFreeStyleProject(templateName1);
        final FreeStyleProject project2 = createFreeStyleProject(templateName2);
        project2.setDescription(description);
        
        final WebClient webClient = createWebClient();
        final HtmlPage page = webClient.goTo("jobtemplates");
        WebAssert.assertTextPresent(page, templateName1);
        WebAssert.assertTextPresent(page, templateName2);
        
        final HtmlForm form = page.getFormByName("jobtemplates");
        form.getInputByName("newName").setValueAttribute(projectName);
        form.getInputsByName("template").get(1).setChecked(true);
        final HtmlPage configPage = submit(form);
        
        assertTrue("Form submit should lead to config page.", configPage.getTitleText().contains("Config"));
        assertTrue("New project should exist", !hudson.getItem(projectName).equals(null));
        assertEquals("New project should have new description", ((AbstractProject) hudson.getItem(projectName)).getDescription(), description);
    }
    
    public void testWithMissingInput() throws Exception {
        final WebClient webClient = createWebClient();
        final HtmlPage page = webClient.goTo("jobtemplates");
        final HtmlForm form = page.getFormByName("jobtemplates");
        HtmlPage errorPage = submit(form);
        WebAssert.assertTextPresent(errorPage, "No name");
    
        form.getInputByName("newName").setValueAttribute("bla");
        errorPage = submit(form);
        WebAssert.assertTextPresent(errorPage, Messages.chooseJob());
    }
    
    public void testWithDuplicateProjectName() throws Exception {
        final String templateName = "Template_1";
        final String projectName = "Test1";

        createFreeStyleProject(projectName);
        createFreeStyleProject(templateName);

        final WebClient webClient = createWebClient();
        final HtmlPage page = webClient.goTo("jobtemplates");
        WebAssert.assertTextPresent(page, templateName);
        
        final HtmlForm form = page.getFormByName("jobtemplates");
        form.getInputByName("newName").setValueAttribute(projectName);
        form.getInputByName("template").setChecked(true);
        final HtmlPage errorPage = submit(form);
        
        WebAssert.assertTextPresent(errorPage, Messages.jobExists());
    }
    
    public void testWithoutPermission() throws IOException, SAXException {
        final WebClient webClient = createWebClient();
        final HtmlPage withoutSecurity = webClient.goTo("/");
        assertTrue("Without any security the link should be there.", withoutSecurity.asText().contains(Messages.displayName()));
       
        hudson.setSecurityRealm(new HudsonPrivateSecurityRealm(false, false, null));
        hudson.setAuthorizationStrategy(new LegacyAuthorizationStrategy());
        final HtmlPage withSecurityEnabled = webClient.goTo("/");
        assertFalse("With security the link should not be there.", withSecurityEnabled.asText().contains(Messages.displayName()));
    }
}
