import {RegionSelection} from "../region-selection";
import {LayerVisibility} from "../layer-visibility";

export interface MapState {
  layers?: Array<string>;
  selection: RegionSelection;
  visibility: LayerVisibility
}
