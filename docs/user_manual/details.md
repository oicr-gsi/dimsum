# Detail Pages

Projects, donors, requisitions, cases, and runs each have their own Details page. The Details page
includes more information about the selected item, all of its cases and QC gates, and the metrics
for each associated item. Clicking on the name of a project, donor, requisition, or run redirects
you to the corresponding details page. Clicking on an assay name in the cases table links to the
Case Details page.

### Metrics

Columns containing QC metrics provide tool tips detailing the exact required threshold. Hover over
the recorded nummber to see its requirements. The cell will be shaded red if the requirement is not
met, and yellow if the value is unavailable.

![Metric cell](/images/metric_cell.png)

## 1. Project, Donor, Case, and Requisition Details Page

The Details page includes tables detailing the following:

- Cases
- Receipts
- Extractions
- Library Preparations
- Library Qualifications
- Full Depth Sequencings
- Informatics Review
- Draft Report
- Final Report

Navigate through each of the tables using the tabbed bar at the top of the page.

![Detail page tabs](/images/detail_tabs.png)

## 2. Run Details Page

This page includes details about the selected run including its QC status, flow cell, and
parameters. A table showing all of the library qualification and/or full-depth sequencing results is
included below, as applicable for the selected run.

![Run Details page](/images/run_details.png)

### Bulk QC Operations in MISO

To perform bulk QC operations in MISO, select the desired table entries by clicking the checkbox at
the left of each item you wish to include. Next, scroll to the bottom of the table and click the
"QC in MISO". This will open the MISO Run-Library Metrics page in a new tab. You can then perform
your desired QC operations for all the selected items.

**NOTE:** Only items with the same assay and library design may be selected in bulk to view in MISO. Entries selected that do not meet this requirement (ie. have different assays and/or library designs) will induce an ERROR popup after pressing the "QC in MISO" button.

---

For more information on common page and table features, see [Common Features](../features/).
