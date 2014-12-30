package org.springframework.boot.autoconfigure.mail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import javax.mail.MessagingException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Phill Webb
 * @author Rohit Gupta
 * @since 1.2.1
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class MailerTemplateTest {

    @Autowired
    MailerTemplate mailerTemplate;

    @Autowired
    MailProperties mailProperties;

    @Before
    public void setUp() throws Exception {
        Assert.notNull(mailProperties, "Application Context does not contain configuration for mails");
        Assert.notNull(mailerTemplate, "Application Context does not contain mailer template");
    }

    @Test
    public void testConfiguredProperties() {
        Assert.notNull(mailProperties.getHost(), "Host can't be null, Please set spring.mail.host in Configuration");
    }


    @Test(expected = MessagingException.class)
    public void testSend() throws Exception {
        Assert.isTrue(mailerTemplate.send("grohit@cdac.in", "Test Mail", "Test Body"));
    }

    @Test
    public void testSendAfterSettingFrom() throws Exception {
        mailProperties.setFrom("grohit@cdac.in");
        Assert.isTrue(mailerTemplate.send("grohit@cdac.in", "Test Mail", "Test Body"));
    }

    @Test
    public void testSendCheckCC() throws Exception {
        mailProperties.setFrom("grohit@cdac.in");
        Set<String> recipients = new HashSet<>();
        recipients.add("gkapil@cdac.in");
        recipients.add("sgalande@cdac.in");
        recipients.add("grohit@cdac.in");
        Assert.isTrue(mailerTemplate.send(recipients, mailProperties.getFrom(), "Test Mail", "Test Body", MailerTemplate.StandardMailCopyOption.CC));

    }

    @Test
    public void testSendWithTemplate() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("${headContent}", "Sending mail with template");
        map.put("${sectionContent}", "This is a sample mail with spring-boot");
        map.put("${footContent}", "Copyright 2014");


        mailerTemplate.sendWithTemplate("errohitmailbox@gmail.com", "grohit@cdac.in", "subject", "default-mail-template.html", map);
    }
}