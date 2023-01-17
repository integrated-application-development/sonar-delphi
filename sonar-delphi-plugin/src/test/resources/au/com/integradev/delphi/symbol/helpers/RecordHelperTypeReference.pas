unit RecordHelperTypeReference;

interface

type
  TStringHelper = record helper for String
    procedure StringProc;
  end;

implementation

procedure TStringHelper.StringProc;
begin
  // Do nothing
end;

procedure Test;
begin
  String.StringProc;
  System.String.StringProc;
end;

end.