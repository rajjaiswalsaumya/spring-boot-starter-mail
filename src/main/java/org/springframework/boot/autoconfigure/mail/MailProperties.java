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

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * Configuration properties for email support.
 *
 * @author Phil Webb
 * @author Oliver Gierke
 * @author Stephane Nicoll
 * @author Rohit Gupta
 * @since 1.2.0
 */
@ConfigurationProperties(prefix = "spring.mail")
public class MailProperties {
    /**
     * SMTP server host.
     */
    private String host;
    /**
     * SMTP server port.
     */
    private Integer port;
    /**
     * Login user of the SMTP server.
     */
    private String username;
    /**
     * Login password of the SMTP server.
     */
    private String password;
    /**
     * Default from mail address
     *
     * @since 1.2.1
     */
    private String from;

    /**
     * Default MailTemplates Folder Path
     *
     * @since 1.2.1
     */
    private String prefix;
    /**
     * Default MimeMessage encoding.
     */
    private String defaultEncoding = "UTF-8";
    /**
     * Additional JavaMail session properties.
     */
    private Map<String, String> properties = new HashMap<String, String>();

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return this.port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getPrefix() {
        return prefix;
    }

    /* Added clean path logic */
    public void setPrefix(String prefix) {
        if (prefix.endsWith("/"))
            this.prefix = prefix.substring(0, prefix.length() - 1);
    }

    public String getDefaultEncoding() {
        return this.defaultEncoding;
    }

    public void setDefaultEncoding(String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

    @PostConstruct
    private void checkConfiguration() {
        if (StringUtils.isEmpty(host) || StringUtils.isEmpty(password) || StringUtils.isEmpty(username))
            throw new NullPointerException("Configuration Setting Incomplete, Please provide spring.mail.host,spring.mail.username,spring.mail.password in Configuration Properties");
    }
}