unit ImplementationVisibility;

interface

implementation

uses
    System.SysUtils
  , UnitWithImplementationMethod
  ;
  
procedure Test(Obj: TObject);
begin
  FreeAndNil(Obj);
end;

end.