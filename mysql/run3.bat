rem @echo of
rem usage run3 mm/dd/yyyy list-of-symbols
java equotes.GetQuote %* > quotes.txt
rb_tmu -d stocks quotes.tmu system manager
