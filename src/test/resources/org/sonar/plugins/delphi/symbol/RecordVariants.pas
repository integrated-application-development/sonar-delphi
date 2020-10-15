unit RecordVariants;

interface

type
  TRecord = record
    case Tag: Boolean of
      True: (Int: Integer);
      False: (Str: String);
  end;

implementation

procedure Foo(Arg: Boolean); overload;
begin
  // do nothing
end;

procedure Foo(Arg: Integer); overload;
begin
  // do nothing
end;

procedure Foo(Arg: String); overload;
begin
  // do nothing
end;

procedure Test;
var
  Rec: TRecord;
begin
  Foo(Rec.Tag);
  Foo(Rec.Int);
  Foo(Rec.Str);
end;

end.