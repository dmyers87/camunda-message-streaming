package com.ultimatesoftware.workflow.messaging.bpmnparsing.exceptions;

public class ExtensionElementNotParsableException extends RuntimeException {

  public ExtensionElementNotParsableException(String message) {
    super("Extension Element Parsing Error: " + message);
  }
}
