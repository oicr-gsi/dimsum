import { Assay } from "../data/assay";

interface SiteConfig {
  misoUrl: string;
  dashiUrl: string;
  jiraUrl?: string;
  pendingStates: string[];
  pipelines: string[];
  assaysById: Record<number, Assay>;
  libraryDesigns: string[];
}

export const siteConfig = (window as any).siteConfig as SiteConfig;
