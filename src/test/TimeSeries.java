//Name: Or Kravitz
//ID: 308248293

package test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.io.*;

public class TimeSeries {
	/*** this is a class of Columns* holding member Arraylist of float*/
	public class Columns{
		private String name; // A, B, C ,D
		private ArrayList<Float> floats;

		public Columns(String name) {
			this.name = name;
			this.floats = new ArrayList<Float>();
		}
		public String getName() {return name;}
		public void setName(String name) { this.name = name; }
		public ArrayList<Float> getFloatArrayList() {return floats; }
		public void setFloatArrayList(ArrayList<Float> floats) { this.floats = floats; }
	}

	private Columns[] columns;
	String myCSVFile;
	Path pathToFile;

	public double correlationThreshold = 0.9;
	public void setCorrelationThreshold(double correlationThreshold) {this.correlationThreshold = correlationThreshold;}
	public Columns[] getColumns(){return this.columns;}

	/***  ArrayList of float into a list*  returning the list*/
	public float[] ArrayListToArr(ArrayList<Float> list){
		float[] f = new float[list.size()];
		for (int i=0; i<list.size();i++){
			f[i] = list.get(i);
		}
		return f;
	}

	/***  Returning Array of Points*/
	public Point[] ArrayToPoint(float[] x, float[] y)
	{
		Point[] p = new Point[x.length];
		for (int i = 0; i < x.length; i++) {
			p[i] = new Point(x[i], y[i]);
		}
		return p;
	}

	public TimeSeries(String csvFileName) {
		this.pathToFile= Paths.get(csvFileName);
		this.myCSVFile=csvFileName;

		try {
			BufferedReader br = new BufferedReader(new FileReader(csvFileName));
			br = Files.newBufferedReader(pathToFile, StandardCharsets.US_ASCII);

			String line = br.readLine();
			String[] records = line.split(",");

			this.columns = new Columns[records.length];
			for (int i=0; i< records.length; i++){
				columns[i] = new Columns(records[i]);
			}
			line= br.readLine();
			/**
			 * after {A, B, C, D} skip row and than start implement data
			 */
			while (line!=null){
				records = line.split(",");
				for (int j=0; j< records.length;j++){
					columns[j].getFloatArrayList().add(Float.parseFloat(records[j]));
				}
				/**
				 * skip next row
				 */
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
