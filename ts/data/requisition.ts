export interface Requisition {
  id: number;
  name: string;
  assayIds: number[];
  stopped: boolean;
  paused: boolean;
  stopReason: string | null;
  pauseReason: string | null;
}
