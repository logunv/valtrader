rem @echo of
java equotes.GetQuote 01/01/2000 %* > quotes.txt
rb_tmu -d stocks quotes.tmu system manager
