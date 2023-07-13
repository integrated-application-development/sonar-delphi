unit ClassOperators;

interface

type
  TMyRecord = record
    var Value: Integer;
    class operator Add(a, b: TMyRecord): TMyRecord;
    class operator Subtract(a, b: TMyRecord): TMyRecord;
    class operator Implicit(a: Integer): TMyRecord;
    class operator Implicit(a: TMyRecord): Integer;
    class operator Explicit(a: Double): TMyRecord;
    class operator In(a: TMyRecord; b: Integer): Boolean;
  end;

implementation

{ TMyRecord }

class operator TMyRecord.Add(a, b: TMyRecord): TMyRecord;
begin
  Result.Value := a.Value + b.Value;
end;

class operator TMyRecord.Explicit(a: Double): TMyRecord;
begin
  Result.Value := Trunc(a);
end;

class operator TMyRecord.Implicit(a: Integer): TMyRecord;
begin
  Result.Value := a;
end;

class operator TMyRecord.Implicit(a: TMyRecord): Integer;
begin
  Result := a.Value;
end;

class operator TMyRecord.In(a: TMyRecord; b: Integer): Boolean;
begin
  Result := b < a.Value;
end;

class operator TMyRecord.Subtract(a, b: TMyRecord): TMyRecord;
begin
  Result.Value := a.Value - b.Value;
end;

end.
