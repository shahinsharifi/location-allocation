
export class ChartMetaData {
	xMin?: number;
	xMax?: number;
	xAxisTitle?: string;
	yMin?: number;
	yMax?: number;
	yAxisTitle?: string;
	yLabel?: string;
}

export class ChartData {
	type?: string;
	metadata?: any | undefined;
	data?: Array<any> | undefined;
}


