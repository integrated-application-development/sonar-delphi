unit RecordVariantAnonymousTagType;

interface

type
  TRecord = record
    case Kind: (rkInt, rkDbl) of
      rkInt: (Int: Integer);
      rkDbl: (Dbl: Double);
  end;

implementation

procedure Test;
var
  Rec: TRecord;
begin
  Rec.Kind := rkDbl;
end;

end.