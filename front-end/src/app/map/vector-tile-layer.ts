export interface VectorTileLayer {
  id?: string
  type?: any
  source?: any
  sourceLayer?: any
  fields?: Map<string, any>
  paint?: any
  bounds?: [number,number,number,number]
  description?: string
}
