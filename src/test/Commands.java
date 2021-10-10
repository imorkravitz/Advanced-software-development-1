package test;
//Name: Or Kravitz
//ID: 308248293

import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Commands {

	// Default IO interface
	public interface DefaultIO{
		public String readText();
		public void write(String text);
		public float readVal();
		public void write(float val);

		// you may add default methods here
	}

	// the default IO to be used in all commands
	DefaultIO dio;
	public Commands(DefaultIO dio) {
		this.dio=dio;
	}

	// you may add other helper classes here

	// the shared state of all commands
	private class SharedState{
		/** added members */
		TimeSeries CSVTrain = null; // a csv train file
		TimeSeries CSVTest = null; // a csv test file
		List<AnomalyReport>  anomalyList = null; // a list of reports
		List<Point> pointList = null; // this members will be used in command 5
	}
	/** added getters */
	private SharedState sharedState=new SharedState();
	public SharedState getSharedState() { return sharedState; }
	public DefaultIO getDio() { return dio; }


	// Command abstract class
	public abstract class Command{
		protected String description;

		public Command(String description) {
			this.description=description;
		}
		public abstract void execute();
	}

	// Command class for example:
	public class ExampleCommand extends Command{

		public ExampleCommand() {
			super("this is an example of command");
		}

		@Override
		public void execute() {
			dio.write(description);
		}
	}

 // ** This class represents the main menu for the client
	public class menuCommand extends Command{

		public menuCommand() {

			super("Welcome to the Anomaly Detection Server.\n"+
					"Please choose an option:\n"+
					"1. upload a time series csv file\n"+
					"2. algorithm settings\n"+
					"3. detect anomalies\n"+
					"4. display results\n"+
					"5. upload anomalies and analyze results\n"+
					"6. exit\n");
		}

		@Override
		public void execute() {

			dio.write(description);
			String read = dio.readText();
			int choice = Integer.parseInt(read);

			switch (choice) {
				case 1: // upload a csv file
					new uploadCSVFileCommand().execute();;
					break;
				case 2:  // algo settings
					new algoSettingsCommand().execute();
					break;
				case 3:  // detect anomalies
					new detectAnomaliesCommand().execute();
					break;
				case 4:  // display the results
					new displayResultsCommand().execute();
					break;
				case 5:  // upload anomalies and analyze
					new uploadAndAnalyzeCommand().execute();
					break;
				case 6:  // exit
					new exitCommand().execute();
					break;
			}
		}
	}

	public class uploadCSVFileCommand extends Command{

		public uploadCSVFileCommand() {
			super("Please upload your local train CSV file.\n");
		}

		@Override
		public void execute() {

			// read CSV Train File
			dio.write(description);

			String line = null;
			ArrayList<String> read1 = new ArrayList<>();

			while (!(line=dio.readText()).equals("done")) {
				read1.add(line + "\n");
			}

			try {
				FileWriter csvTrain= new FileWriter("CSVFileTrain.csv");
				String string1 = String.join("", read1);
				csvTrain.write(string1);
				csvTrain.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			getSharedState().CSVTrain=new TimeSeries("CSVFileTrain.csv");
			dio.write("Upload complete.\n");

			// read CSV test File

			dio.write("Please upload your local test CSV file.\n");

			String line2 = null;
			ArrayList<String> read2 = new ArrayList<>();

			while (!(line2=dio.readText()).equals("done")) {
				read2.add(line2 + "\n");
			}

			try {
				FileWriter csvTest= new FileWriter("CSVFileTest.csv");
				String string2 = read2.stream().collect(Collectors.joining(""));
				csvTest.write(string2);
				csvTest.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			getSharedState().CSVTest=new TimeSeries("CSVFileTest.csv");
			dio.write("Upload complete.\n");

			new menuCommand().execute();
		}
	}


	public class algoSettingsCommand extends Command{

		public algoSettingsCommand() {
			super("");
		}

		@Override
		public void execute() {
			dio.write("The current correlation threshold is: " + getSharedState().CSVTrain.correlationThreshold + "\n"); //0.9
			dio.write("Type a new threshold \n");
			double threshold_ = Double.parseDouble(dio.readText());
			while (threshold_ < 0 || threshold_ > 1){
				dio.write("please choose a value between 0 and 1.");
				threshold_ = Double.parseDouble(dio.readText());
			}
			getSharedState().CSVTrain.setCorrelationThreshold(threshold_);

			new menuCommand().execute();
		}
	}

	public class detectAnomaliesCommand extends Command{

		public detectAnomaliesCommand() {
			super("anomaly detection complete.\n");
		}

		@Override
		public void execute() {
			SimpleAnomalyDetector simpleAnomalyDetector = new SimpleAnomalyDetector();
			simpleAnomalyDetector.learnNormal(getSharedState().CSVTrain);
			List<AnomalyReport> anomalyReports = new ArrayList<>();

			anomalyReports.addAll(simpleAnomalyDetector.detect(getSharedState().CSVTest));
			getSharedState().anomalyList = anomalyReports;

			dio.write(description);

			new menuCommand().execute();
		}
	}

	public class displayResultsCommand extends Command{

		public displayResultsCommand() {
			super("Done.\n");
		}


		@Override
		public void execute() {
			for (AnomalyReport anomalyReport: getSharedState().anomalyList) {
				dio.write((String.valueOf(anomalyReport.timeStep)) + "\t" + anomalyReport.description + "\n");
			}
			dio.write(description);

			new menuCommand().execute();
		}
	}


	public class uploadAndAnalyzeCommand extends Command{

		public uploadAndAnalyzeCommand() {
			super("Please upload your local anomalies file.\n" + "Upload complete.\n" );
		}

		@Override
		public void execute() {

			dio.write(description);

			float range = 0;

			String line = null;

			getSharedState().pointList=new ArrayList<>();

			while (!((line = dio.readText()).equals("done"))) {
				String [] temp = line.split(",");
				getSharedState().pointList.add(new Point(Float.valueOf(temp[0]),Float.valueOf(temp[1]) ));
			}

			for (Point p:getSharedState().pointList) // run all over the point and calculate the ranges between them
				range += p.y - p.x + 1;

			float N = getSharedState().CSVTest.getColumns()[0].getFloatArrayList().size() - range;

			ArrayList<String> discretion = new ArrayList<>();
			ArrayList<Point> time = new ArrayList<>();

			int k = 0;

			if (getSharedState().anomalyList==null) // there are no anomaly report
				return;

			for (int i=0; i<getSharedState().anomalyList.size();i++)
			{
				AnomalyReport start = getSharedState().anomalyList.get(i);
				AnomalyReport end = null;

				int j = i+1;

				while (j<getSharedState().anomalyList.size()
						&& start.description.equals(getSharedState().anomalyList.get(j).description)
						&& start.timeStep + j - k == getSharedState().anomalyList.get(j).timeStep) {
					end = getSharedState().anomalyList.get(j);
					j++;
				}

				if (end != null) {
					discretion.add(end.description); // same description to the current anomaly report
					time.add(new Point(start.timeStep, end.timeStep));
				}

				else {
					discretion.add(start.description);
					time.add(new Point(start.timeStep, start.timeStep)); // started and ended at the same timeStep
				}

				k = j;
				i = j-1;
				/**
				 * 73	 A-B
				 * 74	 A-B
				 * 75	 A-B
				 * 76	 A-B
				 * 133	 C-D
				 * 134	 C-D
				 * 135	 C-D
				 * */
			}

			float FP = 0;
			float TP = 0;
			float TN = 0;
			float FN = 0;

			float P = getSharedState().pointList.size();

			boolean flag = false;

			List<Point> pointList= new ArrayList<>();

			for (Point p1: getSharedState().pointList)
			{
				for (Point p2:time)
				{
					if ((p1.x <= p2.x && p2.y <= p1.y)
							|| (p1.x >= p2.x && p1.y >= p2.y && p2.y >= p1.x)
							|| (p2.x <= p1.x && p1.y <= p2.y)
							|| (p1.x <= p2.x && p1.y <= p2.y && p2.x <= p1.y))
					{
						if (!flag)
							TP++;

						flag = true;

						if (!pointList.contains(p2))
							pointList.add(p2);
					}
				}

				if (!flag)
					FN++;

				flag = false;
			}

			FP = (time.size() - pointList.size()) / N;
			TP /= P;

			DecimalFormat decimalFormat = new DecimalFormat("0.0000");

			String tp = decimalFormat.format(TP);
			String fp = decimalFormat.format(FP);

			tp = tp.substring(0,tp.length()-1);
			fp = fp.substring(0,fp.length()-1);

			while (tp.charAt(tp.length()-1)=='0' && tp.length()!=3)
				tp = tp.substring(0,tp.length()-1);

			while (fp.charAt(fp.length()-1)=='0' && fp.length()!=3)
				fp = fp.substring(0,fp.length()-1);

			dio.write("True Positive Rate: " + tp + "\n");
			dio.write("False Positive Rate: " + fp + "\n");

			new menuCommand().execute();
		}
	}


	public class exitCommand extends Command{

		public exitCommand() {
			super("");
		}

		@Override
		public void execute() {
			dio.write(description);
		}
	}
}


