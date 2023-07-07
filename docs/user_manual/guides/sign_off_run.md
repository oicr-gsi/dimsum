# Complete Run Sign-Offs

Several sign-offs must be completed for each sequencing run. The run itself requires two sign-offs,
as does each library in the run. The first sign-off for each is completed by setting the QC status
in MISO, and the second sign-off is completed by setting the data review in MISO. Sign-offs are
based on metrics defined by the assay. Dimsum offers an easy way to review metrics, and links to
MISO where you can complete the sign-offs.

When any run or run-library sign-off is required in production, Dimsum will create a JIRA ticket.
You will usually get to the [Run Details](../details.md) page in Dimsum by clicking the
link in this ticket, but you can also locate the run using the Runs or Notifications list pages, as
in this example.

1. Go to the [Notifications](../../notifications/) list page and locate the run you'd
   like to review. Note that the sign-offs required for each run are displayed in the table. Click
   the run name to get to the [Run Details](../details.md) page.

      - if the run does not show up in the Active Notifications list, it either does not have any
        pending sign-offs, or metrics are not yet available because QC analysis has not completed

2. Depending on the type of run, a Library Qualifications and/or Full-Depth Sequencing table appears
   on the [Run Details](../details.md) page. Here, you can see all of the run and
   run-library metrics for each library in the run. Review the table and ensure that metrics for the
   libraries in question look good. Metrics that do not pass the threshold defined in the assay will
   be shaded red, while metrics that are unavailable will be shaded yellow.

3. For run-level sign-offs, click the MISO icon next to the title at the top of the page. This
   brings you to the
   [Edit Run](https://miso-lims.readthedocs.io/projects/docs/en/latest/user_manual/sequencer_runs/#editing-a-run)
   page in MISO, where you can set the QC status or data review for the run.

4. For run-library sign-offs, go back to Dimsum and select the libraries you wish to review by
   checking their checkboxes at the left of the table rows. Scroll to the bottom of the table and
   click the "QC in MISO" button. This takes you to the
   [Run-Library Metrics](https://miso-lims.readthedocs.io/projects/docs/en/latest/user_manual/qc_integration/)
   page in MISO, which displays the run-library-level metrics again, and allows you to set the
   status or data review for each run-library.

      - libraries must all have the same assay and library design to "QC in MISO" together. If there
        are multiple assays and/or library designs involved, you will have to QC them in groups
