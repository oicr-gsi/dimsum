export interface Requisition {
  id: number;
  name: string;
  assayId?: number;
  stopped: boolean;
  paused: boolean;
  stopReason?: string;
  pauseReason?: string;
  latestActivityDate?: string;
}
