interface SiteConfig {
  misoUrl: string;
  dashiUrl: string;
  pendingStates: string[];
  pipelines: string[];
}

export const siteConfig = (window as any).siteConfig as SiteConfig;
