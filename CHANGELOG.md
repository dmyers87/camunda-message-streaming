# Change log

A record of all major changes made to the library will be documented in this file.

## [0.6.0-SNAPSHOT] - 2020-09-22

### Added
- New fields processDefinitionId, deploymentId, and version added to extension data models
- Plugin to initialize process definition id values on post deployment, once the ids have been
initialized
- CHANGELOG.md file, correlation-logic-example.md

### Changed

- Generic Message Correlator
    - Refactored code to only attempt correlation on the latest version of a 
    process definition key + tenant id pairing (this was a non-issue prior to the new fields being added)
    

### Fixed
- Generic Message Correlator
    - Fixed issues where extension data was being applied across multiple BPMN versions,
    which lead to unintended results like duplicate correlations or parsing errors

