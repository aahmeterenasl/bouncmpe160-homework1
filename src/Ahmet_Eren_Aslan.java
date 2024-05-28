/**
 * Program finds the path between two stations inputted and animates the path using StdDraw library if the path exist
 * @author Ahmet Eren Aslan, Student ID: 2021400126
 * @since Date: 17.03.2023
 */
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.awt.*;
public class Ahmet_Eren_Aslan {
    /**
     * Read the file coordinates.txt
     * Take inputs from the user
     * Prints out the path between inputs if exists
     * Animate the path using StdDraw library
     * @param args Main input arguments are not used
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {

        String fileName = "coordinates.txt";// Name of the file containing the information about metro lines

        File file = new File(fileName);// Creating file object

        // Checking if the file exist and exiting the code if not
        if(!file.exists()){
            System.out.printf("%s can not be found.", fileName);
            System.exit(1);
        }

        Scanner inputFile = new Scanner(file);// Creating scanner object

        int metroLineNumber =10;// Number of metro lines
        String[] metroLineNames = new String[metroLineNumber];// Creating array for name of lines
        int[][] metroLinesRGBValues = new int[metroLineNumber][3];// Creating array for RGB values of lines for the animation
        String[][] stationNames = new String[metroLineNumber][];// Creating array for name of stations
        int[][] stationCoordinatesX = new int[metroLineNumber][];// Creating array for x coordinates of stations
        int[][] stationCoordinatesY = new int[metroLineNumber][];// Creating array for y coordinates of stations
        String[] breakpoints = new String[7];// Creating array for name of breakpoints
        String[][] breakpointLines = new String[7][];// Creating array showing which breakpoints are in which lines

        int counter = 0;// Variable to count the number of line have read from the file

        // Reading the file line by line
        while (inputFile.hasNextLine()){
            String line = inputFile.nextLine();// Reading a line
            String[] lineContents = line.split(" ");// Splitting line by the delimiter space

            // Checking the line using the text file's format
            if(counter%2==0 && counter<2*metroLineNumber){
                metroLineNames[counter/2] = lineContents[0];// Assigning line names
                String[] strRGBValues = lineContents[1].split(",");
                for (int i = 0; i < 3 ; i++) {
                    metroLinesRGBValues[counter/2][i] = Integer.parseInt(strRGBValues[i]);// Assigning RGB values
                }
            }
            else if (counter<2*metroLineNumber) {
                int currentStationNumber = lineContents.length/2;// Number of stations on the current line
                stationNames[counter/2] = new String[currentStationNumber];
                stationCoordinatesX[counter/2] = new int[currentStationNumber];
                stationCoordinatesY[counter/2] = new int[currentStationNumber];
                for (int j = 0; j<lineContents.length;j++){
                    if(j%2==0){
                        stationNames[counter/2][j/2] = lineContents[j];// Assigning station name
                    }
                    else{
                        String[] coordinatesStr = lineContents[j].split(",");
                        stationCoordinatesX[counter/2][j/2] = Integer.parseInt(coordinatesStr[0]);// Assigning station's x coordinate
                        stationCoordinatesY[counter/2][j/2] = Integer.parseInt(coordinatesStr[1]);// Assigning station's y coordinate
                    }
                }
            }
            else{
                breakpointLines[counter-20] = new String[lineContents.length-1];
                breakpoints[counter-20] = lineContents[0];// Assigning breakpoint stations' names
                // Assigning breakpoint station's lines
                System.arraycopy(lineContents, 1, breakpointLines[counter - 20], 0, lineContents.length - 1);
            }
            counter++;// Increasing counter by 1
        }

        inputFile.close();// Closing the scanner object

        String[][] realStationNames = new String[metroLineNumber][];// Creating array for the real names of the stations

        // Iterating all the values of stationNames
        //Fixing and assigning the new names to the realStationNames
        for (int i = 0; i < stationNames.length; i++){
            realStationNames[i] = new String[stationNames[i].length];
            for (int j = 0; j < stationNames[i].length; j++) {
                String currentStationName = stationNames[i][j];
                if (currentStationName.startsWith("*")) {
                    realStationNames[i][j] = currentStationName.substring(1);
                }
                else{
                    realStationNames[i][j] = currentStationName;
                }
            }
        }


        // Taking the input from user
        Scanner input = new Scanner(System.in);// Creating a new scanner object

        String initialStation = input.nextLine().strip();// First Station
        String destinationStation = input.nextLine().strip();// Last Station

        //Checking the inputs
        boolean initialExist = false;
        boolean lastExist = false;

        for (int i = 0; i < realStationNames.length; i++){
            for (int j = 0; j < realStationNames[i].length; j++) {
                if(realStationNames[i][j].equals(initialStation)){
                    initialExist = true;
                }
                if(realStationNames[i][j].equals(destinationStation)){
                    lastExist = true;
                }
            }
        }

        input.close();// Closing the scanner object

        // If one of the inputs is non-exist exits the code
        if(!(initialExist && lastExist)){
            System.out.println("No such station names in this map");
            System.exit(1);
        }

        // Finding the path
        String[] path = recursivePathFinder(initialStation,destinationStation,"",metroLineNames,realStationNames,breakpoints,breakpointLines);

        // If path doesn't exist exits the code
        if (Arrays.toString(path).equals("[]")){
            System.out.println("These two stations are not connected");
            System.exit(1);
        }

        // Prints out the solution
        for (String station: path) {
            System.out.println(station.strip());
        }

        // Creating the canvas
        int canvasWidth = 1024;
        int canvasHeight = 482;
        StdDraw.setCanvasSize(canvasWidth,canvasHeight);
        StdDraw.setXscale(0,canvasWidth);
        StdDraw.setYscale(0,canvasHeight);
        StdDraw.enableDoubleBuffering();

        // Main Animation
        for(int i = 0; i < path.length; i++) {
            StdDraw.clear();// Clears the canvas
            drawBackground(metroLinesRGBValues, stationNames, stationCoordinatesX, stationCoordinatesY);// Draws the background

            // Takes current animated stations coordinates
            String currentStationName = path[i].strip();
            int indexI = stationIndexFinder(realStationNames,currentStationName)[0];
            int indexJ = stationIndexFinder(realStationNames,currentStationName)[1];
            int currentX = stationCoordinatesX[indexI][indexJ];
            int currentY = stationCoordinatesY[indexI][indexJ];

            StdDraw.setPenColor(StdDraw.PRINCETON_ORANGE);// Setting pen color

            // Iterates all stations until current station in the solution
            for (int j = 0; j < i; j++){
                StdDraw.setPenRadius(0.01);// Setting pen radius

                // Draws the passed stations points
                int indI = stationIndexFinder(realStationNames,path[j].strip())[0];
                int indJ = stationIndexFinder(realStationNames,path[j].strip())[1];
                int pathX = stationCoordinatesX[indI][indJ];
                int pathY = stationCoordinatesY[indI][indJ];
                StdDraw.point(pathX,pathY);
            }
            StdDraw.setPenRadius(0.02);// Setting pen radius
            StdDraw.point(currentX,currentY);// Draws the current stations point
            StdDraw.show();
            StdDraw.pause(300);// Pause for 300 ms
        }




    }




    /**
     * Finds the indexes of the station in the 2D array containing
     * @param realStationNames 2D array containing name of the stations of each line
     * @param stationName target station which is not a breakpoint
     * @return index of the line and station in that line
     */
    public static int[] stationIndexFinder(String[][] realStationNames,String stationName){
        // Iterating all the elements of the array to check the station
        for (int line = 0; line < realStationNames.length; line++){
            for (int st = 0; st < realStationNames[line].length; st++){
                String currentStationName = realStationNames[line][st];
                // Checking the station
                if (currentStationName.equals(stationName)){
                    int[] returnArray = {line,st};
                    return returnArray;// Returns the index of the line and station in that line
                }
            }
        }
        return null;// Returns null if method cannot find the line containing target station
    }

    /**
     * Finds the index of the line in the metroLineNames
     * @param metroLineNames Array containing metro line's names
     * @param metroLineName Target line
     * @return Index of the target line
     */
    public static int lineIndexFinder(String[] metroLineNames,String metroLineName){
        // Iterating all the elements of the array to check the line
        for (int line = 0; line < metroLineNames.length; line++ ){
            // Checking the line
            if (metroLineNames[line].equals(metroLineName)){
                return line;// Returns the index of the line
            }
        }
        return -1;// Returns -1 if method cannot find the line
    }

    /**
     * Finds the index of the breakpoint in the target line
     * @param currentLine Target line
     * @param breakpoint Target breakpoint
     * @return Index of the target breakpoint int eh target line
     */
    public static int breakpointIndexFinder(String[] currentLine,String breakpoint){
        // Iterating all the elements of the array to check the breakpoint
        for (int st = 0; st < currentLine.length; st++){
            // Checking the breakpoint
            if (currentLine[st].equals(breakpoint)){
                return st;// Returns the index of the breakpoint
            }
        }
        return -1;// Returns -1 if method cannot find the breakpoint
    }

    /**
     * Finds stations between input stations if the path exist
     * @param currentStation Name of currently present station
     * @param destination Name of destination
     * @param lastPassedStation Name of the station passed in the previous step
     * @param metroLineNames Array containing name of metro lines
     * @param realStationNames Array containing names of the stations in each line
     * @param breakpoints Array containing names of breakpoints
     * @param breakpointLines Array containing lines the respective breakpoint connects
     * @return Array containing name of stations between two input stations respectively returns empty array if no path exists
     */
    public static String[] recursivePathFinder(String currentStation,String destination,String lastPassedStation,String[] metroLineNames,String[][] realStationNames,String[] breakpoints,String[][] breakpointLines){
        // Initializing return values
        String[] baseReturn = {destination};
        String[] zeroReturn = {};

        // Base condition
        if (currentStation.equals(destination)){
            return baseReturn;
        }

        // Checking whether the current station is breakpoint or not and if it is found its index
        boolean isBreakpoint = false;
        int breakpointIndex = 0;
        for(int bp = 0; bp < breakpoints.length; bp++ ){
            if(currentStation.equals(breakpoints[bp])){
                isBreakpoint = true;
                breakpointIndex = bp;
            }
        }

        // Declaring required variables
        int currentLineIndex;// Index of current line
        int currentStationIndex;// Index of current station in the respective line
        String[] possibleLines;// Names of lines that can be used in the next step

        // Declaring variables for returning
        String solutionStr;// String version of the path
        String[] solutionArray;// Array containing the path

        // Finding possible lines that can be use
        if (isBreakpoint) {
             possibleLines = breakpointLines[breakpointIndex];// Lines connected to the current breakpoint
        }
        else {
            currentLineIndex = stationIndexFinder(realStationNames, currentStation)[0];
            possibleLines = new String[1];
            possibleLines[0] = metroLineNames[currentLineIndex];// Line current station is in
        }

        // Checking each possible line for solution
        for (String posLine: possibleLines){
            currentLineIndex = lineIndexFinder(metroLineNames,posLine);

            // Finding index of the current station in the current checked line
            if (isBreakpoint) {
                currentStationIndex = breakpointIndexFinder(realStationNames[currentLineIndex], currentStation);
            }
            else{
                currentStationIndex = stationIndexFinder(realStationNames, currentStation)[1];
            }

            // Checking the neighbour stations if they exist
            if (currentStationIndex + 1 < realStationNames[currentLineIndex].length){
                // Checking whether the next station is already passed
                if(!realStationNames[currentLineIndex][currentStationIndex+1].equals(lastPassedStation)){
                    // Finding the stations between the neighbour of the current station and the destination
                    solutionStr = Arrays.toString(recursivePathFinder(realStationNames[currentLineIndex][currentStationIndex+1],destination,currentStation,metroLineNames,realStationNames,breakpoints,breakpointLines));
                    solutionStr = solutionStr.substring(1,solutionStr.length()-1);
                    // Checking if the path exist
                    if (!solutionStr.equals("")){
                        solutionStr = currentStation + "," + solutionStr;
                        solutionArray = solutionStr.split(",");
                        return solutionArray;// Returns the path
                    }
                }
            }
            // Functions same with the if block above for the other neighbour in the same line
            if (currentStationIndex - 1 >= 0){

                if(!realStationNames[currentLineIndex][currentStationIndex-1].equals(lastPassedStation)){
                    solutionStr = Arrays.toString(recursivePathFinder(realStationNames[currentLineIndex][currentStationIndex-1],destination,currentStation,metroLineNames,realStationNames,breakpoints,breakpointLines));
                    solutionStr = solutionStr.substring(1,solutionStr.length()-1);
                    if (!solutionStr.equals("")){
                        solutionStr = currentStation + "," + solutionStr;
                        solutionArray = solutionStr.split(",");
                        return solutionArray;
                    }
                }
            }
        }
        return zeroReturn;// Returns empty array if there is no path between two input stations
    }

    /**
     * Draws the map of Istanbul metro line using StdDraw library
     * @param metroLinesRGBValues Array containing RGB values of each line
     * @param stationNames Array containing names of the stations
     * @param stationCoordinatesX Array containing x coordinate of the stations
     * @param stationCoordinatesY Array containing y coordinate of the stations
     */
    public static void drawBackground(int[][] metroLinesRGBValues,String[][] stationNames,int[][] stationCoordinatesX,int[][] stationCoordinatesY){
        StdDraw.picture((double)1024/2,(double)482/2,"background.jpg");// Drawing background

        StdDraw.setPenRadius(0.012);// Setting pen size for lines
        // Drawing evey line
        for (int i = 0; i < stationNames.length; i++){
            StdDraw.setPenColor(metroLinesRGBValues[i][0],metroLinesRGBValues[i][1],metroLinesRGBValues[i][2]);// Setting line colors
            for (int j = 0; j < stationNames[i].length-1; j++){
                StdDraw.line(stationCoordinatesX[i][j],stationCoordinatesY[i][j],stationCoordinatesX[i][j+1],stationCoordinatesY[i][j+1]);// Drawing lines
            }
        }

        StdDraw.setPenRadius(0.01);// Setting pen size for stations
        // Drawing stations and writing station names
        StdDraw.setPenColor(Color.WHITE);// Setting color to white

        for (int i = 0; i < stationNames.length; i++){
            for (int j = 0; j < stationNames[i].length; j++){
                StdDraw.point(stationCoordinatesX[i][j],stationCoordinatesY[i][j]);// Drawing stations
            }
        }
        // Writing station names
        StdDraw.setPenColor(Color.BLACK);// Setting color to black
        StdDraw.setFont(new Font("Helve8ca", Font.BOLD, 8));
        for (int i = 0; i < stationNames.length; i++){
            for (int j = 0; j < stationNames[i].length; j++){
                // Checking if the name will be written
                if (stationNames[i][j].startsWith("*")){
                    StdDraw.text(stationCoordinatesX[i][j],stationCoordinatesY[i][j] + 5,stationNames[i][j].substring(1));// Writing station names
                }
            }
        }
    }
}
