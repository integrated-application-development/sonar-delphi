unit UnusualWhitespace;

interface

implementation

procedure NUL; begin end;
procedureSOH; begin end;
procedureSTX; begin end;
procedureETX; begin end;
procedureEOT; begin end;
procedureENQ; begin end;
procedureACK; begin end;
procedureBEL; begin end;
procedureBS; begin end;
procedure	HT; begin end;
procedure
LF; begin end;
procedureVT; begin end;
procedureFF; begin end;
procedure
CR; begin end;
procedureSO; begin end;
procedureSI; begin end;
procedureDLE; begin end;
procedureDC1; begin end;
procedureDC2; begin end;
procedureDC3; begin end;
procedureDC4; begin end;
procedureNAK; begin end;
procedureSYN; begin end;
procedureETB; begin end;
procedureCAN; begin end;
procedureEM; begin end;
procedureSUB; begin end;
procedureESC; begin end;
procedureFS; begin end;
procedureGS; begin end;
procedureRS; begin end;
procedureUS; begin end;
procedure Space; begin end;

// Note: special space between the identifier and 'end' to ensure that it isn't included in the identifier.
procedure　IdeographicSpace; begin Foo　end;

end.