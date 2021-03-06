package edu.illinois.keshmesh.constants;

import java.io.File;

import edu.illinois.keshmesh.report.KeyValuePair;

public class Constants {

	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	public static final String FILE_SEPARATOR = File.separator;

	public static final String KESHMESH_HOME = System.getProperty("user.home") + Constants.FILE_SEPARATOR + "keshmesh";

	public static final String KESHMESH_PROPERTIES_FILE = KESHMESH_HOME + FILE_SEPARATOR + "keshmesh.properties";

	public static final String KESHMESH_CALL_GRAPH_FILE_NAME = "call-graph.txt";

	public static final String KESHMESH_HEAP_GRAPH_FILE_NAME = "heap-graph.txt";

	public static final String KESHMESH_ENTRY_POINTS_FILE_NAME = "entry-points.txt";

	public static final String PROFILING_RESULTS_FILENAME = "profiling-results.csv";

	public static final KeyValuePair PROFILING_RESULTS_HEADER = new KeyValuePair("KEY", "VALUE");

	public static final String INFINITY = "INF";

}
