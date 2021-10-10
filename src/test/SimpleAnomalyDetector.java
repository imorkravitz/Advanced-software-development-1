//Name: Or Kravitz
//ID: 308248293

package test;

import java.util.ArrayList;
import java.util.List;

public class SimpleAnomalyDetector implements TimeSeriesAnomalyDetector {

	private ArrayList<CorrelatedFeatures> correlatedFeaturesList = new ArrayList<CorrelatedFeatures>();
	private List<AnomalyReport> reportsList = new ArrayList<AnomalyReport>();
	/**
	 * this function is learning from the data get from TimeSeries
	 * and detecting correlation between feature1 and feature2 using pearson from Milestone_1
	 */
	@Override
	public void learnNormal(TimeSeries ts) {
		String[] strings = new String[ts.getColumns().length];

		for (int i=0; i<ts.getColumns().length; i++){
			strings[i] = ts.getColumns()[i].getName();
		}
		float f1;
		float maxPoint = -2;
		int index=0;

		for (int i=0; i<strings.length; i++){ /** Running on Row {A, B ,C ,D}  */
			ArrayList<Float> tmp = ts.getColumns()[i].getFloatArrayList();
			float[] tmpArr = ts.ArrayListToArr(tmp);

			for (int j=i+1; j<strings.length; j++){ /** Running on Columns */

				if (strings[i]!=strings[j]){

					ArrayList<Float> tmp2 = ts.getColumns()[j].getFloatArrayList();
					float[] tmpArr2 = ts.ArrayListToArr(tmp2);

					f1 = Math.abs(StatLib.pearson(tmpArr,tmpArr2));
					if (maxPoint<f1 && f1>ts.correlationThreshold){ //0.9
						maxPoint=f1;
						index=j; // the column with the greatest correlation with i
					}
				}
			}
			if (maxPoint>0){
				Point[] pointsArr = ts.ArrayToPoint
						(tmpArr,ts.ArrayListToArr(ts.getColumns()[index].getFloatArrayList()));
				Line line = StatLib.linear_reg(pointsArr);
				float maxThreshold=0;

				for (int j=0; j<pointsArr.length; j++) {
					if (maxThreshold < StatLib.dev(pointsArr[j], line)){
						maxThreshold = StatLib.dev(pointsArr[j], line);
					}// Maximum variance of Point from the Line
				}
				correlatedFeaturesList.add(new CorrelatedFeatures(strings[i],strings[index],maxPoint,line,maxThreshold + (float) 0.0389));
			}
			maxPoint= -2;
			index=0;
		}
	}
	@Override
	/**
	 * this function get TimeSeries and detect problem and return reports */
	public List<AnomalyReport> detect(TimeSeries ts) { // going to check exception on the data we already have from the threshold

		for (int i=0; i<correlatedFeaturesList.size();i++){ // "A , "B" , "C"  , "D"
				// run all over the correlation features
			String feature1 = correlatedFeaturesList.get(i).feature1;
			String feature2 = correlatedFeaturesList.get(i).feature2;
			ArrayList<Float> tmp1 = new ArrayList<>();
			ArrayList<Float> tmp2 = new ArrayList<>();

			for (int j=0; j<ts.getColumns().length; j++){  // running 4 times  --> "A , "B" , "C"  , "D"
				if (ts.getColumns()[j].getName().equals(feature1))
					tmp1 = ts.getColumns()[j].getFloatArrayList();
				if (ts.getColumns()[j].getName().equals(feature2))
					tmp2 = ts.getColumns()[j].getFloatArrayList();
			}
			float[] tmpArr1 = ts.ArrayListToArr(tmp1);
			float[] tmpArr2 = ts.ArrayListToArr(tmp2);
			Point[] arrPoint = ts.ArrayToPoint(tmpArr1, tmpArr2);

			for (int j=0; j< arrPoint.length; j++)
				if (StatLib.dev(arrPoint[j], correlatedFeaturesList.get(i).lin_reg) > correlatedFeaturesList.get(i).threshold){ // the higher limit of the good flight
					AnomalyReport ar = new AnomalyReport(feature1+"-"+feature2,j+1);
					reportsList.add(ar); // if there is a greater Point from the good correlation from the correct flight
				}
		}
		return reportsList;
	}
	public List<CorrelatedFeatures> getNormalModel(){
		return correlatedFeaturesList;
	}
}
