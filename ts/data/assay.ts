export type MetricCategory =
  | "RECEIPT"
  | "EXTRACTION"
  | "LIBRARY_PREP"
  | "LIBRARY_QUALIFICATION"
  | "FULL_DEPTH_SEQUENCING"
  | "ANALYSIS_REVIEW";

export interface Metric {
  name: string;
  sortPriority: number | null;
  minimum: number | null;
  maximum: number | null;
  units: string | null;
  tissueMaterial: string | null;
  tissueOrigin: string | null;
  tissueType: string | null;
  negateTissueType: boolean | null;
  nucleicAcidType: string | null;
  containerModel: string | null;
  readLength: number | null;
  readLength2: number | null;
  thresholdType: string;
}

export interface MetricSubcategory {
  name?: string;
  sortPriority?: number;
  libraryDesignCode?: string;
  metrics: Metric[];
}

export interface AssayTargets {
  caseDays: number | null;
  receiptDays: number | null;
  extractionDays: number | null;
  libraryPreparationDays: number | null;
  libraryQualificationDays: number | null;
  fullDepthSequencingDays: number | null;
  analysisReviewDays: number | null;
  releaseApprovalDays: number | null;
  releaseDays: number | null;
}

export interface Assay {
  id: number;
  name: string;
  description: string | null;
  version: string;
  metricCategories?: Record<MetricCategory, MetricSubcategory[]>;
  targets?: AssayTargets;
}
