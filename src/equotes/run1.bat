rem @echo of
java GetQuote 01/01/2011 %* > quotes.txt
rb_tmu -d stocks quotes.tmu system manager
