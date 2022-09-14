import { TableBuilder } from "./component/table-builder";
import { notificationDefinition } from "./data/notification";

new TableBuilder(notificationDefinition, "notificationsTableContainer").build();
