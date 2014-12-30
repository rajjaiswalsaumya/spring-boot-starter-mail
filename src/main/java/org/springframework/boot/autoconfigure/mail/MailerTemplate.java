package org.springframework.boot.autoconfigure.mail;

/*
* Copyright 2012-2014 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.StreamUtils;

/**
 * Mail Template class for email support and template engine.
 *
 * @author Phill Webb
 * @author Rohit Gupta
 * @since 1.2.1
 */

@Configuration
@ConditionalOnBean(value = JavaMailSender.class)
@AutoConfigureAfter(value = JavaMailSender.class)
public class MailerTemplate {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MailProperties mailProperties;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ResourceLoader resourceLoader;


    /**
     * This method is used to send plain text mail using the configured auto-configurations.
     * isSent is a typical flag field specifying if the mail successfully.
     *
     * @param to      Specifies receiver mailid
     * @param from    Specifies sender mailid
     * @param subject Specifies mail subject
     * @param body    Specifies mail body
     * @return isSent
     * @throws MessagingException
     */
    public boolean send(String to, String from, String subject,
                        String body) throws MessagingException {
        boolean isSent = false;
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper message = new MimeMessageHelper(mimeMessage, mailProperties.getDefaultEncoding());
        message.setValidateAddresses(true);
        message.setTo(to);
        message.setFrom(from);
        message.setSubject(subject);
        message.setText(body, true);
        message.setPriority(1);

        try {
            logger.debug("Sending Plain Mail with Java-Mail and SpringBoot-MailSenderAutoConfiguration");
            mailSender.send(mimeMessage);
            logger.debug("Mail sent to " + to);
            isSent = true;
        } catch (Exception e) {
            logger.error("Can't send mail, Root Cause : " + e.getLocalizedMessage(), e);
        } finally {
            message = null;
            mimeMessage = null;
        }
        return isSent;
    }

    public boolean send(Set<String> to, String from, String subject,
                        String body, StandardMailCopyOption mailCopyOption) throws MessagingException {
        boolean isSent = false;
        String[] recipients = to.toArray(new String[to.size()]);
        MimeMessage mimeMessage;
        MimeMessageHelper message;
        try {
            if (recipients.length == 0)
                throw new MessagingException("Can't send mail to empty recipients");

            mimeMessage = mailSender.createMimeMessage();
            message = new MimeMessageHelper(mimeMessage, mailProperties.getDefaultEncoding());


            if (mailCopyOption.equals(StandardMailCopyOption.TO))
                message.setTo(recipients);
            else if (mailCopyOption.equals(StandardMailCopyOption.BCC))
                message.setBcc(recipients);
            else
                message.setCc(recipients);

            message.setFrom(from);

            message.setSubject(subject);
            message.setText(body, true);
            message.setPriority(1);


            logger.debug("Sending Plain Mail with Java-Mail and SpringBoot-MailSenderAutoConfiguration");
            mailSender.send(mimeMessage);
            logger.debug("Mail sent!!");
            isSent = true;
        } catch (Exception e) {
            logger.error("Can't send mail, Root Cause : " + e.getLocalizedMessage(), e);
        } finally {
            recipients = null;
            message = null;
            mimeMessage = null;
        }

        return isSent;
    }

    /* Utility Functions*/

    /**
     * A utility function used to send mail with default from address added in Configuration Properties
     *
     * @param to      Specifies receiver mailid
     * @param subject Specifies mail subject
     * @param body    Specifies mail body
     * @return isSent
     * @throws MessagingException when default from address not set in Configuration Properties
     *                            or when application fails to send mail due to invalid configurations
     */
    public boolean send(String to, String subject,
                        String body) throws MessagingException {
        String from = mailProperties.getFrom();
        if (from == null)
            throw new MessagingException("Can't use this version of send when default from address is not set in MailProperties. Please add spring.mail.from to use default from address");
        return send(to, from, subject, body);
    }


    /**
     * A utility function to send bulk mail to given Set of EmailId's
     * Defaults to StandardMailCopyOption.TO setting mail recipients in ToAddress
     * When StandardMailCopyOption.CC is used instead, mail is sent to CC recipients
     * When StandardMailCopyOption.BCC is used instead, mail is sent to BCC recipients
     *
     * @param to      Specifies receiver mailid
     * @param from    Specifies sender mailid
     * @param subject Specifies mail subject
     * @param body    Specifies mail body
     * @return isSent
     * @throws MessagingException
     */
    public boolean send(Set<String> to, String from, String subject,
                        String body) throws MessagingException {
        return send(to, from, subject, body, StandardMailCopyOption.TO);
    }


    /**
     * Internal function to clean given path by user in Configuration Properties
     * and omit unwanted training /
     *
     * @param path Specifies Location of Templates
     * @return boolean Status stating isPath clean
     */
    private boolean hasProtocol(String path) {
        return (path.contains("file:") || path.contains("classpath:"));
    }

    public boolean sendWithTemplate(String to, String from, String subject,
                                    String templateSource, Map<String, String> contentMappingMap) throws MessagingException {
        String body = (hasProtocol(templateSource)) ?
                getReplacedText(templateSource, contentMappingMap, "pre").toString() : getReplacedText(mailProperties.getPrefix() + "/" + templateSource, contentMappingMap, "pre").toString();
        if (body.length() != 0) {
            return send(to, from, subject, body);
        } else {
            logger.info("Could not send mail, body-content length = 0 ");
            return false;
        }
    }

    public boolean sendWithTemplate(Set<String> to, String from, String subject,
                                    String templateSource, Map<String, String> contentMappingMap, StandardMailCopyOption mailCopyOption) throws MessagingException {
        String[] recipients = to.toArray(new String[to.size()]);
        boolean isSent = false;
        if (recipients.length == 0)
            throw new MessagingException("Can't send mail to empty recipients");
        String body = (hasProtocol(templateSource)) ?
                getReplacedText(templateSource, contentMappingMap, "pre").toString() : getReplacedText(mailProperties.getPrefix() + "/" + templateSource, contentMappingMap, "pre").toString();

        if (body.length() == 0)
            throw new MessagingException("Can't send empty mail to  recipients");

        MimeMessage mimeMessage;
        MimeMessageHelper message;
        try {
            mimeMessage = mailSender.createMimeMessage();
            message = new MimeMessageHelper(mimeMessage, mailProperties.getDefaultEncoding());


            if (mailCopyOption.equals(StandardMailCopyOption.TO))
                message.setTo(recipients);
            else if (mailCopyOption.equals(StandardMailCopyOption.BCC))
                message.setBcc(recipients);
            else
                message.setCc(recipients);

            message.setFrom(from);
            message.setSubject(subject);
            message.setText(body, true);
            message.setPriority(1);


            logger.debug("Sending Plain Mail with Java-Mail and SpringBoot-MailSenderAutoConfiguration");
            mailSender.send(mimeMessage);
            logger.debug("Mail sent!!");
            isSent = true;
        } catch (Exception e) {
            logger.error("Can't send mail, Root Cause : " + e.getLocalizedMessage(), e);
        } finally {
            recipients = null;
            message = null;
            mimeMessage = null;
        }
        return isSent;
    }


    /**
     * A utility function used to generate the Template based Mail Content
     *
     * @param templateSource    Source Template file, if contains protocol such as file:// or classpath:// uses path as template
     *                          or if no protocol specified, lookup takes place from spring.mail.prefix dir configurred in
     *                          Configuration Properties
     * @param contentMappingMap Map Containing Parameters and their values to be replaced in templates
     * @param escapeTags        Tags to be escaped from minification
     * @return replaced content StringBuilder
     */
    private StringBuilder getReplacedText(String templateSource, Map<String, String> contentMappingMap, String... escapeTags) {
        Resource resource = resourceLoader.getResource(templateSource);
        InputStream in;
        StringBuilder sb = new StringBuilder();
        try {
            in = resource.getInputStream();
            sb.append(StreamUtils.copyToString(in, Charset.forName(mailProperties.getDefaultEncoding())));
            for (String key : contentMappingMap.keySet()) {
                replaceAll(sb, key, contentMappingMap.get(key));
            }
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        //TODO  Minify Content Here
        return sb;
    }

    /**
     * A utility method used to replace all text within StringBuilder matching the from string
     *
     * @param builder StringBuilder initialized with template source
     * @param from    String to be replaced in String Builder
     * @param to      String replacement of from String
     */
    private void replaceAll(StringBuilder builder, String from, String to) {
        int index = builder.indexOf(from);
        while (index != -1) {
            builder.replace(index, index + from.length(), to);
            index += to.length(); // Move to the end of the replacement
            index = builder.indexOf(from, index);
        }
    }

    private void replaceAll(StringBuilder builder, int startIndex, int endIndex, String from, String to) {
        int index = builder.indexOf(from, startIndex);
        while (index < endIndex && index != -1) {
            builder.replace(index, index + from.length(), to);
            index += to.length(); // Move to the end of the replacement
            index = builder.indexOf(from, index);
        }
    }


    public enum StandardMailCopyOption {
        TO, CC, BCC
    }

}
