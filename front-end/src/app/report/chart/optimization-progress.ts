export class GenerationData {

  public constructor(init: Partial<GenerationData>) {
    Object.assign(this, init);
  }

  public generation: string;
  public value: number;

}
export class OptimizationProgress extends Array<GenerationData> {
  public constructor() {
    super();
  }

  public updateData(data: GenerationData) {
    this.push(data);
  }
}
