unit RecordHelperSelfValue;

interface

type
  TStringHelper = record helper for String
    procedure Bar; overload;
  end;

implementation

procedure Test(Arg: String);
begin
  // Do nothing
end;

procedure TStringHelper.Bar;
begin
  Test(Self);
end;

end.