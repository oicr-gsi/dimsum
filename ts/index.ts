import { caseDefinition } from "./data/case";
import { TableBuilder } from "./util/table-builder";

new TableBuilder(caseDefinition, "casesTableContainer").build();
