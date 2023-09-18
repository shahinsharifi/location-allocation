export interface MapState {
  drawing: boolean;
  regionSelection: {
    selectionRegions: number;
    wkt: string | null;
  }
  visibility: {
    region: boolean;
    regionSelection: boolean;
    location: boolean;
    allocation: boolean;
  }
}
