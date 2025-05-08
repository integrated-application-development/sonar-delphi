unit RoutineReferenceAssignedToProcedural;

interface

implementation

type
  TFoo = function(const A: Integer; B: string; var C: Variant): Boolean;

function Bar(const A: Integer; B: string; var C: Variant): Boolean;
begin
  Result := True;
end;

function Baz(const A: Integer; B: string; var C: Variant): Boolean;
begin
  Result := False;
end;

var
  VarFoo: TFoo = Bar;
  ConstFoo: TFoo = Baz;

const
  FooArray: array[0..1] of TFoo = (Bar, Baz);
  MultiFooArray: array[0..1, 0..1] of TFoo = (
    (Bar, Baz),
    (Baz, Bar)
   );

initialization
  var InlineVarFoo: TFoo := Bar;
  const InlineConstFoo: TFoo = Baz;

end.