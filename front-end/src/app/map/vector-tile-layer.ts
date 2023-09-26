import {FillLayerSpecification} from "maplibre-gl";

export class VectorTileLayer implements FillLayerSpecification {
  id: string
  type: any
  source: any
  sourceLayer?: any
  fields?: Map<string, any>
  paint?: any
  bounds?: [number,number,number,number]
  metadata?: any
  description?: string
}
