import { TableDefinition } from "../component/table-builder";
import { Tooltip } from "../component/tooltip";
import { makeNameDiv, makeTextDivWithTooltip } from "../util/html-utils";
import { siteConfig } from "../util/site-config";
import { urls } from "../util/urls";
import { Donor } from "./case";

interface OmittedSample {
  id: string;
  name: string;
  requisitionId?: number;
  requisitionName?: string;
  assayId?: number;
  project: string;
  donor: Donor;
  createdDate: string;
}

export const omittedSampleDefinition: TableDefinition<OmittedSample, void> = {
  queryUrl: urls.rest.omissions,
  defaultSort: {
    columnTitle: "Created Date",
    descending: true,
    type: "date",
  },
  filters: [
    {
      title: "Donor",
      key: "DONOR",
      type: "text",
      autocompleteUrl: urls.rest.autocomplete.donorNames,
    },
    {
      title: "Project",
      key: "PROJECT",
      type: "text",
      autocompleteUrl: urls.rest.autocomplete.projectNames,
    },
    {
      title: "Requisition",
      key: "REQUISITION",
      type: "text",
      autocompleteUrl: urls.rest.autocomplete.requisitionNames,
    },
  ],
  generateColumns(data) {
    return [
      {
        title: "Name",
        sortType: "text",
        addParentContents(sample, fragment) {
          fragment.appendChild(
            makeNameDiv(sample.name, urls.miso.sample(sample.id))
          );
        },
      },
      {
        title: "Project",
        sortType: "text",
        addParentContents(sample, fragment) {
          fragment.appendChild(
            makeNameDiv(
              sample.project,
              urls.miso.project(sample.project),
              urls.dimsum.project(sample.project)
            )
          );
        },
      },
      {
        title: "Donor",
        sortType: "text",
        addParentContents(sample, fragment) {
          fragment.appendChild(
            makeNameDiv(
              sample.donor.name,
              urls.miso.sample(sample.donor.id),
              urls.dimsum.donor(sample.donor.name)
            )
          );
          fragment.appendChild(
            makeTextDivWithTooltip(sample.donor.externalName, "External Name")
          );
        },
      },
      {
        title: "Requisition",
        sortType: "text",
        addParentContents(sample, fragment) {
          if (sample.requisitionId && sample.requisitionName) {
            fragment.appendChild(
              makeNameDiv(
                sample.requisitionName,
                urls.miso.requisition(sample.requisitionId)
              )
            );
            if (sample.assayId) {
              const assay = siteConfig.assaysById[sample.assayId];
              const assayDiv = document.createElement("div");
              assayDiv.innerText = assay.name;
              fragment.appendChild(assayDiv);
            }
          }
        },
      },
      {
        title: "Created Date",
        sortType: "date",
        addParentContents(sample, fragment) {
          fragment.appendChild(document.createTextNode(sample.createdDate));
        },
      },
    ];
  },
};
