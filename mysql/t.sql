use valtrader;

LOAD DATA LOCAL INFILE 'FQUOTES1.txt'
INTO TABLE FQUOTES1 columns terminated by ',';
;

select count(*) from fquotes1;
