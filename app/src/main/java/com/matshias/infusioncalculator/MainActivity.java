package com.matshias.infusioncalculator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;

import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewAnimator;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DatePicker datePicker;
    private ViewAnimator viewAnim;
    private ViewAnimator viewAnimButton;
    private InfusionCalculator infusion;
    private EditText editWeight;

    final private int ID_REQ_VALUE = 0x10121314;
    final private int ID_REQ_VALUE_TOTAL = ID_REQ_VALUE + 32;
    final private int ID_INF_VALUE = ID_REQ_VALUE + 64;
    final private int ID_SEEK_BAR = ID_REQ_VALUE + 96;

    TextView textBirth;

    TextView textInfusionName;
    TextView textInfusionValue;

    Spinner spinnerParenteral;
    Spinner spinnerFat;
    Spinner spinnerAmino;
    Switch switchK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        editWeight = (EditText) findViewById(R.id.editWeight);

        infusion = new InfusionCalculator();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean hasV = prefs.contains("tolerance_list");
        if (hasV)
        {
            String tolerance = prefs.getString("tolerance_list", "10");
            float tolVal = 0.1F;
            switch (tolerance)
            {
                case "5": tolVal = 0.05F; break;
                case "10": tolVal = 0.1F; break;
                case "15": tolVal = 0.15F; break;
            }
            infusion.config(tolVal);
        }


        viewAnim = (ViewAnimator) findViewById(R.id.viewAnimator);
        viewAnimButton = (ViewAnimator) findViewById(R.id.viewAnimatorButton);

        Animation inAnim = AnimationUtils.loadAnimation(this,R.anim.slide_in_right);
        Animation outAnim = AnimationUtils.loadAnimation(this,R.anim.slide_out_left);

        viewAnim.setInAnimation(inAnim);
        viewAnim.setOutAnimation(outAnim);

        //inAnim = AnimationUtils.loadAnimation(this,android.R.anim.fade_in);
        inAnim = AnimationUtils.loadAnimation(this,R.anim.grow_fade_in_center);
        outAnim = AnimationUtils.loadAnimation(this,android.R.anim.fade_out);

        viewAnimButton.setInAnimation(inAnim);
        viewAnimButton.setOutAnimation(outAnim);

        textBirth = (TextView) findViewById(R.id.textViewBirth);

        spinnerParenteral = (Spinner) findViewById(R.id.spinnerParenteral);
        spinnerFat = (Spinner) findViewById(R.id.spinnerFat);
        spinnerAmino = (Spinner) findViewById(R.id.spinnerAmino);
        switchK = (Switch) findViewById(R.id.switchK);

        editWeight.setOnTouchListener(new EditText.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent m)
            {
                if (m.getActionMasked() == MotionEvent.ACTION_DOWN)
                {
                    editWeight.setText("");
                }

                return false;
            }
        });
        Calendar newCalendar = Calendar.getInstance();

        datePicker = (DatePicker) findViewById(R.id.datePickerBirth);
        //datePicker.updateDate(newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        datePicker.init(newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String age = calculateAge(year, monthOfYear, dayOfMonth);

                textBirth.setText("Geburtsdatum (" + age + " alt)");
            }
        });
        datePicker.setMaxDate(newCalendar.getTimeInMillis());
        newCalendar.set(1900, 1, 1);
        datePicker.setMinDate(newCalendar.getTimeInMillis());

        Button calc = (Button) findViewById(R.id.buttonCalc);
        calc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), 0, 0, 0);

                if (!editWeight.getText().toString().equals(""))
                {
                    infusion.setPatientData(Float.valueOf(editWeight.getText().toString()), cal, spinnerParenteral.getSelectedItemPosition(), spinnerFat.getSelectedItemPosition() - 1, spinnerAmino.getSelectedItemPosition() - 1, switchK.isChecked());
                    fillResult();

                    viewAnim.showNext();
                    viewAnimButton.showNext();
                }
                else {
                    Snackbar.make(view, "Bitte Gewicht ausfüllen", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        Button back = (Button) findViewById(R.id.buttonBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewAnim.showNext();
                viewAnimButton.showNext();
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean hasV = prefs.contains("tolerance_list");
        if (hasV)
        {
            String tolerance = prefs.getString("tolerance_list", "10");
            float tolVal = 0.1F;
            switch (tolerance)
            {
                case "5": tolVal = 0.05F; break;
                case "10": tolVal = 0.1F; break;
                case "15": tolVal = 0.15F; break;
            }
            infusion.config(tolVal);
            if (viewAnim.getDisplayedChild() == 1)
            {
                updateInfusions(null);
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if (viewAnim.getDisplayedChild() == 1)
        {
            viewAnim.showNext();
            viewAnimButton.showNext();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            Intent intent = new Intent(this, FragmentPreferences.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml("<html><body><b>This is a test</b></body></html>"));
            sendIntent.setType("text/html");
            startActivity(sendIntent);
        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void updateReqValue(SeekBar sb)
    {
        int r = sb.getId() - ID_SEEK_BAR;
        InfusionCalculator.AdjustableRequirement req = infusion.getReqData(r);

        TextView t = (TextView) findViewById(ID_REQ_VALUE + r);
        float newVal = req.min + ((float)sb.getProgress() / 64.0F) * (req.max - req.min);

        t.setText(formatFloat(newVal, req.max));
    }


    protected String formatFloat(float val, float max)
    {
        String r;

        if (Math.abs(max) > 50 || val == 0)
        {
            r = String.format("%.0f", val);
        }
        else
        {
            r = String.format("%.1f", val);
        }

        return r;
    }

    protected String calculateAge(int year, int monthOfYear, int dayOfMonth)
    {
        Calendar birth = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        birth.set(year, monthOfYear, dayOfMonth);

        String ageString;

        int curYear = cal.get(Calendar.YEAR);
        int curMonth = cal.get(Calendar.MONTH);
        int curDayM = cal.get(Calendar.DAY_OF_MONTH);


        int ageDays = (int)((cal.getTimeInMillis() - birth.getTimeInMillis()) / 1000 / 86400);
        int ageMonth = 0;
        int ageYear = 0;

        ageMonth = curMonth - monthOfYear;
        if (dayOfMonth > curDayM)
        {
            ageMonth--;
        }
        ageYear = curYear - year;
        ageMonth += ageYear * 12;
        if (monthOfYear > curMonth)
        {
            ageYear--;
        }
        else if (monthOfYear == curMonth && dayOfMonth > curDayM)
        {
            ageYear--;
        }



        if (ageDays == 0)
        {
            ageString = "0 Tage";
        }
        else if (ageDays == 1)
        {
            ageString = "1 Tag";
        }
        else if (ageDays <= 30)
        {
            ageString = String.format("%d Tage", ageDays);
        }
        else if (ageMonth == 1)
        {
            ageString = "1 Monat";
        }
        else if (ageMonth < 24)
        {
            ageString = String.format("%d Monate", ageMonth);
        }
        else
        {
            ageString = String.format("%d Jahre", ageYear);
        }

        return ageString;
    }

    protected void updateInfusions(SeekBar sb)
    {
        if (sb != null)
        {
            int r = sb.getId() - ID_SEEK_BAR;
            InfusionCalculator.AdjustableRequirement req = infusion.getReqData(r);

            TextView t = (TextView) findViewById(ID_REQ_VALUE + r);
            float newVal = req.min + ((float) sb.getProgress() / 64.0F) * (req.max - req.min);

            t.setText(formatFloat(newVal, req.max));
            infusion.setReqValue(r, newVal); // this will recalvulate infusions
        }

        // now update all text fields
        for (int i = 0; i < infusion.INF_NUM; i++)
        {
            float amount = infusion.getAmountInfusion(i);

            TextView infValue = (TextView) findViewById(ID_INF_VALUE + i);
            infValue.setText(formatFloat(amount, 100));
        }

        for (int i = 0; i < infusion.REQ_NUM; i++)
        {
            float amountReq = infusion.getAmountReq(i);
            boolean valid = infusion.isValid(i);
            TextView reqValueTotal = (TextView) findViewById(ID_REQ_VALUE_TOTAL + i);

            if (valid) {
                reqValueTotal.setText(formatFloat(amountReq, amountReq));
                reqValueTotal.setTextColor(Color.parseColor("#40C040"));
            }
            else
            {
                float offset = amountReq - infusion.getAmountReqValid(i);
                String val = formatFloat(offset, amountReq) + "/" + formatFloat(amountReq, amountReq);
                if (offset > 0)
                {
                    val = "+" + val;
                }
                reqValueTotal.setText(val);
                reqValueTotal.setTextColor(Color.parseColor("#C04040"));
            }
        }
    }

    protected void createRequestBar(int r, GridLayout grid, int row)
    {
        final float scale = this.getResources().getDisplayMetrics().density;

        GridLayout.LayoutParams l1 = new GridLayout.LayoutParams(GridLayout.spec(row, 1), GridLayout.spec(0, 1));
        GridLayout.LayoutParams l2 = new GridLayout.LayoutParams(GridLayout.spec(row, 1), GridLayout.spec(1, 1));
        GridLayout.LayoutParams l3 = new GridLayout.LayoutParams(GridLayout.spec(row, 1), GridLayout.spec(2, 1));
        GridLayout.LayoutParams l4 = new GridLayout.LayoutParams(GridLayout.spec(row, 1), GridLayout.spec(3, 1));
        l3.width = (int)(140 * scale + 0.5f);
        l2.setGravity(Gravity.RIGHT);
        l4.setGravity(Gravity.RIGHT);
        boolean valid = infusion.isValid(r);
        String name = infusion.getReqName(r);
        InfusionCalculator.AdjustableRequirement req = infusion.getReqData(r);

        float amountReq = infusion.getAmountReq(r);

        TextView reqName = new TextView(this);
        TextView reqValue = new TextView(this);
        TextView reqValueTotal = new TextView(this);

        reqValue.setId(ID_REQ_VALUE + r);
        reqValue.setText(formatFloat(req.val, req.max));
        reqValueTotal.setId(ID_REQ_VALUE_TOTAL + r);
        //reqValueTotal.setBackgroundColor(Color.parseColor("#C0C0C0"));

        if (valid) {
            reqValueTotal.setText(formatFloat(amountReq, amountReq));
            reqValueTotal.setTextColor(Color.parseColor("#40C040"));
        }
        else
        {
            float offset = amountReq - infusion.getAmountReqValid(r);
            String val = formatFloat(offset, amountReq) + "/" + formatFloat(amountReq, amountReq);;
            if (offset > 0)
            {
                val = "+" + val;
            }
            reqValueTotal.setText(val);
            reqValueTotal.setTextColor(Color.parseColor("#C04040"));
        }

        SeekBar seek = new SeekBar(this);

        if (req.min != req.max)
        {
            reqName.setText(name + " (" + formatFloat(req.min, req.max) + "-" + formatFloat(req.max, req.max) + ")");

            int val = Math.round(64.0F * (req.val - req.min) / (req.max - req.min));
            seek.setProgress(val);
            seek.setMax(64);
            seek.setId(ID_SEEK_BAR + r);
            seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    updateInfusions(seekBar);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)  {
                    if (fromUser) {
                        updateReqValue(seekBar);
                    }
                }
            });
        }
        else
        {

            reqName.setText(name);
            seek.setId(ID_SEEK_BAR + r);
            seek.setEnabled(false);
        }

        grid.addView(reqName, l1);
        grid.addView(reqValue, l2);
        grid.addView(seek, l3);
        grid.addView(reqValueTotal, l4);

    }

    protected void fillResult() {

        int row = 0;
        boolean[] reqsDone = new boolean[infusion.REQ_NUM];
        int reqs[] = new int[infusion.REQ_NUM];


        GridLayout grid = (GridLayout) findViewById(R.id.gridViewResult);
        TextView infName;
        TextView infValue;

        grid.removeAllViewsInLayout();
        grid.setRowCount(infusion.INF_NUM + infusion.REQ_NUM);

        for (int i = 0; i < infusion.INF_NUM; i++) {
            infName = new TextView(this);
            infValue = new TextView(this);

            GridLayout.LayoutParams l1 = new GridLayout.LayoutParams(GridLayout.spec(row, 1), GridLayout.spec(0, 3));
            GridLayout.LayoutParams l2 = new GridLayout.LayoutParams(GridLayout.spec(row, 1), GridLayout.spec(3, 1));

            l2.setGravity(Gravity.RIGHT);

            float amount = infusion.getAmountInfusion(i);

            InfusionCalculator.Infusion inf = infusion.getInfusionData(i, reqs);

            int reqCount = 0;
            while (reqs[reqCount] > 0) {
                reqCount++;
            }

            infName.setText(inf.name);
            infName.setTypeface(Typeface.DEFAULT_BOLD);
            infName.setTextColor(Color.parseColor("#000000"));

            infValue.setText(formatFloat(amount, amount));
            infValue.setId(ID_INF_VALUE + i);
            infValue.setTypeface(Typeface.DEFAULT_BOLD);
            infValue.setTextColor(Color.parseColor("#000000"));

            grid.addView(infName, l1);
            grid.addView(infValue, l2);
            row++;

            for (int j = 0; j < reqCount; j++) {
                int r = reqs[j];
                if (!reqsDone[r]) {
                    createRequestBar(r, grid, row);
                    reqsDone[r] = true;
                    row++;
                }
            }
        }

        // now display remaining reqs
        for (int r = 0; r < infusion.REQ_NUM; r++) {
            if (!reqsDone[r]) {
                createRequestBar(r, grid, row);
                reqsDone[r] = true;
                row++;
            }
        }

        grid.invalidate();
    }
}
