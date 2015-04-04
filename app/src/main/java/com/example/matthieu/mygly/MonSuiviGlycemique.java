package com.example.matthieu.mygly;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.LineChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.XYChart;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MonSuiviGlycemique extends ActionBarActivity {

    private String SAVE_FILE = "MyglyVal.txt";
    private XYMultipleSeriesDataset mDataset;
    private XYMultipleSeriesRenderer mRenderer;
    private TimeSeries series;
    private GraphicalView mChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mon_suivi_glycemique);

        initiateChart();
        mChartView = ChartFactory.getTimeChartView(this,mDataset,mRenderer,"DD MM yyyy");
        mChartView.setBackgroundColor(Color.BLACK);


        LinearLayout layout = (LinearLayout) findViewById(R.id.chartView);
        layout.addView(mChartView);

    }

    public void addValue(View view)  {


        /*EditText glyValue = (EditText)findViewById(R.id.GlyVal);
        double newVal = Double.valueOf(glyValue.getText().toString());*/
        int i=0;
        while (i<10) {
            double newVal = Math.random() * 2.7;
            series.add(new Date(), newVal);
            updateChart();

            if (saveValue(newVal)) {
                Toast toast = Toast.makeText(getApplicationContext(), "Une nouvelle mesure de glycémie a été enregistrée", Toast.LENGTH_SHORT);
                toast.show();
            }
            i++;
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private boolean saveValue(double val) {


        FileOutputStream fos = null;
        try {
            fos = openFileOutput(SAVE_FILE, Context.MODE_APPEND);
            fos.write((new Date().toString()+"\n").getBytes());
            fos.write((val+"\n").getBytes());
            fos.close();
            return true;
        } catch (FileNotFoundException e) {

            e.printStackTrace();
            return false;
        } catch (IOException e) {

            e.printStackTrace();
            return false;
        }




    }

    private void loadValues()  {
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
        BufferedReader br = null;

        int i = 0;
        int count = 0;

        Date date=new Date();
        double val=0;
        boolean isDate=true;
        String file = "";
        String dateValue="";
        String stringVal="";
        FileInputStream fis;
        series = new TimeSeries("MyGlyVal");




        try {
            fis = openFileInput(SAVE_FILE);
            byte[] input = new byte[fis.available()];
            while(fis.read(input) != -1){

                    file += new String(input);

            }
            fis.close();
            for(int j=0;j<file.length();j++){
                if(isDate){
                    if(file.charAt(j)!='\n') {
                        dateValue += file.charAt(j);
                    }else{
                        isDate = false;

                        date = formatter.parse(dateValue);


                        dateValue ="";
                        j++;
                        i++;
                    }
                }
                if(!isDate){
                    if(file.charAt(j)!='\n') {
                        stringVal += file.charAt(j);
                    }else{
                        isDate = true;
                        val = Double.parseDouble(stringVal);

                        stringVal ="";
                        i++;

                    }
                }
                if(i==2){
                    series.add(date,val);
                    i=0;
                }

            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } catch (ParseException e) {
            e.printStackTrace();
        }






    }

    public void updateChart(){
        mRenderer.setXAxisMin(new Date().getTime() - 1800000);

        mRenderer.setXAxisMax(new Date().getTime());
        mChartView.repaint();




    }

    private void initiateChart(){





        mDataset = new XYMultipleSeriesDataset();

       mRenderer = new XYMultipleSeriesRenderer();




        loadValues();





        XYSeriesRenderer renderer = new XYSeriesRenderer();

        renderer.setColor(Color.GREEN);

        renderer.setPointStyle(PointStyle.CIRCLE);

        renderer.setFillPoints(true);

        renderer.setDisplayChartValues(true);

        renderer.setLineWidth(15);




        mDataset.addSeries(series);

        mRenderer.addSeriesRenderer(renderer);

        mRenderer.setXTitle("Heure");

        mRenderer.setYTitle("Glycémie en mg/L");

        mRenderer.setXAxisMin(new Date().getTime()-500000);

        mRenderer.setXAxisMax(new Date().getTime()+500000);

        mRenderer.setYAxisMin(0);

        mRenderer.setYAxisMax(11);

        mRenderer.setAxesColor(Color.BLACK);

        mRenderer.setLabelsColor(Color.WHITE);

        mRenderer.setXLabels(10);

        mRenderer.setAxisTitleTextSize(20);

        mRenderer.setYLabels(10);

        mRenderer.setPointSize(16);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mon_suivi_glycemique, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
