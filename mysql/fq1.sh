getdata()
{
    # usage: symbol url date
    sym=$1
    url=$2
    date=$3
    tmpfile=futures/${sym}.csv

    echo $*

    java fquotes.download $url $tmpfile
    java fquotes.extdata $sym $tmpfile $date >> fquotes.txt
}

runall()
{
    if [ ! -d futures ]; then
        mkdir futures
    fi

    dt=20121201
    rm -f fquotes.txt

    getdata AUD http://www.quandl.com/api/v1/datasets/OFDP/FUTURE_AD1.csv? $dt
    getdata EUR http://www.quandl.com/api/v1/datasets/OFDP/FUTURE_EC1.csv? $dt
    getdata QM http://www.quandl.com/api/v1/datasets/OFDP/FUTURE_CL1.csv? $dt
    getdata CAD http://www.quandl.com/api/v1/datasets/OFDP/FUTURE_CD1.csv? $dt
    getdata CHF http://www.quandl.com/api/v1/datasets/OFDP/FUTURE_SF1.csv? $dt
    getdata GBP http://www.quandl.com/api/v1/datasets/OFDP/FUTURE_BP1.csv? $dt
    # getdata JPY http://www.quandl.com/api/v1/datasets/OFDP/FUTURE_JY1.csv? $dt
    getdata QG http://www.quandl.com/api/v1/datasets/OFDP/FUTURE_NG1.csv? $dt
    getdata ES http://www.quandl.com/api/v1/datasets/OFDP/FUTURE_ES1.csv? $dt
    # getdata YM http://www.quandl.com/api/v1/datasets/OFDP/FUTURE_YM1.csv? $dt
    getdata ZG http://www.quandl.com/api/v1/datasets/OFDP/FUTURE_GC1.csv? $dt
    getdata ZI http://www.quandl.com/api/v1/datasets/OFDP/FUTURE_SI2.csv? $dt
    getdata SB http://www.quandl.com/api/v1/datasets/OFDP/FUTURE_SB1.csv? $dt
    getdata ZL http://www.quandl.com/api/v1/datasets/OFDP/FUTURE_BO2.csv? $dt
    getdata ZC http://www.quandl.com/api/v1/datasets/OFDP/FUTURE_C1.csv? $dt
    getdata ZW http://www.quandl.com/api/v1/datasets/OFDP/FUTURE_W1.csv? $dt
    getdata ZS http://www.quandl.com/api/v1/datasets/OFDP/FUTURE_S2.csv? $dt
    getdata ZM http://www.quandl.com/api/v1/datasets/OFDP/FUTURE_SM2.csv? $dt
    getdata DX http://www.quandl.com/api/v1/datasets/OFDP/FUTURE_DX1.csv? $dt
    getdata ER2 http://www.quandl.com/api/v1/datasets/OFDP/FUTURE_TF1.csv? $dt

#FxData $dt > fxquotes.txt
    #rb_tmu -d stocks fquotes.tmu system manager
    my.sh < ms.sql
#date +%Y%m%d > controldate
}

runall 
