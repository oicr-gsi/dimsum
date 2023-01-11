# QC Dashboard

This is the main dashboard users are presented with. All cases are presented in a table format. See
[Cases](/user_manual/cases/) for details about how cases are constructed. Navigate back to this page
by clicking on the Dimsum logo on the top left of all pages.

![QC Dashboard](/images/qc_dashboard.png)

The table columns are described below.

### Project

Lists the project(s) involved in the case, and their appropriate pipeline. Multiple projects will be
involved in cases where relevant samples from the case's donor have been propagated to a different
project.

Clicking on a project name will take you to the [Project Details](/user_manual/details/) page.
Clicking on the MISO icon next to the project name will open the MISO
[Edit Project](https://miso-lims.readthedocs.io/projects/docs/en/latest/user_manual/projects/#edit-project-page)
page in a new tab.

### Donor

Lists the case donor's internal and external names, and the tumour tissue origin, tissue type, and
timepoint examined in the case.

Clicking on the donor name will take you to the [Donor Details](/user_manual/details/) page.
Clicking on the MISO icon next to the donor name will open the MISO
[Edit Sample](https://miso-lims.readthedocs.io/projects/docs/en/latest/user_manual/samples/#editing-a-single-sample)
page for the donor in a new tab.

### Assay

Lists the case assay and requisition.

Clicking on the assay name will take you to the [Case Details](/user_manual/details/) page.

Clicking on the requisition name will take you to the [Requisition Details](/user_manual/details/)
page. Clicking on the MISO icon next to the requisition name will open the MISO
[Edit Requisition](https://miso-lims.readthedocs.io/projects/docs/en/latest/user_manual/requisitions/#editing-requisitions)
page in a new tab.

### Start Date

Displays the case start date and total turn-around time. Start date is the earliest receipt date of
the tumour sample(s) involved in the case. For ongoing cases, the turn-around time is the number of
days from the start date until the current date. For completed cases, the turn-around time is the
number of days from the start date until the Final Report sign-off.

### Test

Each assay includes one or more tests. Tests must be repeated for each group ID present. See
[Cases](/user_manual/cases/) for more details about how cases are constructed.

### QC Gates

The following are QC gate columns, and their associated items:

| Gate/Column           | Item type                                                 |
| --------------------- | --------------------------------------------------------- |
| Receipt/Inspection    | Requisitioned samples                                     |
| Extraction            | Stock samples                                             |
| Library Preparation   | Libraries                                                 |
| Library Qualification | Library aliquots or run-libraries, depending on the assay |
| Full-Depth Sequencing | Run-libraries                                             |
| Informatics Review    | Requisition-level QC                                      |
| Draft Report          | Requisition-level QC                                      |
| Final Report          | Requisition-level QC                                      |

Each QC gate cell includes icons representing the QC status of each item relevant to that gate. A
checkmark represents an item that has passed QC, while an 'X' represents a QC failure. Click the
Legend button at the bottom of the table for a full list of statuses and their icons. A single
passing item is required for the gate to be considered complete. Hover over an icon to view a
tooltip containing item details and relevant links.

A QC gate cell is shaded yellow if the gate has not been completed.

A QC gate cell is shaded grey if it is not applicable for the test. For example, if stock samples
were received, extraction is not necessary.

### Latest Activity

Displays the latest date that an item involved in the case was created, modified, or signed-off.
