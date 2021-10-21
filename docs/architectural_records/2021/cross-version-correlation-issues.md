## Preventing cross version correlation issues

### Problem

Prior to the 0.6.0-SNAPSHOT release, there were issues with running multiple versions of a BPMN on
top of each other using slightly different configurations for Extension Data. 

For example: One Receive task might be using one matching variable `foo` in deployed version 1. 
In version 2, one might decide to use a matching variable `foo2` instead. 

When it received the message from a message broker that is associated with that BPMN, it will try to
match both sets of extension data against the event and try to resolve it against anything with the
same process definition key.

This broke the versioning expected by one using Camunda and caused major issues.

### Implemented Solution

In order to solve the problem, new fields were added to the Extension Data to help only apply Extension
data elements against the Process Definition in which they were defined. 

The following three fields were added:
- Process Definition Id
- Version
- Deployment Id

Using Process Definition Id, we could match against the specific Process Definition.

Version was added to help with choosing the correct start event when correlating messages to start
events. It was decided that by default, the library will choose the latest version of a Process Definition
deployed for that Process Definition key when looking to correlate.

Deployment Id was added as a way of helping to resolve the Process Definition Ids once the deploy has
been completed. This was needed due to the Process Definition Ids not being available when the Extension
data elements were being parsed.

A plugin was added for the Process Engine in order to set the Process Definition Ids fields after
deployment has been completed.

### Additional considerations

Note: It was later found that the setting of Process Definition Ids method could be call multiple
times, even during correlation (since Camunda can cache partial deployments). Conditional logic
was added later in 0.6.3-SNAPSHOT to fix this.