# Cases

A case consists of all of the work that is required to complete an assay for a set of samples.
Dimsum is based heavily upon this structure. Because an assay is required to construct a case, a
sample can only be included if the sample belongs to a requisition that specifies a well-defined
assay. Samples not meeting this criteria appear on the [Omissions Page](../omissions).

## Case Grouping

A case represents a unique combination of the following **tumour** sample attributes.

- Donor
- Tissue type
- Tissue origin
- Timepoint

All normals from the same donor will be included in all cases for that donor.

## Sample Grouping Criteria

Two samples are considered a match and can be used for the same test if they have the same

- Donor
- Tissue type
- Tissue origin
- Timepoint
- Group ID

The only additional field here is group ID, so if there are two group IDs for otherwise matching
samples, they will each be tested separately.

## Repeatable Tests

Assays can have multiple tests. For example, WGTS assays have 3 tests: Normal WG, Tumour WG, and Tumour WT. One attribute of a test is whether it is "repeatable"

If true:

- The test must be completed once for each sample grouping received
- A case will be created for each case grouping received
- The test will be repeated within the case for each sample grouping
- Typical for tumour tests

If false:

- The test only needs to be completed once for the donor
- The test will be included in every case for the donor
- Typical for normal tests
