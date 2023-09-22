import {AllocationDataItem} from "../chart/allocation-data-item";
import {LocationDataItem} from "../chart/location-data-item";

export interface ReportState {
  logs?: Array<string> | undefined;
  location?: LocationDataItem | undefined;
  allocation?: AllocationDataItem | undefined;
}
