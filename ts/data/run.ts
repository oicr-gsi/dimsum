import { legendAction, TableDefinition } from "../component/table-builder";
import { Tooltip } from "../component/tooltip";
import { makeIcon, makeNameDiv } from "../util/html-utils";
import { urls } from "../util/urls";
import { addStatusTooltipText, Qcable, Run } from "./case";
import { getQcStatusWithDataReview } from "./sample";

export interface OmittedRunSample extends Qcable {
  id: string;
  name: string;
  runId: number;
  sequencingLane: number;
}

export const runDefinition: TableDefinition<Run, void> = {
  queryUrl: urls.rest.runs.list,
  defaultSort: {
    columnTitle: "Completion Date",
    descending: true,
    type: "date",
  },
  staticActions: [legendAction],
  filters: [
    {
      title: "Name",
      key: "NAME",
      type: "text",
      autocompleteUrl: urls.rest.autocomplete.runNames,
      showExternal: false,
    },
  ],
  generateColumns(data) {
    return [
      {
        title: "Name",
        addParentContents(run, fragment) {
          fragment.appendChild(
            makeNameDiv(
              run.name,
              urls.miso.run(run.name),
              urls.dimsum.run(run.name),
              run.name
            )
          );
        },
        sortType: "text",
      },
      {
        title: "Completion Date",
        addParentContents(run, fragment) {
          if (run.completionDate) {
            fragment.appendChild(document.createTextNode(run.completionDate));
          }
        },
        sortType: "date",
      },
    ];
  },
};

export function getOmissionsDefinition(
  queryUrl: string
): TableDefinition<OmittedRunSample, void> {
  return {
    queryUrl: queryUrl,
    defaultSort: {
      columnTitle: "Name",
      descending: true,
      type: "text",
    },
    staticActions: [legendAction],
    generateColumns(data) {
      return [
        {
          title: "QC Status",
          sortType: "custom",
          addParentContents(sample, fragment) {
            const status = getQcStatusWithDataReview(sample);
            const icon = makeIcon(status.icon);
            const tooltipInstance = Tooltip.getInstance();
            tooltipInstance.addTarget(icon, (tooltip) => {
              addStatusTooltipText(
                tooltip,
                status,
                sample.qcReason,
                sample.qcUser,
                sample.qcNote
              );
            });
            fragment.appendChild(icon);
          },
          getCellHighlight(sample) {
            const status = getQcStatusWithDataReview(sample);
            return status.cellStatus || null;
          },
        },
        {
          title: "Name",
          sortType: "text",
          addParentContents(sample, fragment) {
            const text = `${sample.name} (L${sample.sequencingLane})`;
            fragment.appendChild(
              makeNameDiv(
                text,
                urls.miso.sample(sample.id),
                undefined,
                sample.name
              )
            );
          },
        },
      ];
    },
  };
}
