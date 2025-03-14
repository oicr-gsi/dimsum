export type MetricCategory =
  | "RECEIPT"
  | "EXTRACTION"
  | "LIBRARY_PREP"
  | "LIBRARY_QUALIFICATION"
  | "FULL_DEPTH_SEQUENCING"
  | "ANALYSIS_REVIEW";

export interface Metric {
  name: string;
  sortPriority?: number;
  minimum?: number;
  maximum?: number;
  units?: string;
  tissueMaterial?: string;
  tissueOrigin?: string;
  tissueType?: string;
  negateTissueType?: boolean;
  nucleicAcidType?: string;
  containerModel?: string;
  readLength?: number;
  readLength2?: number;
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
