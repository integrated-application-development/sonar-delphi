unit RecordHelperLiteral;

{This is a sample Delphi file.}

interface

type
  TIntHelper = record helper for Integer
    procedure IntProc;
  end;

  TStringHelper = record helper for String
    procedure StringProc;
  end;

  TCharHelper = record helper for Char
    procedure CharProc;
  end;

  TExtendedHelper = record helper for Extended
    procedure ExtendedProc;
  end;

implementation

procedure TIntHelper.IntProc;
begin
  // Do nothing
end;

procedure TStringHelper.StringProc;
begin
  // Do nothing
end;

procedure TCharHelper.CharProc;
begin
  // Do nothing
end;

procedure TExtendedHelper.ExtendedProc;
begin
  // Do nothing
end;

procedure Test;
begin
  2000000.IntProc;
  'Aasf'.StringProc;
  'A'.CharProc;
  100000000000000000000000.0.ExtendedProc;
end;

end.