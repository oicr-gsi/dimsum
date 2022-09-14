import { TableBuilder } from "./component/table-builder";
import { runDefinition } from "./data/run";

new TableBuilder(runDefinition, "runListTableContainer").build();
