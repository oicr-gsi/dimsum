export interface Requisition {
  id: number;
  name: string;
  assayIds?: number[];
  stopped: boolean;
  paused: boolean;
  stopReason?: string;
  pauseReason?: string;
  latestActivityDate?: string;
}
