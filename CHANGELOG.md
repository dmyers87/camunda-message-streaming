# Change log

A record of all major changes made to the library will be documented in this file.

## [0.6.5-SNAPSHOT] - 2021-10-20

### Added
- New method in MySqlMapper for deleting all extension data for a single deployment
- Test class for MySqlMapper

### Changed
- MySqlMapper class extension repo is now a constructor parameter rather than an autowired field

## [0.6.4-SNAPSHOT] - 2021-10-15

### Fixed
- MySqlMapper initialize process definition ids on extension data
    - Fixed an issue where the method could be called during correlation (due to caching deployments)
    by skipping over extension data where process definition was already set

## [0.6.3-SNAPSHOT] - 2021-10-14

### Added
- Additional logs during the initialization of process definition ids for extension data

## [0.6.2-SNAPSHOT] - 2021-09-23

### Removed
- Unnecessary logging on CorrelationMessageListener class

## [0.6.1-SNAPSHOT] - 2021-09-23

### Fixed
- Revert of additionally removed methods in 0.6.0-SNAPSHOT release

## [0.6.0-SNAPSHOT] - 2021-09-22

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

