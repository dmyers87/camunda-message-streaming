Allows BPMN designers to declaratively define what values, from an incoming JSON based message, will be used to correlate to a Process or a Process Instance (e.g. Execution).  Message values are evaluated at runtime using JSON Path expressions which are stored within the BPMN.

The following Message Events are supported:
=====
#### Start events
![Start Event Message](docs/images/start-message.png "Start Event Message")

#### Intermediate Catch Events
![Intermediate Catch Message](docs/images/intermediate-catch.png "Intermediate Catch Message")

#### Interrupting & Non-interrupting Boundary Catch Events
![Interupting Boundary Message](docs/images/interupting-boundary.png "Interupting Boundary Message")

The following extension semantics are currently supported
====== 
Using the modeler you can assign extensions to the message catch
![Extension Sample](docs/images/extension-example.png "Extension Sample")
> Note: {prefix} defaults to `ultimate.workflow` but can be configured via the application properties

##### {prefix}.business-process-key={json-path}
Assigns the business process key that will be used to start a new process or match on a currently running process
> Note: This can also be declared as `{prefix}.match-var.business-process-key={json-path}`
Java equivalent:
```
processEngine.getRuntimeService()
    .createMessageCorrelation("com.acme.messages.important-event")
    .processInstanceBusinessKey("key")
```

##### {prefix}.match-var.{variable-name}={json-path}
Creates a match requirement on a variable whose name is defined by {variable-name}

Java equivalent:
```
processEngine.getRuntimeService()
    .createMessageCorrelation("com.acme.messages.important-event")
    .processVariableValueEquals("key", "value")
```

##### {prefix}.input-var.{variable-name}={json-path}
Sets a variable, whose name is defined by {variable-name}

Java equivalent:
```
processEngine.getRuntimeService()
    .createMessageCorrelation("com.acme.messages.important-event")
    .setVariable("key", "value")
```


Links
=====
* [Getting Started Guide](docs/GET_STARTED.md)
* [References](docs/REFERENCES.md)
