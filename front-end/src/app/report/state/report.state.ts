import {ChartData} from "../chart/chart-data";

export interface ReportState {
  logs?: Array<string> | undefined;
  locationFitnessChart?: ChartData | undefined;
  allocationFitnessChart?: ChartData | undefined;
  costDistributionChart?: ChartData | undefined;
}
