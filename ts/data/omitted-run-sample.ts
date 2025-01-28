import { showAlertDialog } from "../component/dialog";
import {
  ColumnDefinition,
  TableDefinition,
  legendAction,
} from "../component/table-builder";
import { Tooltip } from "../component/tooltip";
import { makeIcon, makeNameDiv } from "../util/html-utils";
import { urls } from "../util/urls";
import { addStatusTooltipText, Qcable } from "./case";
import { getQcStatusWithDataReview } from "./sample";

export interface OmittedRunSample extends Qcable {
  id: string;
  name: string;
  runId: number;
  runName: string;
  sequencingLane: number;
}

export function getOmittedRunSamplesDefinition(
  queryUrl: string,
  includeRun: boolean
): TableDefinition<OmittedRunSample, void> {
  const columns: ColumnDefinition<OmittedRunSample, void>[] = [
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
          makeNameDiv(text, urls.miso.sample(sample.id), undefined, sample.name)
        );
      },
    },
  ];
  if (includeRun) {
    columns.splice(1, 0, {
      title: "Run",
      addParentContents(sample, fragment) {
        fragment.appendChild(
          makeNameDiv(
            sample.runName,
            urls.miso.run(sample.runName),
            urls.dimsum.run(sample.runName),
            sample.runName
          )
        );
      },
    });
  }

  return {
    queryUrl: queryUrl,
    getDefaultSort: () => {
      return {
        columnTitle: "Name",
        descending: true,
        type: "text",
      };
    },
    staticActions: [legendAction],
    bulkActions: [
      {
        title: "Copy Names",
        handler: (items) => {
          const text = items.map((x) => x.name).join("\n");
          navigator.clipboard.writeText(text);
          showAlertDialog(
            "Copy Names",
            "Library aliquot names copied to clipboard"
          );
        },
      },
    ],
    generateColumns(data) {
      return columns;
    },
  };
}
