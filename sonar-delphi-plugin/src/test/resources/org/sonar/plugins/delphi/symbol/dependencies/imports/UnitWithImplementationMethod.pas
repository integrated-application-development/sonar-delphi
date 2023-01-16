unit UnitWithImplementationMethod;

interface

implementation

uses
  System.SysUtils;

procedure FreeAndNil(var Obj);
begin
  System.SysUtils.FreeAndNil(Obj);
end;

end.