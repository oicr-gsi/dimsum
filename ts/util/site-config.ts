import { Assay, MetricCategory } from "../data/assay";
import { CaseQc } from "../data/case";
import { assertRequired } from "../data/data-utils";

interface SiteConfig {
  misoUrl: string;
  dashiUrl: string;
  jiraUrl?: string;
  pendingStates: string[];
  completedGates: string[];
  analysisReviewQcStatuses: Record<string, CaseQc>;
  releaseApprovalQcStatuses: Record<string, CaseQc>;
  releaseQcStatuses: Record<string, CaseQc>;
  pipelines: string[];
  assaysById: Record<number, Assay>;
  libraryDesigns: string[];
  deliverables: string[];
}

export const siteConfig = (window as any).siteConfig as SiteConfig;

export const internalUser = (window as any).internalUser as boolean;

export function getAnalysisReviewQcStatus(name: string | null) {
  return getCaseQc(siteConfig.analysisReviewQcStatuses, name);
}

export function getReleaseApprovalQcStatus(name: string | null) {
  return getCaseQc(siteConfig.releaseApprovalQcStatuses, name);
}

export function getReleaseQcStatus(name: string | null) {
  return getCaseQc(siteConfig.releaseQcStatuses, name);
}

function getCaseQc(qcs: Record<string, CaseQc>, name: string | null) {
  if (name == null) {
    return null;
  } else if (!qcs[name]) {
    throw new Error(`Unknown case QC: ${name}`);
  } else {
    return qcs[name];
  }
}

export function getMetricCategory(assayId: number, category: MetricCategory) {
  const assay = siteConfig.assaysById[assayId];
  assertRequired(assay.metricCategories);
  return assay.metricCategories[category] || [];
}
