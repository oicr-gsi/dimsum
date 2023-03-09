# Changelog

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

This file is updated automatically as described in [Unreleased Changes](changes/README.md).

---------------------------------------------------------------------------------------------------

## [1.3.0] - 2023-03-09

### Added

* tooltip on "CASE STOPPED" text to display the stop reason
* ability to handle renamed metrics:
  * Total Clusters (Passed Filter)
  * Pipeline Filtered Clusters
  * Mean Bait Coverage

### Changed

* Cases now belong to a single requisition, and will include samples from other requisitions only
  when explicitly added as supplemental samples
* Case start date is now determined based on samples in the case's primary requisition, and excludes
  supplemental samples
* Items that have passed QC under an assay different from the case's assay are now indicated in
  Case views. The assay is also now displayed in the detailed tooltip

### Fixed

* Changed references to previous name, 'Dim Sum', to 'Dimsum'


## [1.2.0] - 2023-01-20

### Changed

* Help link in the header now links to the user manual


## [1.1.1] - 2023-01-04

### Fixed

* Metric columns that are n/a for all samples should be hidden


## [1.1.0] - 2022-12-15

### Added

* Omissions list page

### Fixed

* Filter options meant for other pages were appearing on the Run Details page


## [1.0.1] - 2022-12-08

### Fixed

* The Library Qualifications table on detail pages was showing the wrong items


## [1.0.0] - 2022-12-08

### Added

* Add tabbed interface for details page

### Changed

* Change to open external links in new tab
* Improved "pending" filters when applied to tables other than the Cases table
* Notification tickets' description to include instructions for permanently
  closing the ticket
* Notification ticket summary format for easier email filtering (RUN_NAME Dimsum
  Run QC)


## [0.1.0] - 2022-11-17

### Added

* Changelog
  

