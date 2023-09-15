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
  libraryDesignCode: string;
  metrics: Metric[];
}

export interface Assay {
  id: number;
  name: string;
  description?: string;
  version: string;
  metricCategories: Record<MetricCategory, MetricSubcategory[]>;
}
