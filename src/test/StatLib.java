//Name: Or Kravitz
//ID: 308248293

package test;

public class StatLib {

	// simple average
	public static float avg(float[] x){
		float sum=0;
		for (int i=0;i<x.length;i++){
			sum+=x[i];
		}
		return sum/x.length;
	}

	// returns the variance of X and Y
	public static float var(float[] x){
		float sum = 0;
		float u = 0;
		for(int i=0;i< x.length;i++){
			u += x[i]*1/x.length; // finish with U // 5.5
		}
		for (int i=0;i<x.length;i++){
			sum += (float) (Math.pow(x[i],2)); // finish with Sigma of x^2
		}
		sum=sum*1/ x.length; // 38.5
		u = (float) Math.pow(u,2); // 30.25
		return sum - u;
	}

	// returns the covariance of X and Y
	public static float cov(float[] x, float[] y){
		float sum = 0;
		for(int i=0;i< x.length;i++){
			sum += (x[i]-avg(x))*(y[i]-avg(y));
		}
		return sum/x.length;
	}

	// returns the Pearson correlation coefficient of X and Y
	public static float pearson(float[] x, float[] y){
		return cov(x,y)/(float) (Math.sqrt(var(x))*Math.sqrt(var(y)));
	}

	// performs a linear regression and returns the line equation
	public static Line linear_reg(Point[] points){
		float a;
		float b;
		float[] x = new float[points.length];
		float[] y = new float[points.length];
		for (int i = 0; i < points.length; i++){
			x[i]=points[i].x;
			y[i]=points[i].y;
		}
		a = cov(x,y)/var(x);
		b = avg(y)-a*avg(x);

		Line l = new Line(a,b);

		return l;
	}

	// returns the deviation between point p and the line equation of the points
	public static float dev(Point p,Point[] points){
		Line l = linear_reg(points);
		return Math.abs(l.f(p.x)-p.y);//f->> f(x)=line Y
	}

	// returns the deviation between point p and the line
	public static float dev(Point p,Line l){
		return Math.abs(l.f(p.x)-p.y);//f->> f(x)=line Y
	}
}
