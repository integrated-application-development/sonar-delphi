unit GreaterThanEqualAmbiguity;

interface

type
  TFoo = class(TObject)
    procedure Bar(Baz: IBaz<string>=nil);
  end;

implementation

procedure TFoo.Bar(Baz: IBaz<string>=nil);
begin
  // do nothing
end;

function Flarp: TArray<Byte>;
const
  Bytes: TArray<Byte>=[1, 2, 3];
begin
  Result := Bytes;
end;

function FlimFlam: TArray<Byte>;
begin
  const Bytes: TArray<Byte>=[1, 2, 3];
  Result := Bytes;
end;

end.