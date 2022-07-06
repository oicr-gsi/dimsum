interface SiteConfig {
  misoUrl: string;
  dashiUrl: string;
}

export const siteConfig = (window as any).siteConfig as SiteConfig;
