import { Assay } from "../data/assay";

interface SiteConfig {
  misoUrl: string;
  dashiUrl: string;
  pendingStates: string[];
  pipelines: string[];
  assaysById: Record<number, Assay>;
}

export const siteConfig = (window as any).siteConfig as SiteConfig;
