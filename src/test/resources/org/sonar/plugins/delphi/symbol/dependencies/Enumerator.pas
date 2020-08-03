unit Enumerator;

interface

implementation

uses
  UnitWithGetEnumeratorForTObject;

procedure Test(Enumerable: TObject);
var
  Obj: TObject;
begin
  for Obj in Enumerable do begin
    // Do nothing
  end;
end;

end.