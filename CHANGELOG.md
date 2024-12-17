# Changelog

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

This file is updated automatically as described in [Unreleased Changes](changes/README.md).

---------------------------------------------------------------------------------------------------

## [1.48.0] - 2024-12-17

### Added

* Donor Assay Report Download from the Cases table

### Changed

* Any non-pending QC status will now complete analysis review, release approval, and release steps
* Analysis review, release approval, and release now have QC statuses that indicate both QC passed and
  the release status


## [1.47.1] - 2024-12-06

### Fixed

* Items that have an assay but do not have any metrics should not be displayed on the Case QC Report


## [1.47.0] - 2024-12-05

### Added

* Checkbox to select/deselect all QC gates on TAT trend report


## [1.46.0] - 2024-11-21

### Added

* Deliverable filter to find cases for a specific deliverable

### Changed

* The Case QC Report previously showed the case's assay metrics for every item that was included on
  the report. When an item is from a different requisition that doesn't include the case's assay, the
  metrics that the item was QCed under will now be shown instead. A warning message is still displayed
  when this does not match the case's assay

### Fixed

* Items were not displayed on the Case QC Report if they didn't have metrics under the case's assay
  (mainly affects supplemental samples)
* Case QC Report sample tables were not being displayed


## [1.45.0] - 2024-11-15

### Added

* TAT trend report table metrics

### Fixed

* Creating/updating notifications in JIRA failed if there were too many items because the message was
  too long. Lists containing over 100 items will now be reduced to a summary of the library counts per
  project. If there are over 100 projects, then only a count of the libraries and projects will be
  included


## [1.44.0] - 2024-11-07

### Added

* Omissions table on the Run Details page, showing libraries that are included in the run but not a
  part of any case


## [1.43.0] - 2024-10-31

### Changed

* The DARE Input Sheet download now includes an option for whether to include supplemental samples
* The DARE Input Sheet download now includes a Lane column

### Fixed

* Date inputs submitting before entry is completed. Now requires pressing enter or clicking the submit
  (checkmark) button to submit, similar to text inputs


## [1.42.0] - 2024-09-30

### Changed

* Links to MISO have been updated


## [1.41.0] - 2024-09-19

### Added

* TAT Trend report legend for colour by gate

### Fixed

* TAT Trend Report color by gate feature to work correctly with time range grouping
* 'OVERDUE' being shown for completed sequencing gates if there was a failed run where the run-library
  did not have its (unnecessary) QC completed


## [1.40.1] - 2024-09-05

### Fixed

* Security patches


## [1.40.0] - 2024-08-15

### Added

* TAT trend report generated from the Cases list in Dimsum using the same filters
* Support for entering a list of values for text filters

### Changed

* The Case QC Report now displays "CASE STOPPED" along with the stop reason if the case is stopped,
  and correctly shows incomplete sign-offs as "N/A (case stopped)"


## [1.39.0] - 2024-08-06

### Changed

* Added note to Omissions page help that closed projects and pipeline "Sample Tracking only" are not
  included
* Failed run-libraries will now be excluded from the downloadable DARE Input Sheets

### Fixed

* DV200 library metric values were not being displayed


## [1.38.1] - 2024-07-04

### Fixed

* Display of Analysis Review pass/fail metrics


## [1.38.0] - 2024-06-27

### Added

* Details of turn-around time features to user manual

### Changed

* The Case QC Report's default filename when printing to PDF will always use the requisition name and
  never the group ID


## [1.37.0] - 2024-06-13

### Added

* Pending extraction transfer case filter
* Requirement for extraction sample (or aliquot) transfer to complete extraction step. A new icon is
  used to indicate samples pending transfer


## [1.36.0] - 2024-06-06

### Added

* Columns for days spent on loading, sequencing, and QC for library qualification and full-depth
  sequencing in the Case TAT Report
* Ability to link to requisitions by name: `/requisitions/by-name/<name>`


## [1.35.0] - 2024-05-30

### Added

* Display a count of the selected items in a table


## [1.34.0] - 2024-05-23

### Added

* case start and end date filters


## [1.33.0] - 2024-05-09

### Added

* Support for requisitions with multiple assays. A sample that is used for multiple assays may now
  show multiple requirements for the same metric

### Changed

* The format of case IDs has changed. When requesting a case page using an old case ID, an attempt is
  made to determine the updated case ID and redirect you to the intended page


## [1.32.0] - 2024-05-07

### Added

* FAQ section in User Manual
* QC status, user, and notes in tooltips for QC item and QC status tooltips
* Pending Release filter to find cases pending release for a specific deliverable
* Separate columns for clinical report, data release, and all deliverables combined on the Case TAT Report
* More project tracking details in User Manual
* Copy Name button for sample/library names

### Fixed

* Analysis review QC notes were missing from the Case QC Report
* Some columns on the Case TAT Report showed a completion date whether the step was passed (completed)
  or failed (not completed)


## [1.31.0] - 2024-04-18

### Added

* Project and library design filters for Library Qualification and Full-Depth Sequencing tables on the
  Run Details page


## [1.30.0] - 2024-04-11

### Added

* Case TAT report generated from the Cases list and uses the same filters that are applied to the list

### Changed

* Analysis review will be marked 'N/A' for cases where none of the deliverables require analysis review

### Fixed

* Library qualification and full-depth sequencing would be highlighted as incomplete if an item had
  failed run QC with run-library QC left incomplete. These should be considered completed (failed)


## [1.29.0] - 2024-03-28

### Added

* Format option for case downloads, allowing CSV and TSV formats in addition to Excel
* Full-Depth Summary Download from the Cases table
* DARE Input Sheet Download from the Cases table

### Changed

* Button text in first "Case QC" dialog from "Submit" to "Next" to clarify that there will be another step


## [1.28.1] - 2024-03-25

### Fixed

* Error when attempting to record sign offs


## [1.28.0] - 2024-03-14

### Added

* Dashi icon linking to relevant reports at the top of the Run Details and Project Details pages
* New metrics for cfMeDIPs assays

### Changed

* The "Passed" QC status will now be displayed as "Approved"
* Moved the Case Details link from the assay name to a clearer "Case Details" text node


## [1.27.1] - 2024-03-07

### Fixed

* The 'Pending: Release Approval' and 'Pending: Release' filters (without a deliverable type specified) were not including all of the appropriate cases
* The "Pending: Release Approval - Data Release" and "Pending: Release Approval - Clinical Report" filters were not including stopped cases


## [1.27.0] - 2024-03-01

### Added

* Dropdown on the Case QC Report to choose which deliverable type(s) to include in the report


## [1.26.0] - 2024-02-29

### Added

* Separate options for Data Release and Clinical Report in Pending, Completed, and Incomplete filters
* Ability to sign off cases for Analysis Review, Release Approval, and Release directly from Dimsum

### Changed

* Release sign-offs are now tied to a deliverable such as a clinical report or fastQs. Separate
  sign-offs are required for each deliverable that is configured for the project
* Analysis review and release approval sign-offs are now tied to a deliverable type - Clinical Report
  or Data Release. Separate sign-offs are required for each deliverable type that is configured for
  the project

### Fixed

* The pending work icon was not appearing when the previous step was "N/A"
* Some metrics with reported values of zero were wrongly displayed as "Not Found," and their highlighting was inconsistent
* Pending Release Approvals and Releases were not counted for stopped cases in the project summary


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
  

