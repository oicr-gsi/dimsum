export const GATE_COLOR_MAPPING: { [gate: string]: string } = {
  "Receipt": "#4477AA",
  "Extraction": "#66CCEE",
  "Library Prep": "#228833",
  "Library Qual": "#CCBB44",
  "Full-Depth": "#EE6677",
  "Analysis Review": "#44AA99",
  "Release Approval": "#AA3377",
  "Release": "#BBBBBB",
  "Full Case": "#000000",
};

export function getColorForGate(gate: string): string {
  return GATE_COLOR_MAPPING[gate] || "#DDDDDD";
}
