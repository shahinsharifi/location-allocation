
export class ChartMetaData {
	chartTitle?: string = '';
	type?: string = 'Spline';
	xMin?: number;
	xMax?: number;
	xAxisTitle?: string = '';
	yMin?: number;
	yMax?: number;
	yAxisTitle?: string = '';
	yLabel?: string;
}

export class ChartData {
	metadata?: ChartMetaData | undefined;
	data?: Array<any> | undefined;
}


