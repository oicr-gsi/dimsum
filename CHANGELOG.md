# Changelog

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

This file is updated automatically as described in [Unreleased Changes](changes/README.md).

---------------------------------------------------------------------------------------------------

## [1.8.1] - 2023-04-28

### Fixed

* Case QC Report was showing the wrong sign-offs for run-level categories
* Report generated date on Case QC Report was off by one day
* Autocomplete suggestions not appearing


## [1.8.0] - 2023-04-20

### Added

* Clicking a count on the Project List page or the Summary table of the Project Details page links to relevant filtered project details table
* case filter for library design, which applies to all tables in Project/Donor/Requisition/Case Detail pages' tabbed interface

### Changed

* Project Summary Counts (in project Details Page) with Filters now display Pending, Pending QC, and completed counts
* Allow "QC in MISO" with different assays/designs together in run details page

### Fixed

* data for the wrong donor could be included in Informatics Review metrics on the Case QC Report
* sorting and displaying cases when start date and/or latest activity cannot be determined


## [1.7.0] - 2023-04-06

### Added

* The summary table now match any filters applied to the other tables on the Project Details page and displays the counts of completed items at each gate accordingly
  - Project detail table with any Completed Gate filter will now show stopped cases as well for consistency with the counts table
* Case QC Report page


## [1.6.0] - 2023-03-30

### Added

* Project summary table on project detail page that displays counts of items at each QC gate that are pending, pending QC, completed


## [1.5.0] - 2023-03-22

### Added

* New case filters for all tables within Detail pages, which allow filtering by:
  - Test
  - Stopped case
  - Completed gate
* Support for EM-Seq assay metrics


## [1.4.0] - 2023-03-16

### Added

* Tests table now appears on the tabbed interface on Project, Donor, Requisition, and Case Details pages
* Projects list page

### Changed

* Turn-Around Time will not be displayed for stopped cases


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
  

