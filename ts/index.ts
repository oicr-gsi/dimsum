import { caseDefinition } from "./data/case";
import { TableBuilder } from "./component/table-builder";

new TableBuilder(caseDefinition, "casesTableContainer").build();
