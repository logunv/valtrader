FutData()
{
    java fquotes.extdata AUD futures/AD_0_I0B.TXT $1
    java fquotes.extdata ZL futures/BO20_I0B.TXT $1
    java fquotes.extdata GBP futures/BP_0_I0B.TXT $1
    #java fquotes.extdata CC futures/CC_0_I0B.TXT $1
    java fquotes.extdata CAD futures/CD_0_I0B.TXT $1
    java fquotes.extdata QM futures/CL20_I0B.TXT $1
    #java fquotes.extdata CT futures/CT_0_I0B.TXT $1
    java fquotes.extdata EUR futures/CU_0_I0B.TXT $1
    java fquotes.extdata ZC futures/C2_0_I0B.TXT $1
    java fquotes.extdata DX futures/DX_0_I0B.TXT $1
    java fquotes.extdata ED futures/ED_0_I0B.TXT $1
    #java fquotes.extdata EM futures/EM20_I0B.TXT $1
    java fquotes.extdata ES futures/ES_0_I0B.TXT $1
    #java fquotes.extdata FC futures/FC_0_I0B.TXT $1
    java fquotes.extdata ZG futures/GC20_I0B.TXT $1
    java fquotes.extdata HG futures/HG20_I0B.TXT $1
    java fquotes.extdata QH futures/HO20_I0B.TXT $1
    #java fquotes.extdata HU futures/HU_0_I0B.TXT $1
    java fquotes.extdata JPY futures/JY_0_I0B.TXT $1
    #java fquotes.extdata KC futures/KC_0_I0B.TXT $1
    #java fquotes.extdata LB futures/LB_0_I0B.TXT $1
    #java fquotes.extdata LC futures/LC_0_I0B.TXT $1
    #java fquotes.extdata LH futures/LH20_I0B.TXT $1
    #java fquotes.extdata MP futures/MP20_I0B.TXT $1
    java fquotes.extdata NQ futures/ND_0_I0B.TXT $1
    java fquotes.extdata QG futures/NG20_I0B.TXT $1
    #java fquotes.extdata OJ futures/OJ_0_I0B.TXT $1
    #java fquotes.extdata O futures/O__0_I0B.TXT $1
    #java fquotes.extdata PA futures/PA_0_I0B.TXT $1
    #java fquotes.extdata PB futures/PB_0_I0B.TXT $1
    #java fquotes.extdata PL futures/PL_0_I0B.TXT $1
    # java fquotes.extdata QIC futures/QIC0_I0B.TXT $1
    java fquotes.extdata RB futures/RB20_I0B.TXT $1
    java fquotes.extdata SB futures/SB20_I0B.TXT $1
    java fquotes.extdata CHF futures/SF_0_I0B.TXT $1
    java fquotes.extdata ZI futures/SI20_I0B.TXT $1
    java fquotes.extdata ZM futures/SM20_I0B.TXT $1
    # java fquotes.extdata SP futures/SP20_I0B.TXT $1
    #java fquotes.extdata SP futures/SP_0_I0B.TXT $1
    java fquotes.extdata ZS futures/S2_0_I0B.TXT $1
    java fquotes.extdata ZN futures/TY_0_I0B.TXT $1
    #java fquotes.extdata US futures/US20_I0B.TXT $1
    java fquotes.extdata ZW futures/W2_0_I0B.TXT $1
}

FxData()
{
    java fquotes.extdata AUD.JPY forex/AD40000$.TXT $1
    java fquotes.extdata AUD.USD forex/AD60000$.TXT $1
    java fquotes.extdata AUD.CAD forex/AD70000$.TXT $1
    java fquotes.extdata GBP.JPY forex/DR90000$.TXT $1
    java fquotes.extdata GBP.CHF forex/DR^0000$.TXT $1
    java fquotes.extdata EUR.AUD forex/EU10000$.TXT $1
    java fquotes.extdata EUR.CAD forex/EU20000$.TXT $1
    java fquotes.extdata EUR.JPY forex/EU50000$.TXT $1
    java fquotes.extdata EUR.CHF forex/EU60000$.TXT $1
    java fquotes.extdata EUR.GBP forex/EU70000$.TXT $1
    java fquotes.extdata EUR.USD forex/EU90000$.TXT $1
    # java fquotes.extdata USD.JPY forex/JPYX00000$.TXT $1
    java fquotes.extdata USD.CAD forex/QE20000$.TXT $1
    java fquotes.extdata USD.JPY forex/QE90000$.TXT $1
    java fquotes.extdata USD.CHF forex/QE}0000$.TXT $1
    java fquotes.extdata GBP.USD forex/QF40000$.TXT $1
}

runall()
{
    if [ ! -d futures ]; then
        mkdir futures
    fi
    if [ ! -d forex ]; then
        mkdir forex
    fi

# get quotes from TradingBlox.biz as Dataonly.zip
    java fquotes.getdata

# unzip now
    java fquotes.Unzip Dataonly.zip

# now extract only the required data. change this date once a month or so
# dt=`cat controldate`
    dt=20121001
    FutData $dt > fquotes.txt
#FxData $dt > fxquotes.txt
    rb_tmu -d stocks fquotes.tmu system manager
#date +%Y%m%d > controldate
}

runall 
