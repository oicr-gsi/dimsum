# Changelog

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

This file is updated automatically as described in [Unreleased Changes](changes/README.md).

---------------------------------------------------------------------------------------------------

## [1.25.1] - 2024-02-16

### Fixed

* Some JIRA tickets were not being closed automatically after QC was completed


## [1.25.0] - 2024-01-29

### Added

* Range selection by allowing users to click a row, hold the Shift key, and click another row, thereby toggling the selection state of all rows in between
* Sort by QC status in Receipt, Extraction, Library Qualification, and Full-Depth Sequencing QC step tables

### Fixed

* The QC in MISO function would error and not respond if an assay contained a metric with a range threshold (between x and y)
* When using the QC in MISO function, a single assay was used to choose the metrics to relay to MISO. This means that MISO would display the wrong metrics for samples with a different assay


## [1.24.0] - 2024-01-18

### Changed

* Some tests may not have a library qualification step


## [1.23.0] - 2024-01-04

### Added

* Support for missing smMIPS assay metrics

### Changed

* Release approval and release steps are now required even for stopped cases

### Fixed

* Steps that were skipped due to the case being stopped could still be shown as overdue


## [1.22.1] - 2023-12-18

### Fixed

* Skipped (N/A) steps were displayed as "BEHIND SCHEDULE"


## [1.22.0] - 2023-12-14

### Added

* Handling for paused cases, including "CASE PAUSED" text in Assay column, and paused case filter
* MISO link next to assay names in Cases table and at the top of the Case QC Report

### Changed

* Improved display of case-level turn-around time, including days remaining


## [1.21.0] - 2023-11-30

### Added

* Incomplete filter to Case QC Report page
* Option to sort cases by urgency

### Changed

* Cases are now sorted by urgency by default


## [1.20.0] - 2023-11-23

### Added

* Indications of cases behind schedule and overdue (Start Date column)
* Days remaining (or "OVERDUE") for pending steps in Cases table

### Changed

* Replaced the default Spring "Whitelabel Error Page" with custom error page


## [1.19.0] - 2023-11-09

### Added

* Pipeline filter to Project list table filters

### Changed

* Metric values are now truncated instead of rounding for display. e.g. 12.999 will now be displayed
  as "12.99" instead of "13.00"

### Fixed

* Display of samples that have no metrics
* Updating notifications failed if any requisitions included supplemental samples that did not have
  their own fully defined assay


## [1.18.0] - 2023-10-05

### Changed

* Collapse lane-level values to reduce run metric visual clutter


## [1.17.0] - 2023-09-28

### Changed

* Action buttons now appear at both top and bottom of the table

### Fixed

* Removed QC report button from Requisition Details page


## [1.16.0] - 2023-09-20

### Changed

* Renamed steps:
  * Informatics Review -> Analysis Review
  * Draft Report -> Release Approval
  * Final Report -> Release


## [1.15.0] - 2023-09-14

### Added

* Copy button next to Donor name and External Donor name to copy the name to clipboard

### Changed

* The way cases are loaded; uses the case data from live Cardea API instead of QC-Gate-ETL files


## [1.14.0] - 2023-08-03

### Added

* QC notes on Case QC Report

### Fixed

* TGL Tracking Sheet was showing reversed coverage requirements for tumour vs. normal


## [1.13.0] - 2023-07-27

### Added

* Support for median insert size metric


## [1.12.0] - 2023-07-13

### Added

* Copy button next to run and requisition names to copy the name to clipboard

### Changed

* Updated case definition in user manual

### Fixed

* Broken links and images within user manual


## [1.11.0] - 2023-07-05

### Added

* TGL Tracking Sheet project downloads


## [1.10.1] - 2023-06-29

### Fixed

* Project list was failing to load and showing "NO DATA"


## [1.10.0] - 2023-06-28

### Added

* Preliminary values for full-depth WG coverage and WT clusters metrics. These (single lane total)
  values will be shown when available if the final (call ready) value is not yet available
* Page controls at the bottom of tables

### Changed

* Default page size for all tables to 50


## [1.9.2] - 2023-06-15

### Fixed

* yield showing not found instead of zero if sample volume or concentration is zero


## [1.9.1] - 2023-05-18

### Fixed

* Display of thresholds with min/max values of zero


## [1.9.0] - 2023-05-11

### Added

* A date range for the summary table on the Project Details page. This includes:
  - dropdown of common ranges (today, yesterday, this week, etc.)
  - custom ranges
  
  

### Fixed

* display of "Yield (Qubit)" metric
* tests that skip a gate should be counted as having completed that gate


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
  

