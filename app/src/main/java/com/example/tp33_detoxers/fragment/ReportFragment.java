package com.example.tp33_detoxers.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tp33_detoxers.R;
import com.example.tp33_detoxers.adapter.RVReportAdapter;
import com.example.tp33_detoxers.model.ReportRecord;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReportFragment extends Fragment {
    private List<ReportRecord> reportRecords;
    private String[] rName = new String[] {"sugars","salt","saturated-fat","fat"};
    private float maxNum = 0;

    private RecyclerView reportRecycler;
    private RecyclerView.LayoutManager layoutManager;
    private RVReportAdapter reportAdapter;

    private BarChart barChart;
    private BarData barData;

    public ReportFragment() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View reportView = inflater.inflate(R.layout.fragment_myreport, container, false);

        SharedPreferences report = getActivity().getSharedPreferences("list", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = report.getString("list", null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        ArrayList<String> array = gson.fromJson(json, type);

        MaterialToolbar toolbar = reportView.findViewById(R.id.report_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(getActivity()).onBackPressed();
            }
        });

        ImageView imageView = reportView.findViewById(R.id.iv_report);
        TextView tv_suggest = reportView.findViewById(R.id.tv_suggest);
        reportRecycler = reportView.findViewById(R.id.rv_report);
        barChart = reportView.findViewById(R.id.bar_chart);

        reportRecords = new ArrayList<>();
        reportAdapter = new RVReportAdapter(reportRecords);
        ArrayList<String> arrayLevel = new ArrayList<>();
        int risk = 0;
        String suggestion = "";

        for(int i = 0; i < rName.length; i++){
            String name = rName[i];
            String quantity = array.get(i);
            String levels = "";
            switch (name){
                case "salt":
                    if(Double.parseDouble(quantity) > 6){
                        levels = "high";
                    }else if(Double.parseDouble(quantity) < 4.8){
                        levels = "low";
                    }else{
                        levels = "moderate";
                    }
                    break;
                case "sugars":
                    if(Double.parseDouble(quantity) > 90){
                        levels = "high";
                    }else if(Double.parseDouble(quantity) < 72){
                        levels = "low";
                    }else{
                        levels = "moderate";
                    }
                    break;
                case "saturated-fat":
                    if(Double.parseDouble(quantity) > 20){
                        levels = "high";
                    }else if(Double.parseDouble(quantity) < 16){
                        levels = "low";
                    }else{
                        levels = "moderate";
                    }
                    break;
                case "fat":
                    if(Double.parseDouble(quantity) > 70){
                        levels = "high";
                    }else if(Double.parseDouble(quantity) < 56){
                        levels = "low";
                    }else{
                        levels = "moderate";
                    }
                    break;
            }
            arrayLevel.add(levels);
            double percentage = Double.parseDouble(array.get(i))/Double.parseDouble(array.get(array.size()-1));
            saveReport(name, quantity, Double.toString(percentage),levels);
        }

        for(int i = 0; i < arrayLevel.size(); i++){
            if(arrayLevel.get(i).equals("high")){
                risk = -1;
                break;
            }else if(arrayLevel.get(i).equals("moderate")){
                risk = 1;
            }
        }
        if (risk == 1){
            Picasso.get().load(R.drawable.ic_report_problem_24px).placeholder(R.drawable.ic_report_problem_24px).into(imageView);
            suggestion = "Be Care of Toxins";
            tv_suggest.setText(suggestion);
            tv_suggest.setTextColor(Color.parseColor("#F9BF45"));
        }else if(risk == 0){
            Picasso.get().load(R.drawable.ic_verified_user_24px).placeholder(R.drawable.ic_verified_user_24px).into(imageView);
            suggestion = "Healthy Meal";
            tv_suggest.setText(suggestion);
            tv_suggest.setTextColor(Color.parseColor("#00AA90"));
        }else {
            Picasso.get().load(R.drawable.ic_not_interested_24px).placeholder(R.drawable.ic_not_interested_24px).into(imageView);
            suggestion = "Too Much Toxins";
            tv_suggest.setText(suggestion);
            tv_suggest.setTextColor(Color.parseColor("#C73E3A"));
        }

        reportRecycler.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        reportRecycler.setAdapter(reportAdapter);
        layoutManager = new LinearLayoutManager(getActivity());
        reportRecycler.setLayoutManager(layoutManager);

        drawBarChart(array);

        return reportView;
    }

    public void saveReport(String name, String units, String percentage,String level){
        ReportRecord record = new ReportRecord(name, units, percentage, level);
        reportRecords.add(record);
        reportAdapter.addLevel(reportRecords);
    }

    //set the recommended daily intake for adults
    private ArrayList<BarEntry> barSuggest(){
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(1, 90));
        barEntries.add(new BarEntry(2, 6));
        barEntries.add(new BarEntry(3, 20));
        barEntries.add(new BarEntry(4, 70));
        return barEntries;
    }

    //set the actual intakes
    private ArrayList<BarEntry> barActual(ArrayList<String> list) {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        maxNum = Float.parseFloat(list.get(0));
        for(int i = 0; i < list.size(); i++){
            float num = Float.parseFloat(list.get(i));
            if(num > maxNum){
                maxNum = num;
            }
            barEntries.add(new BarEntry(i, num));
        }
        return barEntries;
    }

    //draw the bar chart
    private void drawBarChart(ArrayList<String> list) {
        BarDataSet barDataSet1 = new BarDataSet(barSuggest(),"Recommended");
        barDataSet1.setColor(Color.GREEN);
        BarDataSet barDataSet2 = new BarDataSet(barActual(list), "Actual");
        barDataSet2.setColor(Color.BLUE);
        barData = new BarData(barDataSet1,barDataSet2);
        barChart.setData(barData);

        //set the x axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(rName));
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(rName.length);

        //set the layout of the bar chart
        float barSpace = 0.1f;
        float groupSpace = 0.1f;

        //get the highest value of y axis
        maxNum = Math.max(90, maxNum);
        String df = new java.text.DecimalFormat("#0.0").format(maxNum);
        float newNum = (float) (Float.parseFloat(df)*1.1);

        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.getAxisRight().setEnabled(false);
        barData.setBarWidth(0.35f);
        barChart.getAxisLeft().setAxisMinimum(0);
        barChart.getAxisLeft().setAxisMaximum(newNum);
        barChart.getXAxis().setAxisMaximum(4);
        barChart.getXAxis().setAxisMinimum(0);
        barChart.groupBars(0, groupSpace, barSpace);
        barChart.invalidate();
    }

}