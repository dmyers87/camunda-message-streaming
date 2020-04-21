package com.ultimatesoftware.workflow.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("ultimate-software.workflow")
public class MessagingProperties {
    private final ParserProperties parser = new ParserProperties();

    public ParserProperties getParser() {
        return parser;
    }

    ///////////////////////////
    // NESTED CLASSES
    ///////////////////////////
    public static class ParserProperties {
        private String extensionPrefix = "ultimate.workflow";

        public String getExtensionPrefix() {
            return extensionPrefix;
        }

        public void setExtensionPrefix(String extensionPrefix) {
            this.extensionPrefix = extensionPrefix;
        }
    }
}
