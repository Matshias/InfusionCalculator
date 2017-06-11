package com.matshias.infusioncalculator;

import java.util.Calendar;


/**
 * Created by mlenk on 30.12.16.
 */

public class InfusionCalculator {

    final public int REQ_LIQUID = 0;
    final public int REQ_CALORIES = 1;
    final public int REQ_GLUCOSE = 2;
    final public int REQ_FAT = 3;
    final public int REQ_NA = 4;
    final public int REQ_K = 5;
    final public int REQ_CA = 6;
    final public int REQ_AMINO = 7;
    final public int REQ_PHOSPHATE = 8;
    final public int REQ_VITAMIN = 9;
    final public int REQ_TRACE = 10;
    final public int REQ_NUM = 11;

    final public int INF_GLUCOSE_10 = 0;
    final public int INF_GLUCOSE_20 = 1;
    final public int INF_GLUCOSE_40 = 2;
    final public int INF_GLUCOSE_70 = 3;
    final public int INF_SMOFLIPID = 4;
    final public int INF_AMINOVEN = 5;
    final public int INF_NACL = 6;
    final public int INF_KCL = 7;
    final public int INF_CAGLOKONAT = 8;
    final public int INF_GLYCEROL = 9;
    final public int INF_VITALIPID = 10;
    final public int INF_INZOLEN = 11;
    final public int INF_NUM = 12;

    private float tolerance = 0.1F;

    private int[] amounts = new int[REQ_NUM];
    private float[] reqAmounts = new float[REQ_NUM];
    private boolean reqAllMet = false;
    private boolean[] reqMet = new boolean[REQ_NUM];
    private AdjustableRequirement[] reqs = new AdjustableRequirement[REQ_NUM];
    private Infusion[] infusions = new Infusion[INF_NUM];
    private String[] reqNames = {"Flüssigk.", "Energie", "Glukose", "Fett", "Natrium", "Kalium", "Calcium", "Aminos", "Phosphat", "Vitamine", "Spurenel."};

    private int[][] liquidTable =
                {
                    {70, 60, 60 },
                    {90, 80, 80},
                    {110, 100, 100},
                    {130, 120, 120},
                    {150, 140, 140},
                    {170, 160, 150},
                    {170, 170, 150}
                };

    private float[] liquidWeights = { 0.0F, 1.5F, 2.0F};
    final private int liquidNumWeights = 3;



    public class AdjustableRequirement
    {
        public float min = 0.0F;
        public float max = 0.0F;
        public float val = 0.0F;
    }

    public class Infusion
    {
        public String name = "";
        public float[] provides = new float[REQ_NUM];
        public float pack = 0;
        public int maxAmount = 0;
    }

    private float patientWeight;
    private Calendar patientDate;
    private int patientDayParenteral;
    private int patientDayFat;
    private int patientDayAmino;
    private boolean patientKenabled;


    public InfusionCalculator()
    {
        initInfusions();
    }

    public void config(float toler)
    {
        tolerance = toler;
    }

    public void setPatientData(float weightKg, Calendar birthDate, int dayParenteral, int dayFat, int dayAmino, boolean Kenabled)
    {
        patientWeight = weightKg;
        patientDate = birthDate;
        patientDayParenteral = dayParenteral;
        patientDayFat = dayFat;
        patientDayAmino = dayAmino;
        patientKenabled = Kenabled;

        updateRequirements();
        calculateInfusions();
    }

    public boolean isValid()
    {
        return reqAllMet;
    }

    public boolean isValid(int req)
    {
        return checkRequirement(req);
    }

    public float getAmountReq(int req)
    {
        return reqAmounts[req];
    }

    public float getAmountReqValid(int req)
    {
        return reqs[req].val * patientWeight;
    }

    public float getAmountInfusion(int inf)
    {

        return amounts[inf] * infusions[inf].pack;
    }

    public AdjustableRequirement getReqData(int req)
    {

        return reqs[req];
    }

    public String getReqName(int req)
    {
        return reqNames[req];
    }

    public Infusion getInfusionData(int i, int[] reqs)
    {
        int count = 0;
        for (int r = REQ_GLUCOSE; r < REQ_NUM; r++)
        {
            if (infusions[i].provides[r] > 0)
            {
                reqs[count] = r;
                count++;
            }
        }
        reqs[count] = 0;
        return infusions[i];
    }

    public void setReqValue(int req, float val)
    {
        if (val >= reqs[req].min && val <= reqs[req].max)
        {
            reqs[req].val = val;
            calculateInfusions();
        }
    }



    protected void initInfusions()
    {
        for (int i = 0; i < REQ_NUM; i++)
        {
            reqs[i] = new AdjustableRequirement();
        }
        for (int i = 0; i < INF_NUM; i++)
        {
            infusions[i] = new Infusion();
        }

        infusions[INF_GLUCOSE_10].name = "G 10%";
        infusions[INF_GLUCOSE_10].pack = 1;
        infusions[INF_GLUCOSE_10].maxAmount = 0;
        infusions[INF_GLUCOSE_10].provides[REQ_GLUCOSE] = 0.1F;
        infusions[INF_GLUCOSE_10].provides[REQ_CALORIES] = 0.4F;

        infusions[INF_GLUCOSE_20].name = "G 20%";
        infusions[INF_GLUCOSE_20].pack = 1;
        infusions[INF_GLUCOSE_20].maxAmount = 0;
        infusions[INF_GLUCOSE_20].provides[REQ_GLUCOSE] = 0.2F;
        infusions[INF_GLUCOSE_20].provides[REQ_CALORIES] = 0.8F;

        infusions[INF_GLUCOSE_40].name = "G 40%";
        infusions[INF_GLUCOSE_40].pack = 1;
        infusions[INF_GLUCOSE_40].maxAmount = 0;
        infusions[INF_GLUCOSE_40].provides[REQ_GLUCOSE] = 0.4F;
        infusions[INF_GLUCOSE_40].provides[REQ_CALORIES] = 1.6F;

        infusions[INF_GLUCOSE_70].name = "G 70%";
        infusions[INF_GLUCOSE_70].pack = 1;
        infusions[INF_GLUCOSE_70].maxAmount = 0;
        infusions[INF_GLUCOSE_70].provides[REQ_GLUCOSE] = 0.7F;
        infusions[INF_GLUCOSE_70].provides[REQ_CALORIES] = 2.8F;

        infusions[INF_SMOFLIPID].name = "Smoflipid 20%";
        infusions[INF_SMOFLIPID].pack = 1;
        infusions[INF_SMOFLIPID].maxAmount = 0;
        infusions[INF_SMOFLIPID].provides[REQ_FAT] = 0.2F;
        infusions[INF_SMOFLIPID].provides[REQ_CALORIES] = 1.8F;

        infusions[INF_AMINOVEN].name = "Aminoven 10%";
        infusions[INF_AMINOVEN].pack = 1;
        infusions[INF_AMINOVEN].maxAmount = 0;
        infusions[INF_AMINOVEN].provides[REQ_AMINO] = 0.1F;
        infusions[INF_AMINOVEN].provides[REQ_CALORIES] = 0.4F;

        infusions[INF_NACL].name = "NaCl 5,85%";
        infusions[INF_NACL].pack = 1;
        infusions[INF_NACL].maxAmount = 0;
        infusions[INF_NACL].provides[REQ_NA] = 1;


        infusions[INF_KCL].name = "KCl 7,45%";
        infusions[INF_KCL].pack = 1;
        infusions[INF_KCL].maxAmount = 0;
        infusions[INF_KCL].provides[REQ_K] = 1;

        infusions[INF_CAGLOKONAT].name = "Calcium Glukonat 10 %";
        infusions[INF_CAGLOKONAT].pack = 1;
        infusions[INF_CAGLOKONAT].maxAmount = 0;
        infusions[INF_CAGLOKONAT].provides[REQ_CA] = 0.25F;

        infusions[INF_GLYCEROL].name = "Natrium Glycerolphosphat";
        infusions[INF_GLYCEROL].pack = 1;
        infusions[INF_GLYCEROL].maxAmount = 0;
        infusions[INF_GLYCEROL].provides[REQ_NA] = 2;
        infusions[INF_GLYCEROL].provides[REQ_PHOSPHATE] = 1;

        infusions[INF_VITALIPID].name = "Vitalipid + Frekavit";
        infusions[INF_VITALIPID].pack = 1;
        infusions[INF_VITALIPID].maxAmount = 10;
        infusions[INF_VITALIPID].provides[REQ_VITAMIN] = 1;

        infusions[INF_INZOLEN].name = "Inzolen infantibus S";
        infusions[INF_INZOLEN].pack = 0.5F;
        infusions[INF_INZOLEN].maxAmount = 40;
        infusions[INF_INZOLEN].provides[REQ_TRACE] = 0.5F;

        for (int i = 0; i < INF_NUM; i++)
        {
            infusions[i].provides[REQ_LIQUID] = infusions[i].pack;
        }

    }

    protected void updateRequirements()
    {
        Calendar cur = Calendar.getInstance();
        Calendar birth = Calendar.getInstance();

        birth.set(cur.get(Calendar.YEAR), patientDate.get(Calendar.MONTH), patientDate.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        int ageDay = (int)((cur.getTimeInMillis() - patientDate.getTimeInMillis()) / 1000 / 86400);
        int ageMonth;
        int ageYear;

        ageMonth = cur.get(Calendar.MONTH) - patientDate.get(Calendar.MONTH);
        if (patientDate.get(Calendar.DAY_OF_MONTH) > cur.get(Calendar.DAY_OF_MONTH))
        {
            ageMonth--;
        }
        ageYear = cur.get(Calendar.YEAR) - patientDate.get(Calendar.YEAR);
        ageMonth += ageYear * 12;
        if (patientDate.get(Calendar.MONTH) > cur.get(Calendar.MONTH))
        {
            ageYear--;
        }
        else if (patientDate.get(Calendar.MONTH) == cur.get(Calendar.MONTH) && patientDate.get(Calendar.DAY_OF_MONTH) > cur.get(Calendar.DAY_OF_MONTH))
        {
            ageYear--;
        }

        // liquid
        if (ageDay < 7)
        {
            // find weight class
            int weightClass = 0;
            for (int i = liquidNumWeights - 1; i >=0 ; i--) {
                if (patientWeight >= liquidWeights[i]) {
                    weightClass = i;
                    break;
                }
            }

            reqs[REQ_LIQUID].min = liquidTable[ageDay][weightClass];
            reqs[REQ_LIQUID].max = reqs[REQ_LIQUID].min;
            reqs[REQ_LIQUID].val = reqs[REQ_LIQUID].min;
        }
        else {
            switch (ageYear) {
                // first year
                case 0:
                    reqs[REQ_LIQUID].min = 100;
                    reqs[REQ_LIQUID].max = 140;
                    reqs[REQ_LIQUID].val = (reqs[REQ_LIQUID].max * (ageDay) + reqs[REQ_LIQUID].min * (365 - ageDay)) / 365;
                    break;
                // second to 5th year
                case 1:
                    reqs[REQ_LIQUID].val = 90;
                    reqs[REQ_LIQUID].min = 75;
                    reqs[REQ_LIQUID].max = 90;
                    break;
                case 2:
                    reqs[REQ_LIQUID].val = 85;
                    reqs[REQ_LIQUID].min = 75;
                    reqs[REQ_LIQUID].max = 90;
                    break;
                case 3:
                    reqs[REQ_LIQUID].val = 80;
                    reqs[REQ_LIQUID].min = 75;
                    reqs[REQ_LIQUID].max = 90;
                    break;
                case 4:
                    reqs[REQ_LIQUID].val = 75;
                    reqs[REQ_LIQUID].min = 75;
                    reqs[REQ_LIQUID].max = 90;
                    break;
                // 6th to 10th year
                case 5:
                    reqs[REQ_LIQUID].val = 80;
                    reqs[REQ_LIQUID].min = 60;
                    reqs[REQ_LIQUID].max = 80;
                    break;
                case 6:
                    reqs[REQ_LIQUID].val = 75;
                    reqs[REQ_LIQUID].min = 60;
                    reqs[REQ_LIQUID].max = 80;
                    break;
                case 7:
                    reqs[REQ_LIQUID].val = 70;
                    reqs[REQ_LIQUID].min = 60;
                    reqs[REQ_LIQUID].max = 80;
                    break;
                case 8:
                    reqs[REQ_LIQUID].val = 65;
                    reqs[REQ_LIQUID].min = 60;
                    reqs[REQ_LIQUID].max = 80;
                    break;
                case 9:
                    reqs[REQ_LIQUID].val = 60;
                    reqs[REQ_LIQUID].min = 60;
                    reqs[REQ_LIQUID].max = 80;
                    break;
                // above 10th year
                default:
                    reqs[REQ_LIQUID].min = 50;
                    reqs[REQ_LIQUID].max = 70;
                    reqs[REQ_LIQUID].val = 60;
                    break;
            }

        }

        // Calories
        if (ageDay < 30)
        {
            reqs[REQ_CALORIES].min = 90;
            reqs[REQ_CALORIES].max = 100;
            reqs[REQ_CALORIES].val = 95;
        }
        else if (ageYear < 1)
        {
            reqs[REQ_CALORIES].min = 80;
            reqs[REQ_CALORIES].max = 90;
            reqs[REQ_CALORIES].val = 85;
        }
        else if (ageYear < 7)
        {
            reqs[REQ_CALORIES].min = 75;
            reqs[REQ_CALORIES].max = 90;
            reqs[REQ_CALORIES].val = 80;
        }
        else if (ageYear < 12)
        {
            reqs[REQ_CALORIES].min = 60;
            reqs[REQ_CALORIES].max = 75;
            reqs[REQ_CALORIES].val = 65;
        }
        else
        {
            reqs[REQ_CALORIES].min = 30;
            reqs[REQ_CALORIES].max = 60;
            reqs[REQ_CALORIES].val = 45;
        }


        // Glucose
        if (ageYear < 1)
        {
            reqs[REQ_GLUCOSE].min = 12;
            reqs[REQ_GLUCOSE].max = 18;
            reqs[REQ_GLUCOSE].val = 15;
        }
        else if (ageYear < 5)
        {
            reqs[REQ_GLUCOSE].min = 12;
            reqs[REQ_GLUCOSE].max = 16;
            reqs[REQ_GLUCOSE].val = 14;
        }
        else if (ageYear < 10)
        {
            reqs[REQ_GLUCOSE].min = 10;
            reqs[REQ_GLUCOSE].max = 10;
            reqs[REQ_GLUCOSE].val = 10;
        }
        else {
            reqs[REQ_GLUCOSE].min = 8;
            reqs[REQ_GLUCOSE].max = 8;
            reqs[REQ_GLUCOSE].val = 8;
        }

        int reqsGlucoseMin;
        int reqsGlucoseVal;
        int reqsGlucoseMax;

        if (ageDay < 30)
        {
            reqsGlucoseMin = 6;
            reqsGlucoseMax = 8;
            reqsGlucoseVal = 8;

        }
        else if (patientWeight < 20)
        {
            reqsGlucoseMin = 6;
            reqsGlucoseMax = 8;
            reqsGlucoseVal = 7;
        }
        else
        {
            reqsGlucoseMin = 4;
            reqsGlucoseMax = 6;
            reqsGlucoseVal = 5;
        }
        reqsGlucoseMin += patientDayParenteral;
        reqsGlucoseMax += 2 * patientDayParenteral;
        reqsGlucoseVal += 1.5F * patientDayParenteral;

        if (reqsGlucoseMin < reqs[REQ_GLUCOSE].min)
        {
            reqs[REQ_GLUCOSE].min = reqsGlucoseMin;
        }
        if (reqsGlucoseMax < reqs[REQ_GLUCOSE].max)
        {
            reqs[REQ_GLUCOSE].max = reqsGlucoseMax;
        }
        if (reqsGlucoseVal < reqs[REQ_GLUCOSE].val)
        {
            reqs[REQ_GLUCOSE].val = reqsGlucoseVal;
        }



        // Fat
        if (ageDay <= 30) {
            reqs[REQ_FAT].min = 4;
            reqs[REQ_FAT].val = reqs[REQ_FAT].max = reqs[REQ_FAT].min;
        }
        else if (ageYear < 7) {
            reqs[REQ_FAT].min = 3;
            reqs[REQ_FAT].val = reqs[REQ_FAT].max = reqs[REQ_FAT].min;
        }
        else  {
            reqs[REQ_FAT].min = 2;
            reqs[REQ_FAT].max = 3;
            reqs[REQ_FAT].val = 2.5F;
        }

        if (patientDayFat < 4)
        {
            float req = 1 + patientDayFat;
            if (req < reqs[REQ_FAT].val)
            {
                reqs[REQ_FAT].val = req;
            }
            if (req < reqs[REQ_FAT].min)
            {
                reqs[REQ_FAT].min = reqs[REQ_FAT].max = req;
            }
        }

        // amino acids
        if (patientDayAmino >= 0) {
            if (ageDay <= 30) {
                reqs[REQ_AMINO].min = 2.5F;
                reqs[REQ_AMINO].max = 3.0F;
                reqs[REQ_AMINO].val = 2.75F;
            } else if (ageMonth < 2) {
                reqs[REQ_AMINO].min = 2;
                reqs[REQ_AMINO].val = reqs[REQ_AMINO].max = reqs[REQ_AMINO].min;
            } else if (ageYear < 1) {
                reqs[REQ_AMINO].min = 1.5F;
                reqs[REQ_AMINO].val = reqs[REQ_AMINO].max = reqs[REQ_AMINO].min;
            } else {
                reqs[REQ_AMINO].min = 0.5F;
                reqs[REQ_AMINO].max = 1.0F;
                reqs[REQ_AMINO].val = 0.75F;
            }

            if (patientDayAmino < 5) {
                float req = 1 + patientDayAmino * 0.5F;
                if (req < reqs[REQ_AMINO].val) {
                    reqs[REQ_AMINO].val = req;
                }
                if (req < reqs[REQ_AMINO].min) {
                    reqs[REQ_AMINO].min = reqs[REQ_AMINO].max = req;
                }
            }
        }
        else
        {
            reqs[REQ_AMINO].val = reqs[REQ_AMINO].max = reqs[REQ_AMINO].min = 0;
        }

        // electrolytes
        reqs[REQ_NA].min = 2;
        reqs[REQ_NA].max = 5;
        reqs[REQ_NA].val = 3;

        if (ageDay >= 1 && patientKenabled)
        {
            reqs[REQ_K].min = 1;
            reqs[REQ_K].max = 3;
            reqs[REQ_K].val = 2;
        }
        else
        {
            reqs[REQ_K].val = reqs[REQ_K].max = reqs[REQ_K].min = 0;
        }


        if (ageDay < 4)
        {
            reqs[REQ_CA].min = 0.0F;
            reqs[REQ_CA].val = reqs[REQ_CA].max = reqs[REQ_CA].min;
        }
        else if (ageMonth < 3)
        {
            reqs[REQ_CA].min = 1;
            reqs[REQ_CA].max = 2;
            reqs[REQ_CA].val = 1.5F;
        }
        else
        {
            reqs[REQ_CA].min = 0.5F;
            reqs[REQ_CA].val = reqs[REQ_CA].max = reqs[REQ_CA].min;
        }

        if (ageDay <= 30)
        {
            reqs[REQ_PHOSPHATE].min = 1;
            reqs[REQ_PHOSPHATE].max = 2;
            reqs[REQ_PHOSPHATE].val = 1.5F;
        }
        else if (ageYear < 1)
        {
            reqs[REQ_PHOSPHATE].min = 1;
            reqs[REQ_PHOSPHATE].val = reqs[REQ_PHOSPHATE].max = reqs[REQ_PHOSPHATE].min;
        }
        else
        {
            reqs[REQ_PHOSPHATE].min = 0.8F;
            reqs[REQ_PHOSPHATE].val = reqs[REQ_PHOSPHATE].max = reqs[REQ_PHOSPHATE].min;
        }

        // Vitamins
        reqs[REQ_VITAMIN].min = 1;
        reqs[REQ_VITAMIN].val = reqs[REQ_VITAMIN].max = reqs[REQ_VITAMIN].min;

        // trace elements
        if (patientDayParenteral >= 4) {
            if (ageDay <= 30) {
                reqs[REQ_TRACE].min = 1.5F;
                reqs[REQ_TRACE].val = reqs[REQ_TRACE].max = reqs[REQ_TRACE].min;
            } else {
                if (patientWeight <= 10.0F) {
                    reqs[REQ_TRACE].min = 1.0F;
                    reqs[REQ_TRACE].val = reqs[REQ_TRACE].max = reqs[REQ_TRACE].min;
                } else {
                    reqs[REQ_TRACE].min = 0.5F;
                    reqs[REQ_TRACE].val = reqs[REQ_TRACE].max = reqs[REQ_TRACE].min;
                }
            }
        }
        else
        {
            reqs[REQ_TRACE].val = reqs[REQ_TRACE].max = reqs[REQ_TRACE].min = 0;
        }
    }

    protected boolean checkRequirement(int req)
    {
        boolean res = true;
        float val = reqs[req].val;
        float reqTotal = val * patientWeight;
        float reqTotalMin = reqs[req].min * patientWeight;
        float reqTotalMax = reqs[req].max * patientWeight;

        float amount = reqAmounts[req];

        if (reqTotalMin == reqTotalMax)
        {
            if (Math.abs(reqTotalMin - amount) / reqTotalMin > tolerance) {
                res = false;
            }
        }
        else if (amount < reqTotalMin || amount > reqTotalMax)
        {
            res = false;
        }
        else
        {
            if (Math.abs(reqTotal - amount) / reqTotal > tolerance) {
                res = false;
            }
        }

        return res;
    }

    protected boolean addInfusion(int inf, int req)
    {
        int newAmount;
        int addAmount;

        boolean reqComplete = false;

        float reqTotal = reqs[req].val * patientWeight;
        float reqTotalMin = reqs[req].min * patientWeight;
        float reqTotalMax = reqs[req].max * patientWeight;

        // check that the requirement wasn't already met by another infusion
        reqComplete = checkRequirement(req);

        if (!reqComplete)
        {
            if (infusions[inf].provides[req] > 0)
            {
                reqComplete = true;
                float amountReq;
                addAmount = (int) Math.round((reqTotal - reqAmounts[req]) / infusions[inf].provides[req]);

                if (addAmount < 0)
                {
                    addAmount = 0;
                }

                newAmount = addAmount + amounts[inf];

                // check that we don't exceed maximum if specified
                if (infusions[inf].maxAmount > 0)
                {
                    if ((float) newAmount > infusions[inf].maxAmount)
                    {
                        newAmount = Math.round((float) infusions[inf].maxAmount);
                        addAmount = newAmount - amounts[inf];
                    }
                }

                amountReq = newAmount * infusions[inf].provides[req];

                // check if we fulfilled the requirement with this infusion
                if (reqTotalMin == reqTotalMax) {
                    if (Math.abs(reqTotalMin - amountReq) / reqTotalMin > tolerance) {
                        //addAmount = 0;
                        //newAmount = amounts[inf];
                        reqComplete = false;
                    }
                } else {
                    if (amountReq < reqTotalMin) {
                        reqComplete = false; // hopefully another infusion can compensate
                    }
                    if (amountReq > reqTotalMax) {
                        reqComplete = false; // hopefully another infusion can compensate
                        addAmount = 0;
                        newAmount = amounts[inf];
                    }
                }

                amounts[inf] = newAmount;

                // now add all the requirements in the infusion
                for (int i = 0; i < REQ_NUM; i++) {
                    reqAmounts[i] += addAmount * infusions[inf].provides[i];
                }
            }
        }

        return reqComplete;
    }

    protected void calculateInfusions()
    {
        // reset data
        amounts = new int[INF_NUM];
        reqAmounts = new float[REQ_NUM];
        reqAllMet = false;
        boolean[] doneReq = new boolean[REQ_NUM];
        int[] infPerReq = new int[REQ_NUM];

        // first start with infusions that only provide liquid and
        // one additional requirement

        int lastInf = -1;
        for (int i = REQ_FAT; i < REQ_NUM; i++ )
        {
            for (int j = INF_SMOFLIPID; j < INF_NUM; j++)
            {
                if (infusions[j].provides[i] > 0)
                {
                    infPerReq[i]++;
                    lastInf = j;
                }
            }
            if (infPerReq[i] == 1)
            {
                doneReq[i] = addInfusion(lastInf, i );
            }
        }

        // now start with those that provide two requirements
        for (int i = REQ_FAT; i < REQ_NUM; i++ )
        {
            if (!doneReq[i] && infPerReq[i] == 2)
            {
                for (int j = INF_SMOFLIPID; j < INF_NUM; j++)
                {
                    if (infusions[j].provides[i] > 0 && !doneReq[i])
                    {
                        doneReq[i] = addInfusion(j, i);
                    }
                }
            }
        }

        // now handle glucose, liquid and calories with

        // first handle extreme cases

        int[][] results = new int[6][4];

        final float reqLiquid = reqs[REQ_LIQUID].val * patientWeight - reqAmounts[REQ_LIQUID];
        final float reqGlucose = reqs[REQ_GLUCOSE].val * patientWeight;
        final float reqCalories = reqs[REQ_CALORIES].val * patientWeight - reqAmounts[REQ_CALORIES];

        final float minAmount10 = Math.round(reqGlucose / infusions[INF_GLUCOSE_10].provides[REQ_GLUCOSE]);
        final float maxAmount70 = Math.round(reqGlucose / infusions[INF_GLUCOSE_70].provides[REQ_GLUCOSE]);

        // if even the lightest glucose infusion cannot provide enough liquid we have to choose as much from
        // the 10 % solution and accept not enough liquid
        if (infusions[INF_GLUCOSE_10].provides[REQ_LIQUID] * minAmount10 >= reqLiquid) {
            // likewise if the thickest solution provides to much liquid we have to choose as much
            // 70 % solution and accept that we have too much liquid
            if (infusions[INF_GLUCOSE_70].provides[REQ_LIQUID] * maxAmount70 <= reqLiquid) {

                final int[][] choice = {{0, 1}, {0, 2}, {0, 3}, {1, 2}, {1, 3}, {2, 3}};
                final int numChoices = 6;
                float[][][] linearSystem = new float[numChoices][2][3];

                for (int i = 0; i < numChoices; i++) {

                    // liquid
                    linearSystem[i][0][0] = infusions[INF_GLUCOSE_10 + choice[i][0]].provides[REQ_LIQUID];
                    linearSystem[i][0][1] = infusions[INF_GLUCOSE_10 + choice[i][1]].provides[REQ_LIQUID];
                    linearSystem[i][0][2] = reqLiquid;

                    // glucose
                    linearSystem[i][1][0] = infusions[INF_GLUCOSE_10 + choice[i][0]].provides[REQ_GLUCOSE];
                    linearSystem[i][1][1] = infusions[INF_GLUCOSE_10 + choice[i][1]].provides[REQ_GLUCOSE];
                    linearSystem[i][1][2] = reqGlucose;

                    solveLinearSystem2x2(linearSystem[i], results[i]);

                    if (choice[i][1] != 1) {
                        results[i][choice[i][1]] = results[i][1];
                        results[i][1] = 0;
                    }
                    if (choice[i][0] != 0) {
                        results[i][choice[i][0]] = results[i][0];
                        results[i][0] = 0;
                    }
                }

                /*
                final int[][] choice = {{0,1,2},{0,1,3},{0,2,3},{1,2,3}};
                final int numChoices = 4;
                float[][][] linearSystem = new float[numChoices][3][4];
                for (int i = 0; i < numChoices; i++) {

                    // system 1
                    // liquid
                    linearSystem[i][0][0] = infusions[INF_GLUCOSE_10 + choice[i][0]].provides[REQ_LIQUID];
                    linearSystem[i][0][1] = infusions[INF_GLUCOSE_10 + choice[i][1]].provides[REQ_LIQUID];
                    linearSystem[i][0][2] = infusions[INF_GLUCOSE_10 + choice[i][2]].provides[REQ_LIQUID];
                    linearSystem[i][0][3] = reqLiquid;

                    // glucose
                    linearSystem[i][1][0] = infusions[INF_GLUCOSE_10 + choice[i][0]].provides[REQ_GLUCOSE];
                    linearSystem[i][1][1] = infusions[INF_GLUCOSE_10 + choice[i][1]].provides[REQ_GLUCOSE];
                    linearSystem[i][1][2] = infusions[INF_GLUCOSE_10 + choice[i][2]].provides[REQ_GLUCOSE];
                    linearSystem[i][1][3] = reqGlucose;

                    // calories
                    linearSystem[i][2][0] = infusions[INF_GLUCOSE_10 + choice[i][0]].provides[REQ_CALORIES];
                    linearSystem[i][2][1] = infusions[INF_GLUCOSE_10 + choice[i][1]].provides[REQ_CALORIES];
                    linearSystem[i][2][2] = infusions[INF_GLUCOSE_10 + choice[i][2]].provides[REQ_CALORIES];
                    linearSystem[i][2][3] = reqCalories;

                    solveLinearSystem3x3(linearSystem[i], results[i]);

                    if (choice[i][2] != 2) {
                        results[i][choice[i][1]] = results[i][2] ;
                        results[i][2] = 0;
                    }
                    if (choice[i][1] != 1) {
                        results[i][choice[i][1]] = results[i][1] ;
                        results[i][1] = 0;
                    }
                    if (choice[i][0] != 0) {
                        results[i][choice[i][0]] = results[i][0] ;
                        results[i][0] = 0;
                    }
                }
                */

                // choose the best solution
                float minError = 1000.0F;
                int minIdx = -1;
                for (int i = 0; i < numChoices; i++) {
                    float error = calcError(results[i]);
                    if (error < minError) {
                        minError = error;
                        minIdx = i;
                    }

                }

                reqAmounts[REQ_GLUCOSE] += results[minIdx][0] * infusions[INF_GLUCOSE_10].provides[REQ_GLUCOSE];
                reqAmounts[REQ_GLUCOSE] += results[minIdx][1] * infusions[INF_GLUCOSE_20].provides[REQ_GLUCOSE];
                reqAmounts[REQ_GLUCOSE] += results[minIdx][2] * infusions[INF_GLUCOSE_40].provides[REQ_GLUCOSE];
                reqAmounts[REQ_GLUCOSE] += results[minIdx][3] * infusions[INF_GLUCOSE_70].provides[REQ_GLUCOSE];

                reqAmounts[REQ_LIQUID] += results[minIdx][0] * infusions[INF_GLUCOSE_10].provides[REQ_LIQUID];
                reqAmounts[REQ_LIQUID] += results[minIdx][1] * infusions[INF_GLUCOSE_20].provides[REQ_LIQUID];
                reqAmounts[REQ_LIQUID] += results[minIdx][2] * infusions[INF_GLUCOSE_40].provides[REQ_LIQUID];
                reqAmounts[REQ_LIQUID] += results[minIdx][3] * infusions[INF_GLUCOSE_70].provides[REQ_LIQUID];

                reqAmounts[REQ_CALORIES] += results[minIdx][0] * infusions[INF_GLUCOSE_10].provides[REQ_CALORIES];
                reqAmounts[REQ_CALORIES] += results[minIdx][1] * infusions[INF_GLUCOSE_20].provides[REQ_CALORIES];
                reqAmounts[REQ_CALORIES] += results[minIdx][2] * infusions[INF_GLUCOSE_40].provides[REQ_CALORIES];
                reqAmounts[REQ_CALORIES] += results[minIdx][3] * infusions[INF_GLUCOSE_70].provides[REQ_CALORIES];

                amounts[INF_GLUCOSE_10] = results[minIdx][0];
                amounts[INF_GLUCOSE_20] = results[minIdx][1];
                amounts[INF_GLUCOSE_40] = results[minIdx][2];
                amounts[INF_GLUCOSE_70] = results[minIdx][3];
            }
            else
            {
                addInfusion(INF_GLUCOSE_70, REQ_GLUCOSE);
            }
        }
        else
        {
            addInfusion(INF_GLUCOSE_10, REQ_GLUCOSE);
        }

        boolean result = true;
        for (int i = 0; i < REQ_NUM; i++)
        {
            reqMet[i] = checkRequirement(i);
            if (!reqMet[i])
            {
                result = false;
            }
        }
        reqAllMet = result;
    }

    private float calcError(int[] result)
    {
        final float reqLiquid = reqs[REQ_LIQUID].val * patientWeight - reqAmounts[REQ_LIQUID];
        final float reqGlucose = reqs[REQ_GLUCOSE].val * patientWeight;
        final float reqCalories = reqs[REQ_CALORIES].val * patientWeight - reqAmounts[REQ_CALORIES];

        float liquidSum = 0;

        for (int i = 0; i < 4; i++)
        {
            liquidSum += result[i] * infusions[INF_GLUCOSE_10 + i].provides[REQ_LIQUID];
        }

        float glucoseSum = 0;

        for (int i = 0; i < 4; i++)
        {
            glucoseSum += result[i] * infusions[INF_GLUCOSE_10 + i].provides[REQ_GLUCOSE];
        }

        float calorieSum = 0;

        for (int i = 0; i < 4; i++)
        {
            calorieSum += result[i] * infusions[INF_GLUCOSE_10 + i].provides[REQ_CALORIES];
        }

        float liquidError = Math.abs(liquidSum - reqLiquid) / 100.0F;
        float glucoseError = Math.abs(glucoseSum - reqGlucose);
        float calorieError = Math.abs(calorieSum - reqCalories) / 4.0F;

        return (liquidError + glucoseError + calorieError);
    }

    private void solveLinearSystem2x2(float [][] coeffs, int[] result)
    {
        float pivot = coeffs[1][0] / coeffs[0][0];
        float[] res = new float[2];

        coeffs[1][0] = 0;
        coeffs[1][1] -= pivot * coeffs[0][1];
        coeffs[1][2] -= pivot * coeffs[0][2];

        if (Math.abs(coeffs[1][1]) > 0.0001F) {
            res[1] = coeffs[1][2] / coeffs[1][1];
            res[0] = (coeffs[0][2] - coeffs[0][1] * res[1]) / coeffs[0][0];

            result[0] = Math.round(res[0]);
            result[1] = Math.round(res[1]);
            if (result[0] < 0 || result[1] < 0)
            {
                result[0] = 0;
                result[1] = 0;
            }
        }
        else
        {
            result[0] = 0;
            result[1] = 0;
        }
    }

    private void solveLinearSystem3x3(float [][] coeffs, int[] result)
    {
        float[] res = new float[3];
        // gaussian method
        // step 1:
        float pivot;
        for (int r = 1; r < 3; r++) {
            pivot = coeffs[r][0] / coeffs[0][0];

            for (int c = 0; c < 4; c++) {
                coeffs[r][c] -= pivot * coeffs[0][c];
            }
        }

        pivot = coeffs[2][1] / coeffs[1][1];

        for (int c = 1; c < 4; c++) {
            coeffs[2][c] -= pivot * coeffs[1][c];
        }

        // step 2: calculate result
        if (Math.abs(coeffs[2][2]) > 0.0001F) {
            res[2] = coeffs[2][3] / coeffs[2][2];
            res[1] = (coeffs[1][3] - res[2] * coeffs[1][2]) / coeffs[1][1];
            res[0] = (coeffs[0][3] - res[2] * coeffs[0][2] - res[1] * coeffs[0][1]) / coeffs[0][0];

            result[0] = Math.round(res[0]);
            result[1] = Math.round(res[1]);
            result[2] = Math.round(res[2]);
        }
        else
        {
            result[0] = 0;
            result[1] = 0;
            result[2] = 0;
        }
    }
}


